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

import java.awt.Toolkit;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Time convenience methods.
 *
 * @author Michael Darter
 */
public final class STime {

	/** constructor */
	private STime() {}

	/**
	 * given a java.util.Date, return the number of
	 * milliseconds UTC since Jan 1st 1970 00:00:00.
	 */
	public static long getUTCinMillis(java.util.Date d) {
		long ms = d.getTime();
		return ms;
	}

	/** get current time in MS (UTC) since Jan 1st 1970 00:00:00. */
	public static long getCurTimeUTCinMillis() {
		Date d = new Date();
		long t = STime.getUTCinMillis(d);

		return (t);
	}

	/**
	 *  Calc time difference between now (UTC since 1970)
	 *  and given start time in MS.
	 */
	public static long calcTimeDeltaMS(long startInUTC) {
		long d = STime.getCurTimeUTCinMillis() - startInUTC;

		return (d);
	}

	/** Get current time as short string in local time. */
	public static String getCurTimeShortString() {
		return getCurTimeShortString(true);
	}

	/**
	 *  Get current time as short string in either UTC or local STime.
	 *  e.g.: '23:98:74'
	 */
	public static String getCurTimeShortString(boolean local) {
		Calendar cal = new GregorianCalendar();

		if(!local) {
			cal.setTimeZone(TimeZone.getTimeZone("UTC"));
		}

		int hour24 = cal.get(Calendar.HOUR_OF_DAY);    // 0..23
		int min = cal.get(Calendar.MINUTE);            // 0..59
		int sec = cal.get(Calendar.SECOND);            // 0..59
		String t = "";

		t += SString.intToString(hour24, 2) + ":";
		t += SString.intToString(min, 2) + ":";
		t += SString.intToString(sec, 2);

		return (t);
	}

	/**
	 *  Get current date as string in either UTC or local STime.
	 *  e.g. '2007-02-13 17:11:25.338'
	 */
	public static String getCurDateTimeMSString(boolean local) {
		String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
		java.text.SimpleDateFormat sdf =
			new java.text.SimpleDateFormat(DATE_FORMAT);
		Calendar c = new GregorianCalendar();
		if(!local)
			c.setTimeZone(TimeZone.getTimeZone("UTC"));
		String dt = sdf.format(c.getTime());
		return dt;
	}

	/**
	 *  Get current date and time as string in either UTC or local STime.
	 *  e.g. '2006-10-09 19:48:48'
	 */
	public static String getCurDateTimeString(boolean local) {
		String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
		java.text.SimpleDateFormat sdf =
			new java.text.SimpleDateFormat(DATE_FORMAT);
		Calendar c = new GregorianCalendar();

		if(!local) {
			c.setTimeZone(TimeZone.getTimeZone("UTC"));
		}

		String dt = sdf.format(c.getTime());

		return (dt);
	}

	/**
	 *  given a date in XML time format (UTC), return a Date.
	 *  this method only handles times in the format below. Note
	 *  the terminating Z indicates UTC.
	 *
	 *  'YYYY-MM-DDThh:mm:ssZ'
	 *   01234567890123456789
	 *
	 *  @throws IllegalArgumentException if an illegal date string is received.
	 */
	public static Date XMLtoDate(String xml)
		throws IllegalArgumentException {

		// sanity check
		if((xml == null) || (xml.length() != 20)) {
			throw new IllegalArgumentException(
			    "Bogus XML date string received: " + xml);
		}

		if((xml.charAt(4) != '-') && (xml.charAt(7) != '-')
			&& (xml.charAt(13) != ':') && (xml.charAt(16) != ':')
			&& (xml.charAt(19) != 'Z')) {
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
		Calendar c = new GregorianCalendar(y, m, d, h, mi, s);

		c.setTimeZone(TimeZone.getTimeZone("UTC"));

		Date date = c.getTime();

		return (date);
	}

	/**
	 *  Convert from a Calendar to XML in UTC format. Note that this 
	 *  method does not perform DST conversions.
	 *  @param c Calendar
	 *  @returns XML date string in UTC:
	 *	format 'YYYY-MM-DDThh:mm:ssZ'.
	 *		e.g. 2008-03-22T02:04:21Z
	 * 		     01234567890123456789
	 */
	public static String CalendarToXML(java.util.Calendar c) {

		// create date format
		String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
		java.text.SimpleDateFormat sdf =
			new java.text.SimpleDateFormat(DATE_FORMAT);
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		String dt = sdf.format(c.getTime());
		String x = dt.substring(0, 10) + "T" + dt.substring(11) + "Z";
		assert x.length() == 20 : "STime.CalendarToXML";
		return x;
	}
}
