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
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Time / Date stamp property.
 *
 * @author Douglas Lau
 */
public class TimeDateProp extends E6Property {

	/** System information command */
	static private final Command CMD =new Command(CommandGroup.SYSTEM_INFO);

	/** Store command code */
	static private final int STORE = 0x0003;

	/** Query command code */
	static private final int QUERY = 0x0004;

	/** Time / date stamp */
	private long stamp = TimeSteward.currentTimeMillis();

	/** Get the command */
	@Override
	public Command command() {
		return CMD;
	}

	/** Check if stamp is near current time */
	public boolean isNear(long dur) {
		long now = TimeSteward.currentTimeMillis();
		return (now - dur) < stamp && (now + dur) > stamp;
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
		stamp = parseTimeDate(d, 4);
	}

	/** Get the store packet data */
	@Override
	public byte[] storeData() {
		Calendar cal = getUTCCalendar();
		cal.setTimeInMillis(stamp);
		byte[] d = new byte[9];
		format16(d, 0, STORE);
		format8(d, 2, cal.get(Calendar.HOUR_OF_DAY));
		format8(d, 3, cal.get(Calendar.MINUTE));
		format8(d, 4, cal.get(Calendar.SECOND));
		format8(d, 5, cal.get(Calendar.MILLISECOND) / 10);
		format8(d, 6, cal.get(Calendar.MONTH) + 1);
		format8(d, 7, cal.get(Calendar.DATE));
		format8(d, 8, cal.get(Calendar.YEAR) - 2000);
		return d;
	}

	/** Parse a received store packet */
	@Override
	public void parseStore(byte[] d) throws IOException {
		if (d.length != 4)
			throw new ParsingException("DATA LEN: " + d.length);
		if (parse16(d, 2) != STORE)
			throw new ParsingException("SUB CMD");
	}

	/** Get a string representation */
	@Override
	public String toString() {
		return "time/date: " + new Date(stamp);
	}
}
