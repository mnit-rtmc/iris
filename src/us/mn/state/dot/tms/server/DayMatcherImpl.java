/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2003-2024  Minnesota Department of Transportation
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
import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.tms.DayMatcher;
import us.mn.state.dot.tms.DayPlan;
import us.mn.state.dot.tms.TMSException;

/**
 * A day matcher represents a day or days to be included or excluded from a
 * day plan schedule.
 *
 * @author Douglas Lau
 */
public class DayMatcherImpl extends BaseObjectImpl implements DayMatcher {

	/** Load all the day matchers */
	static protected void loadAll() throws TMSException {
		store.query("SELECT name, day_plan, month, day, weekday, " +
			"week, shift FROM iris." + SONAR_TYPE + ";",
			new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new DayMatcherImpl(
					row.getString(1),           // name
					row.getString(2),           // day_plan
					(Integer) row.getObject(3), // month
					(Integer) row.getObject(4), // day
					(Integer) row.getObject(5), // weekday
					(Integer) row.getObject(6), // week
					(Integer) row.getObject(7)  // shift
				));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("day_plan", day_plan);
		map.put("month", month);
		map.put("day", day);
		map.put("weekday", weekday);
		map.put("week", week);
		map.put("shift", shift);
		return map;
	}

	/** Create a new day matcher */
	public DayMatcherImpl(String n) {
		super(n);
	}

	/** Create a new day matcher */
	private DayMatcherImpl(String n, String pl, Integer m, Integer d,
		Integer wd, Integer w, Integer s)
	{
		this(n);
		day_plan = lookupDayPlan(pl);
		month = m;
		day = d;
		weekday = wd;
		week = w;
		shift = s;
	}

	/** Day plan */
	private DayPlan day_plan;

	/** Get the day plan */
	@Override
	public DayPlan getDayPlan() {
		return day_plan;
	}

	/** Month of year (1-12) */
	private Integer month;

	/** Get the month (1-12) */
	@Override
	public Integer getMonth() {
		return month;
	}

	/** Day of month (1-31) */
	private Integer day;

	/** Get the day-of-month (1-31) */
	@Override
	public Integer getDay() {
		return day;
	}

	/** Day of week (1-7) */
	private Integer weekday;

	/** Get the day-of-week (1-7) */
	@Override
	public Integer getWeekday() {
		return weekday;
	}

	/** Week of month */
	private Integer week;

	/** Get the week-of-month (1-4, or -1 for last) */
	@Override
	public Integer getWeek() {
		return week;
	}

	/** Shift (in days) from actual match (for Black Friday, etc) */
	private Integer shift;

	/** Get the shift from the actual day */
	@Override
	public Integer getShift() {
		return shift;
	}
}
