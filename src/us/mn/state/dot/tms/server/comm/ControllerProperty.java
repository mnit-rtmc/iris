/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2015  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm;

import java.io.EOFException;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import us.mn.state.dot.tms.server.ControllerImpl;

/**
 * A controller property is one element of a CommMessage.  It represents a
 * property which can be queried from or stored to a controller.
 *
 * @author Douglas Lau
 */
abstract public class ControllerProperty {

	/** Maximum number of tries when reading a response */
	static private final int MAX_TRIES = 5;

	/** Format an 8-bit value.
	 * @param buf Buffer to store formatted value.
	 * @param pos Starting position in buffer.
	 * @param value Value to store in buffer. */
	static protected void format8(byte[] buf, int pos, int value) {
		buf[pos] = (byte)value;
	}

	/** Format a 16-bit value.
	 * @param buf Buffer to store formatted value.
	 * @param pos Starting position in buffer.
	 * @param value Value to store in buffer.
	 * @return Next position in buffer. */
	static protected int format16(byte[] buf, int pos, int value) {
		buf[pos + 0] = (byte)((value >> 8) & 0xFF);
		buf[pos + 1] = (byte)((value >> 0) & 0xFF);
		return pos + 2;
	}

	/** Format a 16-bit value (little-endian).
	 * @param buf Buffer to store formatted value.
	 * @param pos Starting position in buffer.
	 * @param value Value to store in buffer.
	 * @return Next position in buffer. */
	static protected int format16le(byte[] buf, int pos, int value) {
		buf[pos + 0] = (byte)((value >> 0) & 0xFF);
		buf[pos + 1] = (byte)((value >> 8) & 0xFF);
		return pos + 2;
	}

	/** Format a 32-bit value.
	 * @param buf Buffer to store formatted value.
	 * @param pos Starting position in buffer.
	 * @param value Value to store in buffer.
	 * @return Next position in buffer. */
	static protected int format32(byte[] buf, int pos, int value) {
		buf[pos + 0] = (byte)((value >> 24) & 0xFF);
		buf[pos + 1] = (byte)((value >> 16) & 0xFF);
		buf[pos + 2] = (byte)((value >> 8) & 0xFF);
		buf[pos + 3] = (byte)((value >> 0) & 0xFF);
		return pos + 4;
	}

	/** Format a 32-bit value (little-endian).
	 * @param buf Buffer to store formatted value.
	 * @param pos Starting position in buffer.
	 * @param value Value to store in buffer.
	 * @return Next position in buffer. */
	static protected int format32le(byte[] buf, int pos, int value) {
		buf[pos + 0] = (byte)((value >> 0) & 0xFF);
		buf[pos + 1] = (byte)((value >> 8) & 0xFF);
		buf[pos + 2] = (byte)((value >> 16) & 0xFF);
		buf[pos + 3] = (byte)((value >> 24) & 0xFF);
		return pos + 4;
	}

	/** Format a 2-digit BCD value.
	 * @param buf Buffer to store formatted value.
	 * @param pos Starting position in buffer.
	 * @param v Binary value to convert to BCD. */
	static protected void formatBCD2(byte[] buf, int pos, int v)
		throws IOException
	{
		if (v < 0 || v > 99)
			throw new IOException("INVALID BCD.2: " + v);
		buf[pos] = (byte)((bcd2(v) << 4) | bcd1(v));
	}

	/** Format a 4-digit BCD value.
	 * @param buf Buffer to store formatted value.
	 * @param pos Starting position in buffer.
	 * @param v Binary value to convert to BCD. */
	static protected void formatBCD4(byte[] buf, int pos, int v)
		throws IOException
	{
		if (v < 0 || v > 9999)
			throw new IOException("INVALID BCD.4: " + v);
		buf[pos + 0] = (byte)((bcd4(v) << 4) | bcd3(v));
		buf[pos + 1] = (byte)((bcd2(v) << 4) | bcd1(v));
	}

	/** Get the first BCD digit (on the right) */
	static private int bcd1(int v) {
		return v % 10;
	}

	/** Get the second BCD digit (from the right) */
	static private int bcd2(int v) {
		return (v / 10) % 10;
	}

	/** Get the third BCD digit (from the right) */
	static private int bcd3(int v) {
		return (v / 100) % 10;
	}

	/** Get the fourth BCD digit (from the right) */
	static private int bcd4(int v) {
		return (v / 1000) % 10;
	}

	/** Parse an 8-bit value.
	 * @param buf Buffer to parse.
	 * @param pos Starting position in buffer.
	 * @return Parsed value. */
	static protected int parse8(byte[] buf, int pos) {
		return buf[pos] & 0xFF;
	}

