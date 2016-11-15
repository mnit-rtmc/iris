/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2016  Minnesota Department of Transportation
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
 * DMS message priority levels.  This enum is designed so that the ordinal
 * values can be used for NTCIP activation and run-time priority.  NTCIP
 * priority values can range from 1 to 255, with higher numbers indicating
 * higher priority.  The enum is also ordered from low to high priority.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public enum DmsMsgPriority {
	INVALID,	/* 0: invalid priority */
	BLANK,		/* 1: blank message run-time priority */
	PREFIX_PAGE,	/* 2: prefix page combining (activation only) */
	PSA,		/* 3: public service announcement */
	TRAVEL_TIME,	/* 4: travel time priority */
	SPEED_LIMIT,	/* 5: variable speed limit priority */
	SCHEDULED,	/* 6: scheduled priority (planned events) */
	OTHER_SYSTEM,	/* 7: other system priority */
	ALERT,		/* 8: alert priority (AMBER alerts, etc.) */
	OPERATOR,	/* 9: operator priority */
	INCIDENT_LOW,	/* 10: low-priority incident */
	INCIDENT_MED,	/* 11: medium-priority incident */
	INCIDENT_HIGH,	/* 12: high-priority incident */
	AWS,		/* 13: automated warning system */
	OVERRIDE;	/* 14: override priority */

	/** Values array */
	static private final DmsMsgPriority[] VALUES = values();

	/** Get a DmsMsgPriority from an ordinal value */
	public static DmsMsgPriority fromOrdinal(int o) {
		if (o >= 0 && o < VALUES.length)
			return VALUES[o];
		else
			return INVALID;
	}

	/** Test if a run-time priority was "scheduled" */
	static public boolean isScheduled(DmsMsgPriority p) {
		switch (p) {
		case INVALID:
		case BLANK:
		case OTHER_SYSTEM:
		case ALERT:
		case OPERATOR:
		case OVERRIDE:
			return false;
		default:
			return true;
		}
	}
}
