/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012-2014  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.g4;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * RTC (real-time clock) property.
 *
 * @author Douglas Lau
 */
public class RTCProperty extends G4Property {

	/** Message packet byte offsets */
	static private final int OFF_RESET_DAY_OF_WEEK = 10;

	/** Encode a QUERY request */
	@Override
	public void encodeQuery(ControllerImpl c, OutputStream os)
		throws IOException
	{
		byte[] data = new byte[0];
		os.write(formatRequest(QualCode.RTC_QUERY, c.getDrop(), data));
	}

	/** Decode a QUERY response */
	@Override
	public void decodeQuery(ControllerImpl c, InputStream is)
		throws IOException
	{
		parseFrame(is, c.getDrop());
	}

	/** Parse the data from one frame.
	 * @param qual Qualifier code.
	 * @param data Data packet. */
	@Override
	protected void parseData(QualCode qual, byte[] data)
		throws IOException
	{
		switch(qual) {
		case RTC:
			parseRTC(data);
			break;
		default:
			super.parseData(qual, data);
		}
	}

	/** Encode a STORE request */
	@Override
	public void encodeStore(ControllerImpl c, OutputStream os)
		throws IOException
	{
		byte[] data = new byte[14];
		formatStamp(data, 0, stamp);
		formatStamp(data, 7, reset_stamp);
		data[OFF_RESET_DAY_OF_WEEK] = 0;
		os.write(formatRequest(QualCode.RTC, c.getDrop(), data));
	}

	/** Decode a STORE response */
	@Override
	public void decodeStore(ControllerImpl c, InputStream is)
		throws IOException
	{
		parseFrame(is, c.getDrop());
	}

	/** Current time stamp */
	private long stamp;

	/** Get the current time stamp */
	public long getStamp() {
		return stamp;
	}

	/** Set the current time stamp */
	public void setStamp(long st) {
		stamp = st;
		reset_stamp = st;
	}

	/** Time stamp of last reset */
	private long reset_stamp;

	/** Parse RTC data */
	private void parseRTC(byte[] data) throws ParsingException {
		if (data.length != 14)
			throw new ParsingException("INVALID RTC LENGTH");
		stamp = parseStamp(data, 0);
		reset_stamp = parseStamp(data, 7);
	}

	/** Get a string representation of the RTC property */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("time:");
		sb.append(new Date(stamp));
		sb.append(" reset:");
		sb.append(new Date(reset_stamp));
		return sb.toString();
	}
}
