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
	RESERVED,	/* 2: reserved for future use */
	PSA,		/* 3: public service announcement */
	TRAVEL_TIME,	/* 4: travel time priority */
	ALERT,		/* 5: alert priority (AMBER alerts, etc.) */
	SCHED_A,	/* 6: scheduled priority A (planned events) */
	SCHED_B,	/* 7: scheduled priority B */
	SCHED_C,	/* 8: scheduled priority C */
	SCHED_D,	/* 9: scheduled priority D */
	OTHER_SYSTEM,	/* 10: other system priority */
	RESERVED_2,	/* 11: reserved for future use */
	OPERATOR,	/* 12: operator priority */
	AWS,		/* 13: automated warning system priority */
	GATE_ARM,	/* 14: gate-arm priority */
	LCS,		/* 15: LCS priority */
	INCIDENT_LOW,	/* 16: incident low-priority */
	INCIDENT_MED,	/* 17: incident medium-priority */
	SCHED_HIGH,	/* 18: scheduled high-priority */
	INCIDENT_HIGH,	/* 19: incident high-priority */
	AWS_HIGH,	/* 20: automated warning system high-priority */
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
		case ALERT:
		case SCHED_A:
		case SCHED_B:
		case SCHED_C:
		case SCHED_D:
		case SCHED_HIGH:
			return SignMsgSource.schedule.bit();
		case OPERATOR:
			return SignMsgSource.operator.bit();
		case GATE_ARM:
			return SignMsgSource.toBits(SignMsgSource.schedule,
			                            SignMsgSource.gate_arm);
		case LCS:
			return SignMsgSource.toBits(SignMsgSource.operator,
			                            SignMsgSource.lcs);
		case INCIDENT_LOW:
		case INCIDENT_MED:
		case INCIDENT_HIGH:
			return SignMsgSource.toBits(SignMsgSource.operator,
			                            SignMsgSource.incident);
		case AWS:
		case AWS_HIGH:
		case OTHER_SYSTEM:
			return SignMsgSource.external.bit();
		default:
			return SignMsgSource.external.bit();
		}
	}
}
