/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2016  Minnesota Department of Transportation
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

import java.io.EOFException;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.ChecksumException;
import us.mn.state.dot.tms.server.comm.ControllerProperty;
import us.mn.state.dot.tms.server.comm.CRC;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * DR500 property.
 *
 * @author Douglas Lau
 */
abstract public class DR500Property extends ControllerProperty {

	/** Charset name for ASCII */
	static protected final String ASCII = "US-ASCII";

	/** Maximum number of tries when reading a response */
	static private final int MAX_TRIES = 5;

	/** Length of receive buffer */
	static private final int RCV_LEN = 4096;

	/** Mask for escape marking bytes */
	static private final int MARK_MASK = 0x0F;

	/** Escape code for begin/end marking bytes */
	static private final int MARK_ESC = 0xFA;

	/** Begin packet marking byte */
	static private final int MARK_BEGIN = 0xF3;

	/** End packet marking byte */
	static private final int MARK_END = 0xFC;

	/** CRC calculator */
	static private final CRC crc = new CRC(16, 0x1021, 0x0000, false);

	/** Format a raw byte value.
	 * @param buf Buffer to store formatted value.
	 * @param pos Starting position in buffer.
	 * @param value Value to store in buffer.
	 * @return Next position in buffer. */
	static private final int formatRaw(byte[] buf, int pos, int value) {
		buf[pos] = (byte)value;
		return pos + 1;
	}

	/** Format a byte value.
	 * @param buf Buffer to store formatted value.
	 * @param pos Starting position in buffer.
	 * @param value Value to store in buffer.
	 * @return Next position in buffer. */
	static protected int formatByte(byte[] buf, int pos, int value) {
		switch (value) {
		case MARK_ESC:
		case MARK_BEGIN:
		case MARK_END:
			pos = formatRaw(buf, pos, MARK_ESC);
			return formatRaw(buf, pos, value & MARK_MASK);
		default:
			return formatRaw(buf, pos, value);
		}
	}

	/** Format header of message packet.
	 * @param buf Buffer to store formatted packet.
	 * @return Next position in buffer. */
	static protected int formatHead(byte[] buf) {
		return formatRaw(buf, 0, MARK_BEGIN);
	}

	/** Format body of message packet.
	 * @param buf Buffer to store formatted packet.
	 * @param pos Starting position in buffer.
	 * @param body Body of packet.
	 * @return Next position in buffer. */
	static protected int formatBody(byte[] buf, int pos, byte[] body) {
		for (byte b: body)
			pos = formatByte(buf, pos, b);
		return pos;
	}

	/** Format a 16-bit value.
	 * @param buf Buffer to store formatted value.
	 * @param pos Starting position in buffer.
	 * @param value Value to store in buffer.
	 * @return Next position in buffer. */
	static protected int format16(byte[] buf, int pos, int value) {
		pos = formatByte(buf, pos, value & 0xFF);
		return formatByte(buf, pos, (value >> 8) & 0xFF);
	}

	/** Format tail of message packet.
	 * @param buf Buffer to store formatted packet.
	 * @param pos Starting position in buffer.
	 * @param c CRC of packet.
	 * @return Total length of packet. */
	static protected int formatTail(byte[] buf, int pos, int c) {
		pos = format16(buf, pos, c);
		return formatRaw(buf, pos, MARK_END);
	}

	/** Encode a request */
	protected void encodeRequest(OutputStream os, byte[] body)
		throws IOException
	{
		byte[] buf = new byte[128];
		int pos = formatHead(buf);
		pos = formatBody(buf, pos, body);
		pos = formatTail(buf, pos, crc.calculate(body));
		os.write(buf, 0, pos);
	}

	/** Decode packet header */
	private void decodeHead(InputStream is) throws IOException {
		for (int i = 0; i < 100; i++) {
			int b = is.read();
			if (b < 0)
				throw new EOFException("END OF STREAM");
			if (MARK_BEGIN == b)
				return;
		}
		throw new ParsingException("RANDOM LINE NOISE");
	}

	/** Buffer for decoding responses */
	private final byte[] rcv = new byte[RCV_LEN];

