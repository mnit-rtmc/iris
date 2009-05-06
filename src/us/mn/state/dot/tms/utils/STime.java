/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2009  Minnesota Department of Transportation
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
 *  Time convenience methods.
 *  @author Michael Darter
 */
final public class STime {

	/** constructor */
	private STime() {}

	/** beep */
	public static void beep() {
		Toolkit.getDefaultToolkit().beep();
	}

	/** return true if the default time zone supports DST */
	public static boolean verifyTimeZoneSupportsDST() {
		return(TimeZone.getDefault().useDaylightTime());
	}

	/**
	 * return true if the default TimeZone matches the expected.
	 *
	 * Note, as of 04/29/08, this only worked by specifying the TZ via
	 * the command line "-Duser.timezone=America/Los_Angeles". Specifying
	 * this programmatically didn't work.
	 *
	 * @param expectedtz Expected time zone e.g. specifying
	 *        "America/Los_Angeles" on the command line results
	 *    in the default TimeZone being called "Pacific Standard
	 *        Time".
	 */
	public static boolean verifyDefaultTimeZone(String expectedtz) {
		TimeZone tz = TimeZone.getDefault();
		boolean ok = tz.getDisplayName().equals(expectedtz);

		// System.err.println("Default Locale:" + Locale.getDefault());
		if(!ok) {
			System.err.println("The default time zone is:"
				 + tz.getDisplayName() + ", expected: "
				 + expectedtz);
		}

		return (ok);
	}

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
		java.util.Date d = STime.getCurTime();
		long t = STime.getUTCinMillis(d);

