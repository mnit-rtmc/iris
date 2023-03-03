/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2023  Minnesota Department of Transportation
 * Copyright (C) 2021  Iteris Inc.
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
 * Sign message source enumeration.  The ordinal values correspond to the bits
 * in the iris.sign_msg_source look-up table.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public enum SignMsgSource {
	blank,          //  0 blank message
	operator,       //  1 IRIS operator
	schedule,       //  2 scheduled DMS action
	tolling,        //  3 DMS action with [tz...] tag
	gate_arm,       //  4 gate arm system
	lcs,            //  5 lane-use control signal
	alert,          //  6 alert system (IPAWS or other)
	external,       //  7 external system
	travel_time,    //  8 DMS action with [tt...] tag
	incident,       //  9 deployed incident
	slow_warning,   // 10 slow warning with [slow...] tag
	speed_advisory, // 11 speed advisory with [vsa] tag
	parking,        // 12 parking availability with [pa...] tag
	clearguide,     // 13 ClearGuide advisory with [cg...] tag
	exit_warning,   // 14 exit backup warning with [exit...] tag
	standby,        // 15 Standby message with [standby] tag
	expired,        // 16 message expired
	reset;          // 17 sign reset

	/** Values array */
	static private final SignMsgSource[] VALUES = values();

	/** Get the bits for a set of sources */
	static public int toBits(SignMsgSource... sources) {
		int bits = 0;
		for (SignMsgSource src: sources)
			bits |= src.bit();
		return bits;
	}

	/** Get a string representation of a set of source bits */
	static public String toString(int bits) {
		StringBuilder sb = new StringBuilder();
		for (SignMsgSource src: VALUES) {
			if (src.checkBit(bits)) {
				if (sb.length() > 0)
					sb.append('+');
				sb.append(src.toString());
			}
		}
		return sb.toString();
	}

	/** Get the bit for a source */
	public int bit() {
		return 1 << ordinal();
	}

	/** Check if the source bit is set */
	public boolean checkBit(int bits) {
		return (bits & bit()) != 0;
	}
}
