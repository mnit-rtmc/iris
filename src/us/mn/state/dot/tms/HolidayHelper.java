/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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
import us.mn.state.dot.sonar.Checker;

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

	/** Find holiday using a Checker */
	static public Holiday find(final Checker<Holiday> checker) {
		return (Holiday)namespace.findObject(Holiday.SONAR_TYPE, 
			checker);
	}

	/** Check if the given date/time matches any holiday */
	static public boolean isHoliday(final Calendar stamp) {
		return null != find(new Checker<Holiday>() {
			public boolean check(Holiday h) {
				return matches(h, stamp);
			}
		});
	}

	/** Check if the holiday matches the given time */
	static public boolean matches(Holiday h, Calendar stamp) {
		int month = h.getMonth();
		int day = h.getDay();
		int week = h.getWeek();
		int weekday = h.getWeekday();
		int shift = h.getShift();
		int period = h.getPeriod();
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
		if(period != Holiday.ANY_PERIOD &&
		   period != stamp.get(Calendar.AM_PM))
			return false;
		return true;
	}
}
