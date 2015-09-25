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

import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;
import us.mn.state.dot.tms.server.comm.ControllerProperty;
import us.mn.state.dot.tms.server.comm.ParsingException;
import us.mn.state.dot.tms.server.comm.ProtocolException;

/**
 * E6 property.
 *
 * @author Douglas Lau
 */
abstract public class E6Property extends ControllerProperty {

	/** Get a UTC calendar */
	static protected Calendar getUTCCalendar() {
		TimeZone UTC = TimeZone.getTimeZone("UTC");
		return Calendar.getInstance(UTC);
	}

	/** Get the command */
	abstract public Command command() throws IOException;

	/** Get the query packet data */
	public byte[] queryData() throws IOException {
		throw new ProtocolException("QUERY not supported");
	}

	/** Get the store packet data */
	public byte[] storeData() throws IOException {
		throw new ProtocolException("STORE not supported");
	}

	/** Parse a received query packet */
	public void parseQuery(byte[] data) throws IOException {
		throw new ProtocolException("QUERY not supported");
	}

	/** Parse a received store packet */
	public void parseStore(byte[] data) throws IOException {
		throw new ProtocolException("STORE not supported");
	}

	/** Parse a time/date */
	protected long parseTimeDate(byte[] d, int off)
		throws ParsingException
	{
		int hour = d[off];
		if (hour < 0 || hour > 23)
			throw new ParsingException("BAD HOUR: " + hour);
		int min = d[off + 1];
		if (min < 0 || min > 59)
			throw new ParsingException("BAD MINUTE: " + min);
		int sec = d[off + 2];
		if (sec < 0 || sec > 59)
			throw new ParsingException("BAD SECOND: " + sec);
		int ms = d[off + 3] * 10;
		if (ms < 0 || ms > 999)
			throw new ParsingException("BAD MS: " + ms);
		int month = d[off + 4] - 1;
		if (month < Calendar.JANUARY || month > Calendar.DECEMBER)
			throw new ParsingException("BAD MONTH: " + month);
		int date = d[off + 5];
		if (date < 1 || date > 32)
			throw new ParsingException("BAD DATE: " + date);
		int year = 2000 + (d[off + 6] & 0xFF);
		Calendar cal = getUTCCalendar();
		cal.clear();
		cal.set(Calendar.MILLISECOND, ms);
		cal.set(year, month, date, hour, min, sec);
		return cal.getTimeInMillis();
	}
}
