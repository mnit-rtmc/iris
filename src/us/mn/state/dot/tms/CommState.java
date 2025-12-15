/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2025  Minnesota Department of Transportation
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
 * Comm state enumeration.   The ordinal values correspond to the records in
 * the iris.comm_state look-up table.
 *
 * @author Douglas Lau
 */
public enum CommState {
	UNKNOWN,          /* 0: Comm state unknown */
	OK,               /* 1: Comm OK */
	FAILED,           /* 2: Comm FAILED */
	CONNECTION_ERROR, /* 3: Connection error */
	CONTROLLER_ERROR, /* 4: Controller error */
	CHECKSUM_ERROR,   /* 5: Checksum error */
	PARSING_ERROR,    /* 6: Parsing error */
	TIMEOUT_ERROR,    /* 7: Timeout error */
	ERROR;            /* 8: Other error */

	/** Values array */
	static private final CommState[] VALUES = values();

	/** Get a comm state from an ordinal value */
	static public CommState fromOrdinal(int o) {
		return (o >= 0 && o < VALUES.length) ? VALUES[o] : UNKNOWN;
	}
}
