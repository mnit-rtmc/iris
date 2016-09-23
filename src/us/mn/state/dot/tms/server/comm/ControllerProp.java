/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016  Minnesota Department of Transportation
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

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * A controller property can be queried from or stored to a controller.
 *
 * @author Douglas Lau
 */
abstract public class ControllerProp {

	/** Format an 8-bit value.
	 * @param buf Buffer to store formatted value.
	 * @param value Value to store in buffer. */
	static protected void format8(ByteBuffer buf, int value) {
		buf.put((byte) value);
	}

	/** Format a 16-bit value.
	 * @param buf Buffer to store formatted value.
	 * @param value Value to store in buffer. */
	static protected void format16(ByteBuffer buf, int value) {
		buf.put((byte) ((value >> 8) & 0xFF));
		buf.put((byte) ((value >> 0) & 0xFF));
	}

	/** Format a 16-bit value (little-endian).
	 * @param buf Buffer to store formatted value.
	 * @param value Value to store in buffer. */
	static protected void format16le(ByteBuffer buf, int value) {
		buf.put((byte) ((value >> 0) & 0xFF));
		buf.put((byte) ((value >> 8) & 0xFF));
	}

	/** Format a 32-bit value.
	 * @param buf Buffer to store formatted value.
	 * @param value Value to store in buffer. */
	static protected void format32(ByteBuffer buf, int value) {
		buf.put((byte) ((value >> 24) & 0xFF));
		buf.put((byte) ((value >> 16) & 0xFF));
		buf.put((byte) ((value >>  8) & 0xFF));
		buf.put((byte) ((value >>  0) & 0xFF));
	}

	/** Format a 32-bit value (little-endian).
	 * @param buf Buffer to store formatted value.
	 * @param value Value to store in buffer. */
	static protected void format32le(ByteBuffer buf, int value) {
		buf.put((byte) ((value >>  0) & 0xFF));
		buf.put((byte) ((value >>  8) & 0xFF));
		buf.put((byte) ((value >> 16) & 0xFF));
		buf.put((byte) ((value >> 24) & 0xFF));
	}

	/** Format a 2-digit BCD value.
	 * @param buf Buffer to store formatted value.
	 * @param v Binary value to convert to BCD. */
	static protected void formatBCD2(ByteBuffer buf, int v)
		throws IOException
	{
		if (v < 0 || v > 99)
			throw new IOException("INVALID BCD.2: " + v);
		buf.put((byte) ((bcd2(v) << 4) | bcd1(v)));
	}

	/** Format a 4-digit BCD value.
	 * @param buf Buffer to store formatted value.
	 * @param v Binary value to convert to BCD. */
	static protected void formatBCD4(ByteBuffer buf, int v)
		throws IOException
	{
		if (v < 0 || v > 9999)
			throw new IOException("INVALID BCD.4: " + v);
		buf.put((byte) ((bcd4(v) << 4) | bcd3(v)));
		buf.put((byte) ((bcd2(v) << 4) | bcd1(v)));
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
	 * @return Parsed value. */
	static protected int parse8(ByteBuffer buf) {
		return buf.get() & 0xFF;
	}

	/** Parse a 16-bit value.
	 * @param buf Buffer to parse.
	 * @return Parsed value. */
	static protected int parse16(ByteBuffer buf) {
		int hi = parse8(buf);
		int lo = parse8(buf);
		return (hi << 8) | lo;
	}

	/** Parse a 16-bit value (little-endian).
	 * @param buf Buffer to parse.
	 * @return Parsed value. */
	static protected int parse16le(ByteBuffer buf) {
		int lo = parse8(buf);
		int hi = parse8(buf);
		return (hi << 8) | lo;
	}

	/** Parse a 32-bit value.
	 * @param buf Buffer to parse.
	 * @return Parsed value. */
	static protected int parse32(ByteBuffer buf) {
		int b3 = parse8(buf);
		int b2 = parse8(buf);
		int b1 = parse8(buf);
		int b0 = parse8(buf);
		return (b3 << 24) | (b2 << 16) | (b1 << 8) | b0;
	}

	/** Parse a 32-bit value (little-endian).
	 * @param buf Buffer to parse.
	 * @return Parsed value. */
	static protected int parse32le(ByteBuffer buf) {
		int b0 = parse8(buf);
		int b1 = parse8(buf);
		int b2 = parse8(buf);
		int b3 = parse8(buf);
		return (b3 << 24) | (b2 << 16) | (b1 << 8) | b0;
	}

	/** Parse a 2-digit BCD value.
	 * @param buf Buffer to parse.
	 * @return Parsed value. */
	static protected int parseBCD2(ByteBuffer buf) throws ParsingException {
		int bcd = parse8(buf);
		int d1 = (bcd >> 0) & 0x0F;
		int d2 = (bcd >> 4) & 0x0F;
		if (d1 < 10 && d2 < 10)
			return d2 * 10 + d1;
		else
			throw new ParsingException("INVALID BCD.2: " + bcd);
	}

	/** Parse a 4-digit BCD value.
	 * @param buf Buffer to parse.
	 * @return Parsed value. */
	static protected int parseBCD4(ByteBuffer buf) throws ParsingException {
		int bcd0 = parse8(buf);
		int bcd1 = parse8(buf);
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

	/** Encode a QUERY request */
	public void encodeQuery(Operation op, ByteBuffer tx_buf)
		throws IOException
	{
		throw new ProtocolException("QUERY not supported");
	}

	/** Decode a QUERY response */
	public void decodeQuery(Operation op, ByteBuffer rx_buf)
		throws IOException
	{
		throw new ProtocolException("QUERY not supported");
	}

	/** Encode a STORE request */
	public void encodeStore(Operation op, ByteBuffer tx_buf)
		throws IOException
	{
		throw new ProtocolException("STORE not supported");
	}

	/** Decode a STORE response */
	public void decodeStore(Operation op, ByteBuffer rx_buf)
		throws IOException
	{
		throw new ProtocolException("STORE not supported");
	}
}
