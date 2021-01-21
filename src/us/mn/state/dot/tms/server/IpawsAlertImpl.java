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
import java.time.Duration;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IllegalFormatException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.json.JSONException;
import org.json.JSONObject;
import org.postgis.MultiPolygon;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.tms.AlertState;
import us.mn.state.dot.tms.CapCertaintyEnum;
import us.mn.state.dot.tms.CapResponse;
import us.mn.state.dot.tms.CapResponseEnum;
import us.mn.state.dot.tms.CapResponseHelper;
import us.mn.state.dot.tms.CapSeverityEnum;
import us.mn.state.dot.tms.CapUrgency;
import us.mn.state.dot.tms.CapUrgencyEnum;
import us.mn.state.dot.tms.CapUrgencyHelper;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DmsMsgPriority;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.IpawsAlert;
import us.mn.state.dot.tms.IpawsConfig;
import us.mn.state.dot.tms.IpawsConfigHelper;
import us.mn.state.dot.tms.IpawsDeployer;
import us.mn.state.dot.tms.IpawsDeployerHelper;
import us.mn.state.dot.tms.IteratorWrapper;
import us.mn.state.dot.tms.NotificationHelper;
import us.mn.state.dot.tms.QuickMessage;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.utils.I18N;
import us.mn.state.dot.tms.utils.MultiBuilder;
import us.mn.state.dot.tms.utils.MultiString;
import us.mn.state.dot.tms.utils.UniqueNameCreator;

/**
 * Integrated Public Alert and Warning System (IPAWS) Alert object
 * server-side implementation.
 *
 * @author Michael Janson
 * @author Gordon Parikh
 */
public class IpawsAlertImpl extends BaseObjectImpl implements IpawsAlert {

	/** Name creator */
	static UniqueNameCreator UNC;
	static {
		UNC = new UniqueNameCreator("ipaws_alert_%d",
			(n)->lookupIpawsDeployer(n));
		UNC.setMaxLength(20);
	}

	/** Create a unique IpawsAlert record name */
	static public String createUniqueName() {
		return UNC.createUniqueName();
	}

	/** Allowed DMS Message Priority values */
	static private final DmsMsgPriority[] ALLOWED_PRIORITIES = {
		DmsMsgPriority.PSA,
		DmsMsgPriority.ALERT,
		DmsMsgPriority.AWS,
		DmsMsgPriority.AWS_HIGH
	};

	/** Time units to use for calculating pre/post alert deployment times.
	 *  This is provided for convenience when testing, where changing to
	 *  TimeUnit.MINUTES will generally allow for easier testing than the
	 *  value of TimeUnit.HOURS that is used in production. */
	static private TimeUnit TIME_UNIT = TimeUnit.HOURS;

