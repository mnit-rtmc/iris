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
public enum Response {
	ACK		(ResponseType.SYNCHRONOUS, ResponseStatus.CONTROL, 0),
	MSG_SEQ_ERROR	(ResponseType.SYNCHRONOUS, ResponseStatus.ERROR, 1),
	COMMAND_COMPLETE(ResponseType.ASYNCHRONOUS, ResponseStatus.OK, 0),
	COMMAND_IN_PROGRESS(ResponseType.ASYNCHRONOUS, ResponseStatus.OK, 1),
	STATUS_CHANGE_ERR(ResponseType.UNSOLICITED, ResponseStatus.ERROR, 10),
	STATUS_CHANGE_OK(ResponseType.UNSOLICITED, ResponseStatus.OK, 10);

	/** Create a new response */
	private Response(ResponseType rt, ResponseStatus rs, int cr) {
		r_type = rt;
		r_stat = rs;
		cmd_resp = cr & CMD_RESPONSE_BITS;
	}

	/** Bit flags for command response */
	static private final int CMD_RESPONSE_BITS = 0xFF;

	/** Response type */
	public final ResponseType r_type;

	/** Response status */
	public final ResponseStatus r_stat;

	/** Command group command response */
	public final int cmd_resp;

	/** Get response bits */
	public int bits() {
		return r_type.bits | r_stat.bits | cmd_resp;
	}

	/** Lookup a response from bits.
	 * @param b Bits of response from packet.
	 * @return Valid response, or null on error. */
	static public Response fromBits(int b) {
		ResponseType rt = ResponseType.lookup(b);
		ResponseStatus rs = ResponseStatus.lookup(b);
		if (rt != null && rs != null) {
			int cr = b & CMD_RESPONSE_BITS;
			if ((rt.bits | rs.bits | cr) == b)
				return fromValues(rt, rs, cr);
		}
		return null;
	}

	/** Lookup a response from values */
	static private Response fromValues(ResponseType rt, ResponseStatus rs,
		int cr)
	{
		for (Response r: values()) {
			if (rt == r.r_type &&
			    rs == r.r_stat &&
			    cr == r.cmd_resp)
				return r;
		}
		return null;
	}
}
