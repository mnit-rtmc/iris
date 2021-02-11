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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.postgis.MultiPolygon;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.ActionPlan;
import us.mn.state.dot.tms.AlertInfo;
import us.mn.state.dot.tms.AlertState;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.IteratorWrapper;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.utils.UniqueNameCreator;

/**
 * CAP Alert Information is an object which is created when a CAP alert triggers
 * one or more DMS to be deployed.
 *
 * @author Michael Janson
 * @author Gordon Parikh
 * @author Douglas Lau
 */
public class AlertInfoImpl extends BaseObjectImpl implements AlertInfo {

	/** Create a unique AlertInfo record name */
	static public String createUniqueName(String template) {
		UniqueNameCreator unc = new UniqueNameCreator(template, 20,
			(n)->lookupAlertInfo(n));
		return unc.createUniqueName();
	}

	/** Load all the alert infos */
	static public void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, AlertInfoImpl.class);
		store.query("SELECT name, alert, replaces, start_date, " +
			"end_date, event, response_type, urgency, severity, " +
			"certainty, headline, description, instruction, " +
			"area_desc, geo_poly, lat, lon, sign_group, " +
			"action_plan, alert_state FROM cap." + SONAR_TYPE +
			" WHERE alert_state < 2;", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new AlertInfoImpl(row));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("alert", alert);
		map.put("replaces", replaces);
		map.put("start_date", start_date);
		map.put("end_date", end_date);
		map.put("event", event);
		map.put("response_type", response_type);
		map.put("urgency", urgency);
		map.put("severity", severity);
		map.put("certainty", certainty);
		map.put("headline", headline);
		map.put("description", description);
		map.put("instruction", instruction);
		map.put("area_desc", area_desc);
		map.put("geo_poly", geo_poly);
		map.put("lat", lat);
		map.put("lon", lon);
		map.put("sign_group", sign_group);
		map.put("action_plan", action_plan);
		map.put("alert_state", alert_state);
		return map;
	}

	/** Get the database table name */
	@Override
	public String getTable() {
		return "cap." + SONAR_TYPE;
	}

	/** Get the SONAR type name */
	@Override
	public String getTypeName() {
		return SONAR_TYPE;
	}

	/** Create an alert info */
	private AlertInfoImpl(ResultSet row) throws SQLException {
		this(row.getString(1),     // name
		     row.getString(2),     // alert
		     row.getString(3),     // replaces
		     row.getTimestamp(4),  // start_date
		     row.getTimestamp(5),  // end_date
		     row.getString(6),     // event
		     row.getInt(7),        // response_type
		     row.getInt(8),        // urgency
		     row.getInt(9),        // severity
		     row.getInt(10),       // certainty
		     row.getString(11),    // headline
		     row.getString(12),    // description
		     row.getString(13),    // instruction
		     row.getString(14),    // area_desc
		     row.getObject(15),    // geo_poly
		     row.getDouble(16),    // lat
		     row.getDouble(17),    // lon
		     row.getString(18),    // sign_group
		     row.getString(19),    // action_plan
		     row.getInt(20)        // alert_state
		);
	}

	/** Create an alert info */
	private AlertInfoImpl(String n, String al, String rpl, Date sd, Date ed,
		String ev, int rt, int ur, int sv, int cy, String hl,
		String dsc, String ins, String adsc, Object gp, double lt,
		double ln, String grp, String pln, int st)
	{
		this(n, al, rpl, sd, ed, ev, rt, ur, sv, cy, hl, dsc, ins, adsc,
		     SQLConnection.multiPolygon(gp), lt, ln,
		     lookupSignGroup(grp), lookupActionPlan(pln), st);
	}

	/** Create an alert info */
	public AlertInfoImpl(String n, String al, String rpl, Date sd, Date ed,
		String ev, int rt, int ur, int sv, int cy, String hl,
		String dsc, String ins, String adsc, MultiPolygon gp, double lt,
		double ln, SignGroup grp, ActionPlanImpl pln, int st)
	{
		super(n);
		alert = al;
		replaces = rpl;
		start_date = sd;
		end_date = ed;
		event = ev;
		response_type = rt;
		urgency = ur;
		severity = sv;
		certainty = cy;
		headline = hl;
		description = dsc;
		instruction = ins;
		area_desc = adsc;
		geo_poly = gp;
		lat = lt;
		lon = ln;
		sign_group = grp;
		action_plan = pln;
		alert_state = st;
	}

	/** Log a message for the alert */
	public void log(String msg) {
		if (CapAlert.LOG.isOpen())
			CapAlert.LOG.log("alert " + alert + ": " + msg);
	}

	/** Identifier for the alert */
	private String alert;

	/** Get the alert identifier */
	@Override
	public String getAlert() {
		return alert;
	}

	/** Name of replaced alert info */
	private String replaces;

	/** Get name of alert info this replaces */
	@Override
	public String getReplaces() {
		return replaces;
	}

	/** Start date */
	private Date start_date;

	/** Get the start date */
	@Override
	public Date getStartDate() {
		return start_date;
	}

	/** End date */
	private Date end_date;

	/** Get the end date */
	@Override
	public Date getEndDate() {
		return end_date;
	}

	/** CAP event code */
	private String event;

	/** Get the CAP event code (CapEvent name) */
	@Override
	public String getEvent() {
		return event;
	}

	/** CAP response type */
	private int response_type;

	/** Get the CAP response type (CapResponseType ordinal) */
	@Override
	public int getResponseType() {
		return response_type;
	}

	/** CAP urgency */
	private int urgency;

	/** Get the CAP urgency (CapUrgency ordinal) */
	@Override
	public int getUrgency() {
		return urgency;
	}

	/** CAP severity */
	private int severity;

	/** Get the CAP severity (CapSeverity ordinal) */
	@Override
	public int getSeverity() {
		return severity;
	}

	/** CAP certainty */
	private int certainty;

	/** Get the CAP certainty (CapCertainty ordinal) */
	@Override
	public int getCertainty() {
		return certainty;
	}

	/** Headline for the alert */
	private String headline;

	/** Get the alert headline */
	@Override
	public String getHeadline() {
		return headline;
	}

	/** Alert Description */
	private String description;

	/** Get the description */
	@Override
	public String getDescription() {
		return description;
	}

	/** Alert instruction */
	private String instruction;

	/** Get the alert instruction */
	@Override
	public String getInstruction() {
		return instruction;
	}

	/** Area description */
	private String area_desc;

	/** Get the area description */
	@Override
	public String getAreaDesc() {
		return area_desc;
	}

	/** Geographic MultiPolygon */
	private MultiPolygon geo_poly;

	/** Get the geographic polygon of the area */
	@Override
	public MultiPolygon getGeoPoly() {
		return geo_poly;
	}

	/** Latitude */
	private double lat;

	/** Get the latitude of the alert area's centroid */
	@Override
	public double getLat() {
		return lat;
	}

	/** Longitude */
	private double lon;

	/** Get the longitude of the alert area's centroid */
	@Override
	public double getLon() {
		return lon;
	}

	/** Group containing all auto and optional signs */
	private SignGroup sign_group;

	/** Get the group containing all auto and optional signs */
	@Override
	public SignGroup getSignGroup() {
		return sign_group;
	}

	/** Action plan */
	private ActionPlan action_plan;

	/** Get the action plan */
	@Override
	public ActionPlan getActionPlan() {
		return action_plan;
	}

	/** Alert state */
	private int alert_state = AlertState.PENDING.ordinal();

	/** Get the alert state (AlertState ordinal) */
	@Override
	public int getAlertState() {
		return alert_state;
	}

	/** Set the alert state (AlertState ordinal) */
	@Override
	public void setAlertStateReq(int st) { }

	/** Set the alert state (AlertState ordinal) */
	public void doSetAlertStateReq(int st) throws TMSException {
		switch (AlertState.fromOrdinal(st)) {
		case ACTIVE_REQ:
			activate();
			break;
		case CLEARED_REQ:
			clear();
			break;
		default:
			throw new ChangeVetoException("Invalid state req");
		}
	}

	/** Activate the alert */
	private void activate() throws TMSException {
		setAlertStateNotify(AlertState.ACTIVE.ordinal());
		setActiveScheduled(true);
	}

	/** Clear the alert */
	private void clear() throws TMSException {
		setAlertStateNotify(AlertState.CLEARED.ordinal());
		setActiveScheduled(false);
		clear_time = TimeSteward.currentTimeMillis();
	}

	/** Set action plan active/scheduled */
	private void setActiveScheduled(boolean a) throws TMSException {
		if (action_plan instanceof ActionPlanImpl) {
			ActionPlanImpl plan = (ActionPlanImpl) action_plan;
			plan.setActiveScheduledNotify(a);
		}
	}

	/** Set the alert state */
	private void setAlertStateNotify(int st) throws TMSException {
		if (st != alert_state) {
			store.update(this, "alert_state", st);
			alert_state = st;
			notifyAttribute("alertState");
		}
	}

	/** Time the alert was cleared */
	private long clear_time = TimeSteward.currentTimeMillis();

	/** Get the time the alert was cleared */
	public long getClearTime() {
		return clear_time;
	}
}
