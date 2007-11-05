/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004  Minnesota Department of Transportation
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
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package us.mn.state.dot.tms.comm.smartsensor;

import java.util.Date;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Time Stamp
 *
 * @author Douglas Lau
 */
public class TimeStamp {

	/** Milliseconds offset for the SmartSensor epoch (Y2K) */
	static protected final long EPOCH;
	static {
		TimeZone UTC = TimeZone.getTimeZone("GMT");
		Calendar c = Calendar.getInstance(UTC);
		c.clear();
		c.set(2000, 0, 1);
		EPOCH = c.getTimeInMillis();
	}

	/** Parse a SmartSensor timestamp */
	static public Date parse(String s) {
		long seconds = Long.parseLong(s, 16);
		long ms = EPOCH + seconds * 1000;
		return new Date(ms);
	}

	/** Format a SmartSensor timestamp */
	static public int seconds(Date d) {
		long ms = d.getTime() - EPOCH;
		// FIXME: check for overflow?
		return (int)(ms / 1000);
	}
}
