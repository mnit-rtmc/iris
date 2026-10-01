/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2026  SRF Consulting Group
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
package us.mn.state.dot.tms.server.comm.ndotgate;

/**
 * Gate status for NdotGateV6 gate arms.
 *
 * Note:  Updated in August 2016 to include
 *  multi-arm gate protocol referred to as v5.
 * Note2:  Updated in January 2026 to use shorter
 *  gate-status fault-message strings.
 *
 * @author John L. Stanley - SRF Consulting
 */
public enum StatusOfGate {
	// Gate response field a - Gate Status (0-10)
	CLOSE_COMPLETE,         //  0 - Closed (lowered)
	OPEN_COMPLETE,          //  1 - Open (raised)
	OPEN_IN_PROGRESS,       //  2 - Gate moving from Closed to Open
	CLOSE_IN_PROGRESS,      //  3 - Gate Moving from Open to Closed
	UNKNOWN_4,
	UNKNOWN_5,
	TIMEOUT_STILL_CLOSED,   //  6 - Error - Timed out and never moved from Closed
	TIMEOUT_OPEN_FAILED,    //  7 - Error - Timed out moving from Closed to Open
	TIMEOUT_STILL_OPEN,     //  8 - Error - Timed out and never moved from Open
	TIMEOUT_CLOSE_FAILED,   //  9 - Error - Timed out moving from Open to Closed

	// Following error state is from v5 protocol extension....
	GATE_NOT_CONFIGURED;    // 10 - Error - Requested gate-arm number not configured

	/** Static array of GateArmState values */
	private static final StatusOfGate[] VALUES = values();

	/** Get gate arm state from an ordinal value */
	static public StatusOfGate fromOrdinal(int o) {
		return ((o >= 0) && (o < VALUES.length)) ? VALUES[o] : null;
	}
}
