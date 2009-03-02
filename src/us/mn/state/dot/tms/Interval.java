/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008  Minnesota Department of Transportation
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

/**
 * Time interval values.  All values are in seconds.
 *
 * @author Douglas Lau
 */
public interface Interval {

	/** Number of seconds in a minute */
	int MINUTE = 60;

	/** Number of seconds in an hour */
	int HOUR = 60 * MINUTE;

	/** Number of seconds in a day */
	int DAY = 24 * HOUR;
}
