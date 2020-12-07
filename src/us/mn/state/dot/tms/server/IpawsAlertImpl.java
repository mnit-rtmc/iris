/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group, Inc.
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

import org.json.JSONObject;
import org.postgis.MultiPolygon;

import us.mn.state.dot.tms.CapResponseTypeEnum;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.IpawsAlert;
import us.mn.state.dot.tms.IteratorWrapper;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.TMSException;

/**
 * Integrated Public Alert and Warning System (IPAWS) Alert object
 * server-side implementation.
 *
 * @author Michael Janson
 * @author Gordon Parikh
 */
public class IpawsAlertImpl extends BaseObjectImpl implements IpawsAlert {
	
	/** Database table name */
	static private final String TABLE = "event.ipaws";
	
	/** Load all the IPAWS alerts */
	static public void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, IpawsAlertImpl.class);
		store.query("SELECT name, identifier, sender, sent_date, status, " +
			"message_type, scope, codes, note, alert_references, incidents, " +
			"categories, event, response_types, urgency, severity, " +
			"certainty, audience, effective_date, onset_date, " +
			"expiration_date, sender_name, headline, alert_description, " + 
			"instruction, parameters, area, ST_AsText(geo_poly), geo_loc, " +
			"purgeable, last_processed FROM event." + SONAR_TYPE + ";",
			new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				try {
					namespace.addObject(new IpawsAlertImpl(row));
				} catch(Exception e) {
					System.out.println("Error adding: " + row.getString(1));
					e.printStackTrace();
				}
			}
		});
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
		map.put("geo_loc", geo_loc);
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
	
	/** Create an incident advice */
	private IpawsAlertImpl(ResultSet row) throws SQLException {
		this(row.getString(1),			// name
			row.getString(2),			// identifier
			row.getString(3),			// sender
			row.getTimestamp(4),		// sent date
			row.getString(5),			// status
			row.getString(6),			// message type
			row.getString(7),			// scope
			getStringArray(row, 8),		// codes
			row.getString(9),			// note
			getStringArray(row, 10),	// alert references
			getStringArray(row, 11),	// incidents
			getStringArray(row, 12),	// categories
			row.getString(13),			// event
			getStringArray(row, 14),	// response types
			row.getString(15),			// urgency
			row.getString(16),			// severity
			row.getString(17),			// certainty
			row.getString(18), 			// audience
			row.getTimestamp(19),		// effective date
			row.getTimestamp(20),		// onset date
			row.getTimestamp(21),		// expiration date
			row.getString(22),			// sender name
			row.getString(23),			// headline
			row.getString(24), 			// alert description
			row.getString(25), 			// instruction
			row.getString(26),			// parameters
			row.getString(27),			// area
			row.getString(28),			// geo_poly
			row.getString(29),			// geo_loc
			getBoolean(row, 30), 		// purgeable flag
			row.getTimestamp(31)		// last processed
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
			store.update("DELETE FROM " + TABLE +
				" WHERE expiration_date < now() - '" + age +
				" days'::interval AND purgeable=true;");
		}
	}

	static public Iterator<IpawsAlertImpl> iterator() {
		return new IteratorWrapper<IpawsAlertImpl>(namespace.iterator(
				IpawsAlertImpl.SONAR_TYPE));
	}
	
	public IpawsAlertImpl(String n) throws TMSException {
		super(n);
	}
	
	public IpawsAlertImpl(String n, String i, String se, Date sd, String sta, 
			String mt, String sc, String[] cd, String nt, String[]ref,
			String[] inc, String[] ct, String ev, String[] rt, String u, 
			String sv, String cy, String au, Date efd, Date od, Date exd, 
			String sn, String hl, String ades, String in, String par, 
			String ar, String gp, String gl, Boolean p, Date pt) 
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
		geo_loc = lookupGeoLoc(gl);
		purgeable = p;
		last_processed = pt;
	}

	/** Notify SONAR clients of a change to an attribute. Clears the purgeable
	 *  flag to trigger reprocessing of the alert.
	 *  
	 *  Attribute names should use lower camel case instead of underscores 
	 *  (e.g. "someAttribute" instead of "some_attribute").
	 */
	@Override
	protected void notifyAttribute(String aname) {
		notifyAttribute(aname, true);
	}

	/** Notify SONAR clients of a change to an attribute. If clearPurgeable is
	 *  true, the the purgeable flag is cleared to trigger reprocessing of the
	 *  alert.
	 */
	protected void notifyAttribute(String aname, boolean clearPurgeable) {
		// notify clients about the change
		super.notifyAttribute(aname);
		
		// clear the purgeable flag so the alert gets reprocessed (since this
		// only gets called when the alert changes)
		if (clearPurgeable) {
			try {
				doSetPurgeable(null);
			} catch (TMSException e) {
				setPurgeable(null);
				e.printStackTrace();
			}
		}
	}
	
	/** Compare the two list values, protecting against null pointers and
	 *  whitespace issues.
	 */
	private boolean listEq(List<?> l1, List<?> l2) {
		// TODO whitespace is making checking for equality here difficult - 
		// this works around that
		String s1 = l1 != null ? l1.toString().replace(", ", ",") : null;
		String s2 = l2 != null ? l2.toString().replace(", ", ",") : null;
		return objectEquals(s1, s2);
	}
	
	/** Compare the two JSON-formatted strings. */
	private boolean jsonStrEq(String js1, String js2) {
		String j1 = (js1 != null && !js1.isEmpty())
				? new JSONObject(js1).toString() : null;
		String j2 = (js2 != null && !js2.isEmpty())
				? new JSONObject(js2).toString() : null;
		return objectEquals(j1, j2);
	}
	
	/** Identifier for the alert */
	private String identifier;

	/** Set the identifier */
	@Override
	public void setIdentifier(String i) {
		identifier = i;
	}

	/** Set the identifier */
	public void doSetIdentifier(String i) throws TMSException {
		if (!objectEquals(i, identifier)) {
			store.update(this, "identifier", i);
			setIdentifier(i);
			notifyAttribute("identifier");
		}
	}

	/** Get the identifier */
	@Override
	public String getIdentifier() {
		return identifier;
	}

	/** Alert sender */
	private String sender;
	
	/** Set the sender */
	@Override
	public void setSender(String se) {
		sender = se;
	}

	/** Set the sender */
	public void doSetSender(String se) throws TMSException {
		if (!objectEquals(se, sender)) {
			store.update(this, "sender", se);
			setSender(se);
			notifyAttribute("sender");
		}
	}
	
	/** Get the sender */
	@Override
	public String getSender() {
		return sender;
	}

	/** Sent date of alert */
	private Date sent_date;
	
	/** Set the sent date */
	@Override
	public void setSentDate(Date sd) {
		sent_date = sd;
	}

	/** Set the sent date */
	public void doSetSentDate(Date sd) throws TMSException {
		if (!objectEquals(sd, sent_date)) {
			store.update(this, "sent_date", sd);
			setSentDate(sd);
			notifyAttribute("sentDate");
		}
	}

	/** Get the sent date */
	@Override
	public Date getSentDate() {
		return sent_date;
	}

	/** Status of alert */
	private String status;
	
	/** Set the status */
	@Override
	public void setStatus(String sta) {
		status = sta;
	}

	/** Set the status */
	public void doSetStatus(String sta) throws TMSException {
		if (!objectEquals(status, sta)) {
			store.update(this, "status", sta);
			setStatus(sta);
			notifyAttribute("status");
		}
	}
	
	/** Get the status */
	@Override
	public String getStatus() {
		return status;
	}

	/** Alert message type */
	private String message_type;
	
	/** Set the message type */
	@Override
	public void setMsgType(String mt) {
		message_type = mt;
	}

	/** Set the message type */
	public void doSetMsgType(String mt) throws TMSException {
		if (!objectEquals(message_type, mt)) {
			store.update(this, "message_type", mt);
			setMsgType(mt);
			notifyAttribute("msgType");
		}
	}
	
	/** Get the message type */
	@Override
	public String getMsgType() {
		return message_type;
	}

	/** Alert scope */
	private String scope;
	
	/** Set the scope */
	@Override
	public void setScope(String sc) {
		scope = sc;
	}

	/** Set the scope */
	public void doSetScope(String sc) throws TMSException {
		if (!objectEquals(scope, sc)) {
			store.update(this, "scope", sc);
			setScope(sc);
			notifyAttribute("scope");
		}
	}
	
	/** Get the scope */
	@Override
	public String getScope() {
		return scope;
	}

	/** Alert codes */
	private List<String> codes;
	
	/** Set the codes */
	@Override
	public void setCodes(List<String> cd) {
		codes = cd;
	}

	/** Set the codes */
	public void doSetCodes(List<String> cd) throws TMSException {
		if (!listEq(codes, cd)) {
			store.update(this, "codes", cd);
			setCodes(cd);
			notifyAttribute("codes");
		}
	}
	
	/** Get the codes */
	@Override
	public List<String> getCodes() {
		return codes;
	}

	/** Alert note */
	private String note;
	
	/** Set the note */
	@Override
	public void setNote(String nt) {
		note = nt;
	}

	/** Set the note */
	public void doSetNote(String nt) throws TMSException {
		if (!objectEquals(note, nt)) {
			store.update(this, "note", nt);
			setNote(nt);
			notifyAttribute("note");
		}
	}
	
	/** Get the note */
	@Override
	public String getNote() {
		return note;
	}
	
	/** Alert references */
	private List<String> alert_references;
	
	/** Set the alert references */
	@Override
	public void setAlertReferences(List<String> ref) {
		alert_references = ref;
	}

	/** Set the alert references */
	public void doSetAlertReferences(List<String> ref) throws TMSException {
		if (!listEq(alert_references, ref)) {
			store.update(this, "alert_references", ref);
			setAlertReferences(ref);
			notifyAttribute("alertReferences");
		}
	}
	
	/** Get the alert references */
	@Override
	public List<String> getAlertReferences() {
		return alert_references;
	}
	/** Alert incidents */
	private List<String> incidents;
	
	/** Set the incidents */
	@Override
	public void setIncidents(List<String> inc) {
		incidents = inc;
	}

	/** Set the incidents */
	public void doSetIncidents(List<String> inc) throws TMSException {
		if (!listEq(incidents, inc)) {
			store.update(this, "incidents", inc);
			setIncidents(inc);
			notifyAttribute("incidents");
		}
	}

	/** Get the incidents */
	@Override
	public List<String> getIncidents() {
		return incidents;
	}

	/** Categories of alert */
	private List<String> categories;
	
	/** Set the categories */
	@Override
	public void setCategories(List<String> ct) {
		categories = ct;
	}

	/** Set the categories */
	public void doSetCategories(List<String> ct) throws TMSException {
		if (!listEq(categories, ct)) {
			store.update(this, "categories", ct);
			setCategories(ct);
			notifyAttribute("categories");
		}
	}

	/** Get the categories */
	@Override
	public List<String> getCategories() {
		return categories;
	}
	
	/** Alert event */
	private String event;

	/** Set the event */
	@Override
	public void setEvent(String ev) {
		event = ev;
	}

	/** Set the event */
	public void doSetEvent(String ev) throws TMSException {
		if (!objectEquals(event, ev)) {
			store.update(this, "event", ev);
			setEvent(ev);
			notifyAttribute("event");
		}
	}
	
	/** Get the event */
	@Override
	public String getEvent() {
		return event;
	}
	
	/** Alert response types */
	private List<String> response_types;
	
	/** Set the response types */
	@Override
	public void setResponseTypes(List<String> rt) {
		response_types = rt;
	}

	/** Set the response types */
	public void doSetResponseTypes(List<String> rt) throws TMSException {
		if (!listEq(response_types, rt)) {
			store.update(this, "response_types", rt);
			setResponseTypes(rt);
			notifyAttribute("responseTypes");
		}
	}

	/** Get the response type(s) */
	@Override
	public List<String> getResponseTypes() {
		return response_types;
	}
	
	/** Get the highest-priority response type */
	public String getPriorityResponseType() {
		// go through all response types to get the one with the highest
		// ordinal
		CapResponseTypeEnum maxRT = CapResponseTypeEnum.NONE;
		for (String rts: response_types) {
			CapResponseTypeEnum crte = CapResponseTypeEnum.fromValue(rts);
			if (crte.ordinal() > maxRT.ordinal())
				maxRT = crte;
		}
		return maxRT.value;
	}
	
	/** Urgency of alert */
	private String urgency;

	/** Set the urgency */
	@Override
	public void setUrgency(String u) {
		urgency = u;
	}

	/** Set the urgency */
	public void doSetUrgency(String u) throws TMSException {
		if (!objectEquals(urgency, u)) {
			store.update(this, "urgency", u);
			setUrgency(u);
			notifyAttribute("urgency");
		}
	}
	
	/** Get the urgency */
	@Override
	public String getUrgency() {
		return urgency;
	}
	
	/** Severity of the alert */
	private String severity;
	
	/** Set the severity */
	@Override
	public void setSeverity(String sv) {
		severity = sv;
	}

	/** Set the severity */
	public void doSetSeverity(String sv) throws TMSException {
		if (!objectEquals(severity, sv)) {
			store.update(this, "severity", sv);
			setSeverity(sv);
			notifyAttribute("severity");
		}
	}
	
	/** Get the severity */
	@Override
	public String getSeverity() {
		return severity;
	}

	/** Certainty of the alert */
	private String certainty;
	
	/** Set the certainty */
	@Override
	public void setCertainty(String cy) {
		certainty = cy;
	}

	/** Set the certainty */
	public void doSetCertainty(String cy) throws TMSException {
		if (!objectEquals(certainty, cy)) {
			store.update(this, "certainty", cy);
			setCertainty(cy);
			notifyAttribute("certainty");
		}
	}
	
	/** Get the certainty */
	@Override
	public String getCertainty() {
		return certainty;
	}
	
	/** Audience for the alert */
	private String audience;
	
	/** Set the audience */
	@Override
	public void setAudience(String au) {
		audience = au;
	}

	/** Set the audience */
	public void doSetAudience(String au) throws TMSException {
		if (!objectEquals(audience, au)) {
			store.update(this, "audience", au);
			setAudience(au);
			notifyAttribute("audience");
		}
	}
	
	/** Get the audience */
	@Override
	public String getAudience() {
		return audience;
	}	

	/** Effective date of the alert */
	private Date effective_date;
	
	/** Set the effective date */
	@Override
	public void setEffectiveDate(Date efd) {
		effective_date = efd;
	}

	/** Set the effective date */
	public void doSetEffectiveDate(Date efd) throws TMSException {
		if (!objectEquals(efd, effective_date)) {
			store.update(this, "effective_date", efd);
			setEffectiveDate(efd);
			notifyAttribute("effectiveDate");
		}
	}
	
	/** Get the effective date */
	@Override
	public Date getEffectiveDate() {
		return effective_date;
	}
	
	/** Onset date for alert */
	private Date onset_date;
	
	/** Set the onset date */
	@Override
	public void setOnsetDate(Date od) {
		onset_date = od;
	}

	/** Set the onset date */
	public void doSetOnsetDate(Date od) throws TMSException {
		if (!objectEquals(od, onset_date)) {
			store.update(this, "onset_date", od);
			setOnsetDate(od);
			notifyAttribute("onsetDate");
		}
	}

	/** Get the onset date */
	@Override
	public Date getOnsetDate() {
		return onset_date;
	}

	/** Expiration date for alert */
	private Date expiration_date;
	
	/** Set the expiration date*/
	@Override
	public void setExpirationDate(Date exd) {
		expiration_date = exd;
	}

	/** Set the expiration date */
	public void doSetExpirationDate(Date exd) throws TMSException {
		if (!objectEquals(exd, expiration_date)) {
			store.update(this, "expiration_date", exd);
			setExpirationDate(exd);
			notifyAttribute("expirationDate");
		}
	}

	/** Get the expiration date */
	@Override
	public Date getExpirationDate() {
		return expiration_date;
	}
	
	/** The alert sender's name */
	private String sender_name;

	/** Set the sender's name */
	@Override
	public void setSenderName(String sn) {
		sender_name = sn;
	}

	/** Set the sender's name */
	public void doSetSenderName(String sn) throws TMSException {
		if (!objectEquals(sender_name, sn)) {
			store.update(this, "sender_name", sn);
			setSenderName(sn);
			notifyAttribute("senderName");
		}
	}
	
	/** Get the sender's name */
	@Override
	public String getSenderName() {
		return sender_name;
	}

	/** Headline for the alert */
	private String headline;
	
	/** Set the alert headline */
	@Override
	public void setHeadline(String hl) {
		headline = hl;
	}

	/** Set the alert headline */
	public void doSetHeadline(String hl) throws TMSException {
		if (!objectEquals(headline, hl)) {
			store.update(this, "headline", hl);
			setHeadline(hl);
			notifyAttribute("headline");
		}
	}
	
	/** Get the alert headline */
	@Override
	public String getHeadline() {
		return headline;
	}
	
	/** Description of alert */
	private String alert_description;
	
	/** Set the description */
	@Override
	public void setAlertDescription(String ad) {
		alert_description = ad;
	}

	/** Set the alert description */
	public void doSetAlertDescription(String ad) throws TMSException {
		if (!objectEquals(alert_description, ad)) {
			store.update(this, "alert_description", ad);
			setAlertDescription(ad);
			notifyAttribute("alertDescription");
		}
	}
	
	/** Get the description */
	@Override
	public String getAlertDescription() {
		return alert_description;
	}
	
	/** Alert instruction */
	private String instruction;
	
	/** Set the instruction */
	@Override
	public void setInstruction(String in) {
		instruction = in;
	}

	/** Set the instruction */
	public void doSetInstruction(String in) throws TMSException {
		if (!objectEquals(instruction, in)) {
			store.update(this, "instruction", in);
			setInstruction(in);
			notifyAttribute("instruction");
		}
	}
	
	/** Get the description */
	@Override
	public String getInstruction() {
		return instruction;
	}
	
	/** Parameters */
	private String parameters;
	
	/** Set the parameters */
	@Override
	public void setParameters(String par) {
		parameters = par;
	}

	/** Set the parameters */
	public void doSetParameters(String par) throws TMSException {
		if (!jsonStrEq(parameters, par)) {
			store.update(this, "parameters", par);
			setParameters(par);
			notifyAttribute("parameters");
		}
	}
	
	/** Get the parameters */
	@Override
	public String getParameters() {
		return parameters;
	}
	
	/** Area */
	private String area;
	
	/** Set the area */
	@Override
	public void setArea(String ar) {
		area = ar;
	}

	/** Set the area */
	public void doSetArea(String ar) throws TMSException {
		if (!jsonStrEq(area, ar)) {
			store.update(this, "area", ar);
			setArea(ar);
			notifyAttribute("area");
		}
	}

	/** Get the area */
	@Override
	public String getArea() {
		return area;
	}
	
	/** Get the geographic polygon representing the area. */
	@Override
	public MultiPolygon getGeoPoly() {
		return geo_poly;
	}
	
	/** Geographic MultiPolygon */
	private MultiPolygon geo_poly;
	
	/** Set the area */
	@Override
	public void setGeoPoly(MultiPolygon gp) {
		geo_poly = gp;
	}
	
	/** Set the area */
	public void doSetGeoPoly(MultiPolygon gp) throws TMSException {
		if (!objectEquals(geo_poly, gp)) {
			store.update(this, "geo_poly", gp);
			setGeoPoly(gp);
			notifyAttribute("geoPoly", false);
		}
	}
	
	/** Set the area from a string */
	@Override
	public void setGeoPoly(String gpstr) {
		try {
			geo_poly = new MultiPolygon(gpstr);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/** Set the area from a string */
	public void doSetGeoPoly(String gpstr) throws TMSException {
		if (!objectEquals(geo_poly.toString(), gpstr)) {
			// NOTE need to use setGeoPoly first, then store.update
			setGeoPoly(gpstr);
			store.update(this, "geo_poly", geo_poly);
			notifyAttribute("geoPoly", false);
		}
	}

	/** GeoLoc for this alert (the alert area's centroid). */
	private GeoLoc geo_loc;
	
	/** Set the GeoLoc, which is the alert area's centroid */
	@Override
	public void setGeoLoc(GeoLoc gl) {
		geo_loc = gl;
	}
	
	/** Set the GeoLoc, which is the alert area's centroid */
	public void doSetGeoLoc(GeoLoc gl) throws TMSException {
		if (!objectEquals(geo_loc, gl)) {
			store.update(this, "geo_loc", gl);
			setGeoLoc(gl);
			notifyAttribute("geoLoc", false);
		}
		geo_loc = gl;
	}
	
	/** Get the GeoLoc, which is the alert area's centroid */
	@Override
	public GeoLoc getGeoLoc() {
		return geo_loc;
	}
	
	/** Purgeable flag. Null if the alert has not yet been processed, true if
	 *  alert is determined to be irrelevant to this system's users.
	 */
	private Boolean purgeable;
	
	/** Set if this alert is purgeable (irrelevant to us) */
	public void setPurgeable(Boolean p) {
		purgeable = p;
	}

	/** Set if this alert is purgeable (irrelevant to us). Notifies clients of
	 *  the change.
	 */
	public void doSetPurgeable(Boolean p) throws TMSException {
		if (!objectEquals(purgeable, p)) {
			IpawsProcJob.log("Setting purgeable flag of alert " +
					name + " to " + p);
			store.update(this, "purgeable", p);
			setPurgeable(p);
			notifyAttribute("purgeable", false);
		}
	}
	
	/** Return if this alert is purgeable (irrelevant to us) */
	public Boolean getPurgeable() {
		return purgeable;
	}

	/** Last processing time of the alert */
	private Date last_processed;
	
	/** Set the last processing time of the alert */
	@Override
	public void setLastProcessed(Date pt) {
		last_processed = pt;
	}

	/** Set the last processing time of the alert */
	public void doSetLastProcessed(Date pt) throws TMSException {
		if (!objectEquals(pt, last_processed)) {
			store.update(this, "last_processed", pt);
			setLastProcessed(pt);
			notifyAttribute("lastProcessed", false);
		}
	}

	/** Get the last processing time of the alert */
	@Override
	public Date getLastProcessed() {
		return last_processed;
	}
}
