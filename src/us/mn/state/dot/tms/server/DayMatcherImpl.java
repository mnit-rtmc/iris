/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2003-2017  Minnesota Department of Transportation
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
import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.DayMatcher;
import us.mn.state.dot.tms.TMSException;

/**
 * A day matcher represents a day or days to be included or excluded from a
 * day plan schedule.
 *
 * @author Douglas Lau
 */
public class DayMatcherImpl extends BaseObjectImpl implements DayMatcher,
	Comparable<DayMatcherImpl>
{
	/** Load all the day matchers */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, DayMatcherImpl.class);
		store.query("SELECT name, holiday, month, day, week, " +
			"weekday, shift FROM iris." + SONAR_TYPE  + ";",
			new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new DayMatcherImpl(
					row.getString(1),	// name
					row.getBoolean(2),	// holiday
					row.getInt(3),		// month
					row.getInt(4),		// day
					row.getInt(5),		// week
					row.getInt(6),		// weekday
					row.getInt(7)		// shift
				));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("holiday", holiday);
		map.put("month", month);
		map.put("day", day);
		map.put("week", week);
		map.put("weekday", weekday);
		map.put("shift", shift);
		return map;
	}

	/** Get the database table name */
	@Override
	public String getTable() {
		return "iris." + SONAR_TYPE;
	}

	/** Get the SONAR type name */
	@Override
	public String getTypeName() {
		return SONAR_TYPE;
	}

	/** Create a new day matcher */
	public DayMatcherImpl(String n) {
		super(n);
	}

	/** Create a new day matcher */
	private DayMatcherImpl(String n, boolean h, int m, int d, int w, int wd,
		int s)
	{
		this(n);
		holiday = h;
		month = m;
		day = d;
		week = w;
		weekday = wd;
		shift = s;
	}

	/** Compare to another day matcher */
	@Override
	public int compareTo(DayMatcherImpl o) {
		return name.compareTo(o.name);
	}

	/** Test if the day matcher equals another day matcher */
	@Override
	public boolean equals(Object o) {
		if (o instanceof DayMatcherImpl)
			return name.equals(((DayMatcherImpl) o).name);
		else
			return false;
	}

	/** Check if a selection of properties is valid */
	private void checkSelection(int d, int w, int wd, int s)
		throws ChangeVetoException
	{
		/* Invalid matchers:
		 *   - Day of month + week
		 *   - Day of month + shift
		 *   - Shift + "any" week
		 *   - Shift + "any" weekday
		 */
		if (d != ANY_DAY && (w != ANY_WEEK || s != 0))
			throw new ChangeVetoException("Invalid selection");
		if (s != 0 && (w == ANY_WEEK || wd == ANY_WEEKDAY))
			throw new ChangeVetoException("Invalid selection");
	}

	/** Holiday flag */
	private boolean holiday = true;

	/** Set the holiday flag */
	@Override
	public void setHoliday(boolean h) {
		holiday = h;
	}

	/** Set the holiday flag */
	public void doSetHoliday(boolean h) throws TMSException {
		if (h != holiday) {
			store.update(this, "holiday", h);
			setHoliday(h);
		}
	}

	/** Get the holiday flag */
	@Override
	public boolean getHoliday() {
		return holiday;
	}

	/** Month of year */
	private int month = ANY_MONTH;

	/** Set the month */
	@Override
	public void setMonth(int m) {
		month = m;
	}

	/** Set the month */
	public void doSetMonth(int m) throws TMSException {
		if (m != month) {
			validateMonth(m);
			store.update(this, "month", m);
			setMonth(m);
		}
	}

	/** Validate the month */
	private void validateMonth(int m) throws ChangeVetoException {
		if (m != ANY_MONTH &&
		   (m < Calendar.JANUARY || m > Calendar.DECEMBER))
			throw new ChangeVetoException("Invalid month:" + m);
	}

	/** Get the month */
	@Override
	public int getMonth() {
		return month;
	}

	/** Day of month */
	private int day = ANY_DAY;

	/** Set the day-of-month */
	@Override
	public void setDay(int d) {
		day = d;
	}

	/** Set the day-of-month */
	public void doSetDay(int d) throws TMSException {
		if (d != day) {
			validateDay(d);
			store.update(this, "day", d);
			setDay(d);
		}
	}

	/** Validate the day-of-month */
	private void validateDay(int d) throws ChangeVetoException {
		if (d != ANY_DAY) {
			if (d < 1 || d > 31)
				throw new ChangeVetoException("Invalid day:"+d);
			checkSelection(d, week, weekday, shift);
		}
	}

	/** Get the day-of-month */
	@Override
	public int getDay() {
		return day;
	}

	/** Week of month */
	private int week = ANY_WEEK;

	/** Set the week-of-month */
	@Override
	public void setWeek(int w) {
		week = w;
	}

	/** Set the week-of-month */
	public void doSetWeek(int w) throws TMSException {
		if (w != week) {
			validateWeek(w);
			store.update(this, "week", w);
			setWeek(w);
		}
	}

	/** Validate the week-of-month */
	private void validateWeek(int w) throws ChangeVetoException {
		if (w != ANY_WEEK) {
			// -1 is last week in month
			if (w < -1 || w > 4)
			       throw new ChangeVetoException("Invalid week:"+w);
			checkSelection(day, w, weekday, shift);
		}
	}

	/** Get the week-of-month */
	@Override
	public int getWeek() {
		return week;
	}

	/** Day of week */
	private int weekday = ANY_WEEKDAY;

	/** Set the day-of-week */
	@Override
	public void setWeekday(int wd) {
		weekday = wd;
	}

	/** Set the day-of-week */
	public void doSetWeekday(int wd) throws TMSException {
		if (wd != weekday) {
			validateWeekday(wd);
			store.update(this, "weekday", wd);
			setWeekday(wd);
		}
	}

	/** Validate the day-of-week */
	private void validateWeekday(int wd) throws ChangeVetoException {
		if (wd != ANY_WEEKDAY) {
			if (wd < Calendar.SUNDAY || wd > Calendar.SATURDAY) {
				throw new ChangeVetoException(
					"Invalid weekday:" + wd);
			}
			checkSelection(day, week, wd, shift);
		}
	}

	/** Get the day-of-week */
	@Override
	public int getWeekday() {
		return weekday;
	}

	/** Shift (in days) from actual match (for Shopping day, etc) */
	private int shift = 0;

	/** Set the shift from the actual day */
	@Override
	public void setShift(int s) {
		shift = s;
	}

	/** Set the shift from the actual day */
	public void doSetShift(int s) throws TMSException {
		if (s != shift) {
			validateShift(s);
			store.update(this, "shift", s);
			setShift(s);
		}
	}

	/** Validate the shift days */
	private void validateShift(int s) throws ChangeVetoException {
		if (s < -2 || s > 2)
			throw new ChangeVetoException("Invalid shift:" + s);
		if (s != 0)
			checkSelection(day, week, weekday, s);
	}

	/** Get the shift from the actual day */
	@Override
	public int getShift() {
		return shift;
	}
}
