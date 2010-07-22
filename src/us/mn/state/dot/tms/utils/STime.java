/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2010  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Time convenience methods.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public final class STime {

	/** constructor */
	private STime() {}

	/**
	 * given a java.util.Date, return the number of
	 * milliseconds UTC since Jan 1st 1970 00:00:00.
	 */
	static public long getUTCinMillis(java.util.Date d) {
		return d.getTime();
	}

	/** get current time in MS (UTC) since Jan 1st 1970 00:00:00. */
	static public long getCurTimeUTCinMillis() {
		Date d = new Date();
		return STime.getUTCinMillis(d);
	}

	/**
	 *  Calc time difference between now (UTC since 1970)
	 *  and given start time in MS.
	 */
	static public long calcTimeDeltaMS(long startInUTC) {
		return STime.getCurTimeUTCinMillis() - startInUTC;
	}

	/** Get current time as short string in local time. */
	static public String getCurTimeShortString() {
		return getCurTimeShortString(true);
	}

	/**
	 * Get current time as short string in either UTC or local STime.
	 * e.g.: '23:98:74'
	 */
	static public String getCurTimeShortString(boolean local) {
		Calendar cal = Calendar.getInstance(getTimeZone(local));
		int hour24 = cal.get(Calendar.HOUR_OF_DAY);    // 0..23
		int min = cal.get(Calendar.MINUTE);            // 0..59
		int sec = cal.get(Calendar.SECOND);            // 0..59
		return SString.intToString(hour24, 2) + ":" +
		       SString.intToString(min, 2) + ":" +
		       SString.intToString(sec, 2);
	}

	/** Get a time zone */
	static public TimeZone getTimeZone(boolean local) {
		if(local)
			return TimeZone.getDefault();
		else
			return TimeZone.getTimeZone("UTC");
	}

	/**
	 * Get current date as string in either UTC or local STime.
	 * e.g. '2007-02-13 17:11:25.338'
	 */
	static public String getCurDateTimeMSString(boolean local) {
		String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		Calendar cal = Calendar.getInstance(getTimeZone(local));
		return sdf.format(cal.getTime());
	}

	/**
	 *  Get current date and time as string in either UTC or local STime.
	 *  e.g. '2006-10-09 19:48:48'
	 */
	static public String getCurDateTimeString(boolean local) {
		String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		Calendar cal = Calendar.getInstance(getTimeZone(local));
		return sdf.format(cal.getTime());
	}

	/**
	 * given a date in XML time format (UTC), return a Date.
	 * this method only handles times in the format below. Note
	 * the terminating Z indicates UTC.
	 *
	 * 'YYYY-MM-DDThh:mm:ssZ'
	 *  01234567890123456789
	 *
	 * @throws IllegalArgumentException if an illegal date string is
	 *                                  received.
	 */
	static public Date XMLtoDate(String xml)
		throws IllegalArgumentException
	{
		if(xml == null ||
		   xml.length() != 20 ||
		   xml.charAt(4) != '-' &&
		   xml.charAt(7) != '-' &&
		   xml.charAt(13) != ':' &&
		   xml.charAt(16) != ':' &&
		   xml.charAt(19) != 'Z')
		{
			throw new IllegalArgumentException(
			    "Bogus XML date string received: " + xml);
		}
		int y = SString.stringToInt(xml.substring(0, 4));
		int m = SString.stringToInt(xml.substring(5, 7))
			- 1;    // month is zero based
		int d = SString.stringToInt(xml.substring(8, 10));
		int h = SString.stringToInt(xml.substring(11, 13));
		int mi = SString.stringToInt(xml.substring(14, 16));
		int s = SString.stringToInt(xml.substring(17, 19));
		Calendar cal = Calendar.getInstance(getTimeZone(false));
		cal.set(y, m, d, h, mi, s);
		return cal.getTime();
	}

	/**
	 * Convert from a Calendar to XML in UTC format. Note that this
	 * method does not perform DST conversions.
	 * @param c Calendar
	 * @returns XML date string in UTC:
	 *           format 'YYYY-MM-DDThh:mm:ssZ'.
	 *              e.g. 2008-03-22T02:04:21Z
	 *                   01234567890123456789
	 */
	static public String CalendarToXML(Calendar c) {
		String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		String dt = sdf.format(c.getTime());
		String x = dt.substring(0, 10) + "T" + dt.substring(11) + "Z";
		assert x.length() == 20 : "STime.CalendarToXML";
		return x;
	}
}
