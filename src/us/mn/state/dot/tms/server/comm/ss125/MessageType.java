/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ss125;

/**
 * Message type enumeration.
 *
 * @author Douglas Lau
 */
public enum MessageType {
	READ(0),
	WRITE(1),
	RESULT(2),
	UNKNOWN(-1);

	/** Message type code */
	public final int code;

	/** Create a message type */
	private MessageType(int c) {
		code = c;
	}

	/** Lookup a message type from value */
	static public MessageType fromCode(int c) {
		for(MessageType mt: MessageType.values()) {
			if(mt.code == c)
				return mt;
		}
		return UNKNOWN;
	}
}
