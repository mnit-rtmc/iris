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
 * Sign message source enumeration.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public enum SignMsgSource {
	unknown,        //  0 unknown source
	reset,          //  1 sign reset
	blank,          //  2 message blank
	expired,        //  3 message expired
	external,       //  4 external system
	operator,       //  5 IRIS operator
	incident,       //  6 deployed incident
	lcs,            //  7 lane-use control signal
	gate_arm,       //  8 gate arm system
	alert,          //  9 alert system (IPAWS or other)
	schedule,       // 10 scheduled DMS action
	clearguide,     // 11 ClearGuide advisory with [cg...] tag
	exit_warning,   // 12 exit backup warning with [exit...] tag
	parking,        // 13 parking availability with [pa...] tag
	slow_warning,   // 14 slow warning with [slow...] tag
	speed_advisory, // 15 speed advisory with [vsa] tag
	standby,        // 16 standby message with [standby] tag
	tolling,        // 17 tolling with [tz...] tag
	travel_time,    // 18 travel time with [tt...] tag
	rwis;           // 19 RWIS subsystem

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

	/** Get source bits from a '+' delimited string */
	static public int fromString(String sources) {
		int bits = 0;
		for (String src: sources.split("\\+"))
			bits |= fromStr(src.trim()).bit();
		return bits;
	}

	/** Get a source from a string */
	static private SignMsgSource fromStr(String src) {
		for (SignMsgSource s: VALUES) {
			if (s.toString().equals(src))
				return s;
		}
		return unknown;
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
