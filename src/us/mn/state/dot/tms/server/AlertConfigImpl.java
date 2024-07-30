/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group, Inc.
 * Copyright (C) 2021-2024  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.Hashtags;
import us.mn.state.dot.tms.PlanPhase;
import us.mn.state.dot.tms.TMSException;

/**
 * Alert Configuration object server-side implementation.
 *
 * Connects a particular alert type ("event" field) to a number of sign
 * group/message pattern pairs to control which signs are eligible for
 * inclusion in an alert and which message template to use.
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
		store.query("SELECT name, event, response_shelter, " +
			"response_evacuate, response_prepare, " +
			"response_execute, response_avoid, response_monitor, " +
			"response_all_clear, response_none, urgency_unknown, " +
			"urgency_past, urgency_future, urgency_expected, " +
			"urgency_immediate, severity_unknown, severity_minor, "+
			"severity_moderate, severity_severe, " +
			"severity_extreme, certainty_unknown, " +
			"certainty_unlikely, certainty_possible, " +
			"certainty_likely, certainty_observed, auto_deploy, " +
			"before_period_hours, after_period_hours, " +
			"dms_hashtag FROM iris." + SONAR_TYPE + ";",
			new ResultFactory()
		{
			@Override
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new AlertConfigImpl(row));
			}
		});
	}

	/** Create an alert config */
	private AlertConfigImpl(ResultSet row) throws SQLException, TMSException
	{
		this(row.getString(1),   // name
		     row.getString(2),   // event
		     row.getBoolean(3),  // response_shelter
		     row.getBoolean(4),  // response_evacuate
		     row.getBoolean(5),  // response_prepare
		     row.getBoolean(6),  // response_execute
		     row.getBoolean(7),  // response_avoid
		     row.getBoolean(8),  // response_monitor
		     row.getBoolean(9),  // response_all_clear
		     row.getBoolean(10), // response_none
		     row.getBoolean(11), // urgency_unknown
		     row.getBoolean(12), // urgency_past
		     row.getBoolean(13), // urgency_future
		     row.getBoolean(14), // urgency_expected
		     row.getBoolean(15), // urgency_immediate
		     row.getBoolean(16), // severity_unknown
		     row.getBoolean(17), // severity_minor
		     row.getBoolean(18), // severity_moderate
		     row.getBoolean(19), // severity_severe
		     row.getBoolean(20), // severity_extreme
		     row.getBoolean(21), // certainty_unknown
		     row.getBoolean(22), // certainty_unlikely
		     row.getBoolean(23), // certainty_possible
		     row.getBoolean(24), // certainty_likely
		     row.getBoolean(25), // certainty_observed
		     row.getBoolean(26), // auto_deploy
		     row.getInt(27),     // before_period_hours
		     row.getInt(28),     // after_period_hours
		     row.getString(29)   // dms_hashtag
		);
	}

	/** Create an alert config */
	private AlertConfigImpl(String n, String ev, boolean rs, boolean re,
		boolean rp, boolean rx, boolean ra, boolean rm, boolean rc,
		boolean rn, boolean uu, boolean up, boolean uf, boolean ue,
		boolean ui, boolean su, boolean sm, boolean sd, boolean ss,
		boolean se, boolean cu, boolean cy, boolean cp, boolean cl,
		boolean co, boolean ad, int bfrh, int afth, String dht)
		throws TMSException
	{
		super(n);
		event = ev;
		response_shelter = rs;
		response_evacuate = re;
		response_prepare = rp;
		response_execute = rx;
		response_avoid = ra;
		response_monitor = rm;
		response_all_clear = rc;
		response_none = rn;
		urgency_unknown = uu;
		urgency_past = up;
		urgency_future = uf;
		urgency_expected = ue;
		urgency_immediate = ui;
		severity_unknown = su;
		severity_minor = sm;
		severity_moderate = sd;
		severity_severe = ss;
		severity_extreme = se;
		certainty_unknown = cu;
		certainty_unlikely = cy;
		certainty_possible = cp;
		certainty_likely = cl;
		certainty_observed = co;
		auto_deploy = ad;
		before_period_hours = bfrh;
		after_period_hours = afth;
		dms_hashtag = dht;
	}

	/** Create an alert config */
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

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("event", event);
		map.put("response_shelter", response_shelter);
		map.put("response_evacuate", response_evacuate);
		map.put("response_prepare", response_prepare);
		map.put("response_execute", response_execute);
		map.put("response_avoid", response_avoid);
		map.put("response_monitor", response_monitor);
		map.put("response_all_clear", response_all_clear);
		map.put("response_none", response_none);
		map.put("urgency_unknown", urgency_unknown);
		map.put("urgency_past", urgency_past);
		map.put("urgency_future", urgency_future);
		map.put("urgency_expected", urgency_expected);
		map.put("urgency_immediate", urgency_immediate);
		map.put("severity_unknown", severity_unknown);
		map.put("severity_minor", severity_minor);
		map.put("severity_moderate", severity_moderate);
		map.put("severity_severe", severity_severe);
		map.put("severity_extreme", severity_extreme);
		map.put("certainty_unknown", certainty_unknown);
		map.put("certainty_unlikely", certainty_unlikely);
		map.put("certainty_possible", certainty_possible);
		map.put("certainty_likely", certainty_likely);
		map.put("certainty_observed", certainty_observed);
		map.put("auto_deploy", auto_deploy);
		map.put("before_period_hours", before_period_hours);
		map.put("after_period_hours", after_period_hours);
		map.put("dms_hashtag", dms_hashtag);
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

	/** Response shelter flag */
	private boolean response_shelter;

	/** Set the response shelter flag */
	@Override
	public void setResponseShelter(boolean fl) {
		response_shelter = fl;
	}

	/** Set the response shelter flag */
	public void doSetResponseShelter(boolean fl) throws TMSException {
		if (fl != response_shelter) {
			store.update(this, "response_shelter", fl);
			setResponseShelter(fl);
		}
	}

	/** Get the response shelter flag */
	@Override
	public boolean getResponseShelter() {
		return response_shelter;
	}

	/** Response evacuate flag */
	private boolean response_evacuate;

	/** Set the response evacuate flag */
	@Override
	public void setResponseEvacuate(boolean fl) {
		response_evacuate = fl;
	}

	/** Set the response evacuate flag */
	public void doSetResponseEvacuate(boolean fl) throws TMSException {
		if (fl != response_evacuate) {
			store.update(this, "response_evacuate", fl);
			setResponseEvacuate(fl);
		}
	}

	/** Get the response evacuate flag */
	@Override
	public boolean getResponseEvacuate() {
		return response_evacuate;
	}

	/** Response prepare flag */
	private boolean response_prepare;

	/** Set the response prepare flag */
	@Override
	public void setResponsePrepare(boolean fl) {
		response_prepare = fl;
	}

	/** Set the response prepare flag */
	public void doSetResponsePrepare(boolean fl) throws TMSException {
		if (fl != response_prepare) {
			store.update(this, "response_prepare", fl);
			setResponsePrepare(fl);
		}
	}

	/** Get the response prepare flag */
	@Override
	public boolean getResponsePrepare() {
		return response_prepare;
	}

	/** Response execute flag */
	private boolean response_execute;

	/** Set the response execute flag */
	@Override
	public void setResponseExecute(boolean fl) {
		response_execute = fl;
	}

	/** Set the response execute flag */
	public void doSetResponseExecute(boolean fl) throws TMSException {
		if (fl != response_execute) {
			store.update(this, "response_execute", fl);
			setResponseExecute(fl);
		}
	}

	/** Get the response execute flag */
	@Override
	public boolean getResponseExecute() {
		return response_execute;
	}

	/** Response avoid flag */
	private boolean response_avoid;

	/** Set the response avoid flag */
	@Override
	public void setResponseAvoid(boolean fl) {
		response_avoid = fl;
	}

	/** Set the response avoid flag */
	public void doSetResponseAvoid(boolean fl) throws TMSException {
		if (fl != response_avoid) {
			store.update(this, "response_avoid", fl);
			setResponseAvoid(fl);
		}
	}

	/** Get the response avoid flag */
	@Override
	public boolean getResponseAvoid() {
		return response_avoid;
	}

	/** Response monitor flag */
	private boolean response_monitor;

	/** Set the response monitor flag */
	@Override
	public void setResponseMonitor(boolean fl) {
		response_monitor = fl;
	}

	/** Set the response monitor flag */
	public void doSetResponseMonitor(boolean fl) throws TMSException {
		if (fl != response_monitor) {
			store.update(this, "response_monitor", fl);
			setResponseMonitor(fl);
		}
	}

	/** Get the response monitor flag */
	@Override
	public boolean getResponseMonitor() {
		return response_monitor;
	}

	/** Response all clear flag */
	private boolean response_all_clear;

	/** Set the response all clear flag */
	@Override
	public void setResponseAllClear(boolean fl) {
		response_all_clear = fl;
	}

	/** Set the response all clear flag */
	public void doSetResponseAllClear(boolean fl) throws TMSException {
		if (fl != response_all_clear) {
			store.update(this, "response_all_clear", fl);
			setResponseAllClear(fl);
		}
	}

	/** Get the response all clear flag */
	@Override
	public boolean getResponseAllClear() {
		return response_all_clear;
	}

	/** Response none flag */
	private boolean response_none;

	/** Set the response none flag */
	@Override
	public void setResponseNone(boolean fl) {
		response_none = fl;
	}

	/** Set the response none flag */
	public void doSetResponseNone(boolean fl) throws TMSException {
		if (fl != response_none) {
			store.update(this, "response_none", fl);
			setResponseNone(fl);
		}
	}

	/** Get the response none flag */
	@Override
	public boolean getResponseNone() {
		return response_none;
	}

	/** Urgency unknown flag */
	private boolean urgency_unknown;

	/** Set the urgency unknown flag */
	@Override
	public void setUrgencyUnknown(boolean fl) {
		urgency_unknown = fl;
	}

	/** Set the urgency unknown flag */
	public void doSetUrgencyUnknown(boolean fl) throws TMSException {
		if (fl != urgency_unknown) {
			store.update(this, "urgency_unknown", fl);
			setUrgencyUnknown(fl);
		}
	}

	/** Get the urgency unknown flag */
	@Override
	public boolean getUrgencyUnknown() {
		return urgency_unknown;
	}

	/** Urgency past flag */
	private boolean urgency_past;

	/** Set the urgency past flag */
	@Override
	public void setUrgencyPast(boolean fl) {
		urgency_past = fl;
	}

	/** Set the urgency past flag */
	public void doSetUrgencyPast(boolean fl) throws TMSException {
		if (fl != urgency_past) {
			store.update(this, "urgency_past", fl);
			setUrgencyPast(fl);
		}
	}

	/** Get the urgency past flag */
	@Override
	public boolean getUrgencyPast() {
		return urgency_past;
	}

	/** Urgency future flag */
	private boolean urgency_future;

	/** Set the urgency future flag */
	@Override
	public void setUrgencyFuture(boolean fl) {
		urgency_future = fl;
	}

	/** Set the urgency future flag */
	public void doSetUrgencyFuture(boolean fl) throws TMSException {
		if (fl != urgency_future) {
			store.update(this, "urgency_future", fl);
			setUrgencyFuture(fl);
		}
	}

	/** Get the urgency future flag */
	@Override
	public boolean getUrgencyFuture() {
		return urgency_future;
	}

	/** Urgency expected flag */
	private boolean urgency_expected;

	/** Set the urgency expected flag */
	@Override
	public void setUrgencyExpected(boolean fl) {
		urgency_expected = fl;
	}

	/** Set the urgency expected flag */
	public void doSetUrgencyExpected(boolean fl) throws TMSException {
		if (fl != urgency_expected) {
			store.update(this, "urgency_expected", fl);
			setUrgencyExpected(fl);
		}
	}

	/** Get the urgency expected flag */
	@Override
	public boolean getUrgencyExpected() {
		return urgency_expected;
	}

	/** Urgency immediate flag */
	private boolean urgency_immediate;

	/** Set the urgency immediate flag */
	@Override
	public void setUrgencyImmediate(boolean fl) {
		urgency_immediate = fl;
	}

	/** Set the urgency immediate flag */
	public void doSetUrgencyImmediate(boolean fl) throws TMSException {
		if (fl != urgency_immediate) {
			store.update(this, "urgency_immediate", fl);
			setUrgencyImmediate(fl);
		}
	}

	/** Get the urgency immediate flag */
	@Override
	public boolean getUrgencyImmediate() {
		return urgency_immediate;
	}

	/** Severity unknown flag */
	private boolean severity_unknown;

	/** Set the severity unknown flag */
	@Override
	public void setSeverityUnknown(boolean fl) {
		severity_unknown = fl;
	}

	/** Set the severity unknown flag */
	public void doSetSeverityUnknown(boolean fl) throws TMSException {
		if (fl != severity_unknown) {
			store.update(this, "severity_unknown", fl);
			setSeverityUnknown(fl);
		}
	}

	/** Get the severity unknown flag */
	@Override
	public boolean getSeverityUnknown() {
		return severity_unknown;
	}

	/** Severity minor flag */
	private boolean severity_minor;

	/** Set the severity minor flag */
	@Override
	public void setSeverityMinor(boolean fl) {
		severity_minor = fl;
	}

	/** Set the severity minor flag */
	public void doSetSeverityMinor(boolean fl) throws TMSException {
		if (fl != severity_minor) {
			store.update(this, "severity_minor", fl);
			setSeverityMinor(fl);
		}
	}

	/** Get the severity minor flag */
	@Override
	public boolean getSeverityMinor() {
		return severity_minor;
	}

	/** Severity moderate flag */
	private boolean severity_moderate;

	/** Set the severity moderate flag */
	@Override
	public void setSeverityModerate(boolean fl) {
		severity_moderate = fl;
	}

	/** Set the severity moderate flag */
	public void doSetSeverityModerate(boolean fl) throws TMSException {
		if (fl != severity_moderate) {
			store.update(this, "severity_moderate", fl);
			setSeverityModerate(fl);
		}
	}

	/** Get the severity moderate flag */
	@Override
	public boolean getSeverityModerate() {
		return severity_moderate;
	}

	/** Severity severe flag */
	private boolean severity_severe;

	/** Set the severity severe flag */
	@Override
	public void setSeveritySevere(boolean fl) {
		severity_severe = fl;
	}

	/** Set the severity severe flag */
	public void doSetSeveritySevere(boolean fl) throws TMSException {
		if (fl != severity_severe) {
			store.update(this, "severity_severe", fl);
			setSeveritySevere(fl);
		}
	}

	/** Get the severity severe flag */
	@Override
	public boolean getSeveritySevere() {
		return severity_severe;
	}

	/** Severity extreme flag */
	private boolean severity_extreme;

	/** Set the severity extreme flag */
	@Override
	public void setSeverityExtreme(boolean fl) {
		severity_extreme = fl;
	}

	/** Set the severity extreme flag */
	public void doSetSeverityExtreme(boolean fl) throws TMSException {
		if (fl != severity_extreme) {
			store.update(this, "severity_extreme", fl);
			setSeverityExtreme(fl);
		}
	}

	/** Get the severity extreme flag */
	@Override
	public boolean getSeverityExtreme() {
		return severity_extreme;
	}

	/** Certainty unknown flag */
	private boolean certainty_unknown;

	/** Set the certainty unknown flag */
	@Override
	public void setCertaintyUnknown(boolean fl) {
		certainty_unknown = fl;
	}

	/** Set the certainty unknown flag */
	public void doSetCertaintyUnknown(boolean fl) throws TMSException {
		if (fl != certainty_unknown) {
			store.update(this, "certainty_unknown", fl);
			setCertaintyUnknown(fl);
		}
	}

	/** Get the certainty unknown flag */
	@Override
	public boolean getCertaintyUnknown() {
		return certainty_unknown;
	}

	/** Certainty unlikely flag */
	private boolean certainty_unlikely;

	/** Set the certainty unlikely flag */
	@Override
	public void setCertaintyUnlikely(boolean fl) {
		certainty_unlikely = fl;
	}

	/** Set the certainty unlikely flag */
	public void doSetCertaintyUnlikely(boolean fl) throws TMSException {
		if (fl != certainty_unlikely) {
			store.update(this, "certainty_unlikely", fl);
			setCertaintyUnlikely(fl);
		}
	}

	/** Get the certainty unlikely flag */
	@Override
	public boolean getCertaintyUnlikely() {
		return certainty_unlikely;
	}

	/** Certainty possible flag */
	private boolean certainty_possible;

	/** Set the certainty possible flag */
	@Override
	public void setCertaintyPossible(boolean fl) {
		certainty_possible = fl;
	}

	/** Set the certainty possible flag */
	public void doSetCertaintyPossible(boolean fl) throws TMSException {
		if (fl != certainty_possible) {
			store.update(this, "certainty_possible", fl);
			setCertaintyPossible(fl);
		}
	}

	/** Get the certainty possible flag */
	@Override
	public boolean getCertaintyPossible() {
		return certainty_possible;
	}

	/** Certainty likely flag */
	private boolean certainty_likely;

	/** Set the certainty likely flag */
	@Override
	public void setCertaintyLikely(boolean fl) {
		certainty_likely = fl;
	}

	/** Set the certainty likely flag */
	public void doSetCertaintyLikely(boolean fl) throws TMSException {
		if (fl != certainty_likely) {
			store.update(this, "certainty_likely", fl);
			setCertaintyLikely(fl);
		}
	}

	/** Get the certainty likely flag */
	@Override
	public boolean getCertaintyLikely() {
		return certainty_likely;
	}

	/** Certainty observed flag */
	private boolean certainty_observed;

	/** Set the certainty observed flag */
	@Override
	public void setCertaintyObserved(boolean fl) {
		certainty_observed = fl;
	}

	/** Set the certainty observed flag */
	public void doSetCertaintyObserved(boolean fl) throws TMSException {
		if (fl != certainty_observed) {
			store.update(this, "certainty_observed", fl);
			setCertaintyObserved(fl);
		}
	}

	/** Get the certainty observed flag */
	@Override
	public boolean getCertaintyObserved() {
		return certainty_observed;
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

	/** Duration in hours for the "before" alert period */
	private int before_period_hours = 6;

	/** Set the duration in hours for the "before" alert period */
	@Override
	public void setBeforePeriodHours(int hours) {
		before_period_hours = hours;
	}

	/** Set the duration in hours for the "before" alert period */
	public void doSetBeforePeriodHours(int hours) throws TMSException {
		if (hours != before_period_hours) {
			store.update(this, "before_period_hours", hours);
			setBeforePeriodHours(hours);
		}
	}

	/** Get the duration in hours for the "before" alert period */
	@Override
	public int getBeforePeriodHours() {
		return before_period_hours;
	}

	/** Duration in hours for the "after" alert period */
	private int after_period_hours = 0;

	/** Set the duration in hours for the "after" alert period */
	@Override
	public void setAfterPeriodHours(int hours) {
		after_period_hours = hours;
	}

	/** Set the duration in hours for the "after" alert period */
	public void doSetAfterPeriodHours(int hours) throws TMSException {
		if (hours != after_period_hours) {
			store.update(this, "after_period_hours", hours);
			setAfterPeriodHours(hours);
		}
	}

	/** Get the duration in hours for the "after" alert period */
	@Override
	public int getAfterPeriodHours() {
		return after_period_hours;
	}

	/** Lookup the current plan phase name */
	public String getCurrentPhase(Date start_date, Date end_date) {
		// Use time in thirty seconds to avoid missing time actions
		long now = TimeSteward.currentTimeMillis() + 30 * 1000;
		long sd = start_date.getTime();
		long bfr_ms = getBeforePeriodHours() * HOUR_MS;
		long ed = end_date.getTime();
		long aft_ms = getAfterPeriodHours() * HOUR_MS;
		if (now < sd - bfr_ms)
			return PlanPhase.UNDEPLOYED;
		else if (now < sd)
			return PlanPhase.ALERT_BEFORE;
		else if (now < ed)
			return PlanPhase.ALERT_DURING;
		else if (now < ed + aft_ms)
			return PlanPhase.ALERT_AFTER;
		else
			return PlanPhase.UNDEPLOYED;
	}

	/** DMS hashtag */
	private String dms_hashtag;

	/** Set the DMS hashtag */
	@Override
	public void setDmsHashtag(String ht) {
		dms_hashtag = ht;
	}

	/** Set the DMS hashtag */
	public void doSetDmsHashtag(String ht) throws TMSException {
		String t = Hashtags.normalize(ht);
		if (!objectEquals(t, ht))
			throw new ChangeVetoException("Bad hashtag");
		if (!objectEquals(ht, dms_hashtag)) {
			store.update(this, "dms_hashtag", ht);
			setDmsHashtag(ht);
		}
	}

	/** Get the DMS hashtag */
	@Override
	public String getDmsHashtag() {
		return dms_hashtag;
	}
}
