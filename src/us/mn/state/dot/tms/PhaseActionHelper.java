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
package us.mn.state.dot.tms;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Helper class for phase actions.
 *
 * @author Douglas Lau
 */
public class PhaseActionHelper extends BaseHelper {

	/** Don't allow instances to be created */
	private PhaseActionHelper() {
		assert false;
	}

	/** Clock time (no date) condition format */
	static private final DateFormat CLOCK_FORMAT =
		new SimpleDateFormat("HH:mm");

	/** Clock time (with date) condition format */
	static private final DateFormat CLOCK_DATE_FORMAT =
		new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");

	/** Lookup the phase action with the specified name */
	static public PhaseAction lookup(String name) {
		return (PhaseAction) namespace.lookupObject(
			PhaseAction.SONAR_TYPE, name);
	}

	/** Get a phase action iterator */
	static public Iterator<PhaseAction> iterator() {
		return new IteratorWrapper<PhaseAction>(namespace.iterator(
			PhaseAction.SONAR_TYPE));
	}

	/** Get phase action parameters */
	static private String getParams(PhaseAction pa) {
		String p = pa.getParams();
		return (p != null) ? p.replaceAll(" ", "") : null;
	}

	/** Get HOLD_TIME seconds */
	static public Integer getHoldSecs(PhaseAction pa) {
		if (pa.getCondition() == ActCondition.HOLD_TIME.ordinal()) {
			String p = getParams(pa);
			if (p != null)
				return parseHoldTime(p);
		}
		return null;
	}

	/** Parse a hold time param */
	static private Integer parseHoldTime(String p) {
		String[] v = p.split(":", 3);
		try {
			int hr = 0;
			int mn = 0;
			int sc = 0;
			switch (v.length) {
			case 1:
				sc = Integer.parseUnsignedInt(v[0]);
				break;
			case 2:
				mn = Integer.parseUnsignedInt(v[0]);
				sc = Integer.parseUnsignedInt(v[1]);
				break;
			case 3:
				hr = Integer.parseUnsignedInt(v[0]);
				mn = Integer.parseUnsignedInt(v[1]);
				sc = Integer.parseUnsignedInt(v[2]);
				break;
			default:
				return null;
			}
			return (hr * 3600) + (mn * 60) + sc;
		}
		catch (NumberFormatException e) {
			return null;
		}
	}

	/** Minute of 12 Noon in day */
	static public final int NOON = 12 * 60;

	/** Get the peak period for a minute-of-day */
	static public int getPeriod(int min) {
		return (min < NOON) ? Calendar.AM : Calendar.PM;
	}

	/** Get CLOCK_TIME minute-of-day (0-1440) */
	static public Integer getClockTime(PhaseAction pa) {
		if (pa.getCondition() == ActCondition.CLOCK_TIME.ordinal()) {
			String p = getParams(pa);
			if (p != null) {
				Date d = parseClockTime(p);
				if (null == d)
					d = parseClockDateTime(p);
				return (d != null) ? getMinuteOfDay(d) : null;
			}
		}
		return null;
	}

