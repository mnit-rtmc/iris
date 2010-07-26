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
	 * Calc time difference between now (UTC since 1970)
	 * and given start time in MS.
	 */
	static public long calcTimeDeltaMS(long startInUTC) {
		return System.currentTimeMillis() - startInUTC;
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
		return formatDate("HH:mm:ss", local);
	}

	/**
	 * Get current date as string in either UTC or local STime.
	 * e.g. '2007-02-13 17:11:25.338'
	 */
	static public String getCurDateTimeMSString(boolean local) {
		return formatDate("yyyy-MM-dd HH:mm:ss.SSS", local);
	}

	/**
	 * Get current date and time as string in either UTC or local STime.
	 * e.g. '2006-10-09 19:48:48'
	 */
	static public String getCurDateTimeString(boolean local) {
		return formatDate("yyyy-MM-dd HH:mm:ss", local);
	}

	/** Get a calendar */
	static private Calendar getCalendar(boolean local) {
		return Calendar.getInstance(getTimeZone(local));
	}

	/** Get a time zone */
	static private TimeZone getTimeZone(boolean local) {
		if(local)
			return TimeZone.getDefault();
		else
			return TimeZone.getTimeZone("UTC");
	}

	/** Format a date to a string.
	 * @param format Format specifier.
	 * @param local Use local time or UTC.
	 * @param date Date to format.
	 * @return Formatted string. */
	static private String formatDate(String format, boolean local,
		Date date)
	{
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		sdf.setTimeZone(getTimeZone(local));
		return sdf.format(date);
	}

	/** Format the current date/time.
	 * @param format Format specifier.
	 * @param local Use local time or UTC.
	 * @return Formatted string. */
	static private String formatDate(String format, boolean local) {
		return formatDate(format, local, new Date());
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
		Calendar cal = getCalendar(false);
		cal.setTimeInMillis(0);
		cal.set(y, m, d, h, mi, s);
		return cal.getTime();
	}

	/**
	 * Convert from a Calendar to XML in UTC format.
	 * @param c Calendar
	 * @return XML date string in UTC:
	 *           format 'YYYY-MM-DDThh:mm:ssZ'.
	 *              e.g. 2008-03-22T02:04:21Z
	 *                   01234567890123456789
	 */
	static public String CalendarToXML(Calendar c) {
		return formatDate("yyyy-MM-dd'T'HH:mm:ss'Z'", false,
			c.getTime());
	}
}
