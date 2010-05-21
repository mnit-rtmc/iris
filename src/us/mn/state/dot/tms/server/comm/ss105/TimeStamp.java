/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2010  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ss105;

import java.util.Date;
import java.util.Calendar;
import java.util.TimeZone;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * A SS105 time stamp is the number of seconds since the "epoch".  This can be
 * any point in time, but is conventionally the beginning of 1 January 2000.
 *
 * @author Douglas Lau
 */
public class TimeStamp {

	/** Milliseconds offset for the SS105 epoch (Y2K) */
	static protected final long EPOCH;
	static {
		TimeZone UTC = TimeZone.getTimeZone("GMT");
		Calendar c = Calendar.getInstance(UTC);
		c.clear();
		c.set(2000, 0, 1);
		EPOCH = c.getTimeInMillis();
	}

	/** Parse an SS105 timestamp */
	static public Date parse(String s) throws ParsingException {
		try {
			long seconds = Long.parseLong(s, 16);
			long ms = EPOCH + seconds * 1000;
			return new Date(ms);
		}
		catch(NumberFormatException e) {
			throw new ParsingException("INVALID TIME STAMP: " + s);
		}
	}

	/** Format an SS105 timestamp */
	static public int secondsSinceEpoch(Date d) {
		long ms = d.getTime() - EPOCH;
		if(ms > 0 && ms < Integer.MAX_VALUE * 1000L)
			return (int)(ms / 1000);
		else
			return 0;
	}
}
