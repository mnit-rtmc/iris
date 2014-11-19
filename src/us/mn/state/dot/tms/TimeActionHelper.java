/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2014  Minnesota Department of Transportation
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
package us.mn.state.dot.tms;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

/**
 * Helper class for time actions.
 *
 * @author Douglas Lau
 */
public class TimeActionHelper extends BaseHelper {

	/** Don't allow instances to be created */
	private TimeActionHelper() {
		assert false;
	}

	/** Lookup the time action with the specified name */
	static public TimeAction lookup(String name) {
		return (TimeAction)namespace.lookupObject(TimeAction.SONAR_TYPE,
			name);
	}

	/** Get a time action iterator */
	static public Iterator<TimeAction> iterator() {
		return new IteratorWrapper<TimeAction>(namespace.iterator(
			TimeAction.SONAR_TYPE));
	}

	/** Get the minute-of-day (0-1440) */
	static public Integer getMinuteOfDay(TimeAction ta) {
		Date d = parseTime(ta.getTimeOfDay());
		return (d != null) ? getMinuteOfDay(d) : null;
	}

	/** Get the minute-of-day (0-1440) */
	static private int getMinuteOfDay(Date d) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(d);
		return cal.get(Calendar.HOUR_OF_DAY) * 60 +
		       cal.get(Calendar.MINUTE);
	}

	/** Minute of 12 Noon in day */
	static public final int NOON = 12 * 60;

	/** Get the peak period for a minute-of-day */
	static public int getPeriod(int min) {
		return (min < NOON) ? Calendar.AM : Calendar.PM;
	}

	/** Date parser formats */
	static private final DateFormat[] DATE_FORMATS = {
		new SimpleDateFormat("MM/dd/yy"),
		new SimpleDateFormat("MM/dd/yyyy"),
		new SimpleDateFormat("yyyy-MM-dd"),
	};

	/** Time parser formats */
	static private final DateFormat[] TIME_FORMATS = {
		new SimpleDateFormat("h:mm a"),
		new SimpleDateFormat("H:mm"),
		new SimpleDateFormat("h a"),
		new SimpleDateFormat("H")
	};

	/** Parse a date string */
	static public Date parseDate(String t) {
		for(DateFormat df: DATE_FORMATS) {
			try {
				return df.parse(t);
			}
			catch(ParseException e) {
				// Ignore
			}
		}
		return null;
	}

	/** Convert date to string */
	static public String formatDate(Date d) {
		if(d != null) {
			SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
			return f.format(d);
		} else
			return null;
	}

	/** Parse a time string in one of the supported formats.
	 * @param t Time string.
	 * @return Date object, or null if time could not be parsed. */
	static public Date parseTime(String t) {
		for (DateFormat df: TIME_FORMATS) {
			try {
				return df.parse(t);
			}
			catch (ParseException e) { /* ignore */ }
			catch (NumberFormatException e) { /* ignore */ }
		}
		return null;
	}

	/** Convert date to time string */
	static public String formatTime(Date tod) {
		if(tod != null) {
			SimpleDateFormat f = new SimpleDateFormat("H:mm");
			return f.format(tod);
		} else
			return null;
	}
}