		return (t);
	}

	/**
	 *  get current time as java.util.Date
	 */
	public static Date getCurTime() {
		Calendar cal = new GregorianCalendar();
		java.util.Date d = cal.getTime();

		return (d);
	}

	/**
	 *  Calc time difference between now (UTC since 1970)
	 *  and given start time in MS.
	 */
	public static long calcTimeDeltaMS(long startInUTC) {
		long d = STime.getCurTimeUTCinMillis() - startInUTC;

		return (d);
	}

	/**
	 *  Get current time as string in either local or UTC.
	 *  e.g.: '14:23:09.342'
	 */
	public static String getCurTimeString(boolean local) {

		// Date d=STime.getCurTimeDate();
		// d.get
		Calendar cal = new GregorianCalendar();

		if(!local) {
			cal.setTimeZone(TimeZone.getTimeZone("UTC"));
		}

		int hour24 = cal.get(Calendar.HOUR_OF_DAY);    // 0..23
		int min = cal.get(Calendar.MINUTE);            // 0..59
		int sec = cal.get(Calendar.SECOND);            // 0..59
		int ms = cal.get(Calendar.MILLISECOND);        // 0..999
		String t = "";

		t += SString.intToString(hour24, 2) + ":";
		t += SString.intToString(min, 2) + ":";
		t += SString.intToString(sec, 2) + ".";
		t += ms;

		return (t);
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
	 *  Return a Date as a String.
	 *  @return String in the format '2007-02-13 17:11:25'
	 */
	public static String dateToString(Date date, boolean local) {
		String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
		java.text.SimpleDateFormat sdf =
			new java.text.SimpleDateFormat(DATE_FORMAT);
		Calendar c = new GregorianCalendar();
		if(!local)
			c.setTimeZone(TimeZone.getTimeZone("UTC"));
		c.setTime(date);
		String dt = sdf.format(c.getTime());
		return dt;
	}

	/**
	 *  Convert a String containing a date and time to a Date.
	 *  @param String containing a date in the format: 2009-01-07 05:51:08
	 *     e.g. '2009-01-07 05:51:08'
	 *	     YYYY-MM-DD hh:mm:ss
	 *           0123456789012345678
	 *  @param local True if the time zone is local else UTC.
	 *  @return The Date associated with the parameters or null on error.
	 */
	public static Date stringToDate(String date, boolean local) {
		if(date == null)
			return null;
		Date retdate = null;
		try {
			// sanity checks
			boolean ok = true;
			ok = ok && date.length() == 19;
			ok = ok && date.charAt(4) == '-';
			ok = ok && date.charAt(7) == '-';
			ok = ok && date.charAt(10) == ' ';
			ok = ok && date.charAt(13) == ':';
			ok = ok && date.charAt(16) == ':';
			if(!ok) {
				System.err.println("Bogus date string: " + date);
				return null;
			}

			// extract quantities
			int m = Integer.parseInt(date.substring(5, 7))
				- 1;    // month is zero based
			int d = Integer.parseInt(date.substring(8, 10));
			int y = Integer.parseInt(date.substring(0, 4));
			int h = Integer.parseInt(date.substring(11, 13));
			int mi = Integer.parseInt(date.substring(14, 16));
			int s = Integer.parseInt(date.substring(17, 19));
			Calendar c = new GregorianCalendar(y, m, d, h, mi, s);
			if(!local)
				c.setTimeZone(TimeZone.getTimeZone("UTC"));
			retdate = c.getTime();
		} catch(Exception ex) {
			System.err.println("Exception in stringToDate(): " + ex);
			ex.printStackTrace();
		}
		return retdate;
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
	 *  Get current date and time as string that can be used in 
	 *  a file name. The arg controls if it is in UTC or local STime.
	 *  e.g. '102106160533' for 10/21/06, 16:05:33
	 */
	public static String getCurTimeSparseString(boolean local) {
		java.util.Date d = STime.getCurTime();
		String dt = STime.getCurTimeSparseString(d, local);

		return (dt);
	}

	/**
	 *  Given a date, format and return the date as a string
	 *  that can be used in a file name, formated in UTC.
	 *  e.g. 102106160533 for 10/21/06, 16:05:33
	 */
	public static String getCurTimeSparseString(java.util.Date d,
		boolean local) {
		String DATE_FORMAT = "MMddyyHHmmss";
		java.text.SimpleDateFormat df =
			new java.text.SimpleDateFormat(DATE_FORMAT);

		if(!local) {
			df.setTimeZone(TimeZone.getTimeZone("UTC"));
		}

		String dt = df.format(d);

		return (dt);
	}

	/**
	 *  Return an XML UTC time string given a Date in local STime.
	 *  Note that this method does not perform DST conversions.
	 *  @returns XML date string in UTC:
	 *  format 'YYYY-MM-DDThh:mm:ssZ'.
	 *     e.g. 2008-03-22T02:04:21Z
	 *          01234567890123456789
	 */
	public static String toXMLDateTime(java.util.Date d) {

		// create date format
		String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
		java.text.SimpleDateFormat sdf =
			new java.text.SimpleDateFormat(DATE_FORMAT);

		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

		String dt = sdf.format(d);
		String xmld = dt.substring(0, 10) + "T" + dt.substring(11)
			      + "Z";

		assert xmld.length() == 20 : "STime.toXMLDateTime";

		return (xmld);
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

	/** Given Date return a new Date subtracted by arg ms */
	public static Date subtract(java.util.Date d, long ms) {
		return add(d, -ms);
	}

	/** Given Date return a new Date incremented by arg ms */
	public static Date add(java.util.Date d, long ms) {
		if(d == null)
			return null;
		Calendar cal = new GregorianCalendar();
		// set time in millis UTC from start of epoch
		long newtime = d.getTime() + ms;
		cal.setTimeInMillis(newtime);
		Date newd = cal.getTime();
		return newd;
	}

	/**
	 *  return true if the 2nd specified arg is after the 1st.
	 */
	public static boolean after(Date d1, Date d2) {
		long l1 = d1.getTime();
		long l2 = d2.getTime();

		return (l1 < l2);
	}

	/**
	 *  Convert a String containing a date and time to a Date.
	 *  @param String containing a date in the format: 2009-01-07 05:51:08
	 *     e.g. '111606181951'
	 *           MMDDYYhhmmss
	 *           012345678901
	 *  @param local True if the time zone is local else UTC.
	 *  @return The Date associated with the parameters or null on error.
	 */
	public static Date ShortStringToDate(String xml, boolean local) {
		if(xml == null)
			return (null);

		Date retdate = null;
		try {
			// sanity checks
			boolean ok = true;
			ok = ok && xml.length() == 12;
			if(!ok) {
				System.err.println("Bogus date string: " + xml);
				return null;
			}

			int m = SString.stringToInt(xml.substring(0, 2))
				- 1;    // month is zero based
			int d = SString.stringToInt(xml.substring(2, 4));
			int y = SString.stringToInt(xml.substring(4, 6)) + 2000;
			int h = SString.stringToInt(xml.substring(6, 8));
			int mi = SString.stringToInt(xml.substring(8, 10));
			int s = SString.stringToInt(xml.substring(10, 12));
			Calendar c = new GregorianCalendar(y, m, d, h, mi, s);

			if(!local) {
				c.setTimeZone(TimeZone.getTimeZone("UTC"));
			}

			retdate = c.getTime();
		} catch(Exception ex) {
			System.err.println("Exception in ShortStringToDate(): " + ex);
			ex.printStackTrace();
		}
		return retdate;
	}

	/**
	 *  given a date in XML time format (UTC), return a Calendar.
	 *
	 *      @arg xml XMl date string in this format:
	 *      'YYYY-MM-DDThh:mm:ssZ'
	 *      01234567890123456789
	 *
	 *  @throws IllegalArgumentException if an illegal date string is received.
	 */
	public static Calendar XMLtoCalendar(String xml)
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
		return c;
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

	/** 
	 *  Return the rounded number of seconds difference between 2 Dates.
	 *  e.g. a delta of 600 returns 1, a delta of 200 returns 0.
	 */
	public static int secondsDiff(Date d1, Date d2) {
		if(d1 == null || d2 == null)
			return 0;
		double delta = Math.abs(d1.getTime() - d2.getTime());
		return (int)Math.round(delta/1000);
	}

	/** 
	 *  Return the minimum number of seconds difference between 2 Dates.
	 *  e.g. a delta of 600 returns 0, a delta of 1100 returns 1.
	 */
	public static int secondsDiffMin(Date d1, Date d2) {
		if(d1 == null || d2 == null)
			return 0;
		double delta = Math.abs(d1.getTime() - d2.getTime());
		return (int)Math.floor(delta/1000);
	}

	/** 
	 *  Return the maximum number of seconds difference between 2 Dates.
	 *  e.g. a delta of 600 returns 1, a delta of 1100 returns 2.
	 */
	public static int secondsDiffMax(Date d1, Date d2) {
		if(d1 == null || d2 == null)
			return 0;
		double delta = Math.abs(d1.getTime() - d2.getTime());
		return (int)Math.ceil(delta/1000);
	}

	/* sleep for the specified number of MS */
	public static void sleep(long ms) {
		ms = (ms<0 ? 0 : ms);
		try {
			Thread.sleep(ms);
		} catch (InterruptedException ex) {}
	}
}
