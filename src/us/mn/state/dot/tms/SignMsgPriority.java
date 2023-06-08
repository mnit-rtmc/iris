/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2023  Minnesota Department of Transportation
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
 * Sign message priority levels.  This enum is designed so that the ordinal
 * values can be used for NTCIP activation and run-time priority.  NTCIP
 * priority values can range from 1 to 255, with higher numbers indicating
 * higher priority.  The enum is also ordered from low to high priority.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public enum SignMsgPriority {
	invalid,    /* 0: invalid priority */
	low_1,      /* 1: low 1 (blank) */
	low_2,      /* 2: low 2 */
	low_3,      /* 3: low 3 */
	low_4,      /* 4: low 4 */
	low_sys,    /* 5: low system priority (cleared incidents) */
	medium_1,   /* 6: medium 1 */
	medium_2,   /* 7: medium 2 */
	medium_3,   /* 8: medium 3 */
	medium_4,   /* 9: medium 4 */
	medium_sys, /* 10: medium system (other system) */
	high_1,     /* 11: high 1 (operator) */
	high_2,     /* 12: high 2 */
	high_3,     /* 13: high 3 */
	high_4,     /* 14: high 4 */
	high_sys;   /* 15: high system priority */

	/** Values array */
	static private final SignMsgPriority[] VALUES = values();

	/** Get a SignMsgPriority from an ordinal value */
	static public SignMsgPriority fromOrdinal(int o) {
		return (o >= 0 && o < VALUES.length) ? VALUES[o] : invalid;
	}
}
