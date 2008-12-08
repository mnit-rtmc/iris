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
 * DMS message priority levels. This enum is designed so that the ordinal
 * values can be used for NTCIP activation and run-time priority. NTCIP
 * priority values can range from 1 to 255, with higher numbers indicating
 * higher priority. The enum is also ordered from low to high priority.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public enum DMSMessagePriority {
	INVALID,	/* 0: invalid priority */
	BLANK,		/* 1: blank message run-time priority */
	TRAVEL_TIME,	/* 2: travel time priority */
	SCHEDULED,	/* 3: scheduled priority */
	ALERT,		/* 4: alert priority (AMBER alerts, etc.) */
	OPERATOR,	/* 5: operator (override activation) priority */
	AWS,		/* 6: automated warning system */
	CLEAR		/* 7: operator clear activation priority */
}
