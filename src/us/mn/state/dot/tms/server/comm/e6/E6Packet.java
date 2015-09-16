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
import us.mn.state.dot.tms.utils.HexString;

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

	/** Number of bytes in packet */
	private int n_bytes = 0;

	/** Message sequence number */
	private byte msn = 0;

	/** Command sequence number (SYSTEM_INFO CommandGroup only) */
	private byte csn = 0;

	/** Format command packet */
	public void format(Command cmd, byte[] data) {
		n_bytes = data.length + 6;
		pkt[0] = (byte) ((n_bytes >> 8) & 0xFF);
		pkt[1] = (byte) ((n_bytes >> 0) & 0xFF);
		pkt[2] = (byte) (msn & 0xFF);
		msn++;
		int b = cmd.bits();
		pkt[3] = (byte) ((b >> 8) & 0xFF);
		pkt[4] = (byte) ((b >> 0) & 0xFF);
		if (cmd.group == CommandGroup.SYSTEM_INFO) {
			pkt[5] = (byte) (csn & 0xFF);
			csn++;
		} else
			pkt[5] = 0;
		System.arraycopy(data, 0, pkt, 6, data.length);
		int xsum = 0;
		for (int i = 0; i < n_bytes; i++)
			xsum += pkt[i];
		pkt[n_bytes] = (byte) (xsum & 0xFF);
		n_bytes++;
	}

	/** Send one packet */
	public void send(OutputStream os) throws IOException {
		os.write(pkt, 0, n_bytes);
		os.flush();
	}

	/** Receive one packet */
	public void receive(InputStream is) throws IOException {
		n_bytes = is.read(pkt);
		if (n_bytes < 0)
			throw CLOSED;
		int n_len = (pkt[0] << 8) | pkt[1];
		if (n_bytes < 7 || n_len != n_bytes)
			throw new ParsingException("BAD LEN: " + n_len);
		int xsum = 0;
		for (int i = 0; i < n_bytes - 1; i++)
			xsum += pkt[i];
		xsum &= 0xFF;
		if (xsum != pkt[n_bytes - 1])
			throw new ChecksumException(pkt);
	}

	/** Parse the command */
	public Command parseCommand() throws IOException {
		int cmd = (pkt[3] << 8) | (pkt[4] << 0);
		Command r_cmd = Command.create(cmd);
		if (r_cmd != null)
			return r_cmd;
		else
			throw new ParsingException("BAD CMD: " + cmd);
	}

	/** Parse the message sequence number */
	public byte parseMsn() {
		return pkt[2];
	}

	/** Parse the packet data */
	public byte[] parseData() throws IOException {
		if (n_bytes >= 7) {
			byte[] data = new byte[n_bytes - 7];
			System.arraycopy(pkt, 6, data, 0, data.length);
			return data;
		}
		throw new ParsingException("BAD LEN: " + n_bytes);
	}

	/** Get a string representation */
	@Override
	public String toString() {
		return "pkt:" + HexString.format(pkt, n_bytes, ':');
	}
}
