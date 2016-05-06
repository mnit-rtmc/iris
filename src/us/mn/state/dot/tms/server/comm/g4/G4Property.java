/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012-2016  Minnesota Department of Transportation
 * Copyright (C) 2012  Iteris Inc.
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

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.TimeZone;
import us.mn.state.dot.tms.server.comm.ChecksumException;
import us.mn.state.dot.tms.server.comm.ControllerException;
import us.mn.state.dot.tms.server.comm.ControllerProperty;
import us.mn.state.dot.tms.server.comm.InvalidAddressException;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * A property which can be sent or received from a controller.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
abstract public class G4Property extends ControllerProperty {

	/** Frame sentinel value */
	static private final int SENTINEL = 0xFFAA;

	/** Byte offsets from beginning of frame */
	static private final int OFF_SENTINEL = 0;
	static private final int OFF_QUAL = 2;
	static private final int OFF_LENGTH = 3;
	static private final int OFF_SENSOR_ID = 4;
	static private final int OFF_DATA = 6;

	/** Minimum data length */
	static private final int MIN_DATA_LEN = 2;

	/** Calculate a checksum */
	static private int checksum(byte[] buf, int pos, int len) {
		int c = 0;
		for (int i = pos; i < pos + len; i++)
			c += buf[i] & 0xFF;
		return c;
	}

	/** Check if a sensor id is valid */
	static private boolean isAddressValid(int drop) {
		return drop >= 0 && drop < 65536;
	}

	/** Format a request frame */
	static protected final byte[] formatRequest(QualCode qual, int drop,
		byte[] data) throws IOException
	{
		if (!isAddressValid(drop))
			throw new InvalidAddressException(drop);
		byte[] req = new byte[OFF_DATA + data.length + 2];
		format16(req, OFF_SENTINEL, SENTINEL);
		format8(req, OFF_QUAL, qual.code);
		format8(req, OFF_LENGTH, data.length + 2);
		format16(req, OFF_SENSOR_ID, drop);
		System.arraycopy(data, 0, req, OFF_DATA, data.length);
		format16(req, OFF_DATA + data.length,
			checksum(req, OFF_SENSOR_ID, data.length + 2));
		return req;
	}

	/** Format time stamp */
	protected void formatStamp(byte[] data, int pos, long st)
		throws IOException
	{
		TimeZone utc = TimeZone.getTimeZone("GMT");
		Calendar cal = Calendar.getInstance(utc);
		cal.setTimeInMillis(st);
		formatBCD2(data, pos, cal.get(Calendar.SECOND));
		formatBCD2(data, pos + 1, cal.get(Calendar.MINUTE));
		formatBCD2(data, pos + 2, cal.get(Calendar.HOUR_OF_DAY));
		formatBCD2(data, pos + 3, cal.get(Calendar.DAY_OF_WEEK));
		formatBCD2(data, pos + 4, cal.get(Calendar.DATE));
		formatBCD2(data, pos + 5, cal.get(Calendar.MONTH) + 1);
		formatBCD2(data, pos + 6, cal.get(Calendar.YEAR) % 100);
	}

	/** Parse one frame.
	 * @param is Input stream to read from.
	 * @param drop Sensor ID (drop address). */
	protected void parseFrame(InputStream is, int drop) throws IOException {
		byte[] header = recvResponse(is, OFF_SENSOR_ID);
		if (parse16(header, OFF_SENTINEL) != SENTINEL)
			throw new ParsingException("INVALID SENTINEL");
		QualCode qual = QualCode.fromCode(parse8(header, OFF_QUAL));
		int length = parse8(header, OFF_LENGTH);
		if (length < MIN_DATA_LEN)
			throw new ParsingException("INVALID LENGTH: " + length);
		byte[] body = recvResponse(is, 2 + length); // 2 byte sensor ID
		int sid = parse16(body, 0);
		if (sid != drop)
			throw new ParsingException("INVALID ID");
		int cs = parse16(body, body.length - 2);
		if (cs != checksum(body, 0, body.length - 2))
			throw new ChecksumException(body);
		// Copy everything but sensor ID and checksum (4 bytes)
		byte[] data = new byte[body.length - 4];
		System.arraycopy(body, 2, data, 0, data.length);
		parseData(qual, data);
	}

	/** Parse the data from one frame.
	 * @param qual Qualifier code.
	 * @param data Data packet. */
	protected void parseData(QualCode qual, byte[] data) throws IOException{
		switch (qual) {
		case ACK:
			return;
		case NAK:
			throw new ControllerException("NAK");
		default:
			throw new ParsingException("UNEXPECTED QUAL: " + qual);
		}
	}

	/** Parse a time stamp */
	protected long parseStamp(byte[] data, int pos) throws ParsingException{
		int second = parseBCD2(data, pos);
		int minute = parseBCD2(data, pos + 1);
		int hour = parseBCD2(data, pos + 2);
		int weekday = parseBCD2(data, pos + 3);
		int day = parseBCD2(data, pos + 4);
		int month = parseBCD2(data, pos + 5);
		// NOTE: year is last 2 digits only.  Seriously.  In a protocol
		//       designed after Y2K.  Why couldn't you leave out
		//       weekday, since it contains no additional information?
		//       Or simply add one more byte, ffs!
		int year = 2000 + parseBCD2(data, pos + 6);
		TimeZone utc = TimeZone.getTimeZone("GMT");
		Calendar cal = Calendar.getInstance(utc);
		cal.set(year, month - 1, day, hour, minute, second);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTimeInMillis();
	}
}
