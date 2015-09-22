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
import java.util.Date;
import java.util.TimeZone;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Time / Date stamp property.
 *
 * @author Douglas Lau
 */
public class TimeDateProp extends E6Property {

	/** Get a UTC calendar */
	static private Calendar getUTCCalendar() {
		TimeZone UTC = TimeZone.getTimeZone("UTC");
		return Calendar.getInstance(UTC);
	}

	/** System information command */
	static private final Command CMD =new Command(CommandGroup.SYSTEM_INFO);

	/** Store command code */
	static private final int STORE = 0x0003;

	/** Query command code */
	static private final int QUERY = 0x0004;

	/** Time / date stamp */
	private long stamp = 0;

	/** Get the command */
	@Override
	public Command command() {
		return CMD;
	}

	/** Get the query packet data */
	@Override
	public byte[] queryData() {
		byte[] d = new byte[2];
		format16(d, 0, QUERY);
		return d;
	}

	/** Parse a received query packet */
	@Override
	public void parseQuery(byte[] d) throws IOException {
		if (d.length != 11)
			throw new ParsingException("DATA LEN: " + d.length);
		if (parse16(d, 2) != QUERY)
			throw new ParsingException("SUB CMD");
		int year = 2000 + (d[10] & 0xFF);
		int month = d[8] - 1;
		if (month < Calendar.JANUARY || month > Calendar.DECEMBER)
			throw new ParsingException("BAD MONTH");
		int date = d[9];
		if (date < 1 || date > 32)
			throw new ParsingException("BAD DATE");
		int hour = d[4];
		if (hour < 0 || hour > 23)
			throw new ParsingException("BAD HOUR");
		int min = d[5];
		if (min < 0 || min > 59)
			throw new ParsingException("BAD MINUTE");
		int sec = d[6];
		if (sec < 0 || sec > 59)
			throw new ParsingException("BAD SECOND");
		int ms = d[7] * 10;
		if (ms < 0 || ms > 999)
			throw new ParsingException("BAD MS");
		Calendar cal = getUTCCalendar();
		cal.clear();
		cal.set(Calendar.MILLISECOND, ms);
		cal.set(year, month, date, hour, min, sec);
		stamp = cal.getTimeInMillis();
	}

	/** Get a string representation */
	@Override
	public String toString() {
		return "time/date: " + new Date(stamp);
	}
}
