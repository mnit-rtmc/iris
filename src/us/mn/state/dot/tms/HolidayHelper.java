/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2012  Minnesota Department of Transportation
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
 * Helper class for holidays.
 *
 * @author Douglas Lau
 */
public class HolidayHelper extends BaseHelper {

	/** Don't allow instances to be created */
	private HolidayHelper() {
		assert false;
	}

	/** Lookup the holiday with the specified name */
	static public Holiday lookup(String name) {
		return (Holiday)namespace.lookupObject(Holiday.SONAR_TYPE,name);
	}

	/** Check if the holiday matches the given time */
	static public boolean matches(Holiday h, Calendar stamp) {
		int month = h.getMonth();
		int day = h.getDay();
		int week = h.getWeek();
		int weekday = h.getWeekday();
		int shift = h.getShift();
		if(month != Holiday.ANY_MONTH &&
		   month != stamp.get(Calendar.MONTH))
			return false;
		if(day != Holiday.ANY_DAY &&
		   day != stamp.get(Calendar.DAY_OF_MONTH))
			return false;
		if(week != Holiday.ANY_WEEK) {
			Calendar t = (Calendar)stamp.clone();
			t.set(Calendar.DAY_OF_WEEK, weekday);
			t.set(Calendar.DAY_OF_WEEK_IN_MONTH, week);
			t.add(Calendar.DAY_OF_MONTH, shift);
			if(!t.equals(stamp))
				return false;
		} else if(weekday != Holiday.ANY_WEEKDAY) {
			if(weekday != stamp.get(Calendar.DAY_OF_WEEK))
				return false;
		}
		return true;
	}
}
