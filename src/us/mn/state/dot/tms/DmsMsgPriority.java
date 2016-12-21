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
	SCHED_A,	/* 6: scheduled priority A (planned events) */
	SCHED_B,	/* 7: scheduled priority B */
	SCHED_C,	/* 8: scheduled priority C */
	SCHED_D,	/* 9: scheduled priority D */
	OTHER_SYSTEM,	/* 10: other system priority */
	ALERT,		/* 11: alert priority (AMBER alerts, etc.) */
	OPERATOR,	/* 12: operator priority */
	GATE_ARM,	/* 13: gate-arm priority */
	LCS_LOW,	/* 14: low-priority LCS */
	LCS_MED,	/* 15: medium-priority LCS */
	LCS_HIGH,	/* 16: high-priority LCS */
	INCIDENT_LOW,	/* 17: low-priority incident */
	INCIDENT_MED,	/* 18: medium-priority incident */
	INCIDENT_HIGH,	/* 19: high-priority incident */
	AWS,		/* 20: automated warning system */
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

	/** Get SignMsgSource for a run-time priority */
	public int getSource() {
		switch (this) {
		case BLANK:
		case OVERRIDE:
			return SignMsgSource.blank.bit();
		case TRAVEL_TIME:
			return SignMsgSource.toBits(SignMsgSource.schedule,
			                            SignMsgSource.travel_time);
		case PSA:
		case SPEED_LIMIT:
		case SCHED_A:
		case SCHED_B:
		case SCHED_C:
		case SCHED_D:
			return SignMsgSource.schedule.bit();
		case ALERT:
		case OPERATOR:
			return SignMsgSource.operator.bit();
		case GATE_ARM:
			return SignMsgSource.gate_arm.bit();
		case LCS_LOW:
		case LCS_MED:
		case LCS_HIGH:
			return SignMsgSource.lcs.bit();
		case INCIDENT_LOW:
		case INCIDENT_MED:
		case INCIDENT_HIGH:
			return SignMsgSource.incident.bit();
		case AWS:
			return SignMsgSource.aws.bit();
		default:
			return SignMsgSource.external.bit();
		}
	}
}
