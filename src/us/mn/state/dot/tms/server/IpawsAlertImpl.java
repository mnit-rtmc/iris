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
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import org.postgis.MultiPolygon;
import us.mn.state.dot.tms.CapResponseEnum;
import us.mn.state.dot.tms.IpawsAlert;
import us.mn.state.dot.tms.IteratorWrapper;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.TMSException;
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
			IpawsProcJob.log("Setting purgeable flag of alert " +
				name + " to " + p);
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
}
