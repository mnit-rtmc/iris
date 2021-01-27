/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2021  Minnesota Department of Transportation
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
 * Common Alerting Protocol (CAP) message type enum.
 *
 * The ordinal values correspond to the records in the cap.msg_type look-up
 * table.
 *
 * @author Douglas Lau
 */
public enum CapMsgType {
	UNKNOWN,
	ALERT,
	UPDATE,
	CANCEL,
	ACK,
	ERROR;

	/** Values array */
	static private final CapMsgType[] VALUES = values();

	/** Get a CapMsgType from an ordinal value */
	static public CapMsgType fromOrdinal(int o) {
		return (o >= 0 && o < VALUES.length) ? VALUES[o] : null;
	}

	/** Get the CapMsgType from the value provided */
	static public CapMsgType fromValue(String v) {
		for (CapMsgType e: VALUES) {
			if (e.name().equalsIgnoreCase(v))
				return e;
		}
		return UNKNOWN;
	}
}
