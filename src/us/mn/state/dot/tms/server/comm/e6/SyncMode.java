/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018-2023  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.e6;

/**
 * Tag reader synchronization mode enumeration.
 *
 * @author Douglas Lau
 */
public enum SyncMode {
	slave,          // 0 (1 << 0)
	master,         // 1 (1 << 1)
	gps_secondary,  // 2 (1 << 2)
	gps_primary;    // 3 (1 << 3)

	/** Cached values array */
	static private final SyncMode[] VALUES = values();

	/** Get sync mode from bit flags */
	static public SyncMode fromBits(int b) {
		for (SyncMode sm: VALUES) {
			if ((1 << sm.ordinal()) == b)
				return sm;
		}
		return null;
	}

	/** Get sync mode from a value */
	static public SyncMode fromValue(String v) {
		for (SyncMode sm: VALUES) {
			if (sm.toString().equals(v))
				return sm;
		}
		return null;
	}
}
