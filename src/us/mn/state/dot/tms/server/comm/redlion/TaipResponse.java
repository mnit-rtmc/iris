/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2017  SRF Consulting Group
 * Copyright (C) 2018-2020  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.redlion;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TAIP response parser.
 *
 * @author John L. Stanley
 * @author Douglas Lau
 */
public class TaipResponse {

	/** Pattern to match RPV response */
	static private final Pattern RPV = Pattern.compile(
	    ".*?>RPV\\d{5}(\\d{3})(\\d{5})(\\d{4})(\\d{5})\\.{7}(\\d)[^<]*<");

	/** Pattern to match RCP response */
	static private final Pattern RCP = Pattern.compile(
	    ".*?>RCP\\d{5}(\\d{3})(\\d{4})(\\d{4})(\\d{4})\\.{1}(\\d)[^<]*<");

	/** Pattern to match RLN response */
	static private final Pattern RLN = Pattern.compile(
	    ".*?>RLN\\d{8}(\\d{3})(\\d{7})(\\d{4})(\\d{7})\\.{39}(\\d)[^<]*<");

	/** Make a double from integral and fractional parts.
	 * @param s1 Integer part of number.
	 * @param s2 Fractional part of number.
	 * @return Double value.
	 * @throws NumberFormatException if there's a problem. */
	static private double makeDouble(String s1, String s2) {
		String ssign = "";
		switch (s1.charAt(0)) {
		case '+':  // remove leading '+'
			s1 = s1.substring(1);
			break;
		case '-':  // remove leading '-'
			s1 = s1.substring(1);
			ssign = "-";
		}
		// remove leading '0's (but leave a
		// single zero if that's all there is)
		while ((s1.length() > 1) && (s1.charAt(0) == '0'))
			s1 = s1.substring(1);
		return Double.parseDouble(ssign + s1 + "." + s2);
	}

	/** Parse a string containing GPS coordinates in one of three TAIP
	 * response formats (RCP, RLN, or RPV).
	 * Note: TAIP checksum suffix is ignored, if present.
	 *
	 * @param t TAIP response.
	 * @return GPS data object, or null if unable to parse. */
	static public GpsData parse(String t) {
		// FIXME: Deal with optional TAIP checksum suffix.
		Matcher m = RPV.matcher(t);
		if (!m.find()) {
			m = RCP.matcher(t);
			if (!m.find()) {
				m = RLN.matcher(t);
				if (!m.find())
					return null;
			}
		}
		// Check if AgeOfData indicator says data is not usable.
		if (m.group(5).equals("0"))
			return null;
		try {
			double lt = makeDouble(m.group(1), m.group(2));
			double ln = makeDouble(m.group(3), m.group(4));
			return new GpsData(true, lt, ln);
		}
		catch (NumberFormatException ex) {
			return null;
		}
	}

	/** Disallow instantiation */
	private TaipResponse() { }
}
