/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008  Minnesota Department of Transportation
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
public class STime {

	/** instance can't be created */
	private STime(){}

	/**
	 * test Time methods. This method helps define procedural contracts for each method.
	 *
	 * @return true on success.
	 */
	public static boolean test() {
		boolean ok = true;

		// verify default time zone is specified correctly via command line arg.
		if(!STime.verifyDefaultTimeZone("Pacific Standard Time")) {
			System.err.println(
			    "Specify the correct time zone using the command line switch: -Duser.timezone=America/Los_Angeles");
			return (false);
		}

		// STime.subtract
		{
			System.err.println("STime.subtract():" + ok);

			Date d = STime.getCurTime();

			System.err.println("          Current Time (UTC):"
				 + STime.getCurTimeSparseString(d, false));
			System.err.println("          Current Time (loc):"
				 + STime.getCurTimeSparseString(d, true));
			System.err.println("  Current Time minus 20 mins:"
				 + STime.getCurTimeSparseString(STime.subtract(d,
					 20 * 60 * 1000), false));
			System.err.println("  Current Time minus 60 mins:"
				 + STime.getCurTimeSparseString(STime.subtract(d,
					 60 * 60 * 1000), false));
			System.err.println("    Current Time minus 1 day:"
				 + STime.getCurTimeSparseString(STime.subtract(d,
					 24 * 60 * 60 * 1000), false));
			System.err.println("STime.subtract():" + ok);
		}

		// STime.XMLtoDate
		{
			System.err.println("STime.XMLtoDate():" + ok);

			// this date is within DST so should convert to GMT-7
			Date d1 = STime.XMLtoDate("2008-04-29T15:37:22Z");
			ok=ok && d1.toString().equals("Tue Apr 29 08:37:22 PDT 2008");
		}

		// STime.toXMLDateTime
		/*
		{
			System.err.println("STime.toXMLDateTime():" + ok);

			// note, this time is within DST so is GMT-7
			Date d = new Date(2008 - 1900, 4 - 1, 29, 8, 53, 31);
			String xml = STime.toXMLDateTime(d);

			ok = ok && (xml.charAt(4) == '-')
			     && (xml.charAt(7) == '-')
			     && (xml.charAt(13) == ':')
			     && (xml.charAt(16) == ':')
			     && (xml.charAt(19) == 'Z');
			ok = ok && xml.substring(0, 4).equals("2008");
			ok = ok && xml.substring(5, 7).equals("04");
			ok = ok && xml.substring(8, 10).equals("29");
			ok = ok && xml.substring(11, 13).equals(
				"15");    // UTC hour via DST
			ok = ok && xml.substring(14, 16).equals("53");
			ok = ok && xml.substring(17, 19).equals("31");
		}
		*/

		// STime.XMLtoCalendar
		{
			System.err.println("STime.XMLtoCalendar():" + ok);

			Calendar c1 =
				STime.XMLtoCalendar("2008-04-29T15:37:22Z");

			ok = ok && (c1.get(Calendar.DAY_OF_MONTH) == 29);
			ok = ok && (c1.get(Calendar.HOUR_OF_DAY) == 15);
			ok = ok && (c1.get(Calendar.MINUTE) == 37);
			ok = ok && (c1.get(Calendar.YEAR) == 2008);
			ok = ok && (c1.get(Calendar.MONTH) == 4 - 1);
			ok = ok && (c1.get(Calendar.SECOND) == 22);
		}

		// STime.CalendarToXML
		{
			System.err.println("STime.CalendarToXML():" + ok);

			// local time
			Calendar c =
				new GregorianCalendar(2008, 4 - 1, 29, 8, 53,
						      31);    // local time in DST
			String xml = STime.CalendarToXML(c);

			ok = ok && xml.equals("2008-04-29T15:53:31Z");    // UTM

		}

		// DST conversions
		{

			// using default time zone, which has no DST
			System.err.println("STime.DST default zone:" + ok);

			Calendar c = null;

			c = new GregorianCalendar();
			System.err.println("Default Locale:" + Locale.getDefault());
			System.err.println("Default TimeZone:"
				 + c.getTimeZone().getDisplayName());
			System.err.println("Default TimeZone getDSTSavings():"
				 + c.getTimeZone().getDSTSavings() / 1000 / 60
				   / 60);
			System.err.println("Default DST_OFFSET:"
				 + c.get(Calendar.DST_OFFSET) / 1000 / 60 / 60
				 + " hours");
			System.err.println("Default ZONE_OFFSET:"
				 + c.get(Calendar.ZONE_OFFSET) / 1000 / 60 / 60
				 + " (hours).");

			// using default time zone
			System.err.println("STime.DST conversions with default zone:"
				 + ok);
			c = new GregorianCalendar();
			c.set(2008, 4 - 1, 29, 8, 53, 31);    // within DST
			System.err.println("In default zone, local time is:"
				 + c.getTime().toString());
			ok = ok && (c.getTime().toString().equals(
				"Tue Apr 29 08:53:31 PDT 2008"));

			Date local = c.getTime();

			System.err.println("As Date:" + local);
			ok = ok && (local.toString().equals(
				"Tue Apr 29 08:53:31 PDT 2008"));
			//System.err.println("As Date to UTC:" + local.toGMTString());
			//ok = ok && (local.toGMTString().toString().equals(
			//	"29 Apr 2008 15:53:31 GMT"));
		}

		// beep
		// STime.beep();

		return (ok);
	}

