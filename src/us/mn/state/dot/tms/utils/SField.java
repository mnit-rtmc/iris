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

/**
 *  Convenience methods for dealing with fields separated with '_'.
 *  @author Michael Darter
 */
public class SField {

	/** field separator for name field */
	static public final String SEPARATOR = "_";

	/** instance can't be created */
	private SField(){}

	/** Join strings with a separator */
	static public String join(String a, String b) {
		return a + SEPARATOR + b;
	}

	/** Extract the first field */
	static public String head(String s) {
		s = (s == null ? "" : s);
		int i = s.indexOf(SEPARATOR);
		if(i < 0)
			return s;
		else if(i < s.length() )
			return s.substring(0, i);
		return "";
	}

	/** Extract the last field */
	static public String tail(String s) {
		s = (s == null ? "" : s);
		int i = s.lastIndexOf(SEPARATOR);
		if(i < 0)
			return s;
		else if(i + 1 < s.length() )
			return s.substring(i + 1);
		return "";
	}
}
