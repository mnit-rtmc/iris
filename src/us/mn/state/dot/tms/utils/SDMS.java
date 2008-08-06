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

import us.mn.state.dot.tms.utils.SString;

/**
 * DMS convenience methods.
 *
 * @author Michael Darter
 */
public class SDMS {

	/** instance can't be created */
	private SDMS(){}

	/**
	 *  test methods.
	 */
	public static boolean test() {
		boolean ok = true;

		// getValidText
		ok = ok & SDMS.getValidText("abcDEF").equals("DEF");

		return (ok);
	}

	/** return validated sign text, with invalid chars removed */
	public static String getValidText(String t) {
		final String DMS_VALID_CHARS="ABCDEFGHIJKLMNOPQRSTUVWXYZ01234567890 !#$%&()*+,-./:;<=>?@]*'";
		return SString.union(t.toUpperCase(),DMS_VALID_CHARS);
	}

	/** 
	  * This is a hack. It is used by the ComboBoxEditor and 
	  * SignMessageModel to recognize when a sign message line
	  * should be ignored. By convension, a line begining and
	  * ending with an underscore is to be ignored. IRIS assumes
	  * that non-blank DMS messages have both a bitmap and multistring,
	  * which is not the case for D10, so a bogus multistring is created
	  * in comm/dmslite (with a prepended and appended underscore). 
	  */
	public static boolean ignoreLineHack(String line) {
		return SString.enclosedBy(line,"_");
	}
	// see the above note
	public static String flagIgnoredSignLineHack(String line) {
		return "_"+line+"_";
	}

}

