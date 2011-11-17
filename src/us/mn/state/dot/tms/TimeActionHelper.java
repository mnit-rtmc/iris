/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2011  Minnesota Department of Transportation
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
import us.mn.state.dot.sonar.Checker;

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

	/** Find time actions using a Checker */
	static public TimeAction find(final Checker<TimeAction> checker) {
		return (TimeAction)namespace.findObject(TimeAction.SONAR_TYPE, 
			checker);
	}

	/** Lookup the time action with the specified name */
	static public TimeAction lookup(String name) {
		return (TimeAction)namespace.lookupObject(TimeAction.SONAR_TYPE,
			name);
	}

	/** Minute of 12 Noon in day */
	static public final int NOON = 12 * 60;

	/** Get the minute-of-day (0-1440) */
	static public int getMinute(TimeAction ta) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(parseTime(ta.getTimeOfDay()));
		return cal.get(Calendar.HOUR_OF_DAY) * 60 +
		       cal.get(Calendar.MINUTE);
	}

	/** Get the peak period for a time action */
	static public int getPeriod(TimeAction ta) {
		if(getMinute(ta) < NOON)
			return Calendar.AM;
		else
			return Calendar.PM;
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

	/** Parse a time string */
	static public Date parseTime(String t) {
		for(DateFormat df: TIME_FORMATS) {
			try {
				return df.parse(t);
			}
			catch(ParseException e) {
				// Ignore
			}
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
