/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2012  Minnesota Department of Transportation
 * Copyright (C) 2008-2010  AHMCT, University of California
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
package us.mn.state.dot.tms.server.comm.dmsxml;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import us.mn.state.dot.sched.TimeSteward;

/**
 * Time convenience methods.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public final class STime {

	/** constructor */
	private STime() {}

	/** Calc time difference between now (UTC since 1970)
	 * and given start time in MS. */
	static public long calcTimeDeltaMS(long startInUTC) {
		return TimeSteward.currentTimeMillis() - startInUTC;
	}

	/** Get current time as short string in local time. */
	static public String getCurTimeShortString() {
		return getCurTimeShortString(true);
	}

	/**
	 * Get current time as short string in either UTC or local time.
	 * e.g.: '23:98:74'
	 */
	static public String getCurTimeShortString(boolean local) {
		return formatDate("HH:mm:ss", local);
	}

	/**
	 * Get current date and time as string in either UTC or local time.
	 * e.g. '2006-10-09 19:48:48'
	 */
	static public String getCurDateTimeString(boolean local) {
		return formatDate("yyyy-MM-dd HH:mm:ss", local);
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

	/** Get a time zone */
	static private TimeZone getTimeZone(boolean local) {
		if(local)
			return TimeZone.getDefault();
		else
			return TimeZone.getTimeZone("UTC");
	}

	/** Format the current date/time.
	 * @param format Format specifier.
	 * @param local Use local time or UTC.
	 * @return Formatted string. */
	static private String formatDate(String format, boolean local) {
		return formatDate(format, local, new Date());
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
		if(xml != null) {
			try {
				return parseDate("yyyy-MM-dd'T'HH:mm:ss'Z'",
					false, xml);
			}
			catch(ParseException e) {
				// throw illegal arg exception below
			}
		}
		throw new IllegalArgumentException(
		    "Bogus XML date string received: " + xml);
	}

	/** Parse a date from a string.
	 * @param format Format specifier.
	 * @param local Use local time or UTC.
	 * @param date Date to format.
	 * @return Parsed date. */
	static private Date parseDate(String format, boolean local,
		String date) throws ParseException
	{
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		sdf.setLenient(false);
		sdf.setTimeZone(getTimeZone(local));
		return sdf.parse(date);
	}
}
