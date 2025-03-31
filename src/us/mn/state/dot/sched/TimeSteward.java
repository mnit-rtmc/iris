/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2025  Minnesota Department of Transportation
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
package us.mn.state.dot.sched;

import java.net.Socket; // Suppress javadoc warning
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * The time steward provides static methods dealing with time sources.
 * To use this class correctly, there are several standard library methods
 * which must be avoided (or used only carefully).
 *
 * @see java.lang.System#currentTimeMillis()
 * @see java.lang.Object#wait(long)
 * @see java.lang.Object#wait(long, int)
 * @see java.lang.Thread#join(long)
 * @see java.lang.Thread#join(long, int)
 * @see java.lang.Thread#sleep(long)
 * @see java.lang.Thread#sleep(long, int)
 * @see java.net.DatagramSocket#setSoTimeout(int)
 * @see java.net.Socket#connect(java.net.SocketAddress, int)
 * @see java.net.Socket#setSoTimeout(int)
 * @see java.net.URLConnection#setConnectTimeout(int)
 * @see java.net.URLConnection#setReadTimeout(int)
 * @see java.util.Calendar#getInstance()
 * @see java.util.Date#Date()
 *
 * @author Douglas Lau
 */
public final class TimeSteward {

	/** Time source */
	static private TimeSource source = new SystemTimeSource();

	/** Don't allow instantiation */
	private TimeSteward() { }

	/** Set the time source */
	static public void setTimeSource(TimeSource ts) {
		assert ts != null;
		source = ts;
	}

	/** Get the current time */
	static public long currentTimeMillis() {
		return source.currentTimeMillis();
	}

	/** Sleep for the specified number of milliseconds */
	static public void sleep(long ms) throws InterruptedException {
		source.sleep(ms);
	}

	/** Sleep without interruption */
	static public void sleep_well(long ms) {
		try {
			sleep(ms);
		}
		catch (InterruptedException e) {
			// Ignore
		}
	}

	/** Wait until an object is notified, or timeout expires */
	static public void wait(Object monitor, long ms)
		throws InterruptedException
	{
		source.wait(monitor, ms);
	}

	/** Get a date instance from the time source */
	static public Date getDateInstance() {
		return new Date(currentTimeMillis());
	}

	/** Get a calendar instance from the time source */
	static public Calendar getCalendarInstance() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(currentTimeMillis());
		return cal;
	}

	/** Get the current local minute-of-day as an int */
	static public int currentMinuteOfDayInt() {
		Calendar cal = getCalendarInstance();
		return cal.get(Calendar.HOUR_OF_DAY) * 60 +
		       cal.get(Calendar.MINUTE);
	}

	/** Get the current local second-of-day as an int */
	static public int currentSecondOfDayInt() {
		return secondOfDayInt(currentTimeMillis());
	}

	/** Get the local second-of-day as an int */
	static public int secondOfDayInt(long time) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);
		return cal.get(Calendar.HOUR_OF_DAY) * 3600 +
		       cal.get(Calendar.MINUTE) * 60 +
		       cal.get(Calendar.SECOND);
	}

	/** Get the current local date as a short YYYYMMDD string */
	static public String currentDateShortString() {
		return dateShortString(currentTimeMillis());
	}

	/** Get current date and time as string.
	 * @param local True for local time, false for UTC.
	 */
	static public String currentDateTimeString(boolean local) {
		SimpleDateFormat sdf = createDateFormat("yyyy-MM-dd HH:mm:ss",
			local);
		return sdf.format(new Date(currentTimeMillis()));
	}

	/** Format date/time in ISO 8601 format */
	static public String format8601(long dt) {
		SimpleDateFormat sdf =
			new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		return sdf.format(new Date(dt));
	}

	/** Parse date/time in ISO 8601 format */
	static public Long parse8601(String dt) {
		try {
			SimpleDateFormat sdf =
				new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
			return sdf.parse(dt).getTime();
		}
		catch (ParseException e) {
			return null;
		}
	}

	/** Get the local date as a short YYYYMMDD string */
	static public String dateShortString(long date) {
		SimpleDateFormat sdf = createDateFormat("yyyyMMdd", true);
		return sdf.format(new Date(date));
	}

	/** Get the current local time as short string */
	static public String currentTimeShortString() {
		return timeShortString(currentTimeMillis());
	}

	/** Get the local time as short HH:mm:ss string */
	static public String timeShortString(long date) {
		SimpleDateFormat sdf = createDateFormat("HH:mm:ss", true);
		return sdf.format(new Date(date));
	}

	/** Format a date to a string.
	 * @param format Format specifier.
	 * @param local Use local time or UTC.
	 * @param date Date to format.
	 * @return Formatted string. */
	static private SimpleDateFormat createDateFormat(String format,
		boolean local)
	{
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		sdf.setTimeZone(getTimeZone(local));
		return sdf;
	}

	/** Get a time zone */
	static private TimeZone getTimeZone(boolean local) {
		if(local)
			return TimeZone.getDefault();
		else
			return TimeZone.getTimeZone("UTC");
	}
}
