/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2015  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ss105;

import java.io.IOException;
import static us.mn.state.dot.tms.VehLengthClass.SHORT;
import static us.mn.state.dot.tms.VehLengthClass.MEDIUM;
import static us.mn.state.dot.tms.VehLengthClass.LONG;
import us.mn.state.dot.tms.units.Distance;
import static us.mn.state.dot.tms.units.Distance.Units.FEET;
import us.mn.state.dot.tms.utils.HexString;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * A Classification Property contains vehicle classification length thresholds.
 *
 * @author Douglas Lau
 */
public class ClassificationProperty extends MemoryProperty {

	/** Minimum length for SHORT vehicle classification */
	static private final Distance SHORT_MIN = new Distance(
		SHORT.lower_bound.round(FEET) + 1, FEET);

	/** Maximum length for SHORT vehicle classification */
	static private final Distance SHORT_MAX = SHORT.upper_bound;

	/** Minimum length for MEDIUM vehicle classification */
	static private final Distance MEDIUM_MIN = new Distance(
		MEDIUM.lower_bound.round(FEET) + 1, FEET);

	/** Maximum length for MEDIUM vehicle classification */
	static private final Distance MEDIUM_MAX = MEDIUM.upper_bound;

	/** Minimum length for LONG vehicle classification */
	static private final Distance LONG_MIN = new Distance(
		LONG.lower_bound.round(FEET) + 1, FEET);

	/** Maximum length for LONG vehicle classification */
	static private final Distance LONG_MAX = LONG.upper_bound;

	/** Minimum length for SHORT vehicle classification */
	private Distance short_min = SHORT_MIN;

	/** Maximum length for SHORT vehicle classification */
	private Distance short_max = SHORT_MAX;

	/** Minimum length for MEDIUM vehicle classification */
	private Distance medium_min = MEDIUM_MIN;

	/** Maximum length for MEDIUM vehicle classification */
	private Distance medium_max = MEDIUM_MAX;

	/** Minimum length for LONG vehicle classification */
	private Distance long_min = LONG_MIN;

	/** Maximum length for LONG vehicle classification */
	private Distance long_max = LONG_MAX;

	/** Get the SS105 memory buffer address */
	protected int memoryAddress() {
		return 0x020000;
	}

	/** Get the SS105 memory buffer length */
	protected short memoryLength() {
		return 40;
	}

	/** Format the buffer to write to SS105 memory */
	protected String formatBuffer() {
		return HexString.format(short_min.round(FEET), 4) +
		       HexString.format(short_max.round(FEET), 4) +
		       HexString.format(0, 8) +
		       HexString.format(medium_min.round(FEET), 4) +
		       HexString.format(medium_max.round(FEET), 4) +
		       HexString.format(0, 8) +
		       HexString.format(long_min.round(FEET), 4) +
		       HexString.format(long_max.round(FEET), 4);
	}

	/** Parse the response to a QUERY request */
	protected void parseQuery(String r) throws IOException {
		try {
			short_min = parseFeet(r, 0);
			short_max = parseFeet(r, 4);
			medium_min = parseFeet(r, 16);
			medium_max = parseFeet(r, 20);
			long_min = parseFeet(r, 32);
			long_max = parseFeet(r, 36);
		}
		catch(NumberFormatException e) {
			throw new ParsingException(
				"Invalid classification lengths: " + r);
		}
	}

	/** Parse length (distance) in feet. */
	static private Distance parseFeet(String r, int pos)
		throws NumberFormatException
	{
		return new Distance(Integer.parseInt(r.substring(pos, pos + 4),
			16), FEET);
	}

	/** Is the classification set to the default values? */
	public boolean isDefault() {
		return short_min.equals(SHORT_MIN) &&
		       short_max.equals(SHORT_MAX) &&
		       medium_min.equals(MEDIUM_MIN) &&
		       medium_max.equals(MEDIUM_MAX) &&
		       long_min.equals(LONG_MIN) &&
		       long_max.equals(LONG_MAX);
	}

	/** Get a string representation */
	public String toString() {
		Distance.Formatter df = new Distance.Formatter(0);
		StringBuilder sb = new StringBuilder();
		sb.append("Classification: ");
		sb.append(df.format(short_min));
		sb.append('-');
		sb.append(df.format(short_max));
		sb.append(',');
		sb.append(df.format(medium_min));
		sb.append('-');
		sb.append(df.format(medium_max));
		sb.append(',');
		sb.append(df.format(long_min));
		sb.append('-');
		sb.append(df.format(long_max));
		return sb.toString();
	}
}
