/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2012  Minnesota Department of Transportation
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
import java.util.Calendar;
import java.util.TimeZone;
import us.mn.state.dot.tms.server.comm.ChecksumException;
import us.mn.state.dot.tms.server.comm.ControllerException;
import us.mn.state.dot.tms.server.comm.ControllerProperty;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * SS125 property
 * FIXME: convert to use ControllerProperty encode/decode methods.
 *
 * @author Douglas Lau
 */
abstract public class SS125Property extends ControllerProperty {

	/** Charset name for ASCII */
	static private final String ASCII = "US-ASCII";

	/** Byte offsets from beginning of packet */
	static protected final int OFF_DEST_SUB_ID = 2;
	static protected final int OFF_DEST_ID = 3;
	static protected final int OFF_SOURCE_SUB_ID = 5;
	static protected final int OFF_SOURCE_ID = 6;
	static protected final int OFF_SEQUENCE = 8;
	static protected final int OFF_BODY_SIZE = 9;

	/** Maximum number of octets in message body */
	static private final int MAX_BODY_OCTETS = 244;

	/** Sub ID must be configured to zero */
	static private final int SUB_ID = 0;

	/** Message sub ID "don't care" */
	static protected final byte SUB_ID_DONT_CARE = 0;

	/** Message read request */
	static protected final byte REQ_READ = 0;

	/** Message write request */
	static protected final byte REQ_WRITE = 1;

	/** Polynomial for CRC */
	static private final int POLYNOMIAL = 0x1c;

	/** Look-up table for CRC calculations */
	static private final byte[] CRC_TABLE = new byte[256];

	/** Initialize the lookup table */
	static {
		for(int i = 0; i < CRC_TABLE.length; i++) {
			int v = i;
		        for(int j = 0; j < 8; j++) {
				if((v & 0x80) != 0)
					v = (v << 1) ^ POLYNOMIAL;
				else
					v = v << 1;
			}
			CRC_TABLE[i] = (byte)v;
		}
	}

	/** Calculate the CRC-8 of a buffer.
	 * @param buffer Buffer to be checked.
	 * @return CRC-8 of the buffer. */
	static public byte crc8(byte[] buffer) {
		int crc = 0;
		for(byte b: buffer)
			crc = CRC_TABLE[(crc ^ b) & 0xFF];
		return (byte)crc;
	}

	/** Format a boolean value.
	 * @param buf Buffer to store formatted value.
	 * @param pos Starting position in buffer.
	 * @param value Value to store in buffer. */
	static protected void formatBool(byte[] buf, int pos, boolean value) {
		buf[pos] = value ? (byte)1 : (byte)0;
	}

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
	 * @param value Value to store in buffer. */
	static protected void format16(byte[] buf, int pos, int value) {
		buf[pos] = (byte)((value >> 8) & 0xFF);
		buf[pos + 1] = (byte)(value & 0xFF);
	}

	/** Format a 16-bit fixed-point value.
	 * @param buf Buffer to store formatted value.
	 * @param pos Starting position in buffer.
	 * @param value Value to store in buffer. */
	static protected void format16Fixed(byte[] buf, int pos, float value) {
		int intg = (int)value;
		int frac = (int)(256 * (value - intg));
		buf[pos] = (byte)intg;
		buf[pos + 1] = (byte)frac;
	}

	/** Format a 24-bit value.
	 * @param buf Buffer to store formatted value.
	 * @param pos Starting position in buffer.
	 * @param value Value to store in buffer. */
	static protected void format24(byte[] buf, int pos, int value) {
		buf[pos] = (byte)((value >> 16) & 0xFF);
		buf[pos + 1] = (byte)((value >> 8) & 0xFF);
		buf[pos + 2] = (byte)(value & 0xFF);
	}

	/** Format a 32-bit value.
	 * @param buf Buffer to store formatted value.
	 * @param pos Starting position in buffer.
	 * @param value Value to store in buffer. */
	static protected void format32(byte[] buf, int pos, int value) {
		buf[pos] = (byte)((value >> 24) & 0xFF);
		buf[pos + 1] = (byte)((value >> 16) & 0xFF);
		buf[pos + 2] = (byte)((value >> 8) & 0xFF);
		buf[pos + 3] = (byte)(value & 0xFF);
	}

