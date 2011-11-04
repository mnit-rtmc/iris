/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2003-2011  Minnesota Department of Transportation
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
 * A holiday is a date which ramp meters (and travel times) are not deployed.
 *
 * @author Douglas Lau
 */
public interface Holiday extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "holiday";

	/** Constant for holidays not determined by month */
	int ANY_MONTH = -1;

	/** Set the month */
	void setMonth(int m);

	/** Get the month */
	int getMonth();

	/** Constant for holidays not determined by day-of-month */
	int ANY_DAY = 0;

	/** Set the day-of-month */
	void setDay(int d);

	/** Get the day-of-month */
	int getDay();

	/** Constant for holidays not determined by week-of-month */
	int ANY_WEEK = 0;

	/** Set the week-of-month */
	void setWeek(int w);

	/** Get the week-of-month */
	int getWeek();

	/** Constant for holidays not determined by day-of-week */
	int ANY_WEEKDAY = 0;

	/** Set the day-of-week */
	void setWeekday(int d);

	/** Get the day-of-week */
	int getWeekday();

	/** Set the shift from the actual holiday */
	void setShift(int s);

	/** Get the shift from the actual holiday */
	int getShift();

	/** Constant for holidays not determined by period */
	int ANY_PERIOD = -1;
}
