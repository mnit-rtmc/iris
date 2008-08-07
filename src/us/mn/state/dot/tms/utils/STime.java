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

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Time convenience methods.
 *
 * @author Michael Darter
 */
public class STime {

	/** instance can't be created */
	private STime(){}

	/**
	 *  test methods.
	 */
	static public boolean test() {
		boolean ok = true;

		return (ok);
	}

	/** return the current time as a local short string, e.g. "16:52:14" */
	public static String getCurTimeShortString() {
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

