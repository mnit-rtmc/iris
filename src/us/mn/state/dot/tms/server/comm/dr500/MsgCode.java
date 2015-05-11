/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.dr500;

/**
 * Codes for messages to and from doppler radar.
 *
 * @author Douglas Lau
 */
public enum MsgCode {
	UNKNOWN		(-1),
	TIME_SET	(0x0D),	/* time set req */
	SYS_INFO	(0x0E),	/* system info query req / resp */
	STATUS_RESP	(0x13),	/* status resp */
	VAR_NAME_SET	(0x17),	/* variable name set req */
	VAR_NAME_QUERY	(0x18),	/* variable name query req */
	VAR_RESP	(0x19),	/* variable query resp */
	VAR_INDEX_QUERY	(0x20),	/* variable index query req */
	VIN_QUERY	(0x2A),	/* voltage query req */
	VIN_RESP	(0x2B),	/* voltage query resp */
	TEMP_QUERY	(0x2C),	/* temperature query req */
	TEMP_RESP	(0x2D),	/* temperature query resp */
	AVG_SPEED_QUERY	(0x31),	/* average speed query req */
	AVG_SPEED_RESP	(0x32);	/* average speed query resp */

	/** Message code */
	public final int code;

	/** Create a message code */
	private MsgCode(int c) {
		code = c;
	}

	/** Lookup a message code from value */
	static public MsgCode fromCode(int c) {
		for (MsgCode mc: values()) {
			if (mc.code == c)
				return mc;
		}
		return UNKNOWN;
	}
}
