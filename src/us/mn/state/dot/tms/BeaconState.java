/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2022  Minnesota Department of Transportation
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
 * Beacon state enumeration.   The ordinal values correspond to the records in
 * the iris.beacon_state look-up table.
 *
 * @author Douglas Lau
 */
public enum BeaconState {
	UNKNOWN,           /* 0: beacon state unknown */
	DARK_REQ,          /* 1: command dark (off) */
	DARK,              /* 2: beacon dark (off) */
	FLASHING_REQ,      /* 3: command flashing (on) */
	FLASHING,          /* 4: lights flashing, commanded by IRIS */
	FAULT_NO_VERIFY,   /* 5: flashing commanded, but not verified */
	FAULT_STUCK_ON,    /* 6: lights flashing, but not commanded */
	FLASHING_EXTERNAL; /* 7: lights flashing, external system */

	/** Values array */
	static private final BeaconState[] VALUES = values();

	/** Get a beacon state from an ordinal value */
	static public BeaconState fromOrdinal(int o) {
		return (o >= 0 && o < VALUES.length) ? VALUES[o] : UNKNOWN;
	}
}
