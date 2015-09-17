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
 * E6 response codes.
 *
 * @author Douglas Lau
 */
public class Response {

	/** Bit flags for command response */
	static private final int CMD_RESPONSE_BITS = 0xFF;

	/** Create a response from bits.
	 * @param b Bits of response from packet.
	 * @return Valid response, or null on error. */
	static public Response create(int b) {
		ResponseType rt = ResponseType.lookup(b);
		ResponseStatus rs = ResponseStatus.lookup(b);
		if (rt != null && rs != null) {
			int cr = b & CMD_RESPONSE_BITS;
			if ((rt.bits | rs.bits | cr) == b)
				return new Response(rt, rs, cr);
		}
		return null;
	}

	/** Response type */
	public final ResponseType r_type;

	/** Response status */
	public final ResponseStatus r_stat;

	/** Command group command response */
	public final int cmd_resp;

	/** Create a new response */
	public Response(ResponseType rt, ResponseStatus rs, int cr) {
		r_type = rt;
		r_stat = rs;
		cmd_resp = cr & CMD_RESPONSE_BITS;
	}

	/** Get response bits */
	public int bits() {
		return r_type.bits | r_stat.bits | cmd_resp;
	}

	/** Get a string representation */
	@Override
	public String toString() {
		return "r_type=" + r_type + "," +
		       "r_stat=" + r_stat + "," +
		       "cmd_resp=" + cmd_resp;
	}
}
