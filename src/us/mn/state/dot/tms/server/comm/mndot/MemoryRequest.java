/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.mndot;

import java.io.IOException;
import us.mn.state.dot.tms.server.comm.ProtocolException;

/**
 * Memory Request to read or write 170 controller memory.
 *
 * @author Douglas Lau
 */
public class MemoryRequest extends Request {

	/** Maximum length of a memory request (128 - 5 octet overhead) */
	static protected final int MAX_LENGTH = 123;

	/** Offset for MSB of memory address */
	static protected final int OFF_ADDRESS_MSB = 2;

	/** Offset for LSB of memory address */
	static protected final int OFF_ADDRESS_LSB = 3;

	/** Offset for memory read length */
	static protected final int OFF_READ_LENGTH = 4;

	/** 170 controller memory address */
	protected final int address;

	/** 170 controller memory payload length */
	protected final byte[] payload;

	/** Check for a valid payload length */
	protected void checkPayloadLength() throws ProtocolException {
		if(payload.length < 1 || payload.length > MAX_LENGTH)
			throw new ProtocolException("INVALID PAYLOAD SIZE");
	}

	/** Create a memory request */
	public MemoryRequest(int a, byte[] buf) throws ProtocolException {
		address = a;
		payload = buf;
		checkPayloadLength();
	}

	/** Format a basic "GET" request */
	protected byte[] formatPayloadGet(Message m) throws IOException {
		byte[] req = new byte[6];
		req[OFF_DROP_CAT] = m.dropCat(READ_MEMORY);
		req[OFF_LENGTH] = 3;
		req[OFF_ADDRESS_MSB] = (byte)((address >> 8) & 0xFF);
		req[OFF_ADDRESS_LSB] = (byte)(address & 0xFF);
		req[OFF_READ_LENGTH] = (byte)payload.length;
		req[req.length - 1] = checksum(req);
		return req;
	}

	/** Get the expected number of octets in response to a GET request */
	protected int expectedGetOctets() {
		return payload.length + 3;
	}

	/** Parse the response from a GET request */
	protected void parseGetResponse(byte[] buf) {
		System.arraycopy(buf, OFF_PAYLOAD, payload, 0, payload.length);
	}

	/** Format a basic "SET" request */
	protected byte[] formatPayloadSet(Message m) throws IOException {
		byte[] req = new byte[payload.length + 5];
		req[OFF_DROP_CAT] = m.dropCat(WRITE_MEMORY);
		req[OFF_LENGTH] = (byte)(payload.length + 2);
		req[OFF_ADDRESS_MSB] = (byte)((address >> 8) & 0xFF);
		req[OFF_ADDRESS_LSB] = (byte)(address & 0xFF);
		System.arraycopy(payload, 0, req, 4, payload.length);
		req[req.length - 1] = checksum(req);
		return req;
	}

	/** Get the expected number of octets in response to a SET request */
	protected int expectedSetOctets() {
		return 3;
	}
}
