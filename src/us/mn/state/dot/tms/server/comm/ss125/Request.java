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
import us.mn.state.dot.tms.server.comm.ParsingException;

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

	/** Format a boolean value */
	static protected void formatBool(boolean value, byte[] body, int pos) {
		body[pos] = value ? (byte)1 : (byte)0;
	}

	/** Format an 8-bit value */
	static protected void format8(int value, byte[] body, int pos) {
		body[pos] = (byte)value;
	}

	/** Format a 16-bit value */
	static protected void format16(int value, byte[] body, int pos) {
		body[pos] = (byte)((value >> 8) & 0xFF);
		body[pos + 1] = (byte)(value & 0xFF);
	}

	/** Format a 16-bit fixed-point value */
	static protected void format16Fixed(float value, byte[] body, int pos) {
		int intg = (int)value;
		int frac = (int)(256 * (value - intg));
		body[pos] = (byte)intg;
		body[pos + 1] = (byte)frac;
	}

	/** Format a 32-bit value */
	static protected void format32(int value, byte[] body, int pos) {
		body[pos] = (byte)((value >> 24) & 0xFF);
		body[pos + 1] = (byte)((value >> 16) & 0xFF);
		body[pos + 2] = (byte)((value >> 8) & 0xFF);
		body[pos + 3] = (byte)(value & 0xFF);
	}

	/** Parse a string value */
	static protected String parseString(byte[] body, int pos, int len)
		throws IOException
	{
		return new String(body, pos, len, CHARSET).trim();
	}

	/** Parse a boolean value */
	static protected boolean parseBoolean(byte b) throws IOException {
		if(b == 0)
			return false;
		else if(b == 1)
			return true;
		else
			throw new ParsingException("Invalid boolean value");
	}

	/** Parse an 8-bit value */
	static protected int parse8(byte b) {
		return b & 0xFF;
	}

	/** Parse a 16-bit value */
	static protected int parse16(byte[] body, int pos) {
		int hi = body[pos] & 0xFF;
		int lo = body[pos + 1] & 0xFF;
		return (hi << 8) | lo;
	}

	/** Parse a 16-bit fixed-point value */
	static protected float parse16Fixed(byte[] body, int pos) {
		int intg = body[pos] & 0xFF;
		int frac = body[pos + 1] & 0xFF;
		return intg + frac / 256f;
	}

	/** Parse a 32-bit value */
	static protected int parse32(byte[] body, int pos) {
		int b3 = body[pos] & 0xFF;
		int b2 = body[pos + 1] & 0xFF;
		int b1 = body[pos + 2] & 0xFF;
		int b0 = body[pos + 3] & 0xFF;
		return (b3 << 24) | (b2 << 16) | (b1 << 8) | b0;
	}

	/** Format the body of a GET request */
	abstract byte[] formatBodyGet() throws IOException;

	/** Format the body of a SET request */
	abstract byte[] formatBodySet() throws IOException;

	/** Parse the payload of a GET response */
	abstract void parsePayload(byte[] body) throws IOException;
}