	/** beep */
	public static void beep() {
		Toolkit.getDefaultToolkit().beep();
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

		return (ms);
	}

	/**
	 *  get current time in MS (UTC) since Jan 1st 1970 00:00:00.
	 */
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
	 *  calc time difference between now (UTC since 1970)
	 *  and given start time in MS.
	 */
	public static long calcTimeDeltaMS(long startInUTC) {
		long d = STime.getCurTimeUTCinMillis() - startInUTC;

		return (d);
	}

	/**
	 *  get current time as string in either local or UTC.
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

	/**
	 *  get current date as string in either UTC or local time.
	 *  e.g. '2007-02-13 17:11:25.338'
	 */
	public static String getCurDateTimeMSString(boolean local) {
		String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
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
	 *  get current date and time as string in either UTC or local time.
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
	 *  get current date and time as string that can be used in a file name.
	 *  the arg controls if it is in UTC or local time.
	 *  e.g. '102106160533' for 10/21/06, 16:05:33
	 */
	public static String getCurTimeSparseString(boolean local) {
		java.util.Date d = STime.getCurTime();
		String dt = STime.getCurTimeSparseString(d, local);
		return (dt);
	}

	/**
	 *  given a date, format and return the date as a string
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
	 *  Return an XML UTC time string given a Date in local time.
	 *  Note that this method does not perform DST conversions.
	 *  @returns XML date string in UTC:
	 *  format 'YYYY-MM-DDThh:mm:ssZ'.
	 *  e.g. 2008-03-22T02:04:21Z
	 *       01234567890123456789
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

	/**
	 *  given Date return a new Date subtracted by arg MS.
	 */
	public static Date subtract(java.util.Date d, long ms) {

		// build a Date ms earlier than current
		long now = d.getTime();
		long newtime = now - ms;    // subtract ms
		Calendar cal = new GregorianCalendar();

		cal.setTimeInMillis(
		    newtime);    // set time in millis UTC from start of epoch

		Date newd = cal.getTime();

		return (newd);
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
	 *  given a date in the following format, return a Date.
	 *  an arg specifies if a UTC or local time zone.
	 *
	 *     e.g. '111606181951'
	 *           MMDDYYhhmmss
	 *           012345678901
	 */
	public static Date ShortStringToDate(String xml, boolean local) {
		if((xml == null) || (xml.length() != 12)) {
			return (null);
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

		Date date = c.getTime();

		return (date);
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

		return (c);
	}

	/**
	 *  Return an XML UTC time string given a Calendar time.
	 *  Note that this method does not perform DST conversions.
	 *
	 *  @returns XML date string in UTC:
	 *  format 'YYYY-MM-DDThh:mm:ssZ'.
	 *     e.g. 2008-03-22T02:04:21Z
	 *          01234567890123456789
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

		return (x);
	}

	/** return the current time as a local short string, e.g. "16:52:14" */
	public static String getCurTimeShortString() {
		//if(!local)
		//	cal.setTimeZone(TimeZone.getTimeZone("UTC"));
		Calendar cal = new GregorianCalendar();
		int hour24 = cal.get(Calendar.HOUR_OF_DAY);    // 0..23
		int min = cal.get(Calendar.MINUTE);            // 0..59
		int sec = cal.get(Calendar.SECOND);            // 0..59
		String t = "";
		t += SString.intToString(hour24, 2) + ":";
		t += SString.intToString(min, 2) + ":";
		t += SString.intToString(sec, 2);
		return t;
	}

}

