/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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

import java.io.IOException;

/**
 * A request is a specific SS105 message.
 *
 * @author Douglas Lau
 */
abstract public class Request {

	/** Charset encoding for character strings in requests */
	static protected final String CHARSET = "US-ASCII";

	/** Message sub ID "don't care" */
	static protected final byte SUB_ID_DONT_CARE = 0;

	/** Message read request */
	static protected final byte REQ_READ = 0;

	/** Message write request */
	static protected final byte REQ_WRITE = 1;

	/** Format a string to a byte array */
	static protected void formatString(String str, byte[] dest, int destPos,
		int max_len) throws IOException
	{
		byte[] src = str.getBytes(CHARSET);
		int len = Math.min(max_len, src.length);
		System.arraycopy(src, 0, dest, destPos, len);
	}

	/** Parse a 16-bit value */
	static protected int parse16(byte[] body, int pos) {
		int lo = body[pos] & 0xFF;
		int hi = body[pos + 1] & 0xFF;
		return (hi << 8) | lo;
	}

	/** Format the body of a GET request */
	abstract byte[] formatBodyGet() throws IOException;

	/** Format the body of a SET request */
	abstract byte[] formatBodySet() throws IOException;

	/** Parse the payload of a GET response */
	abstract void parsePayload(byte[] body) throws IOException;
}
