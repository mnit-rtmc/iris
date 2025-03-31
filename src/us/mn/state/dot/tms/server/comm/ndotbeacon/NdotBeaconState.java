/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2022  SRF Consulting Group
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
package us.mn.state.dot.tms.server.comm.ndotbeacon;

/**
 * Control state of NDOT Beacon controller.
 *
 * @author John L. Stanley - SRF Consulting
 */
public enum NdotBeaconState {
	// Beacon response field a - Beacon Control Status (0-10)
	BEACON_ENABLED,  // 0 - On/flashing/deployed
	BEACON_DISABLED, // 1 - Off/dark/clear
	UNKNOWN_2,
	UNKNOWN_3,
	UNKNOWN_4,
	UNKNOWN_5,
	UNKNOWN_6,
	UNKNOWN_7,
	UNKNOWN_8,
	UNKNOWN_9,
	UNKNOWN_10;

	/** Static array of beacon state values */
	private static final NdotBeaconState[] VALUES = values();

	/** Get beacon state from an ordinal value */
	static public NdotBeaconState fromOrdinal(int o) {
		return ((o >= 0) && (o < VALUES.length)) ? VALUES[o] : null;
	}
}
