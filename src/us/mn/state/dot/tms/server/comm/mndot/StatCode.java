/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.mndot;

/**
 * Message status codes.
 *
 * @author Douglas Lau
 */
public enum StatCode {
	OK,			// 0
	BAD_MESSAGE,		// 1
	BAD_POLL_CHECKSUM,	// 2
	DOWNLOAD_REQUEST,	// 3
	WRITE_PROTECT,		// 4
	MESSAGE_SIZE,		// 5
	NO_DATA,		// 6
	NO_RAM,			// 7
	DOWNLOAD_REQUEST_4,	// 8 (4-bit addressing)
	INVALID;		// 9 (not used in protocol)

	/** Get status code an ordinal value */
	static public StatCode fromOrdinal(int s) {
		for (StatCode sc: values()) {
			if (sc.ordinal() == s)
				return sc;
		}
		return INVALID;
	}
}
