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
package us.mn.state.dot.tms;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Helper class for time actions.
 *
 * @author Douglas Lau
 */
public class TimeActionHelper extends BaseHelper {

	/** Don't allow instances to be created */
	private TimeActionHelper() {
		assert false;
	}

	/** Lookup the time action with the specified name */
	static public TimeAction lookup(String name) {
		return (TimeAction)namespace.lookupObject(TimeAction.SONAR_TYPE,
			name);
	}

	/** Get a time action iterator */
	static public Iterator<TimeAction> iterator() {
		return new IteratorWrapper<TimeAction>(namespace.iterator(
			TimeAction.SONAR_TYPE));
	}

	/** Get the minute-of-day (0-1440) */
	static public Integer getMinuteOfDay(TimeAction ta) {
		Date d = parseTime(ta.getTimeOfDay());
		return (d != null) ? getMinuteOfDay(d) : null;
	}

	/** Get the minute-of-day (0-1440) */
	static private int getMinuteOfDay(Date d) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(d);
		return cal.get(Calendar.HOUR_OF_DAY) * 60 +
		       cal.get(Calendar.MINUTE);
	}

	/** Minute of 12 Noon in day */
	static public final int NOON = 12 * 60;

	/** Get the peak period for a minute-of-day */
	static public int getPeriod(int min) {
		return (min < NOON) ? Calendar.AM : Calendar.PM;
	}

	/** Date parser formats */
	static private final DateFormat[] DATE_FORMATS = {
		new SimpleDateFormat("MM/dd/yy"),
		new SimpleDateFormat("MM/dd/yyyy"),
		new SimpleDateFormat("yyyy-MM-dd"),
	};

	/** Time parser formats */
	static private final DateFormat[] TIME_FORMATS = {
		new SimpleDateFormat("h:mm a"),
		new SimpleDateFormat("H:mm"),
		new SimpleDateFormat("h a"),
		new SimpleDateFormat("H")
	};

	/** Parse a date string */
	static public Date parseDate(String t) {
		for (DateFormat df: DATE_FORMATS) {
			try {
				return df.parse(t);
			}
			catch (ParseException e) {
				// Ignore
			}
		}
		return null;
	}

	/** Convert date to string */
	static public String formatDate(Date d) {
		if(d != null) {
			SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
			return f.format(d);
		} else
			return null;
	}

	/** Parse a time string in one of the supported formats.
	 * @param tod Time string.
	 * @return Date object, or null if time could not be parsed. */
	static public Date parseTime(String tod) {
		for (DateFormat df: TIME_FORMATS) {
			try {
				return df.parse(tod);
			}
			catch (ParseException e) { /* ignore */ }
			catch (NumberFormatException e) { /* ignore */ }
		}
		return null;
	}

	/** Convert date to time string */
	static public String formatTime(Date tod) {
		if (tod != null) {
			SimpleDateFormat f = new SimpleDateFormat("H:mm");
			return f.format(tod);
		} else
			return null;
	}

	/** Interface to filter dates */
	static private interface DateFilter {
		boolean check(Date date, TimeAction ta);
	}

	/** Get most recent action before now from an action plan */
	static public TimeAction getMostRecentAction(ActionPlan plan, Date now){
		final Date[] best = new Date[1];
		final TimeAction[] act = new TimeAction[1];
		filterSchedule(plan, new DateFilter() {
			@Override
			public boolean check(Date dt, TimeAction ta) {
				// Most recent time before now
				boolean res = dt.before(now) &&
				    (best[0] == null || dt.after(best[0]));
				if (res) {
					best[0] = dt;
					act[0] = ta;
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
			public boolean check(Date dt, TimeAction ta) {
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
			public boolean check(Date dt, TimeAction ta) {
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
		Iterator<TimeAction> it = iterator();
		while (it.hasNext()) {
			TimeAction ta = it.next();
			if (ta.getActionPlan() == plan)
				filterSchedule(ta, filter);
		}
	}

	/** Filter scheduled time action */
	static private void filterSchedule(TimeAction ta, DateFilter filter) {
		Date tod = parseTime(ta.getTimeOfDay());
		if (tod != null) {
			String sched = ta.getSchedDate();
			if (sched != null)
				filterSchedule(ta, sched, tod, filter);
			else {
				DayPlan dp = ta.getDayPlan();
				if (dp != null)
					filterSchedule(ta, dp, tod, filter);
			}
		}
	}

	/** Filter scheduled day of year / time of day */
	static private void filterSchedule(TimeAction ta, String sched,
		Date tod, DateFilter filter)
	{
		Date doy = parseDate(sched);
		if (doy != null)
			filter.check(getScheduledDate(doy, tod), ta);
	}

	/** Get scheduled date and time */
	static private Date getScheduledDate(Date doy, Date tod) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(doy);
		Calendar todc = Calendar.getInstance();
		todc.setTime(tod);
		cal.set(Calendar.HOUR_OF_DAY, todc.get(Calendar.HOUR_OF_DAY));
		cal.set(Calendar.MINUTE, todc.get(Calendar.MINUTE));
		cal.set(Calendar.SECOND, todc.get(Calendar.SECOND));
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	/** Filter scheduled day plan / time of day */
	static private void filterSchedule(TimeAction ta, DayPlan dp, Date tod,
		DateFilter filter)
	{
		Calendar fut = Calendar.getInstance();
		fut.setTime(tod);
		if (!DayPlanHelper.isHoliday(dp, fut))
			filter.check(fut.getTime(), ta);
		Calendar pst = Calendar.getInstance();
		pst.setTime(tod);
		// Check a week in both directions
		for (int i = 0; i < 7; i++) {
			// Another day in the future
			fut.add(Calendar.DATE, 1);
			if (!DayPlanHelper.isHoliday(dp, fut))
				filter.check(fut.getTime(), ta);
			// Another day in the past
			pst.add(Calendar.DATE, -1);
			if (!DayPlanHelper.isHoliday(dp, pst))
				filter.check(pst.getTime(), ta);
		}
	}

	/** Find all time actions from a list of device actions */
	static public ArrayList<TimeAction> find(
		ArrayList<DeviceAction> actions)
	{
		HashSet<ActionPlan> plans = new HashSet<ActionPlan>();
		for (DeviceAction da : actions) {
			plans.add(da.getActionPlan());
		}
		ArrayList<TimeAction> times = new ArrayList<TimeAction>();
		Iterator<TimeAction> it = iterator();
		while (it.hasNext()) {
			TimeAction ta = it.next();
			if (plans.contains(ta.getActionPlan()))
				times.add(ta);
		}
		return times;
	}
}
