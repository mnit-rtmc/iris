/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011-2017  Minnesota Department of Transportation
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
 * Encoding enumeration.  The ordinal values correspond to the records
 * in the iris.encoding look-up table.
 *
 * @author Douglas Lau
 */
public enum Encoding {
	UNKNOWN,	/* 0: use for MMS or other weird protocols */
	MJPEG,		/* 1: motion JPEG */
	MPEG2,		/* 2 */
	MPEG4,		/* 3 */
	H264,		/* 4 */
	H265;		/* 5 */

	/** Cached values array */
	static private final Encoding[] VALUES = values();

	/** Get an encoding from an ordinal value */
	static public Encoding fromOrdinal(int o) {
		return (o >= 0 && o < VALUES.length) ? VALUES[o] : UNKNOWN;
	}
}