	/** Load all the IPAWS alerts */
	static public void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, IpawsAlertImpl.class);
		store.query("SELECT name, identifier, sender, sent_date, " +
			"status, message_type, scope, codes, note, " +
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
				try {
					namespace.addObject(new IpawsAlertImpl(
						row));
				} catch (Exception e) {
					System.out.println("Error adding: " +
						row.getString(1));
					e.printStackTrace();
				}
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
	static private IpawsDeployerImpl lookupFromAlert(String alertId,
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
		map.put("message_type", message_type);
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
		     row.getString(5),        // status
		     row.getString(6),        // message type
		     row.getString(7),        // scope
		     getStringArray(row, 8),  // codes
		     row.getString(9),        // note
		     getStringArray(row, 10), // alert references
		     getStringArray(row, 11), // incidents
		     getStringArray(row, 12), // categories
		     row.getString(13),       // event
		     getStringArray(row, 14), // response types
		     row.getString(15),       // urgency
		     row.getString(16),       // severity
		     row.getString(17),       // certainty
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

	/** Purge old records that have been marked "purgeable". The age of the
	 *  records is determined based on the expiration_date field.
	 */
	static public void purgeRecords() throws TMSException {
		int age = getPurgeDays();
		IpawsProcJob.log("Purging purgeable IPAWS alert records older " +
				"than " + age + " days...");
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

	public IpawsAlertImpl(String n, String i) throws TMSException {
		super(n);
		identifier = i;
	}

	public IpawsAlertImpl(String n, String i, String se, Date sd,
		String sta, String mt, String sc, String[] cd, String nt,
		String[] ref, String[] inc, String[] ct, String ev, String[] rt,
		String u, String sv, String cy, String au, Date efd, Date od,
		Date exd, String sn, String hl, String ades, String in,
		String par, String ar, String gp, Double lt, Double ln,
		Boolean p, Date pt)
	{
		super(n);
		identifier = i;
		sender = se;
		sent_date = sd;
		status = sta;
		message_type = mt;
		scope = sc;
		codes = Arrays.asList(cd);
		note = nt;
		alert_references = Arrays.asList(ref);
		incidents = Arrays.asList(inc);
		categories = Arrays.asList(ct);
		event = ev;
		response_types = Arrays.asList(rt);
		urgency = u;
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
				System.out.println("Error generating polygon from: " + gp);
				e.printStackTrace();
			}
		}
		lat = lt;
		lon = ln;
		purgeable = p;
		last_processed = pt;
	}

	/** Log a message for the alert */
	private void log(String msg) {
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

	/** Notify SONAR clients of a change to an attribute.  If clearPurgeable
	 *  is true, the the purgeable flag is cleared to trigger reprocessing
	 *  of the alert.
	 */
	protected void notifyAttribute(String aname, boolean clearPurgeable) {
		// notify clients about the change
		super.notifyAttribute(aname);

		// clear the purgeable flag so the alert gets reprocessed (since
		// this only gets called when the alert changes)
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

	/** Status of alert */
	private String status;

	/** Get the status */
	@Override
	public String getStatus() {
		return status;
	}

	/** Set the status */
	public void setStatusNotify(String sta) throws TMSException {
		if (!objectEquals(status, sta)) {
			store.update(this, "status", sta);
			status = sta;
			notifyAttribute("status");
		}
	}

	/** Alert message type */
	private String message_type;

	/** Get the message type */
	@Override
	public String getMsgType() {
		return message_type;
	}

	/** Set the message type */
	public void setMsgTypeNotify(String mt) throws TMSException {
		if (!objectEquals(message_type, mt)) {
			store.update(this, "message_type", mt);
			message_type = mt;
			notifyAttribute("msgType");
		}
	}

	/** Alert scope */
	private String scope;

	/** Get the scope */
	@Override
	public String getScope() {
		return scope;
	}

	/** Set the scope */
	public void setScopeNotify(String sc) throws TMSException {
		if (!objectEquals(scope, sc)) {
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

	/** Urgency of alert */
	private String urgency;

	/** Get the urgency */
	@Override
	public String getUrgency() {
		return urgency;
	}

	/** Set the urgency */
	public void setUrgencyNotify(String u) throws TMSException {
		if (!objectEquals(urgency, u)) {
			store.update(this, "urgency", u);
			urgency = u;
			notifyAttribute("urgency");
		}
	}

	/** Severity of the alert */
	private String severity;

	/** Get the severity */
	@Override
	public String getSeverity() {
		return severity;
	}

	/** Set the severity */
	public void setSeverityNotify(String sv) throws TMSException {
		if (!objectEquals(severity, sv)) {
			store.update(this, "severity", sv);
			severity = sv;
			notifyAttribute("severity");
		}
	}

	/** Certainty of the alert */
	private String certainty;

	/** Get the certainty */
	@Override
	public String getCertainty() {
		return certainty;
	}

	/** Set the certainty */
	public void setCertaintyNotify(String cy) throws TMSException {
		if (!objectEquals(certainty, cy)) {
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

	/** Parameters */
	private String parameters;

	/** Get the parameters */
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

	/** Area */
	private String area;

	/** Get the area */
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
	public void setGeoPolyNotify(MultiPolygon gp) throws TMSException {
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
	public void setPurgeableNotify(Boolean p) throws TMSException {
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
	public void setLastProcessedNotify(Date pt) throws TMSException {
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
	public void processAlert() throws TMSException {
		if (isAfterExpirationTime()) {
			log("past expiration time");
			return;
		}
		int deployers = 0;
		String event = getEvent();
		Iterator<IpawsConfig> it = IpawsConfigHelper.iterator();
		while (it.hasNext()) {
			IpawsConfig iac = it.next();
			if (event.equals(iac.getEvent()))
				deployers += createDeployers(iac);
		}
		if (deployers == 0 && getPurgeable() == null) {
			log("no deployers created, marking as purgeable");
			setPurgeableNotify(true);
		} else if (deployers > 0)
			setPurgeableNotify(false);
		setLastProcessedNotify(new Date());
	}

	/** Check if it's after the alert expiration time */
	private boolean isAfterExpirationTime() {
		Date now = new Date();
		return now.after(getExpirationDate());
	}

	/** Create deployers for an alert config */
	private int createDeployers(IpawsConfig iac) throws TMSException {
		final int[] deployers = new int[1];
		log("searching for DMS in group " + iac.getSignGroup());
		store.query(buildDMSQuery(iac), new ResultFactory() {
			@Override
			public void create(ResultSet row) {
				try {
					deployers[0] += createDeployer(iac, row);
				} catch (Exception e) {
					log("no DMS found");
				}
			}
		});
		return deployers[0];
	}

	/** Build DMS query for an IPAWS config.
	 *
	 * Query the list of DMS that falls within the MultiPolygon for this
	 * alert - use array_agg to get one array instead of multiple rows */
	private String buildDMSQuery(IpawsConfig cfg) {
		int t = SystemAttrEnum.IPAWS_SIGN_THRESH_AUTO_METERS.getInt();
		return "SELECT array_agg(d.name) " +
			"FROM iris." + DMS.SONAR_TYPE + " d " +
			"JOIN iris." + GeoLoc.SONAR_TYPE + " g " +
			"ON d.geo_loc=g.name " +
			"WHERE ST_DWithin((" +
				"SELECT geo_poly FROM " + getTable() + " " +
				"WHERE name='" + getName() + "')," +
				"ST_Point(g.lon,g.lat)::geography," + t + ") " +
			"AND d.name IN (" +
				"SELECT dms FROM iris.dms_sign_group " +
				"WHERE sign_group='" + cfg.getSignGroup() +
			"');";
	}

	/** Create an IPAWS deployer */
	private int createDeployer(IpawsConfig iac, ResultSet row)
		throws SQLException, SonarException, TMSException
	{
		String[] dms = (String[]) row.getArray(1).getArray();
		log("found " + dms.length + " signs");
		if (dms.length > 0) {
			IpawsDeployerImpl iad = createDeployer(iac, dms);
			if (iad != null)
				return 1;
		}
		return 0;
	}

	/** Create an alert deployer for this alert.  Called after querying
	 *  PostGIS for relevant DMS (only if some were found).  Handles
	 *  generating MULTI and other object creating housekeeping.
	 */
	private IpawsDeployerImpl createDeployer(IpawsConfig iac, String[] adms) 
		throws SonarException, TMSException
	{
		// try to look up the most recent deployer object for this alert
		IpawsDeployerImpl iad = lookupFromAlert(getIdentifier(), iac);

		// get alert start/end times
		Date aStart = getAlertStart();
		Date aEnd = getExpirationDate();

		String autoMulti = generateMulti(iac, aStart, aEnd);
		int priority = calculateMsgPriority().ordinal();

		// check if any attributes have changed from this last deployer
		// (if we got one)
		boolean updated = (iad != null) && !iad.autoValsEqual(aStart,
			aEnd, adms, autoMulti, priority);

		if (iad == null || updated) {
			// if they have, or we didn't get one, make a new one
			String dp_name = IpawsDeployerImpl.createUniqueName();

			// make sure to note that this is a replacement - when
			// this is eventually deployed that will be used to
			// cancel the old alert
			String replaces = null;
			String[] ddms = null;
			int preAlert = iac.getPreAlertTime();
			int postAlert = iac.getPostAlertTime();
			if (updated) {
				replaces = iad.getName();
				ddms = iad.getDeployedDms();
				preAlert = iad.getPreAlertTime();
				postAlert = iad.getPostAlertTime();
			}

			log("creating deployer " + dp_name + " replacing " +
				replaces);

			iad = new IpawsDeployerImpl(dp_name, getIdentifier(),
				aStart, aEnd, iac, adms, ddms, autoMulti,
				priority, preAlert, postAlert, replaces);
			iad.notifyCreate();
			addOptionalDms(iad);
			deployAlert(iad);
		}
		return iad;
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

	/** Generate a MULTI message from an alert config */
	private String generateMulti(IpawsConfig iac, Date alertStart,
		Date alertEnd)
	{
		QuickMessage qm = iac.getQuickMessage();
		String qmMulti = (qm != null) ? qm.getMulti() : "";
		log("got message template: " + qmMulti);

		// use a MultiBuilder to process cap action tags
		MultiBuilder builder = new MultiBuilder() {
			@Override
			public void addCapTime(String f_txt, String a_txt, String p_txt) {
				// check the alert times against the current time to know
				// which text and time fields to use
				Date now = new Date();
				String tmplt;
				Date dt;
				if (now.before(alertStart)) {
					// alert hasn't started yet
					tmplt = f_txt;
					dt = alertStart;
				} else if (now.before(alertEnd)) {
					// alert is currently active
					tmplt = a_txt;
					dt = alertEnd;
				} else {
					// alert has expired
					tmplt = p_txt;
					dt = alertEnd;
				}

				// format any time strings in the text and add to the msg
				String s = IpawsDeployerHelper.replaceTimeFmt(tmplt,
					dt.toInstant().atZone(ZoneId.systemDefault())
					.toLocalDateTime());
				addSpan(s);
			}

			@Override
			public void addCapResponse(String[] rtypes) {
				// make a HashSet of the allowed response types
				HashSet<String> rtSet = new HashSet<String>(
						Arrays.asList(rtypes));

				// check the response types in the alert to see if we should
				// substitute anything, taking the highest priority one
				CapResponseEnum maxRT = CapResponseEnum.NONE;
				CapResponse rtSub = null;
				for (String rt: getResponseTypes()) {
					if (rtSet.contains(rt)) {
						// make sure we have a matching substitution value too
						CapResponse crt = CapResponseHelper.lookupFor(
								getEvent(), rt);

						if (crt != null) {
							CapResponseEnum crte =
									CapResponseEnum.fromValue(rt);
							if (crte.ordinal() > maxRT.ordinal()) {
								maxRT = crte;
								rtSub = crt;
							}
						}
					}
				}

				// if we had a match add the MULTI, otherwise leave it blank
				addSpan(rtSub != null ? rtSub.getMulti() : "");
			}

			@Override
			public void addCapUrgency(String[] uvals) {
				// make a HashSet of the allowed urgency values
				HashSet<String> urgSet = new HashSet<String>(
						Arrays.asList(uvals));

				// check the urgency value in the alert to see if we should
				// substitute anything
				String urg = getUrgency();
				String multi = "";
				if (urgSet.contains(urg)) {
					CapUrgency subst = CapUrgencyHelper.lookupFor(
						getEvent(), urg);
					if (subst != null)
						multi = subst.getMulti();
				}
				addSpan(multi);
			}
		};

		// process the QuickMessage with the MultiBuilder
		new MultiString(qmMulti).parse(builder);
		MultiString ms = builder.toMultiString();
		log("MULTI: " + ms.toString());

		// return the MULTI if it's valid and not blank
		if (ms.isValid() && !ms.isBlank()) {
			return ms.toString();
		}

		// return null if we couldn't generate a valid message (nothing
		// else will happen)
		return null;
	}

	/** Add optional DMS for suggesting to the user for inclusion in the alert
	 *  (in addition to the auto-selected messages) to the alert deployer.
	 */
	private void addOptionalDms(IpawsDeployerImpl iad) throws TMSException {
		// add the auto threshold to the suggested threshold to get the
		// total threshold we will use
		int t = SystemAttrEnum.IPAWS_SIGN_THRESH_AUTO_METERS.getInt() +
			SystemAttrEnum.IPAWS_SIGN_THRESH_OPT_METERS.getInt();
		SignGroup sg = iad.getConfig().getSignGroup();

		// query the signs in the same group as the deployer within the
		// threshold from the alert area
		IpawsAlertImpl.store.query(
		"SELECT array_agg(d.name) FROM iris." + DMS.SONAR_TYPE + " d" +
		" JOIN iris." + GeoLoc.SONAR_TYPE + " g ON d.geo_loc=g.name" +
		" WHERE ST_DWithin((SELECT geo_poly FROM " + getTable() +
		" WHERE name='" + getName() + "'), ST_Point(g.lon," +
		" g.lat)::geography, " + t + ") AND d.name IN (SELECT dms" +
		" FROM iris.dms_sign_group WHERE sign_group='" + sg + "');",
		new ResultFactory() {
			@Override
			public void create(ResultSet row) throws Exception {
				try {
					// if we get a result, set the list of
					// optional DMS in the deployer
					String[] dms = (String[]) row.getArray(1).getArray();
					iad.setOptionalDmsNotify(dms);
				} catch (Exception e) {
					log("no optional DMS found");
				}
			}
		});
	}

	/** Calculate the message priority for an alert given the urgency,
	 *  severity, and certainty values and weights stored as system
	 *  attributes.
	 */
	private DmsMsgPriority calculateMsgPriority() {
		// get the weights
		float wu = SystemAttrEnum.IPAWS_PRIORITY_WEIGHT_URGENCY.getFloat();
		float ws = SystemAttrEnum.IPAWS_PRIORITY_WEIGHT_SEVERITY.getFloat();
		float wc = SystemAttrEnum.IPAWS_PRIORITY_WEIGHT_CERTAINTY.getFloat();

		// get the urgency, severity, and certainty values
		CapUrgencyEnum u = CapUrgencyEnum.fromValue(getUrgency());
		CapSeverityEnum s = CapSeverityEnum.fromValue(getSeverity());
		CapCertaintyEnum c = CapCertaintyEnum.fromValue(getCertainty());

		// convert those values to decimals
		float uf = (float) u.ordinal() / (float) CapUrgencyEnum.nValues();
		float sf = (float) s.ordinal() / (float) CapSeverityEnum.nValues();
		float cf = (float) c.ordinal() / (float) CapCertaintyEnum.nValues();

		// calculate a priority "score" (higher = more important)
		float score = wu * uf + ws * sf + wc * cf;

		log("priority score: " + wu + " * " + uf + " + " + ws
			+ " * " + sf + " + " + wc + " * " + cf + " = " + score);

		// convert the score to an index and return one of the allowed values
		int i = Math.round(score * ALLOWED_PRIORITIES.length);
		if (i >= ALLOWED_PRIORITIES.length)
			i = ALLOWED_PRIORITIES.length - 1;
		else if (i < 0)
			i = 0;
		return ALLOWED_PRIORITIES[i];
	}

	/** Process the alert for deploying, checking the mode (auto or manual)
	 *  first.  In manual mode, this sends a push notification to clients
	 *  to request a user to review and approve the alert.  In auto mode,
	 *  this either deploys the alert then sends a notification indicating
	 *  the new alert, or (if a non-zero timeout is configured) sends a
	 *  notification then waits until the timeout has elapsed and (if a user
	 *  hasn't deployed it manually) deploys the alert.
	 */
	private void deployAlert(IpawsDeployerImpl iad) throws SonarException,
		TMSException
	{
		// check the deploy mode and timeouts in system attributes
		boolean autoMode = SystemAttrEnum.IPAWS_DEPLOY_AUTO_MODE.getBoolean();
		int timeout = SystemAttrEnum.IPAWS_DEPLOY_AUTO_TIMEOUT_SECS.getInt();

		// generate and send the notification - the static method
		// handles the content of the notification based on the mode
		// and timeout
		NotificationImpl pn = createNotification(getEvent(),
			autoMode, timeout, iad.getName());

		// NOTE: manual deployment triggered in IpawsDeployerImpl
		if (!autoMode)
			return;

		// auto mode - check timeout first
		if (timeout > 0) {
			// non-zero timeout - wait for timeout to pass.
			// NOTE that this is already happening in it's
			// own thread, so we will just wait here
			for (int i = 0; i < timeout; i++) {
				// wait a second
				try { Thread.sleep(1000); }
				catch (InterruptedException e) {
					/* Ignore */
				}

				// update the notification with a new
				// message (so the user knows how much
				// time they have before the alert
				// deploys)
				pn.setDescriptionNotify(getTimeoutString(
					getEvent(), timeout - i));
			}

			// after timeout passes, check if the alert has
			// been deployed
			if (iad.getAlertState() == AlertState.PENDING.ordinal()) {
				// if it hasn't, deploy it (if it has,
				// we're done!)
				iad.setAlertStateNotify(AlertState.DEPLOYED
					.ordinal());

				// change the description - use the
				// no-timeout description
				pn.setDescriptionNotify(getNoTimeoutDescription(
					getEvent()));

				// also record that it was addressed
				// automatically so the notification
				// goes away
				// TODO may want to make this an option
				// to force reviewing
				pn.setAddressedByNotify("auto");
				pn.setAddressedTimeNotify(new Date());
			}
			// NOTE: alert canceling handled in IpawsDeployerImpl
		} else
			// no timeout - just deploy it
			iad.setAlertStateNotify(AlertState.DEPLOYED.ordinal());
	}

	/** Create a notification for an alert deployer given the event type,
	 *  deployment mode (auto/manual), timeout, and the name of the deployer
	 *  object.
	 */
	static private NotificationImpl createNotification(String event,
		boolean autoMode, int timeout, String dName)
		throws SonarException, TMSException
	{
		// substitute the event type into the notification title
		String title;
		try {
			title = String.format(I18N.get("ipaws.notification.title"), event);
		} catch (IllegalFormatException e) {
			title = String.format("New %s Alert", event);
		}
		// same with the description
		String description;
		if (autoMode) {
			if (timeout == 0)
				description = getNoTimeoutDescription(event);
			else
				description = getTimeoutString(event, timeout);
		} else {
			try {
				description = String.format(I18N.get(
					"ipaws.notification.description.manual"), event);
			} catch (IllegalFormatException e) {
				description = String.format("New %s alert received from " +
					"IPAWS. Please review it for deployment.", event);
			}
		}
		// create the notification object with the values we got
		// note that users must be able to write alert deployer objects
		// to see these (otherwise they won't be able to to approve
		// them)
		NotificationImpl pn = new NotificationImpl(
			IpawsDeployer.SONAR_TYPE, dName, true, title,
			description);
		IpawsProcJob.log("Sending notification " + pn.getName());

		// notify clients of the creation so they receive it, then return
		pn.notifyCreate();
		return pn;
	}

	/** Get an alert description string for auto-deploy, no-timeout cases. */
	static private String getNoTimeoutDescription(String event) {
		try {
			return String.format(I18N.get(
				"ipaws.notification.description.auto.no_timeout"), event);
		} catch (IllegalFormatException e) {
			return String.format("New %s alert received from IPAWS. " +
				"This alert has been automatically deployed.", event);
		}
	}

	/** Get the alert description string containing the amount of time until
	 *  the timeout expires (for auto deployment mode) given an event type and
	 *  the time in seconds.
	 */
	static private String getTimeoutString(String event, int secs) {
		// use the time value to create a duration string
		String dur = NotificationHelper.getDurationString(
			Duration.ofSeconds(secs));

		// add the string
		String dFormat = I18N.get(
			"ipaws.notification.description.auto.timeout");
		try {
			return String.format(dFormat, event, dur);
		} catch (IllegalFormatException e) {
			return String.format("New %s alert received from IPAWS. This " +
				"alert will be automatically deployed in %s if no action " +
				"is taken.", event, dur);
		}
	}
}