	/** Get the minute-of-day (0-1440) */
	static private int getMinuteOfDay(Date d) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(d);
		return cal.get(Calendar.HOUR_OF_DAY) * 60 +
		       cal.get(Calendar.MINUTE);
	}

	/** Get CLOCK_TIME calendar date */
	static public Calendar getClockDate(PhaseAction pa) {
		if (pa.getCondition() == ActCondition.CLOCK_TIME.ordinal()) {
			String p = getParams(pa);
			if (p != null) {
				Date d = parseClockDateTime(p);
				if (d != null) {
					Calendar cal = Calendar.getInstance();
					cal.setTime(d);
					return cal;
				}
			}
		}
		return null;
	}

	/** Parse a clock time param */
	static private Date parseClockTime(String p) {
		try {
			return CLOCK_FORMAT.parse(p);
		}
		catch (ParseException e) {
			return null;
		}
	}

	/** Parse a clock date / time param */
	static private Date parseClockDateTime(String p) {
		try {
			return CLOCK_DATE_FORMAT.parse(p);
		}
		catch (ParseException e) {
			return null;
		}
	}

	/** Get TRAFFIC_THRESHOLD condition */
	static public TrafThreshold getTrafficThreshold(PhaseAction pa) {
		if (pa.getCondition() == ActCondition.TRAFFIC_THRESHOLD
			.ordinal())
		{
			String p = getParams(pa);
			if (p != null)
				return TrafThreshold.parse(p);
		}
		return null;
	}

	/** Get RWIS_THRESHOLD condition */
	static public RwisThreshold getRwisThreshold(PhaseAction pa) {
		if (pa.getCondition() == ActCondition.RWIS_THRESHOLD
			.ordinal())
		{
			String p = getParams(pa);
			if (p != null)
				return RwisThreshold.parse(p);
		}
		return null;
	}

	/** Get ALARM condition */
	static public AlarmCondition getAlarmCondition(PhaseAction pa) {
		if (pa.getCondition() == ActCondition.ALARM.ordinal()) {
			String p = getParams(pa);
			if (p != null)
				return AlarmCondition.parse(p);
		}
		return null;
	}

	/** Interface to filter dates */
	static private interface DateFilter {
		boolean check(Date date, PhaseAction pa);
	}

	/** Get most recent action before now from an action plan */
	static public PhaseAction getMostRecentAction(ActionPlan plan, Date now)
	{
		final Date[] best = new Date[1];
		final PhaseAction[] act = new PhaseAction[1];
		filterSchedule(plan, new DateFilter() {
			@Override
			public boolean check(Date dt, PhaseAction pa) {
				// Most recent time before now
				boolean res = dt.before(now) &&
				    (best[0] == null || dt.after(best[0]));
				if (res) {
					best[0] = dt;
					act[0] = pa;
				}
				return res;
			}
		});
		return act[0];
	}

	/** Get most recent scheduled date from an action plan */
	static public Date getMostRecent(ActionPlan plan, Date now) {
		final Date[] best = new Date[1];
		filterSchedule(plan, new DateFilter() {
			@Override
			public boolean check(Date dt, PhaseAction pa) {
				// Most recent time before now
				boolean res = dt.before(now) &&
				    (best[0] == null || dt.after(best[0]));
				if (res)
					best[0] = dt;
				return res;
			}
		});
		return best[0];
	}

	/** Get soonest scheduled date from an action plan */
	static public Date getSoonest(ActionPlan plan, Date now) {
		final Date[] best = new Date[1];
		filterSchedule(plan, new DateFilter() {
			@Override
			public boolean check(Date dt, PhaseAction pa) {
				// Soonest time after now
				boolean res = dt.after(now) &&
				    (best[0] == null || dt.before(best[0]));
				if (res)
					best[0] = dt;
				return res;
			}
		});
		return best[0];
	}

	/** Filter scheduled times in an action plan */
	static private void filterSchedule(ActionPlan plan, DateFilter filter){
		Iterator<PhaseAction> it = iterator();
		while (it.hasNext()) {
			PhaseAction pa = it.next();
			if (pa.getActionPlan() == plan)
				filterSchedule(pa, filter);
		}
	}

	/** Filter scheduled phase action */
	static private void filterSchedule(PhaseAction pa, DateFilter filter) {
		Integer mod = getClockTime(pa);
		if (mod != null) {
			DayPlan dp = pa.getDayPlan();
			Calendar cd = getClockDate(pa);
			if (cd != null) {
				// Check only the specified clock date
				Date sched = cd.getTime();
				sched = getScheduledDate(sched, mod);
				if (checkDayPlan(dp, sched))
					filter.check(sched, pa);
			} else {
				// Check all dates in day plan
				filterSchedule(pa, dp, mod, filter);
			}
		}
	}

	/** Get scheduled date and time */
	static private Date getScheduledDate(Date doy, int mod) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(doy);
		cal.set(Calendar.HOUR_OF_DAY, mod % 60);
		cal.set(Calendar.MINUTE, mod / 60);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	/** Check if a date is valid for a day plan */
	static private boolean checkDayPlan(DayPlan dp, Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return (dp == null) || !DayPlanHelper.isHoliday(dp, cal);
	}

	/** Filter scheduled day plan / minute-of-day */
	static private void filterSchedule(PhaseAction pa, DayPlan dp, int mod,
		DateFilter filter)
	{
		Calendar fut = Calendar.getInstance();
		Calendar pst = Calendar.getInstance();
		fut.setTime(getScheduledDate(fut.getTime(), mod));
		pst.setTime(fut.getTime());
		if (checkDayPlan(dp, fut.getTime()))
			filter.check(fut.getTime(), pa);
		// Check a week in both directions
		for (int i = 0; i < 7; i++) {
			// Another day in the future
			fut.add(Calendar.DATE, 1);
			if (checkDayPlan(dp, fut.getTime()))
				filter.check(fut.getTime(), pa);
			// Another day in the past
			pst.add(Calendar.DATE, -1);
			if (checkDayPlan(dp, pst.getTime()))
				filter.check(pst.getTime(), pa);
		}
	}

	/** Find all phase actions from a list of device actions */
	static public ArrayList<PhaseAction> find(
		ArrayList<DeviceAction> dev_actions)
	{
		HashSet<ActionPlan> plans = new HashSet<ActionPlan>();
		for (DeviceAction da : dev_actions) {
			plans.add(da.getActionPlan());
		}
		ArrayList<PhaseAction> actions = new ArrayList<PhaseAction>();
		Iterator<PhaseAction> it = iterator();
		while (it.hasNext()) {
			PhaseAction pa = it.next();
			if (plans.contains(pa.getActionPlan()))
				actions.add(pa);
		}
		return actions;
	}
}
