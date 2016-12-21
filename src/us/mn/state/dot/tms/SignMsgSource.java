/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2016  Minnesota Department of Transportation
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
 */
public enum SignMsgSource {
	blank,		// 0  blank message
	operator,	// 1  IRIS operator
	schedule,	// 2  scheduled DMS action
	tolling,	// 3  DMS action with [tz...] tag
	gate_arm,	// 4  gate arm system
	lcs,		// 5  lane-use control signal
	aws,		// 6  automated warning system
	external,	// 7  external system
	travel_time,	// 8  DMS action with [tt...] tag
	incident;	// 9  deployed incident

	/** Get the bit for a source */
	public int bit() {
		return 1 << ordinal();
	}

	/** Check if the source bit is set */
	public boolean checkBit(int bits) {
		return (bits & bit()) != 0;
	}

	/** Get the bits for a set of sources */
	static public int toBits(SignMsgSource... sources) {
		int bits = 0;
		for (SignMsgSource src: sources)
			bits |= src.bit();
		return bits;
	}
}