	/** Format a string to a byte array.
	 * @param buf Buffer to store formatted value.
	 * @param pos Starting position in buffer.
	 * @param len Length of field in buffer.
	 * @param value Value to store in buffer. */
	static protected void formatString(byte[] buf, int pos,
		int len, String value) throws IOException
	{
		byte[] src = value.getBytes(ASCII);
		int vlen = Math.min(len, src.length);
		System.arraycopy(src, 0, buf, pos, vlen);
		for(int i = vlen; i < len; i++)
			buf[pos + i] = 0;
	}

	/** Parse a string value */
	static protected String parseString(byte[] body, int pos, int len)
		throws IOException
	{
		return new String(body, pos, len, ASCII).trim();
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
	static protected int parse8(byte[] body, int pos) {
		return body[pos] & 0xFF;
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

	/** Parse a 24-bit value */
	static protected int parse24(byte[] body, int pos) {
		int b2 = body[pos] & 0xFF;
		int b1 = body[pos + 1] & 0xFF;
		int b0 = body[pos + 2] & 0xFF;
		return (b2 << 16) | (b1 << 8) | b0;
	}

	/** Parse a 24-bit fixed-point value */
	static protected Float parse24Fixed(byte[] body, int pos) {
		int flag = (body[pos] >> 7) & 0x01;
		if(flag == 0)
			return null;
		int b1 = body[pos] & 0x7F;
		int b0 = body[pos + 1] & 0xFF;
		short sint = (short)((b1 << 8) | b0);
		int intg = (short)(sint << 1) >> 1;	// extend sign
		int frac = body[pos + 2] & 0xFF;
		int fr = intg >= 0 ? frac : -frac;
		return intg + fr / 256f;
	}

	/** Parse a 32-bit value */
	static protected int parse32(byte[] body, int pos) {
		int b3 = body[pos] & 0xFF;
		int b2 = body[pos + 1] & 0xFF;
		int b1 = body[pos + 2] & 0xFF;
		int b0 = body[pos + 3] & 0xFF;
		return (b3 << 24) | (b2 << 16) | (b1 << 8) | b0;
	}

	/** Parse a date / time stamp */
	static protected long parseDate(byte[] body, int pos) {
		int date = parse32(body, pos);
		int time = parse32(body, pos + 4);
		int year = (date >> 9) & 0x0FFF;
		int month = (date >> 5) & 0x0F;
		int day = date & 0x1F;
		int hour = (time >> 22) & 0x1F;
		int minute = (time >> 16) & 0x3F;
		int second = (time >> 10) & 0x3F;
		int ms = time & 0x3FF;
		TimeZone utc = TimeZone.getTimeZone("GMT");
		Calendar cal = Calendar.getInstance(utc);
		cal.set(year, month - 1, day, hour, minute, second);
		cal.set(Calendar.MILLISECOND, ms);
		return cal.getTimeInMillis();
	}

	/** Parse the result code.
	 * @param rbody Received response body.
	 * @throws ParsingException On any errors parsing result code.
	 * @throws ControllerException If result indicates an error. */
	static void parseResult(byte[] rbody) throws IOException {
		if(rbody.length != 5)
			throw new ParsingException("RESULT LENGTH");
		int result = parse16(rbody, 3);
		if(result > 0)
			throw new ControllerException(lookupResult(result));
	}

	/** Lookup a result code */
	static protected String lookupResult(int result) {
		switch(result) {
		case 1:
			return "PAYLOAD SIZE";
		case 2:
			return "BODY CRC";
		case 3:
			return "READ ONLY";
		case 15:
			return "INTERVAL NONEXISTANT";
		case 16:
			return "LANE NONEXISTANT";
		case 17:
			return "FLASH BUSY (A)";
		case 19:
			return "INVALID PUSH STATE";
		case 20:
			return "ERROR SETTING RTC";
		case 21:
			return "RTC SYNC ERROR";
		case 22:
			return "FLASH ERASE ERROR";
		case 23:
			return "FLASH BUSY (B)";
		case 24:
			return "INVALID PROTOCOL STATE";
		case 25:
			return "TOO MANY APPROACHES";
		case 26:
			return "TOO MANY LANES";
		case 30:
			return "AUTOMATIC LANE";
		case 31:
			return "WRONG LANE COUNT";
		case 33:
			return "INVALID BAUD RATE";
		default:
			return "UNKNOWN ERROR";
		}
	}

	/** Source sub ID */
	private final int source_sub_id = SUB_ID;

	/** Source ID */
	private final int source_id = 0;

	/** Destination sub ID */
	private final int dest_sub_id = SUB_ID;

	/** Packet sequence number (FIXME?) */
	private byte seq_num = 0;

	/** Format a request header.
	 * @param body Body of message to send.
	 * @param drop Destination ID (drop address).
	 * @return Header appropriate for polling message. */
	protected byte[] formatHeader(byte[] body, int drop) {
		assert body.length <= MAX_BODY_OCTETS;
		byte[] header = new byte[10];
		header[0] = 'Z';			// Sentinel
		header[1] = '1';			// Protocol version
		format8(header, OFF_DEST_SUB_ID, dest_sub_id);
		format16(header, OFF_DEST_ID, drop);
		format8(header, OFF_SOURCE_SUB_ID, source_sub_id);
		format16(header, OFF_SOURCE_ID, source_id);
		format8(header, OFF_SEQUENCE, seq_num);
		format8(header, OFF_BODY_SIZE, body.length);
		return header;
	}

	/** Format the body of a GET request */
	abstract byte[] formatBodyGet() throws IOException;

	/** Format the body of a SET request */
	abstract byte[] formatBodySet() throws IOException;

	/** Parse the payload of a GET response */
	abstract void parsePayload(byte[] body) throws IOException;

	/** Flag to indicate the request is complete */
	protected boolean complete = false;

	/** Test if the request is complete */
	public boolean isComplete() {
		return complete;
	}

	/** Set the complete flag */
	protected void setComplete(boolean c) {
		complete = c;
	}

	/** Test the if property has some data */
	public boolean hasData() {
		return false;
	}

	/** Delay before checking for response */
	void delayResponse() {
		// Only needed if flash is being reprogrammed
		// see FlashConfigProperty
	}

	/** Parse a message response header.
	 * @param rhead Received response header.
	 * @param crc Received header crc.
	 * @param drop Destination ID (drop address).
	 * @return Number of bytes in response body.
	 * @throws ParsingException On any errors parsing response header. */
	public int parseHead(byte[] rhead, byte crc, int drop)
		throws ParsingException
	{
		assert rhead.length == 10;
		if(crc != SS125Property.crc8(rhead))
			throw new ChecksumException("HEADER");
		if(rhead[0] != 'Z')
			throw new ParsingException("SENTINEL");
		if(rhead[1] != '1')
			throw new ParsingException("VERSION");
		if(parse8(rhead, OFF_DEST_SUB_ID) != source_sub_id)
			throw new ParsingException("DEST SUB ID");
		if(parse16(rhead, OFF_DEST_ID) != source_id)
			throw new ParsingException("DEST ID");
		if(parse8(rhead, OFF_SOURCE_SUB_ID) != dest_sub_id)
			throw new ParsingException("SRC SUB ID");
		if(parse16(rhead, OFF_SOURCE_ID) != drop)
			throw new ParsingException("SRC ID");
		if(parse8(rhead, OFF_SEQUENCE) != seq_num + 1)
			throw new ParsingException("SEQUENCE");
		int n_body = parse8(rhead, OFF_BODY_SIZE);
		if(n_body < 3 || n_body > MAX_BODY_OCTETS)
			throw new ParsingException("BODY SIZE");
		return n_body;
	}
}
