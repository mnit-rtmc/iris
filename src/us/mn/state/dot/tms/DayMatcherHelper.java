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

import java.util.Calendar;
import java.util.Iterator;

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

	/** Get an iterator */
	static public Iterator<DayMatcher> iterator() {
		return new IteratorWrapper<DayMatcher>(
			namespace.iterator(DayMatcher.SONAR_TYPE));
	}

	/** Lookup the day matcher with the specified name */
	static public DayMatcher lookup(String name) {
		return (DayMatcher) namespace.lookupObject(
			DayMatcher.SONAR_TYPE, name);
	}

	/** Check if the given stamp matches */
	static public boolean matches(DayPlan plan, Calendar stamp) {
		Iterator<DayMatcher> it = iterator();
		while (it.hasNext()) {
			DayMatcher dm = it.next();
			if (dm.getDayPlan() == plan &&
			    matches(dm, stamp))
				return true;
		}
		return false;
	}

	/** Check if the given stamp matches */
	static private boolean matches(DayMatcher dm, Calendar stamp) {
		Integer month = dm.getMonth();
		if (month != null && month != 1 + stamp.get(Calendar.MONTH))
			return false;
		Integer day = dm.getDay();
		if (day != null && day != stamp.get(Calendar.DAY_OF_MONTH))
			return false;
		Calendar st = (Calendar) stamp.clone();
		Integer weekday = dm.getWeekday();
		if (weekday != null)
			st.set(Calendar.DAY_OF_WEEK, weekday);
		Integer week = dm.getWeek();
		if (week != null) {
			// NOTE: setting this to -1 means "last in month"
			st.set(Calendar.DAY_OF_WEEK_IN_MONTH, week);
		}
		Integer shift = dm.getShift();
		if (shift != null)
			st.add(Calendar.DAY_OF_MONTH, shift);
		return st.equals(stamp);
	}
}
