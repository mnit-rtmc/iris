/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2019  Minnesota Department of Transportation
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
	STANDBY,	/* 2: standby message */
	PSA,		/* 3: public service announcement */
	TRAVEL_TIME,	/* 4: travel time priority */
	ALERT_LOW,	/* 5: low alert priority */
	SCHED_A,	/* 6: scheduled priority A (planned events) */
	SCHED_B,	/* 7: scheduled priority B */
	SCHED_C,	/* 8: scheduled priority C */
	SCHED_D,	/* 9: scheduled priority D */
	OTHER_SYSTEM,	/* 10: other system priority */
	RESERVED_2,	/* 11: reserved for future use */
	OPERATOR,	/* 12: operator priority */
	ALERT_MED,	/* 13: medium alert priority */
	GATE_ARM,	/* 14: gate-arm priority */
	LCS,		/* 15: LCS priority */
	INCIDENT_LOW,	/* 16: incident low-priority */
	INCIDENT_MED,	/* 17: incident medium-priority */
	SCHED_HIGH,	/* 18: scheduled high-priority */
	INCIDENT_HIGH,	/* 19: incident high-priority */
	ALERT_HIGH,	/* 20: high alert priority */
	OVERRIDE;	/* 21: override priority */

	/** Values array */
	static private final DmsMsgPriority[] VALUES = values();

	/** Get a DmsMsgPriority from an ordinal value */
	static public DmsMsgPriority fromOrdinal(int o) {
		if (o >= 0 && o < VALUES.length)
			return VALUES[o];
		else
			return INVALID;
	}
}
