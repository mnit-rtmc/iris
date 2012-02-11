/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010  AHMCT, University of California
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * CSV convenience methods.
 *
 * @author Michael Darter
 */
public class SCsv {

	/** instance can't be created */
	private SCsv(){}

	/** Separate CSV line into fields.
	 * @param line CSV line, may be null.
	 * @return Array of fields, never null. */
	static public String[] separate(String line) {
		if(line == null)
			return new String[0];
		return preprocess(line.split(","));
	}

	/** Preprocess fields */
	static public String[] preprocess(String[] fs) {
		if(fs == null)
			return new String[0];
		for(int i = 0; i < fs.length; ++i)
			fs[i] = fs[i].trim();
		return fs;
	}

	/** Get field as String */
	static public String getFieldString(String[] fs, int i) {
		if(i >= 0 && i < fs.length) {
			String f = fs[i];
			return (f == null ? "" : f);
		}
		return "";
	}

	/** Get field as int */
	static public int getFieldInt(String[] fs, int i) {
		try {
			String f = getFieldString(fs, i);
			if(f.length() > 0)
				return Integer.parseInt(f);
			return 0;
		} catch(Exception ex) {
			return 0;
		}
	}

	/** Get field as float */
	static public float getFieldFloat(String[] fs, int i) {
		try {
			String f = getFieldString(fs, i);
			if(f.length() > 0)
				return Float.parseFloat(f);
			return 0;
		} catch(Exception ex) {
			return 0;
		}
	}

	/** Get field as long which is a time.
	 * @param fs Array of fields
	 * @param i Index into array of fields, e.g.: 02/09/2010 15:52:17
	 * @return Time or -1 on error. */
	static public long getFieldTime(String[] fs, int i) {
		String d = getFieldString(fs, i);
		boolean valid = SString.count(d, ':') == 2 && 
			SString.count(d, '/') == 2 && 
			SString.count(d, ' ') == 1;
		if(!valid)
			return -1;
		return parseDate("MM/dd/yyyy HH:mm:ss", d);
	}

	/** Parse a date string using a specific format.
	 * @return The date as a long else -1 on error. */
	static private long parseDate(String format, String d) {
		boolean local = true;
		if(d == null)
			return -1;
		try {
			//Date pd = parseDate(format, true, d);

			SimpleDateFormat sdf = new SimpleDateFormat(format);
			sdf.setLenient(false);
			sdf.setTimeZone(getTimeZone(local));
			Date pd = sdf.parse(d);

			return pd.getTime();
		}
		catch(ParseException e) {}
		return -1;
	}

	/** Get a time zone */
	static private TimeZone getTimeZone(boolean local) {
		if(local)
			return TimeZone.getDefault();
		else
			return TimeZone.getTimeZone("UTC");
	}
}