	/** Decode the body of a response packet */
	private Response decodeBody(InputStream is) throws IOException {
		int n_rcv = 0;
		int n_ex = rcv.length - n_rcv;
		for (int t = 0; (t < MAX_TRIES) && (n_ex > 0); t++) {
			int a = Math.max(Math.min(is.available(), n_ex), 1);
			int b = is.read(rcv, n_rcv, a);
			if (b <= 0)
				throw new EOFException("END OF STREAM");
			n_rcv += b;
			int n_end = endMarker(n_rcv);
			if (n_end >= 0) {
				int n_pkt = removeEscapes(n_end);
				int n_body = compareCRC(n_pkt);
				return buildResponse(n_body);
			}
			n_ex = rcv.length - n_rcv;
		}
		throw new ParsingException("TOO MANY TRIES");
	}

	/** Check for packet end marker.
	 * @param n_rcv Number of bytes received.
	 * @return Position of end marker, or -1 if not received. */
	private int endMarker(int n_rcv) {
		for (int i = 0; i < n_rcv; i++) {
			if (MARK_END == parse8(rcv, i))
				return i;
		}
		return -1;
	}

	/** Remove escape sequences from received packet.
	 * @param n_end Position of end marker.
	 * @return Number of bytes after removing escape sequences. */
	private int removeEscapes(int n_end) throws ParsingException {
		int j = 0;
		for (int i = 0; i < n_end; i++, j++) {
			switch (parse8(rcv, i)) {
			case MARK_ESC:
				i++;
				rcv[j] = (byte) parseEsc(parse8(rcv, i));
				break;
			case MARK_BEGIN:
			case MARK_END:
				throw new ParsingException("BAD MARK");
			default:
				rcv[j] = rcv[i];
				break;
			}
		}
		return j;
	}

	/** Parse an escape sequence */
	private int parseEsc(int b) throws ParsingException {
		switch (b) {
		case MARK_ESC & MARK_MASK:
			return MARK_ESC;
		case MARK_BEGIN & MARK_MASK:
			return MARK_BEGIN;
		case MARK_END & MARK_MASK:
			return MARK_END;
		default:
			throw new ParsingException("INVALID ESC");
		}
	}

	/** Compare the received packet CRC.
	 * @param n_pkt Number of bytes in packet.
	 * @return Number of bytes in body (at least 1).
	 * @throws ParsingException if CRC does not match. */
	private int compareCRC(int n_pkt) throws ParsingException {
		int n_body = n_pkt - 2;
		if (n_body < 1)
			throw new ParsingException("PACKET TOO SMALL");
		int crc1 = parse16le(rcv, n_body);
		int crc2 = crc.calculate(rcv, n_body);
		if (crc1 == crc2)
			return n_body;
		else
			throw new ChecksumException("CRC: " + crc2);
	}

	/** Build a response object.
	 * @param n_body Number of bytes in body (including msg code).
	 * @return Response object */
	private Response buildResponse(int n_body) throws ParsingException {
		MsgCode mc = MsgCode.fromCode(parse8(rcv, 0));
		if (mc != MsgCode.UNKNOWN) {
			byte[] body = new byte[n_body - 1];
			System.arraycopy(rcv, 1, body, 0, body.length);
			return new Response(mc, body);
		} else
			throw new ParsingException("MSG CODE:" + mc);
	}

	/** Decode a response */
	protected Response decodeResponse(InputStream is) throws IOException {
		decodeHead(is);
		return decodeBody(is);
	}

	/** Parse a STATUS response message */
	protected int parseStatus(Response resp) throws ParsingException {
		checkMsgCode(resp, MsgCode.STATUS_RESP);
		if (resp.body.length != 2)
			throw new ParsingException("STATUS LEN");
		int status = parse16le(resp.body, 0);
		if (status < 0)
			throw new ParsingException("STATUS:" + status);
		return status;
	}

	/** Check a response message code */
	protected void checkMsgCode(Response resp, MsgCode mc)
		throws ParsingException
	{
		if (resp.msg_code != mc)
			throw new ParsingException("MSG CODE:" + resp.msg_code);
	}
}
