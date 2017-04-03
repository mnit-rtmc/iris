/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2017  Minnesota Department of Transportation
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

import java.util.Calendar;

/**
 * Helper class for day matchers.
 *
 * @author Douglas Lau
 */
public class DayMatcherHelper extends BaseHelper {

	/** Don't allow instances to be created */
	private DayMatcherHelper() {
		assert false;
	}

	/** Lookup the day matcher with the specified name */
	static public DayMatcher lookup(String name) {
		return (DayMatcher) namespace.lookupObject(
			DayMatcher.SONAR_TYPE, name);
	}

	/** Check if the given time matches */
	static public boolean matches(DayMatcher dm, Calendar stamp) {
		int month = dm.getMonth();
		int day = dm.getDay();
		int week = dm.getWeek();
		int weekday = dm.getWeekday();
		int shift = dm.getShift();
		if (month != DayMatcher.ANY_MONTH &&
		    month != stamp.get(Calendar.MONTH))
			return false;
		if (day != DayMatcher.ANY_DAY &&
		    day != stamp.get(Calendar.DAY_OF_MONTH))
			return false;
		if (week != DayMatcher.ANY_WEEK) {
			Calendar t = (Calendar) stamp.clone();
			t.set(Calendar.DAY_OF_WEEK, weekday);
			t.set(Calendar.DAY_OF_WEEK_IN_MONTH, week);
			t.add(Calendar.DAY_OF_MONTH, shift);
			if (!t.equals(stamp))
				return false;
		} else if (weekday != DayMatcher.ANY_WEEKDAY) {
			if (weekday != stamp.get(Calendar.DAY_OF_WEEK))
				return false;
		}
		return true;
	}
}
