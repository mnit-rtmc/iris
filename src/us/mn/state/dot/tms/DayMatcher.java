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
package us.mn.state.dot.tms;

import us.mn.state.dot.sonar.SonarObject;

/**
 * A day matcher represents a day or days to be included or excluded from a
 * day plan schedule.
 *
 * @author Douglas Lau
 */
public interface DayMatcher extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "day_matcher";

	/** Get the SONAR type name */
	@Override
	default String getTypeName() {
		return SONAR_TYPE;
	}

	/** SONAR base type name */
	String SONAR_BASE = ActionPlan.SONAR_TYPE;

	/** Get the day plan */
	DayPlan getDayPlan();

	/** Get the month (1-12) */
	Integer getMonth();

	/** Get the day-of-month (1-31) */
	Integer getDay();

	/** Get the day-of-week (1-7) */
	Integer getWeekday();

	/** Get the week-of-month (1-4 or -1 for last) */
	Integer getWeek();

	/** Get the shift from the actual day */
	Integer getShift();
}
