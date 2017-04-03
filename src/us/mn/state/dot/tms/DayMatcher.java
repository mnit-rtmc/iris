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
package us.mn.state.dot.tms;

import us.mn.state.dot.sonar.SonarObject;

/**
 * A day matcher represents a day or days to be included or excluded from a
 * day plan schedule.
 * FIXME: should be immutable after creation.
 *
 * @author Douglas Lau
 */
public interface DayMatcher extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "day_matcher";

	/** Set the holiday flag */
	void setHoliday(boolean h);

	/** Get the holiday flag */
	boolean getHoliday();

	/** Constant for days not matched by month */
	int ANY_MONTH = -1;

	/** Set the month */
	void setMonth(int m);

	/** Get the month */
	int getMonth();

	/** Constant for days not matched by day-of-month */
	int ANY_DAY = 0;

	/** Set the day-of-month */
	void setDay(int d);

	/** Get the day-of-month */
	int getDay();

	/** Constant for days not matched by week-of-month */
	int ANY_WEEK = 0;

	/** Set the week-of-month (1-4; 0 for any; -1 for last) */
	void setWeek(int w);

	/** Get the week-of-month */
	int getWeek();

	/** Constant for days not matched by day-of-week */
	int ANY_WEEKDAY = 0;

	/** Set the day-of-week */
	void setWeekday(int d);

	/** Get the day-of-week */
	int getWeekday();

	/** Set the shift from the actual day */
	void setShift(int s);

	/** Get the shift from the actual day */
	int getShift();
}
