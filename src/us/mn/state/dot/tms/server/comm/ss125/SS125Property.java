/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2010  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.server.comm.ControllerException;
import us.mn.state.dot.tms.server.comm.ControllerProperty;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * SS125 property
 *
 * @author Douglas Lau
 */
abstract public class SS125Property implements ControllerProperty {

	/** Charset encoding for character strings */
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

	/** Format a 24-bit value */
	static protected void format24(int value, byte[] body, int pos) {
		body[pos] = (byte)((value >> 16) & 0xFF);
		body[pos + 1] = (byte)((value >> 8) & 0xFF);
		body[pos + 2] = (byte)(value & 0xFF);
	}

	/** Format a 32-bit value */
	static protected void format32(int value, byte[] body, int pos) {
		body[pos] = (byte)((value >> 24) & 0xFF);
		body[pos + 1] = (byte)((value >> 16) & 0xFF);
		body[pos + 2] = (byte)((value >> 8) & 0xFF);
		body[pos + 3] = (byte)(value & 0xFF);
	}

	/** Format the body of a GET request */
	abstract byte[] formatBodyGet() throws IOException;

	/** Format the body of a SET request */
	abstract byte[] formatBodySet() throws IOException;

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
	protected long parseDate(byte[] body, int pos) {
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
	void parseResult(byte[] rbody) throws IOException {
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

	/** Delay before checking for response */
	void delayResponse() {
		// Only needed if flash is being reprogrammed
		// see FlashConfigProperty
	}
}
