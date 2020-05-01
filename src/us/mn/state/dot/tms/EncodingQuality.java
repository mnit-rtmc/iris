/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2019-2020  Minnesota Department of Transportation
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
 * Encoding quality enumeration.  The ordinal values correspond to the records
 * in the iris.encoding_quality look-up table.
 *
 * @author Douglas Lau
 */
public enum EncodingQuality {
	LOW,    /* 0 */
	MEDIUM, /* 1 */
	HIGH;   /* 2 */

	/** Cached values array */
	static public final EncodingQuality[] VALUES = values();

	/** Get an encoding quality from an ordinal value */
	static public EncodingQuality fromOrdinal(int o) {
		return (o >= 0 && o < VALUES.length) ? VALUES[o] : null;
	}
}
