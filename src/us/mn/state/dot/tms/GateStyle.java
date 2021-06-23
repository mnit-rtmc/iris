/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2021  SRF Consulting Group
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

/** Static GATE_STYLE system-attribute utility class
 * 
 * Used to switch GUI elements and protocol support
 * between Minnesota and Nebraska gate styles.
 * 
 * @author John L. Stanley - SRF Consulting
 */
public enum GateStyle {
	NONE,  // 0 = none
	MNDOT, // 1 = Minnesota
	NDOT;  // 2 = Nebraska

	/** Don't allow instantiation */
	private GateStyle() { }

// Note:  The VALUES array and fromOrdinal(...)
//        method were intentionally omitted
//        because they're not used anywhere.

	/** Test if a specific gate-style is the
	 *  current style */
	static private boolean is(GateStyle gs) {
		int gsi = SystemAttrEnum.GATE_STYLE.getInt();
		return gs.ordinal() == gsi;
	}
	
	/** Shorthand test for MnDOT/Minnesota style */
	static public boolean isMnDOT() {
		return GateStyle.is(MNDOT);
	}

	/** Shorthand test for NDOT/NDOR/Nebraska style */
	static public boolean isNDOT() {
		return GateStyle.is(NDOT);
	}
	
}