	/** Parse a 16-bit value.
	 * @param buf Buffer to parse.
	 * @param pos Starting position in buffer.
	 * @return Parsed value. */
	static protected int parse16(byte[] buf, int pos) {
		int hi = buf[pos + 0] & 0xFF;
		int lo = buf[pos + 1] & 0xFF;
		return (hi << 8) | lo;
	}

	/** Parse a 16-bit value (little-endian).
	 * @param buf Buffer to parse.
	 * @param pos Starting position in buffer.
	 * @return Parsed value. */
	static protected int parse16le(byte[] buf, int pos) {
		int lo = buf[pos + 0] & 0xFF;
		int hi = buf[pos + 1] & 0xFF;
		return (hi << 8) | lo;
	}

	/** Parse a 32-bit value.
	 * @param buf Buffer to parse.
	 * @param pos Starting position in buffer.
	 * @return Parsed value. */
	static protected int parse32(byte[] buf, int pos) {
		int b3 = buf[pos + 0] & 0xFF;
		int b2 = buf[pos + 1] & 0xFF;
		int b1 = buf[pos + 2] & 0xFF;
		int b0 = buf[pos + 3] & 0xFF;
		return (b3 << 24) | (b2 << 16) | (b1 << 8) | b0;
	}

	/** Parse a 32-bit value (little-endian).
	 * @param buf Buffer to parse.
	 * @param pos Starting position in buffer.
	 * @return Parsed value. */
	static protected int parse32le(byte[] buf, int pos) {
		int b0 = buf[pos + 0] & 0xFF;
		int b1 = buf[pos + 1] & 0xFF;
		int b2 = buf[pos + 2] & 0xFF;
		int b3 = buf[pos + 3] & 0xFF;
		return (b3 << 24) | (b2 << 16) | (b1 << 8) | b0;
	}

	/** Parse a 2-digit BCD value.
	 * @param buf Buffer to parse.
	 * @param pos Starting position in buffer.
	 * @return Parsed value. */
	static protected int parseBCD2(byte[] buf, int pos)
		throws ParsingException
	{
		int bcd = buf[pos];
		int d1 = (bcd >> 0) & 0x0F;
		int d2 = (bcd >> 4) & 0x0F;
		if (d1 < 10 && d2 < 10)
			return d2 * 10 + d1;
		else
			throw new ParsingException("INVALID BCD.2: " + bcd);
	}

	/** Parse a 4-digit BCD value.
	 * @param buf Buffer to parse.
	 * @param pos Starting position in buffer.
	 * @return Parsed value. */
	static protected int parseBCD4(byte[] buf, int pos)
		throws ParsingException
	{
		int bcd0 = buf[pos + 0];
		int bcd1 = buf[pos + 1];
		int d1 = (bcd1 >> 0) & 0x0F;
		int d2 = (bcd1 >> 4) & 0x0F;
		int d3 = (bcd0 >> 0) & 0x0F;
		int d4 = (bcd0 >> 4) & 0x0F;
		if (d1 < 10 && d2 < 10 && d3 < 10 && d4 < 10)
			return (d4 * 1000) + (d3 * 100) + (d2 * 10) + d1;
		else
			throw new ParsingException("INVALID BCD.4: " + bcd0 +
				":" + bcd1);
	}

	/** Get the path for a property */
	public String getPath() {
		return "";
	}

	/** Encode a QUERY request */
	public void encodeQuery(ControllerImpl c, OutputStream os)
		throws IOException
	{
		throw new ProtocolException("QUERY not supported");
	}

	/** Decode a QUERY response */
	public void decodeQuery(ControllerImpl c, InputStream is)
		throws IOException
	{
		throw new ProtocolException("QUERY not supported");
	}

	/** Encode a STORE request */
	public void encodeStore(ControllerImpl c, OutputStream os)
		throws IOException
	{
		throw new ProtocolException("STORE not supported");
	}

	/** Decode a STORE response */
	public void decodeStore(ControllerImpl c, InputStream is)
		throws IOException
	{
		throw new ProtocolException("STORE not supported");
	}

	/** Receive a response.
	 * @param is Input stream to read.
	 * @param n_bytes Number of bytes to receive.
	 * @return Array of bytes received.
	 * @throws IOException On any errors receiving response. */
	protected final byte[] recvResponse(InputStream is, int n_bytes)
		throws IOException
	{
		byte[] buf = new byte[n_bytes];
		int n_tries = 0;
		int n_rcv = 0;
		while (n_rcv < n_bytes) {
			if (n_tries > MAX_TRIES)
				throw new ParsingException("TOO MANY TRIES");
			int b = is.read(buf, n_rcv, n_bytes - n_rcv);
			if (b <= 0)
				throw new EOFException("END OF STREAM");
			n_rcv += b;
			n_tries++;
		}
		return buf;
	}
}
