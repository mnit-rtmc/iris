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
package us.mn.state.dot.tms.server.comm.e6;

/**
 * E6 response status.
 *
 * @author Douglas Lau
 */
public enum ResponseStatus {
	OK			(0x0800),
	ERROR			(0x0200),
	CONTROL			(0x0100);

	/** Create a new response status */
	private ResponseStatus(int b) {
		bits = b;
	}

	/** Bits for response status */
	public int bits;

	/** Get the bits for all response statuses */
	static private int status_bits() {
		int b = 0;
		for (ResponseStatus rs: values())
			b |= rs.bits;
		return b;
	}

	/** Lookup the response status for a response */
	static public ResponseStatus lookup(int b) {
		int s_bits = b & status_bits();
		for (ResponseStatus rs: values()) {
			if ((rs.bits & s_bits) == rs.bits)
				return rs;
		}
		return null;
	}
}
