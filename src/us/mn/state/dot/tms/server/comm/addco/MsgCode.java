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
package us.mn.state.dot.tms.server.comm.addco;

/**
 * Addco message codes.
 *
 * @author Douglas Lau
 */
public enum MsgCode {
	UNKNOWN(-1),
	NORMAL(1),		/** Normal message */
	ACK_MORE(5),		/** Acknowledge; response following */
	ACK(6),			/** Acknowledge */
	ERR(8);			/** Invalid -- maybe CRC error? */

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
