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
 * Response to a request packet.
 *
 * @author Douglas Lau
 */
public class Response {

	/** Message code */
	public final MsgCode msg_code;

	/** Body of response */
	public final byte[] body;

	/** Create a new response packet */
	public Response(MsgCode mc, byte[] b) {
		msg_code = mc;
		body = b;
	}
}
