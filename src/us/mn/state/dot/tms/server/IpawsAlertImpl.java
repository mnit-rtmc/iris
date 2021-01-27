/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group, Inc.
 * Copyright (C) 2021  Minnesota Department of Transportation
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
package us.mn.state.dot.tms.server;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.postgis.MultiPolygon;
import org.postgis.Polygon;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.tms.AlertState;
import us.mn.state.dot.tms.CapResponseEnum;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DmsMsgPriority;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.IpawsAlert;
import us.mn.state.dot.tms.IpawsConfig;
import us.mn.state.dot.tms.IpawsConfigHelper;
import us.mn.state.dot.tms.IpawsDeployer;
import us.mn.state.dot.tms.IpawsDeployerHelper;
import us.mn.state.dot.tms.IteratorWrapper;
import us.mn.state.dot.tms.QuickMessage;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.utils.MultiString;
import us.mn.state.dot.tms.utils.UniqueNameCreator;

/**
 * Integrated Public Alert and Warning System (IPAWS) Alert object
 * server-side implementation.
 *
 * @author Michael Janson
 * @author Gordon Parikh
 * @author Douglas Lau
 */
public class IpawsAlertImpl extends BaseObjectImpl implements IpawsAlert {

	/** Table containing NWS Forecast Zone Geometries.  This can be obtained
	 *  / updated from the NWS by going to this website and importing the
	 *  shapefile into PostGIS: https://www.weather.gov/gis/PublicZones. */
	static private final String NWS_ZONE_TABLE = "iris.nws_zones";

	/** Get the distance threshold for auto DMS */
	static private int autoDmsMeters() {
		return SystemAttrEnum.IPAWS_SIGN_THRESH_AUTO_METERS.getInt();
	}

	/** Get the distance threshold for optional DMS */
	static private int optionalDmsMeters() {
		return SystemAttrEnum.IPAWS_SIGN_THRESH_AUTO_METERS.getInt() +
		       SystemAttrEnum.IPAWS_SIGN_THRESH_OPT_METERS.getInt();
	}

	/** Name creator */
	static private final UniqueNameCreator UNC = new UniqueNameCreator(
		"ipaws_alert_%d", 20, (n)->lookupIpawsDeployer(n));

	/** Create a unique IpawsAlert record name */
	static private String createUniqueName() {
		return UNC.createUniqueName();
	}

	/** Parse a CAP alert polygon section and format as WKT syntax used by
	 *  PostGIS.
	 *
	 *  The string comes in as space-delimited coordinate pairs
	 *  (which themselves are separated by commas) in lat, lon order,
	 *  e.g.: 45.0,-93.0 45.0,-93.1 ...
	 *  We need something that looks like this (note coordinates are in
	 *  lon, lat order (which is x, y)
	 *  POLYGON((-93.0 45.0, -93.1 45.0, ...))
	 */
	static private Polygon parseCapPolygon(String cap_poly)
		throws SQLException
	{
		StringBuilder sb = new StringBuilder();
		sb.append("POLYGON((");
		String coords[] = cap_poly.split(" ");
		for (String c: coords) {
			String clatlon[] = c.split(",");
			String lat, lon;
			if (clatlon.length == 2) {
				lat = clatlon[0];
				lon = clatlon[1];
			} else
				return null;
			// add the coordinates to the string as "lon lat"
			sb.append(lon);
			sb.append(" ");
			sb.append(lat);
			sb.append(", ");
		}
		if (sb.substring(sb.length() - 2).equals(", "))
			sb.setLength(sb.length() - 2);
		sb.append("))");
		return new Polygon(sb.toString());
	}

	/** Format UGC codes as a string for an array in an SQL statement */
	static private String formatUGC(JSONArray ugcj) {
		// UGC fields will come in as "<STATE>Z<CODE>", e.g. "MNZ060"
		// we want "<STATE><CODE>" (e.g. "MN060") which matches
		// the data from the NWS
		StringBuilder sb = new StringBuilder();
		sb.append("('");
		for (int i = 0; i < ugcj.length(); ++i) {
			String iugc = ugcj.getString(i);
			String state = iugc.substring(0, 2);
			String code = iugc.substring(3);
			sb.append(state + code);
			sb.append("','");
		}
		if (",'".equals(sb.substring(sb.length() - 2)))
			sb.setLength(sb.length() - 2);
		sb.append(")");
		return sb.toString();
	}

