/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2026  Minnesota Department of Transportation
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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.tms.ActCondition;
import us.mn.state.dot.tms.ActionPlan;
import us.mn.state.dot.tms.AlarmCondition;
import us.mn.state.dot.tms.AlertCondition;
import us.mn.state.dot.tms.DayPlan;
import us.mn.state.dot.tms.DayPlanHelper;
import us.mn.state.dot.tms.Detector;
import us.mn.state.dot.tms.DetectorHelper;
import us.mn.state.dot.tms.PlanPhase;
import us.mn.state.dot.tms.PhaseAction;
import us.mn.state.dot.tms.PhaseActionHelper;
import us.mn.state.dot.tms.RwisThreshold;
import us.mn.state.dot.tms.Station;
import us.mn.state.dot.tms.StationHelper;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.TollMode;
import us.mn.state.dot.tms.TrafThreshold;
import us.mn.state.dot.tms.utils.UniqueNameCreator;

/**
 * Action for triggering an action plan to change phases.
 *
 * @author Douglas Lau
 */
public class PhaseActionImpl extends BaseObjectImpl implements PhaseAction {

	/** Check if traffic threshold is currently triggered */
	static private boolean isTriggered(TrafThreshold tt) {
		Integer dv = dataValue(tt);
		if (dv != null) {
			if (tt.greater)
				return (dv > tt.value);
			else
				return (dv < tt.value);
		} else
			return false;
	}

	/** Get the current traffic data value */
	static private Integer dataValue(TrafThreshold tt) {
		Detector det = DetectorHelper.lookup(tt.id);
		if (det != null) {
			// FIXME
		} else {
			Station st = StationHelper.lookup(tt.id);
			if (st != null) {
				// FIXME
			}
		}
		return null;
	}

	/** Check if RWIS threshold is currently triggered */
	static private boolean isTriggered(RwisThreshold rt) {
		Integer dv = dataValue(rt);
		if (dv != null) {
			if (rt.greater)
				return (dv > rt.value);
			else
				return (dv < rt.value);
		} else
			return false;
	}

	/** Get the current RWIS data value */
	static private Integer dataValue(RwisThreshold rt) {
		// FIXME
		return null;
	}

	/** Create a unique PhaseAction record name */
	static public String createUniqueName(String template) {
		UniqueNameCreator unc = new UniqueNameCreator(template, 30,
			(n)->lookupPhaseAction(n));
		return unc.createUniqueName();
	}

