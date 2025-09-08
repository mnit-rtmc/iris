/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2024  Minnesota Department of Transportation
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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.tms.ActionPlan;
import us.mn.state.dot.tms.DayPlan;
import us.mn.state.dot.tms.DayPlanHelper;
import us.mn.state.dot.tms.PlanPhase;
import us.mn.state.dot.tms.TimeAction;
import us.mn.state.dot.tms.TimeActionHelper;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.utils.UniqueNameCreator;

/**
 * Action for triggering an action plan to be deployed or undeployed.
 *
 * @author Douglas Lau
 */
public class TimeActionImpl extends BaseObjectImpl implements TimeAction {

	/** Create a unique TimeAction record name */
	static public String createUniqueName(String template) {
		UniqueNameCreator unc = new UniqueNameCreator(template, 30,
			(n)->lookupTimeAction(n));
		return unc.createUniqueName();
	}

	/** Load all the time actions */
	static protected void loadAll() throws TMSException {
		store.query("SELECT name, action_plan, day_plan, sched_date, " +
			"time_of_day, phase FROM iris." + SONAR_TYPE  +";",
			new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new TimeActionImpl(
					row.getString(1), // name
					row.getString(2), // action_plan
					row.getString(3), // day_plan
					row.getDate(4),   // sched_date
					row.getTime(5),   // time_of_day
					row.getString(6)  // phase
				));
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
		map.put("sched_date", sched_date);
		map.put("time_of_day", time_of_day);
		map.put("phase", phase);
		return map;
	}

	/** Create a new time action */
	public TimeActionImpl(String n) {
		super(n);
	}

	/** Create a new time action */
	protected TimeActionImpl(String n, String a, String d, Date sd,
		Date tod, String p)
	{
		this(n);
		action_plan = lookupActionPlan(a);
		day_plan = lookupDayPlan(d);
		sched_date = TimeActionHelper.formatDate(sd);
		time_of_day = TimeActionHelper.formatTime(tod);
		phase = lookupPlanPhase(p);
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

	/** Get the day plan */
	@Override
	public DayPlan getDayPlan() {
		return day_plan;
	}

	/** Scheduled date */
	private String sched_date;

	/** Get the scheduled date */
	@Override
	public String getSchedDate() {
		return sched_date;
	}

	/** Time-of-day */
	private String time_of_day;

	/** Get the time-of-day */
	@Override
	public String getTimeOfDay() {
		return time_of_day;
	}

	/** Phase to trigger */
	private PlanPhase phase;

	/** Set the phase to trigger */
	@Override
	public void setPhase(PlanPhase p) {
		phase = p;
	}

	/** Set the phase to trigger */
	public void doSetPhase(PlanPhase p) throws TMSException {
		if (p != phase) {
			store.update(this, "phase", p);
			setPhase(p);
		}
	}

	/** Get the phase to trigger */
	@Override
	public PlanPhase getPhase() {
		return phase;
	}

	/** Perform action if date and time is right */
	public void perform(Calendar cal, int min) {
		if (isDayValid(cal) &&
		    TimeActionHelper.getMinuteOfDay(this) == min)
			perform();
	}

	/** Test if the time action is scheduled for the specified date */
	private boolean isDayValid(Calendar cal) {
		return isDayPlanValid(cal) || isDateScheduleValid(cal);
	}

	/** Test if the day plan is valid for the specified date */
	private boolean isDayPlanValid(Calendar cal) {
		return day_plan != null &&
		       !DayPlanHelper.isHoliday(day_plan, cal);
	}

	/** Test if the date schedule is valid for the specified date */
	private boolean isDateScheduleValid(Calendar cal) {
		if (sched_date == null)
			return false;
		Calendar sd = Calendar.getInstance();
		sd.setTime(TimeActionHelper.parseDate(sched_date));
		return sd.get(Calendar.YEAR) == cal.get(Calendar.YEAR) &&
		       sd.get(Calendar.MONTH) == cal.get(Calendar.MONTH) &&
		       sd.get(Calendar.DATE) == cal.get(Calendar.DATE);
	}

	/** Perform the time action */
	protected void perform() {
		ActionPlan ap = action_plan;	// Avoid race
		if (ap instanceof ActionPlanImpl) {
			ActionPlanImpl api = (ActionPlanImpl) ap;
			if (api.getActive())
				api.setPhaseNotify(getPhase(), null);
		}
	}
}