	/** Load all the IPAWS alerts */
	static public void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, IpawsAlertImpl.class);
		store.query("SELECT name, identifier, sender, sent_date, " +
			"status, msg_type, scope, codes, note, " +
			"alert_references, incidents, categories, event, " +
			"response_types, urgency, severity, certainty, " +
			"audience, effective_date, onset_date, " +
			"expiration_date, sender_name, headline, " +
			"alert_description, instruction, parameters, area, " +
			"ST_AsText(geo_poly), lat, lon, purgeable, " +
			"last_processed FROM event." + SONAR_TYPE + ";",
			new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new IpawsAlertImpl(row));
			}
		});
	}

	/** Compare the two list values, protecting against null pointers and
	 *  whitespace issues.
	 */
	static private boolean listEq(List<?> l1, List<?> l2) {
		// TODO whitespace is making checking for equality here
		//      difficult - this works around that
		String s1 = l1 != null ? l1.toString().replace(", ", ",") : null;
		String s2 = l2 != null ? l2.toString().replace(", ", ",") : null;
		return objectEquals(s1, s2);
	}

	/** Compare the two JSON-formatted strings. */
	static private boolean jsonStrEq(String js1, String js2) {
		try {
			String j1 = (js1 != null && !js1.isEmpty())
				  ? new JSONObject(js1).toString()
				  : null;
			String j2 = (js2 != null && !js2.isEmpty())
				  ? new JSONObject(js2).toString()
				  : null;
			return objectEquals(j1, j2);
		}
		// Stupidly, this is an unchecked exception
		catch (JSONException e) {
			// invalid JSON
			return false;
		}
	}

	/** Lookup the most recent IpawsDeployerImpl given an alert
	 *  identifier and an IpawsConfig. */
	static private IpawsDeployerImpl lookupDeployer(String alertId,
		IpawsConfig cfg)
	{
		IpawsDeployerImpl iad = null;
		Iterator<IpawsDeployerImpl> it = IpawsDeployerImpl.iterator();
		while (it.hasNext()) {
			IpawsDeployerImpl dp = it.next();
			if (checkAlertDeployer(dp, alertId, cfg)) {
				if (iad == null ||
				    dp.getGenTime().after(iad.getGenTime()))
					iad = dp;
			}
		}
		return iad;
	}

	/** Check an alert deployer */
	static private boolean checkAlertDeployer(IpawsDeployerImpl dp,
		String alertId, IpawsConfig cfg)
	{
		return alertId.equals(dp.getAlertId()) &&
		       (cfg == dp.getConfig()) &&
		       AlertState.DEPLOYED.ordinal() == dp.getAlertState();
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("identifier", identifier);
		map.put("sender", sender);
		map.put("sent_date", sent_date);
		map.put("status", status);
		map.put("msg_type", msg_type);
		map.put("scope", scope);
		map.put("codes", codes);
		map.put("note", note);
		map.put("alert_references", alert_references);
		map.put("incidents", incidents);
		map.put("categories", categories);
		map.put("event", event);
		map.put("response_types", response_types);
		map.put("urgency", urgency);
		map.put("severity", severity);
		map.put("certainty", certainty);
		map.put("audience", audience);
		map.put("effective_date", effective_date);
		map.put("onset_date", onset_date);
		map.put("expiration_date", expiration_date);
		map.put("sender_name", sender_name);
		map.put("headline", headline);
		map.put("alert_description", alert_description);
		map.put("instruction", instruction);
		map.put("parameters", parameters);
		map.put("area", area);
		map.put("geo_poly", geo_poly);
		map.put("lat", lat);
		map.put("lon", lon);
		map.put("purgeable", purgeable);
		map.put("last_processed", last_processed);
		return map;
	}

	/** Get the database table name */
	@Override
	public String getTable() {
		return "event." + SONAR_TYPE;
	}

	/** Get the SONAR type name */
	@Override
	public String getTypeName() {
		return SONAR_TYPE;
	}

	/** Create an IPAWS alert */
	private IpawsAlertImpl(ResultSet row) throws SQLException {
		this(row.getString(1),        // name
		     row.getString(2),        // identifier
		     row.getString(3),        // sender
		     row.getTimestamp(4),     // sent date
		     row.getInt(5),           // status
		     row.getInt(6),           // msg_type
		     row.getInt(7),           // scope
		     getStringArray(row, 8),  // codes
		     row.getString(9),        // note
		     getStringArray(row, 10), // alert references
		     getStringArray(row, 11), // incidents
		     getStringArray(row, 12), // categories
		     row.getString(13),       // event
		     getStringArray(row, 14), // response types
		     row.getInt(15),          // urgency
		     row.getInt(16),          // severity
		     row.getInt(17),          // certainty
		     row.getString(18),       // audience
		     row.getTimestamp(19),    // effective date
		     row.getTimestamp(20),    // onset date
		     row.getTimestamp(21),    // expiration date
		     row.getString(22),       // sender name
		     row.getString(23),       // headline
		     row.getString(24),       // alert description
		     row.getString(25),       // instruction
		     row.getString(26),       // parameters
		     row.getString(27),       // area
		     row.getString(28),       // geo_poly
		     (Double) row.getObject(29), // lat
		     (Double) row.getObject(30), // lon
		     getBoolean(row, 31),     // purgeable flag
		     row.getTimestamp(32)     // last processed
		);
	}

	/** Get IPAWS alert purge threshold (days) */
	static private int getPurgeDays() {
		return SystemAttrEnum.IPAWS_ALERT_PURGE_DAYS.getInt();
	}

	/** Purge old records that have been marked "purgeable".  The age of the
	 *  records is determined based on the expiration_date field.
	 */
	static public void purgeRecords() throws TMSException {
		int age = getPurgeDays();
		IpawsProcJob.log("Purging purgeable IPAWS alert records " +
			"older than " + age + " days");
		if (store != null && age > 0) {
			store.update("DELETE FROM event." + SONAR_TYPE +
				" WHERE expiration_date < now() - '" + age +
				" days'::interval AND purgeable=true;");
		}
	}

	static public Iterator<IpawsAlertImpl> iterator() {
		return new IteratorWrapper<IpawsAlertImpl>(namespace.iterator(
			IpawsAlertImpl.SONAR_TYPE));
	}

	public IpawsAlertImpl(String i) throws TMSException {
		super(createUniqueName());
		identifier = i;
	}

	public IpawsAlertImpl(String n, String i, String se, Date sd,
		int sta, int mt, int sc, String[] cd, String nt, String[] ref,
		String[] inc, String[] ct, String ev, String[] rt, int ur,
		int sv, int cy, String au, Date efd, Date od, Date exd,
		String sn, String hl, String ades, String in, String par,
		String ar, String gp, Double lt, Double ln, Boolean p, Date pt)
	{
		super(n);
		identifier = i;
		sender = se;
		sent_date = sd;
		status = sta;
		msg_type = mt;
		scope = sc;
		codes = Arrays.asList(cd);
		note = nt;
		alert_references = Arrays.asList(ref);
		incidents = Arrays.asList(inc);
		categories = Arrays.asList(ct);
		event = ev;
		response_types = Arrays.asList(rt);
		urgency = ur;
		severity = sv;
		certainty = cy;
		audience = au;
		effective_date = efd;
		onset_date = od;
		expiration_date = exd;
		sender_name = sn;
		headline = hl;
		alert_description = ades;
		instruction = in;
		parameters = par;
		area = ar;
		if (gp != null) {
			try {
				geo_poly = new MultiPolygon(gp);
			} catch (SQLException e) {
				log("error generating polygon from: " + gp);
			}
		}
		lat = lt;
		lon = ln;
		purgeable = p;
		last_processed = pt;
	}

	/** Log a message for the alert */
	public void log(String msg) {
		IpawsProcJob.log("Alert " + name + ": " + msg);
	}

	/** Notify SONAR clients of a change to an attribute.  Clears the
	 *  purgeable flag to trigger reprocessing of the alert.
	 *
	 *  Attribute names should use lower camel case instead of underscores
	 *  (e.g. "someAttribute" instead of "some_attribute").
	 */
	@Override
	protected void notifyAttribute(String aname) {
		notifyAttribute(aname, true);
	}

	/** Notify SONAR clients of a change to an attribute.
	 *  If clearPurgeable is true, the the purgeable flag is cleared to
	 *  trigger reprocessing of the alert. */
	protected void notifyAttribute(String aname, boolean clearPurgeable) {
		super.notifyAttribute(aname);
		if (clearPurgeable) {
			try {
				setPurgeableNotify(null);
			} catch (TMSException e) {
				e.printStackTrace();
			}
		}
	}

	/** Identifier for the alert */
	private String identifier;

	/** Get the identifier */
	@Override
	public String getIdentifier() {
		return identifier;
	}

	/** Alert sender */
	private String sender;

	/** Get the sender */
	@Override
	public String getSender() {
		return sender;
	}

	/** Set the sender */
	public void setSenderNotify(String se) throws TMSException {
		if (!objectEquals(se, sender)) {
			store.update(this, "sender", se);
			sender = se;
			notifyAttribute("sender");
		}
	}

	/** Sent date of alert */
	private Date sent_date;

	/** Get the sent date */
	@Override
	public Date getSentDate() {
		return sent_date;
	}

	/** Set the sent date */
	public void setSentDateNotify(Date sd) throws TMSException {
		if (!objectEquals(sd, sent_date)) {
			store.update(this, "sent_date", sd);
			sent_date = sd;
			notifyAttribute("sentDate");
		}
	}

	/** Get the start date/time for an alert.  Checks onset time first, then
	 *  effective time, and finally sent time (which is required). */
	public Date getAlertStart() {
		Date alertStart = getOnsetDate();
		if (alertStart == null)
			alertStart = getEffectiveDate();
		if (alertStart == null)
			alertStart = getSentDate();
		return alertStart;
	}

	/** CAP status */
	private int status;

	/** Get the CAP status (CapStatus ordinal) */
	@Override
	public int getStatus() {
		return status;
	}

	/** Set the status */
	public void setStatusNotify(int sta) throws TMSException {
		if (sta != status) {
			store.update(this, "status", sta);
			status = sta;
			notifyAttribute("status");
		}
	}

	/** CAP message type */
	private int msg_type;

	/** Get the CAP message type (CapMsgType ordinal) */
	@Override
	public int getMsgType() {
		return msg_type;
	}

	/** Set the message type */
	public void setMsgTypeNotify(int mt) throws TMSException {
		if (mt != msg_type) {
			store.update(this, "msg_type", mt);
			msg_type = mt;
			notifyAttribute("msgType");
		}
	}

	/** CAP scope */
	private int scope;

	/** Get the CAP scope (CapScope ordinal) */
	@Override
	public int getScope() {
		return scope;
	}

	/** Set the scope */
	public void setScopeNotify(int sc) throws TMSException {
		if (sc != scope) {
			store.update(this, "scope", sc);
			scope = sc;
			notifyAttribute("scope");
		}
	}

	/** Alert codes */
	private List<String> codes;

	/** Get the codes */
	@Override
	public List<String> getCodes() {
		return codes;
	}

	/** Set the codes */
	public void setCodesNotify(List<String> cd) throws TMSException {
		if (!listEq(codes, cd)) {
			store.update(this, "codes", cd);
			codes = cd;
			notifyAttribute("codes");
		}
	}

	/** Alert note */
	private String note;

	/** Get the note */
	@Override
	public String getNote() {
		return note;
	}

	/** Set the note */
	public void setNoteNotify(String nt) throws TMSException {
		if (!objectEquals(note, nt)) {
			store.update(this, "note", nt);
			note = nt;
			notifyAttribute("note");
		}
	}

	/** Alert references */
	private List<String> alert_references;

	/** Get the alert references */
	@Override
	public List<String> getAlertReferences() {
		return alert_references;
	}

	/** Set the alert references */
	public void setAlertReferencesNotify(List<String> ref)
		throws TMSException
	{
		if (!listEq(alert_references, ref)) {
			store.update(this, "alert_references", ref);
			alert_references = ref;
			notifyAttribute("alertReferences");
		}
	}

	/** Alert incidents */
	private List<String> incidents;

	/** Get the incidents */
	@Override
	public List<String> getIncidents() {
		return incidents;
	}

	/** Set the incidents */
	public void setIncidentsNotify(List<String> inc) throws TMSException {
		if (!listEq(incidents, inc)) {
			store.update(this, "incidents", inc);
			incidents = inc;
			notifyAttribute("incidents");
		}
	}

	/** Categories of alert */
	private List<String> categories;

	/** Get the categories */
	@Override
	public List<String> getCategories() {
		return categories;
	}

	/** Set the categories */
	public void setCategoriesNotify(List<String> ct) throws TMSException {
		if (!listEq(categories, ct)) {
			store.update(this, "categories", ct);
			categories = ct;
			notifyAttribute("categories");
		}
	}

	/** Alert event */
	private String event;

	/** Get the event */
	@Override
	public String getEvent() {
		return event;
	}

	/** Set the event */
	public void setEventNotify(String ev) throws TMSException {
		if (!objectEquals(event, ev)) {
			store.update(this, "event", ev);
			event = ev;
			notifyAttribute("event");
		}
	}

	/** Alert response types */
	private List<String> response_types;

	/** Get the response type(s) */
	@Override
	public List<String> getResponseTypes() {
		return response_types;
	}

	/** Set the response types */
	public void setResponseTypesNotify(List<String> rt)
		throws TMSException
	{
		if (!listEq(response_types, rt)) {
			store.update(this, "response_types", rt);
			response_types = rt;
			notifyAttribute("responseTypes");
		}
	}

	/** Get the highest-priority response type */
	public String getPriorityResponseType() {
		// go through all response types to get the one with the highest
		// ordinal
		CapResponseEnum maxRT = CapResponseEnum.NONE;
		for (String rts: response_types) {
			CapResponseEnum crte = CapResponseEnum.fromValue(rts);
			if (crte.ordinal() > maxRT.ordinal())
				maxRT = crte;
		}
		return maxRT.value;
	}

	/** CAP urgency */
	private int urgency;

	/** Get the CAP urgency (CapUrgency ordinal) */
	@Override
	public int getUrgency() {
		return urgency;
	}

	/** Set the urgency */
	public void setUrgencyNotify(int ur) throws TMSException {
		if (ur != urgency) {
			store.update(this, "urgency", ur);
			urgency = ur;
			notifyAttribute("urgency");
		}
	}

	/** CAP severity */
	private int severity;

	/** Get the CAP severity (CapSeverity ordinal) */
	@Override
	public int getSeverity() {
		return severity;
	}

	/** Set the severity */
	public void setSeverityNotify(int sv) throws TMSException {
		if (sv != severity) {
			store.update(this, "severity", sv);
			severity = sv;
			notifyAttribute("severity");
		}
	}

	/** CAP certainty */
	private int certainty;

	/** Get the CAP certainty (CapCertainty ordinal) */
	@Override
	public int getCertainty() {
		return certainty;
	}

	/** Set the certainty */
	public void setCertaintyNotify(int cy) throws TMSException {
		if (cy != certainty) {
			store.update(this, "certainty", cy);
			certainty = cy;
			notifyAttribute("certainty");
		}
	}

	/** Audience for the alert */
	private String audience;

	/** Get the audience */
	@Override
	public String getAudience() {
		return audience;
	}

	/** Set the audience */
	public void setAudienceNotify(String au) throws TMSException {
		if (!objectEquals(audience, au)) {
			store.update(this, "audience", au);
			audience = au;
			notifyAttribute("audience");
		}
	}

	/** Effective date of the alert */
	private Date effective_date;

	/** Get the effective date */
	@Override
	public Date getEffectiveDate() {
		return effective_date;
	}

	/** Set the effective date */
	public void setEffectiveDateNotify(Date efd) throws TMSException {
		if (!objectEquals(efd, effective_date)) {
			store.update(this, "effective_date", efd);
			effective_date = efd;
			notifyAttribute("effectiveDate");
		}
	}

	/** Onset date for alert */
	private Date onset_date;

	/** Get the onset date */
	@Override
	public Date getOnsetDate() {
		return onset_date;
	}

	/** Set the onset date */
	public void setOnsetDateNotify(Date od) throws TMSException {
		if (!objectEquals(od, onset_date)) {
			store.update(this, "onset_date", od);
			onset_date = od;
			notifyAttribute("onsetDate");
		}
	}

	/** Expiration date for alert */
	private Date expiration_date;

	/** Get the expiration date */
	@Override
	public Date getExpirationDate() {
		return expiration_date;
	}

	/** Set the expiration date */
	public void setExpirationDateNotify(Date exd) throws TMSException {
		if (!objectEquals(exd, expiration_date)) {
			store.update(this, "expiration_date", exd);
			expiration_date = exd;
			notifyAttribute("expirationDate");
		}
	}

	/** The alert sender's name */
	private String sender_name;

	/** Get the sender's name */
	@Override
	public String getSenderName() {
		return sender_name;
	}

	/** Set the sender's name */
	public void setSenderNameNotify(String sn) throws TMSException {
		if (!objectEquals(sender_name, sn)) {
			store.update(this, "sender_name", sn);
			sender_name = sn;
			notifyAttribute("senderName");
		}
	}

	/** Headline for the alert */
	private String headline;

	/** Get the alert headline */
	@Override
	public String getHeadline() {
		return headline;
	}

	/** Set the alert headline */
	public void setHeadlineNotify(String hl) throws TMSException {
		if (!objectEquals(headline, hl)) {
			store.update(this, "headline", hl);
			headline = hl;
			notifyAttribute("headline");
		}
	}

	/** Description of alert */
	private String alert_description;

	/** Get the description */
	@Override
	public String getAlertDescription() {
		return alert_description;
	}

	/** Set the alert description */
	public void setAlertDescriptionNotify(String ad) throws TMSException {
		if (!objectEquals(alert_description, ad)) {
			store.update(this, "alert_description", ad);
			alert_description = ad;
			notifyAttribute("alertDescription");
		}
	}

	/** Alert instruction */
	private String instruction;

	/** Get the alert instruction */
	@Override
	public String getInstruction() {
		return instruction;
	}

	/** Set the instruction */
	public void setInstructionNotify(String in) throws TMSException {
		if (!objectEquals(instruction, in)) {
			store.update(this, "instruction", in);
			instruction = in;
			notifyAttribute("instruction");
		}
	}

	/** Parameters as JSON value */
	private String parameters;

	/** Get the parameters as JSON value */
	@Override
	public String getParameters() {
		return parameters;
	}

	/** Set the parameters */
	public void setParametersNotify(String par) throws TMSException {
		if (!jsonStrEq(parameters, par)) {
			store.update(this, "parameters", par);
			parameters = par;
			notifyAttribute("parameters");
		}
	}

	/** Area as JSON value */
	private String area;

	/** Get the areas as JSON value */
	@Override
	public String getArea() {
		return area;
	}

	/** Set the area */
	public void setAreaNotify(String ar) throws TMSException {
		if (!jsonStrEq(area, ar)) {
			store.update(this, "area", ar);
			area = ar;
			notifyAttribute("area");
		}
	}

	/** Geographic MultiPolygon */
	private MultiPolygon geo_poly;

	/** Get the geographic polygon of the area */
	@Override
	public MultiPolygon getGeoPoly() {
		return geo_poly;
	}

	/** Set the geographic polygon of the area */
	private void setGeoPolyNotify(MultiPolygon gp) throws TMSException {
		if (!objectEquals(geo_poly, gp)) {
			store.update(this, "geo_poly", gp);
			geo_poly = gp;
			notifyAttribute("geoPoly", false);
		}
	}

	/** Latitude */
	private Double lat;

	/** Set the latitude of the alert area's centroid, and notify */
	public void setLatNotify(Double lt) throws TMSException {
		if (!objectEquals(lt, lat)) {
			GeoLocImpl.checkLat(lt);
			store.update(this, "lat", lt);
			lat = lt;
			notifyAttribute("lat");
		}
	}

	/** Get the latitude of the alert area's centroid */
	@Override
	public Double getLat() {
		return lat;
	}

	/** Longitude */
	private Double lon;

	/** Set the longitude and notify clients */
	public void setLonNotify(Double ln) throws TMSException {
		if (!objectEquals(ln, lon)) {
			GeoLocImpl.checkLon(ln);
			store.update(this, "lon", ln);
			lon = ln;
			notifyAttribute("lon");
		}
	}

	/** Get the longitude of the alert area's centroid */
	@Override
	public Double getLon() {
		return lon;
	}

	/** Purgeable flag.  Null if the alert has not yet been processed, true
	 *  if alert is determined to be irrelevant to this system's users. */
	private Boolean purgeable;

	/** Flag indicating if this alert is purgeable (irrelevant to us) */
	@Override
	public Boolean getPurgeable() {
		return purgeable;
	}

	/** Set purgeable flag and notify clients */
	private void setPurgeableNotify(Boolean p) throws TMSException {
		if (!objectEquals(purgeable, p)) {
			log("setting purgeable flag to " + p);
			store.update(this, "purgeable", p);
			purgeable = p;
			notifyAttribute("purgeable", false);
		}
	}

	/** Last processing time of the alert */
	private Date last_processed;

	/** Get the last processing time of the alert */
	@Override
	public Date getLastProcessed() {
		return last_processed;
	}

	/** Set the last processing time of the alert */
	private void setLastProcessedNotify(Date pt) throws TMSException {
		if (!objectEquals(pt, last_processed)) {
			store.update(this, "last_processed", pt);
			last_processed = pt;
			notifyAttribute("lastProcessed", false);
		}
	}

	/** Check the IpawsAlert provided for relevance to this system and (if
	 *  relevant) process it for posting.  Relevance is determined based on
	 *  whether there is one or more existing IpawsConfig objects that
	 *  match the event in the alert and whether the alert area(s) encompass
	 *  any DMS known to the system.
	 *
	 *  DMS selection uses PostGIS to handle the geospatial operations.
	 *  This method must be called after getGeoPoly() is used to create a
	 *  polygon object from the alert's area field, and after that polygon
	 *  is written to the database with the alert's setGeoPolyNotify()
	 *  method.
	 *
	 *  If at least one sign is selected, an IpawsDeployer object is created
	 *  to deploy the alert and optionally notify clients for approval.
	 *
	 *  If no signs are found, no deployer object is created and the
	 *  IpawsAlert object is marked purgeable.
	 *
	 *  One deployer object is created for each matching IpawsConfig,
	 *  allowing different messages to be posted to different sign types.
	 */
	public void processAlert() throws SQLException, TMSException {
		// FIXME: check CAP status
		if (getPurgeable() == null)
			processNewAlert();
		processPhase();
	}

	/** Process a new IPAWS alert */
	private void processNewAlert() throws SQLException, TMSException {
		log("processing new alert");
		createGeoPoly();
		if (getGeoPoly() != null)
			updateCentroid();
		else {
			log("no polygon found");
			setPurgeableNotify(true);
		}
	}

	/** Check if alert has changed phases.
	 *  New deployers are created for each phase. */
	private void processPhase() throws TMSException {
		if (Boolean.TRUE.equals(getPurgeable()))
			return;
		Date proc = getLastProcessed();
		if (proc == null) {
			log("phase new");
			createDeployers();
		} else {
			Date start = getAlertStart();
			Date end = getExpirationDate();
			Date now = TimeSteward.getDateInstance();
			if (proc.before(start) && now.after(start) &&
				now.before(end))
			{
				log("phase starting");
				createDeployers();
			} else if (proc.before(end) && now.after(end)) {
				log("phase ending");
				createDeployers();
			}
		}
		setLastProcessedNotify(TimeSteward.getDateInstance());
	}

	/** Create deployers for alert */
	private void createDeployers() throws TMSException {
		int deployers = 0;
		String event = getEvent();
		Iterator<IpawsConfig> it = IpawsConfigHelper.iterator();
		while (it.hasNext()) {
			IpawsConfig cfg = it.next();
			if (event.equals(cfg.getEvent()))
				deployers += createDeployer(cfg);
		}
		if (deployers > 0)
			setPurgeableNotify(false);
		else if (getPurgeable() == null) {
			log("no deployers created");
			// FIXME: on updates, purgeable can be reset to null.
			//        It may have been deployed before then.
			setPurgeableNotify(true);
		}
	}

	/** Create deployer for an alert config */
	private int createDeployer(IpawsConfig cfg) throws TMSException {
		log("searching for DMS in group " + cfg.getSignGroup());
		String[] adms = findDMS(cfg, autoDmsMeters());
		if (adms.length > 0) {
			log("found " + adms.length + " auto signs");
			String[] odms = findDMS(cfg, optionalDmsMeters());
			log("found " + odms.length + " optional signs");
			if (createDeployer(cfg, adms, odms) != null)
				return 1;
		}
		return 0;
	}

	/** Find all DMS within given alert area threshold */
	private String[] findDMS(IpawsConfig cfg, int th) throws TMSException {
		ArrayList<String> dms = new ArrayList<String>();
		store.query(buildDMSQuery(cfg, th), new ResultFactory() {
			@Override public void create(ResultSet row) {
				try {
					dms.add(row.getString(1));
				} catch (Exception e) {
					log("querying DMS, " + e.getMessage());
				}
			}
		});
		return dms.toArray(new String[0]);
	}

	/** Build DMS query for an IPAWS config.
	 *
	 * Query the DMS that fall within the MultiPolygon for this alert */
	private String buildDMSQuery(IpawsConfig cfg, int th) {
		return "SELECT d.name " +
			"FROM iris." + DMS.SONAR_TYPE + " d " +
			"JOIN iris." + GeoLoc.SONAR_TYPE + " g " +
			"ON d.geo_loc=g.name " +
			"WHERE ST_DWithin((" +
				"SELECT geo_poly FROM " + getTable() + " " +
				"WHERE name='" + getName() + "')," +
				"ST_Point(g.lon,g.lat)::geography," + th + ") "+
			"AND d.name IN (" +
				"SELECT dms FROM iris.dms_sign_group " +
				"WHERE sign_group='" + cfg.getSignGroup() +
			"');";
	}

	/** Create a deployer for this alert.
	 *
	 *  @param cfg IPAWS config
	 *  @param adms Suggested auto-deploy DMS list
	 *  @param odms Suggested optional-deploy DMS list
	 */
	private IpawsDeployerImpl createDeployer(IpawsConfig cfg, String[] adms,
		String[] odms) throws TMSException
	{
		IpawsDeployerImpl iad = lookupDeployer(getIdentifier(), cfg);

		Date aStart = getAlertStart();
		Date aEnd = getExpirationDate();

		String autoMulti = generateMulti(cfg);
		int priority = DmsMsgPriority.ALERT.ordinal();

		// check if any attributes have changed from this last deployer
		// (if we got one)
		boolean updated = (iad != null) && !iad.autoValsEqual(aStart,
			aEnd, adms, autoMulti, priority);

		if (iad == null || updated) {
			// make sure to note that this is a replacement - when
			// this is eventually deployed that will be used to
			// cancel the old alert
			String replaces = null;
			int preAlert = cfg.getPreAlertTime();
			int postAlert = cfg.getPostAlertTime();
			if (updated) {
				replaces = iad.getName();
				preAlert = iad.getPreAlertTime();
				postAlert = iad.getPostAlertTime();
			}

			iad = new IpawsDeployerImpl(getIdentifier(), aStart,
				aEnd, cfg, adms, odms, autoMulti, priority,
				preAlert, postAlert, replaces);
			log("created deployer " + iad.getName() +
				" replacing " + replaces);
			try {
				iad.notifyCreate();
			}
			catch (SonarException e) {
				log("deployer notify create failed, " +
					e.getMessage());
			}
			iad.createNotification(getEvent());
		}
		return iad;
	}

	/** Generate a MULTI message from an alert config */
	private String generateMulti(IpawsConfig iac) {
		QuickMessage qm = iac.getQuickMessage();
		String qmMulti = (qm != null) ? qm.getMulti() : "";
		log("got message template: " + qmMulti);
		CapMultiBuilder builder = new CapMultiBuilder(this);
		new MultiString(qmMulti).parse(builder);
		MultiString ms = builder.toMultiString();
		log("MULTI: " + ms.toString());
		return (ms.isValid() && !ms.isBlank()) ? ms.toString() : null;
	}

	/** Create the geoPoly attribute using the area section of the alert */
	private void createGeoPoly() throws TMSException {
		String ar = getArea();
		if (ar != null) {
			MultiPolygon mp = createGeoPoly(ar);
			setGeoPolyNotify(mp);
		}
	}

	/** Create a MultiPolygon geography object from a given area section
	 *  from the alert. */
	private MultiPolygon createGeoPoly(String ar) {
		try {
			log("area: " + ar);
			return createGeoPoly(new JSONObject(ar));
		}
		catch (JSONException e) {
			log("invalid area JSON: " + e.getMessage());
		}
		catch (SQLException e) {
			log("error creating geo_poly: " + e.getMessage());
		}
		catch (TMSException e) {
			log("error looking up UGC codes: " + e.getMessage());
		}
		return null;
	}

	/** Create a MultiPolygon geography object from a given area section
	 *  from the alert.
	 *
	 *  If a polygon section is found, it is used to create a MultiPolygon
	 *  object (one for each polygon).  If there is no polygon, the other
	 *  location information is used to look up one or more polygons. */
	private MultiPolygon createGeoPoly(JSONObject jarea)
		throws JSONException, SQLException, TMSException
	{
		JSONArray polygon = jarea.getJSONArray("polygon");
		if (polygon != null && polygon.length() > 0)
			return createMultiPolygon(polygon);
		else {
			// if we didn't get a polygon, check the other fields
			// to find one we can use to lookup a geographical area
			log("no polygon, trying UGC codes");
			JSONObject gj = jarea.getJSONObject("geocode");
			JSONArray ugcj = gj.getJSONArray("UGC");
			String ugc = formatUGC(ugcj);
			log("got UGC codes: " + ugc);
			String mp = lookupUGC(ugc);
			if (mp == null)
				log("no polygon found for UGC codes!");
			return new MultiPolygon(mp);
		}
	}

	/** Create a MultiPolygon geography object from a polygon JSON array */
	private MultiPolygon createMultiPolygon(JSONArray polygon)
		throws JSONException, SQLException
	{
		ArrayList<Polygon> polys = new ArrayList<Polygon>();
		for (int i = 0; i < polygon.length(); i++) {
			String ps = polygon.getString(i);
			log("got polygon: " + ps);
			Polygon pg = parseCapPolygon(ps);
			if (pg != null)
				polys.add(pg);
			else
				log("invalid polygon!");
		}
		return new MultiPolygon(polys.toArray(new Polygon[0]));
	}

	/** Lookup UGC polygons from NWS zone table */
	private String lookupUGC(String ugc) throws TMSException {
		String[] ps = new String[1];
		store.query("SELECT ST_AsText(ST_Multi(ST_Union(geom))) FROM " +
			NWS_ZONE_TABLE + " WHERE state_zone IN " + ugc + ";",
		new ResultFactory() {
			@Override
			public void create(ResultSet row) throws SQLException {
				ps[0] = row.getString(1);
			}
		});
		return ps[0];
	}

	/** Update the centroid of an alert area */
	private void updateCentroid() throws TMSException {
		store.query("SELECT ST_AsText(ST_Centroid(geo_poly)) " +
			"FROM event." + SONAR_TYPE + " WHERE name='" +
			getName() + "';", new ResultFactory()
		{
				@Override public void create(ResultSet row)
					throws Exception
				{
					updateCentroid(row);
				}
			}
		);
	}

	/** Update the centroid of an IPAWS alert from a row with a POINT */
	private void updateCentroid(ResultSet row) throws Exception {
		// parse out the lat/long from the POINT(lon lat) string
		String pStr = row.getString(1);
		String llStr = pStr.replace("POINT(", "").replace(")", "");
		String[] ll = llStr.split(" ");
		if (ll.length == 2) {
			double lon = Double.parseDouble(ll[0]);
			double lat = Double.parseDouble(ll[1]);
			setLatNotify(lat);
			setLonNotify(lon);
		} else
			log("invalid point: " + llStr);
	}
}