	/** Load all the phase actions */
	static protected void loadAll() throws TMSException {
		store.query("SELECT name, action_plan, day_plan, from_phase, " +
			"to_phase, condition, params FROM iris." +
			SONAR_TYPE  +";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new PhaseActionImpl(row));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("action_plan", action_plan);
		map.put("day_plan", day_plan);
		map.put("from_phase", from_phase);
		map.put("to_phase", to_phase);
		map.put("condition", condition.ordinal());
		map.put("params", params);
		return map;
	}

	/** Create a new phase action */
	public PhaseActionImpl(String n) {
		super(n);
		condition = ActCondition.fromOrdinal(0);
	}

	/** Create a new phase action */
	private PhaseActionImpl(ResultSet row) throws SQLException {
		this(row.getString(1), // name
		     row.getString(2), // action_plan
		     row.getString(3), // day_plan
		     row.getString(4), // from_phase
		     row.getString(5), // to_phase
		     row.getInt(6),    // condition
		     row.getString(7)  // params
		);
	}

	/** Create a new phase action */
	private PhaseActionImpl(String n, String a, String dp, String fp,
		String tp, int c, String p)
	{
		this(n);
		action_plan = lookupActionPlan(a);
		day_plan = lookupDayPlan(dp);
		from_phase = lookupPlanPhase(fp);
		to_phase = lookupPlanPhase(tp);
		condition = ActCondition.fromOrdinal(c);
		params = p;
	}

	/** Action plan */
	private ActionPlan action_plan;

	/** Get the action plan */
	@Override
	public ActionPlan getActionPlan() {
		return action_plan;
	}

	/** Day plan */
	private DayPlan day_plan;

	/** Set the day plan */
	@Override
	public void setDayPlan(DayPlan dp) {
		day_plan = dp;
	}

	/** Set the day plan */
	public void doSetDayPlan(DayPlan dp) throws TMSException {
		if (dp != day_plan) {
			store.update(this, "day_plan", dp);
			setDayPlan(dp);
		}
	}

	/** Get the day plan */
	@Override
	public DayPlan getDayPlan() {
		return day_plan;
	}

	/** "From" Phase */
	private PlanPhase from_phase;

	/** Set the "from" phase */
	@Override
	public void setFromPhase(PlanPhase fp) {
		from_phase = fp;
	}

	/** Set the "from" phase */
	public void doSetFromPhase(PlanPhase fp) throws TMSException {
		if (fp != from_phase) {
			store.update(this, "from_phase", fp);
			setFromPhase(fp);
		}
	}

	/** Get the "from" phase */
	@Override
	public PlanPhase getFromPhase() {
		return from_phase;
	}

	/** "To" Phase */
	private PlanPhase to_phase;

	/** Set the "to" phase */
	@Override
	public void setToPhase(PlanPhase tp) {
		to_phase = tp;
	}

	/** Set the "to" phase */
	public void doSetToPhase(PlanPhase tp) throws TMSException {
		if (tp != to_phase) {
			store.update(this, "to_phase", tp);
			setToPhase(tp);
		}
	}

	/** Get the "to" phase */
	@Override
	public PlanPhase getToPhase() {
		return to_phase;
	}

	/** Action condition */
	private ActCondition condition;

	/** Set the action condition */
	@Override
	public void setCondition(int c) {
		condition = ActCondition.fromOrdinal(c);
	}

	/** Set the action condition */
	public void doSetCondition(int c) throws TMSException {
		if (c != condition.ordinal()) {
			store.update(this, "condition", c);
			setCondition(c);
		}
	}

	/** Get the action condition */
	@Override
	public int getCondition() {
		return condition.ordinal();
	}

	/** Condition parameters */
	private String params;

	/** Set the condition parameters */
	@Override
	public void setParams(String p) {
		params = p;
	}

	/** Set the condition parameters */
	public void doSetParams(String p) throws TMSException {
		if (!objectEquals(p, params)) {
			store.update(this, "params", p);
			setParams(p);
		}
	}

	/** Get the condition parameters */
	@Override
	public String getParams() {
		return params;
	}

	/** Log a message to the debug log */
	private void log(String msg) {
		if (DeviceActionJob.PLAN_LOG.isOpen()) {
			DeviceActionJob.PLAN_LOG.log("phase action: " +
				action_plan + ", " + msg);
		}
	}

	/** Perform action after checking condition */
	public void checkPerform(Calendar cal, int min) {
		if (isDayPlanHoliday(cal))
			return;
		if (action_plan instanceof ActionPlanImpl) {
			ActionPlanImpl ap = (ActionPlanImpl) action_plan;
			if (!ap.getActive())
				return;
			PlanPhase fp = getFromPhase();
			if (fp != null && fp != ap.getPhase())
				return;
			if (checkCondition(cal, min))
				ap.setPhaseNotify(getToPhase(), null);
		}
	}

	/** Test if a date is a holiday for the day plan */
	private boolean isDayPlanHoliday(Calendar cal) {
		DayPlan dp = day_plan;
		return dp != null && DayPlanHelper.isHoliday(dp, cal);
	}

	/** Check phase action condition */
	private boolean checkCondition(Calendar cal, int min) {
		switch (condition) {
			case HOLD_TIME:
				return checkHoldTime(cal);
			case CLOCK_TIME:
				return checkClockTime(cal, min);
			case TRAFFIC_THRESHOLD:
				return checkTrafficThreshold();
			case RWIS_THRESHOLD:
				return checkRwisThreshold();
			case TOLL_MODE:
				return checkTollMode();
			case ALERT_PERIOD:
				return checkAlertPeriod();
			case ALARM:
				return checkAlarm();
			default:
				return false;
		}
	}

	/** Check HOLD_TIME condition */
	private boolean checkHoldTime(Calendar cal) {
		Integer hs = PhaseActionHelper.getHoldSecs(this);
		if (action_plan instanceof ActionPlanImpl) {
			ActionPlanImpl ap = (ActionPlanImpl) action_plan;
			int ht = ap.phaseSecs(cal.getTimeInMillis());
			boolean trigger = (hs != null) && (ht >= hs);
			if (trigger)
				log("HOLD_TIME " + hs);
			return trigger;
		} else {
			log("HOLD_TIME invalid: " + params);
			return false;
		}
	}

	/** Check CLOCK_TIME condition */
	private boolean checkClockTime(Calendar cal, int min) {
		Calendar dt = PhaseActionHelper.getClockDate(this);
		if (dt != null) {
			if (dt.get(Calendar.YEAR) != cal.get(Calendar.YEAR) ||
			    dt.get(Calendar.MONTH) != cal.get(Calendar.MONTH) ||
			    dt.get(Calendar.DATE) != cal.get(Calendar.DATE))
				return false;
		}
		Integer mn = PhaseActionHelper.getClockTime(this);
		if (mn != null) {
			boolean trigger = (mn == min);
			if (trigger)
				log("CLOCK_TIME at " + min);
			return trigger;
		} else {
			log("CLOCK_TIME invalid: " + params);
			return false;
		}
	}

	/** Check TRAFFIC_THRESHOLD condition */
	private boolean checkTrafficThreshold() {
		TrafThreshold tt = PhaseActionHelper.getTrafficThreshold(this);
		if (tt != null) {
			boolean trigger = isTriggered(tt);
			if (trigger)
				log("TRAFFIC_THRESHOLD " + tt);
			return trigger;
		} else {
			log("TRAFFIC_THRESHOLD invalid: " + params);
			return false;
		}
	}

	/** Check RWIS_THRESHOLD condition */
	private boolean checkRwisThreshold() {
		RwisThreshold rt = PhaseActionHelper.getRwisThreshold(this);
		if (rt != null) {
			boolean trigger = isTriggered(rt);
			if (trigger)
				log("RWIS_THRESHOLD " + rt);
			return trigger;
		} else {
			log("RWIS_THRESHOLD invalid: " + params);
			return false;
		}
	}

	/** Check TOLL_MODE condition */
	private boolean checkTollMode() {
		TollMode tm = PhaseActionHelper.getTollMode(this);
		if (tm != null) {
			// FIXME: add mode to toll zone
			return false;
		} else {
			log("TOLL_MODE invalid: " + params);
			return false;
		}
	}

	/** Check ALERT_PERIOD condition */
	private boolean checkAlertPeriod() {
		AlertCondition ac = PhaseActionHelper.getAlertCondition(this);
		if (ac != null) {
			// FIXME
			return false;
		} else {
			log("ALERT_PERIOD invalid: " + params);
			return false;
		}
	}

	/** Check ALARM condition */
	private boolean checkAlarm() {
		AlarmCondition ac = PhaseActionHelper.getAlarmCondition(this);
		if (ac != null) {
			// FIXME
			return false;
		} else {
			log("ALARM invalid: " + params);
			return false;
		}
	}
}
