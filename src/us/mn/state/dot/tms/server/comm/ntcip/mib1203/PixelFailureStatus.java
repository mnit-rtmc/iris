/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2015  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ntcip.mib1203;

/**
 * Enumeration of pixel failure status flags.
 *
 * @author Douglas Lau
 */
public enum PixelFailureStatus {
	STUCK_ON,
	COLOR_ERROR,
	ELECTRICAL_ERROR,
	MECHANICAL_ERROR,
	STUCK_OFF,		// v2
	PARTIAL_FAILURE;	// v2

	/** Test if a bit flag is set */
	private boolean isSet(int v) {
		int bit = 1 << ordinal();
		return (v & bit) != 0;
	}

	/** Test if the pixel is stuck on */
	static public boolean isStuckOn(int v) {
		return STUCK_ON.isSet(v);
	}

	/** Test if the pixel is stuck off */
	static public boolean isStuckOff(int v) {
		return isStuckOffV1(v) || isStuckOffV2(v);
	}

	/** Test if the pixel is stuck off (1203v1) */
	static private boolean isStuckOffV1(int v) {
		/** stuck off assumed for 1203v1 when STUCK_ON unset */
		return !(STUCK_ON.isSet(v) | PARTIAL_FAILURE.isSet(v));
	}

	/** Test if the pixel is stuck off (1203v2) */
	static private boolean isStuckOffV2(int v) {
		return STUCK_OFF.isSet(v);
	}
}
