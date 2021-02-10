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
import java.util.Map;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.AlertConfig;
import us.mn.state.dot.tms.QuickMessage;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.TMSException;

/**
 * Alert Configuration object server-side implementation.
 *
 * Connects a particular alert type ("event" field) to a number of sign
 * group/quick message pairs to control which signs are eligible for inclusion
 * in an alert and which message template to use.
 *
 * @author Gordon Parikh
 * @author Douglas Lau
 */
public class AlertConfigImpl extends BaseObjectImpl implements AlertConfig {

	/** Interval value of one hour (ms) */
	static private final long HOUR_MS = 60 * 60 * 1000;

	/** Load all the alert config objects */
	static public void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, AlertConfigImpl.class);
		store.query("SELECT name, event, response_type, urgency, " +
			"sign_group, quick_message, pre_alert_hours, " +
			"post_alert_hours, auto_deploy FROM iris." +
			SONAR_TYPE + ";", new ResultFactory()
		{
			@Override
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new AlertConfigImpl(row));
			}
		});
	}

	private AlertConfigImpl(ResultSet row) throws SQLException {
		this(row.getString(1), // name
		     row.getString(2), // event
		     row.getInt(3),    // response_type
		     row.getInt(4),    // urgency
		     row.getString(5), // sign_group
		     row.getString(6), // quick_message
		     row.getInt(7),    // pre_alert_hours
		     row.getInt(8),    // post_alert_hours
		     row.getBoolean(9) // auto_deploy
		);
	}

	private AlertConfigImpl(String n, String ev, int rt, int urg, String sg,
		String qm, int preh, int posth, boolean ad)
	{
		super(n);
		event = ev;
		response_type = rt;
		urgency = urg;
		sign_group = lookupSignGroup(sg);
		quick_message = lookupQuickMessage(qm);
		pre_alert_hours = preh;
		post_alert_hours = posth;
		auto_deploy = ad;
	}

	public AlertConfigImpl(String n) {
		super(n);
	}

	/** Get the SONAR type name */
	@Override
	public String getTypeName() {
		return SONAR_TYPE;
	}

	/** Get the database table name */
	@Override
	public String getTable() {
		return "iris." + SONAR_TYPE;
	}

	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("event", event);
		map.put("response_type", response_type);
		map.put("urgency", urgency);
		map.put("sign_group", sign_group);
		map.put("quick_message", quick_message);
		map.put("pre_alert_hours", pre_alert_hours);
		map.put("post_alert_hours", post_alert_hours);
		map.put("auto_deploy", auto_deploy);
		return map;
	}

	/** Alert event type */
	private String event;

	/** Set the alert event type */
	@Override
	public void setEvent(String ev) {
		event = ev;
	}

	/** Set the alert event type */
	public void doSetEvent(String ev) throws TMSException {
		if (ev != event) {
			store.update(this, "event", ev);
			setEvent(ev);
		}
	}

	/** Get the alert event type */
	@Override
	public String getEvent() {
		return event;
	}

	/** Response type (ordinal of CapResponseType enum) */
	private int response_type;

	/** Set the response type (ordinal of CapResponseType enum) */
	@Override
	public void setResponseType(int rt) {
		response_type = rt;
	}

	/** Set the response type (ordinal of CapResponseType enum) */
	public void doSetResponseType(int rt) throws TMSException {
		if (rt != response_type) {
			store.update(this, "response_type", rt);
			setResponseType(rt);
		}
	}

	/** Get the response type (ordinal of CapResponseType enum) */
	@Override
	public int getResponseType() {
		return response_type;
	}

	/** Urgency (ordinal of CapUrgency enum) */
	private int urgency;

	/** Set the urgency (ordinal of CapUrgency enum) */
	@Override
	public void setUrgency(int u) {
		urgency = u;
	}

	/** Set the urgency (ordinal of CapUrgency enum) */
	public void doSetUrgency(int u) throws TMSException {
		if (u != urgency) {
			store.update(this, "urgency", u);
			setUrgency(u);
		}
	}

	/** Get the urgency (ordinal of CapUrgency enum) */
	@Override
	public int getUrgency() {
		return urgency;
	}

	/** Sign group */
	private SignGroup sign_group;

	/** Set the sign group */
	@Override
	public void setSignGroup(SignGroup sg) {
		sign_group = sg;
	}

	/** Set the sign group */
	public void doSetSignGroup(SignGroup sg) throws TMSException {
		if (sg != sign_group) {
			store.update(this, "sign_group", sg);
			setSignGroup(sg);
		}
	}

	/** Get the sign group */
	@Override
	public SignGroup getSignGroup() {
		return sign_group;
	}

	/** Quick message (template) */
	private QuickMessage quick_message;

	/** Set the quick message (template) */
	@Override
	public void setQuickMessage(QuickMessage qm) {
		quick_message = qm;
	}

	/** Set the quick message (template) */
	public void doSetQuickMessage(QuickMessage qm) throws TMSException {
		if (qm != quick_message) {
			store.update(this, "quick_message", qm);
			setQuickMessage(qm);
		}
	}

	/** Get the quick message (template) */
	@Override
	public QuickMessage getQuickMessage() {
		return quick_message;
	}

	/** Number of hours to display a pre-alert message before the alert
	 *  becomes active. */
	private int pre_alert_hours = 6;

	/** Set the number of hours to display a pre-alert message before the
	 *  alert becomes active. */
	@Override
	public void setPreAlertHours(int hours) {
		pre_alert_hours = hours;
	}

	/** Set the number of hours to display a pre-alert message before the
	 *  alert becomes active. */
	public void doSetPreAlertHours(int hours) throws TMSException {
		if (hours != pre_alert_hours) {
			store.update(this, "pre_alert_hours", hours);
			setPreAlertHours(hours);
		}
	}

	/** Get the number of hours to display a pre-alert message before the
	 *  alert becomes active. */
	@Override
	public int getPreAlertHours() {
		return pre_alert_hours;
	}

	/** Number of hours to display a post-alert message after an alert
	 *  expires or is cleared. */
	private int post_alert_hours = 0;

	/** Set the number of hours to display a post-alert message after an
	 *  alert expires or is cleared. */
	@Override
	public void setPostAlertHours(int hours) {
		post_alert_hours = hours;
	}

	/** Set the number of hours to display a post-alert message after an
	 *  alert expires or is cleared. */
	public void doSetPostAlertHours(int hours) throws TMSException {
		if (hours != post_alert_hours) {
			store.update(this, "post_alert_hours", hours);
			setPostAlertHours(hours);
		}
	}

	/** Get the number of hours to display a post-alert message after an
	 *  alert expires or is cleared. */
	@Override
	public int getPostAlertHours() {
		return post_alert_hours;
	}

	/** Auto-deploy enabled */
	private boolean auto_deploy;

	/** Enable/disable auto deploy */
	@Override
	public void setAutoDeploy(boolean ad) {
		auto_deploy = ad;
	}

	/** Enable/disable auto deploy */
	public void doSetAutoDeploy(boolean ad) throws TMSException {
		if (ad != auto_deploy) {
			store.update(this, "auto_deploy", ad);
			setAutoDeploy(ad);
		}
	}

	/** Get auto deploy enabled state */
	@Override
	public boolean getAutoDeploy() {
		return auto_deploy;
	}

	/** Lookup the current plan phase name */
	public String getCurrentPhase(Date start_date, Date end_date) {
		// Use time from one minute ago to avoid missing time actions
		long now = TimeSteward.currentTimeMillis() - 60 * 1000;
		long sd = start_date.getTime();
		long pre_ms = getPreAlertHours() * HOUR_MS;
		long ed = end_date.getTime();
		long post_ms = getPostAlertHours() * HOUR_MS;
		if (now < sd - pre_ms)
			return "undeployed";
		else if (now < sd)
			return "alert_before";
		else if (now < ed)
			return "alert_during";
		else if (now < ed + post_ms)
			return "alert_after";
		else
			return "undeployed";
	}
}
