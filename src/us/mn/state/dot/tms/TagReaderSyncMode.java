/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018  Minnesota Department of Transportation
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
 * Tag reader synchronization mode enumeration.  The ordinal values correspond
 * to the records in the iris.tag_reader_sync_mode look-up table.  For some
 * unknown reason, these are set up as bit flags, even though it is never valid
 * to set more than one at a time.
 *
 * @author Douglas Lau
 */
public enum TagReaderSyncMode {
	SLAVE,          // 0 (1 << 0)
	MASTER,         // 1 (1 << 1)
	GPS_SECONDARY,  // 2 (1 << 2)
	GPS_PRIMARY;    // 3 (1 << 3)

	/** Cached values array */
	static private final TagReaderSyncMode[] VALUES = values();

	/** Get mode from bit flags */
	static public TagReaderSyncMode fromBits(int b) {
		for (TagReaderSyncMode m: VALUES) {
			if ((1 << m.ordinal()) == b)
				return m;
		}
		return null;
	}

	/** Get mode from an ordinal value */
	static public TagReaderSyncMode fromOrdinal(Integer o) {
		return (o != null && o >= 0 && o < VALUES.length)
		      ? VALUES[o]
		      : null;
	}
}
