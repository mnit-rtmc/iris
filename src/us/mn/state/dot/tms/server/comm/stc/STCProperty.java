/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.stc;

import java.io.IOException;
import java.io.InputStream;
import us.mn.state.dot.tms.server.comm.ChecksumException;
import us.mn.state.dot.tms.server.comm.ControllerProperty;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * A property which can be sent or received from a controller.
 *
 * @author Douglas Lau
 */
abstract public class STCProperty extends ControllerProperty {

	/** Charset name for ASCII */
	static protected final String ASCII = "US-ASCII";

	/** Frame sentinel value */
	static private final int SENTINEL = 0xFF;

	/** Drop address of master (for responses) */
	static private final int DROP_MASTER = 0;

	/** Byte offsets from beginning of frame */
	static private final int OFF_SENTINEL = 0;
	static private final int OFF_ADDRESS = 1;
	static private final int OFF_SIZE = 2;
	static private final int OFF_MESSAGE = 3;

	/** Minimum and maximum message size */
	static private final int MIN_MESSAGE_SIZE = 1;
	static private final int MAX_MESSAGE_SIZE = 254;

	/** Calculate a checksum */
	static private int checksum(byte[] buf) {
		int c = 0;
		for(int i = 0; i < buf.length; i++)
			c += buf[i] & 0xFF;
		return c;
	}

	/** Format a request frame */
	static protected final byte[] formatRequest(int drop, byte[] data) {
		byte[] req = new byte[OFF_MESSAGE + data.length + 1];
		format8(req, OFF_SENTINEL, SENTINEL);
		format8(req, OFF_ADDRESS, drop);
		format8(req, OFF_SIZE, data.length);
		System.arraycopy(data, 0, req, OFF_MESSAGE, data.length);
		int c = checksum(req);
		format8(req, OFF_MESSAGE + data.length, (~c) + 1);
		return req;
	}

	/** Parse one frame.
	 * @param is Input stream to read from.
	 * @param drop Drop address. */
	protected void parseFrame(InputStream is, int drop) throws IOException {
		byte[] header = recvResponse(is, OFF_MESSAGE);
		if(parse8(header, OFF_SENTINEL) != SENTINEL)
			throw new ParsingException("INVALID SENTINEL");
		int d = parse8(header, OFF_ADDRESS);
		if(d != DROP_MASTER)
			throw new ParsingException("INVALID ADDRESS: " + d);
		int size = parse8(header, OFF_SIZE);
		if(size < MIN_MESSAGE_SIZE || size > MAX_MESSAGE_SIZE)
			throw new ParsingException("INVALID SIZE: " + size);
		byte[] body = recvResponse(is, size + 1);
		if((checksum(header) + checksum(body)) % 256 != 0)
			throw new ChecksumException(body);
		parseMessage(body, body.length - 1);
	}

	/** Parse a received message */
	abstract protected void parseMessage(byte[] msg, int len)
		throws IOException;

	/** Parse a 1-digit ASCII-hex value */
	static protected int parseAsciiHex1(byte[] body, int pos)
		throws IOException
	{
		String hex = new String(body, pos, 1, ASCII);
		try {
			return Integer.parseInt(hex, 16);
		}
		catch(NumberFormatException e) {
			throw new ParsingException("INVALID HEX: " + hex);
		}
	}

	/** Parse a 2-digit ASCII-hex value */
	static protected int parseAsciiHex2(byte[] body, int pos)
		throws IOException
	{
		String hex = new String(body, pos, 2, ASCII);
		try {
			return Integer.parseInt(hex, 16);
		}
		catch(NumberFormatException e) {
			throw new ParsingException("INVALID HEX: " + hex);
		}
	}

	/** Parse a boolean ASCII-hex value */
	static protected boolean parseBoolean(byte[] body, int pos)
		throws IOException
	{
		int b = body[pos];
		switch(b) {
		case '0':
			return false;
		case '1':
			return true;
		default:
			throw new ParsingException("INVALID BOOL: " + b);
		}
	}
}
