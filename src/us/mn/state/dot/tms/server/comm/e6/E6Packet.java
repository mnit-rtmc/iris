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

import java.io.EOFException;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import us.mn.state.dot.tms.server.comm.ChecksumException;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * E6 packet.
 *
 * @author Douglas Lau
 */
public class E6Packet {

	/** Exception thrown when stream is closed */
	static private final EOFException CLOSED = new EOFException("CLOSED");

	/** Packet buffer */
	private final byte[] pkt = new byte[128];

	/** Message sequence number */
	private byte msn = 0;

	/** Send one packet */
	public void send(OutputStream os) throws IOException {
		// FIXME
	}

	/** Receive one packet */
	public void receive(InputStream is) throws IOException {
		int n_bytes = parse(is);
		// FIXME
	}

	/** Parse one packet */
	private int parse(InputStream is) throws IOException {
		int n_bytes = is.read(pkt);
		if (n_bytes < 0)
			throw CLOSED;
		int n_len = (pkt[0] << 8) | pkt[1];
		if (n_bytes < 2 || n_len != n_bytes)
			throw new ParsingException("BAD LEN: " + n_len);
		int xsum = 0;
		for (int i = 0; i < n_bytes - 1; i++)
			xsum += pkt[i];
		xsum &= 0xFF;
		if (xsum != pkt[n_bytes - 1])
			throw new ChecksumException(pkt);
		return n_bytes;
	}
}
