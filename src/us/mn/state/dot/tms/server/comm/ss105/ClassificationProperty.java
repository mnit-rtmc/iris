/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2012  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.VehLengthClass;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * A Classification Property contains vehicle classification length thresholds.
 *
 * @author Douglas Lau
 */
public class ClassificationProperty extends MemoryProperty {

	/** Minimum length for SHORT vehicle classification */
	private int short_min = VehLengthClass.MOTORCYCLE.bound + 1;

	/** Maximum length for SHORT vehicle classification */
	private int short_max = VehLengthClass.SHORT.bound;

	/** Minimum length for MEDIUM vehicle classification */
	private int medium_min = VehLengthClass.SHORT.bound + 1;

	/** Maximum length for MEDIUM vehicle classification */
	private int medium_max = VehLengthClass.MEDIUM.bound;

	/** Minimum length for LONG vehicle classification */
	private int long_min = VehLengthClass.MEDIUM.bound + 1;

	/** Maximum length for LONG vehicle classification */
	private int long_max = VehLengthClass.LONG.bound;

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
		return hex(short_min, 4) + hex(short_max, 4) + hex(0, 8) +
		       hex(medium_min, 4) + hex(medium_max, 4) + hex(0, 8) +
		       hex(long_min, 4) + hex(long_max, 4);
	}

	/** Parse the response to a QUERY request */
	protected void parseQuery(String r) throws IOException {
		try {
			short_min = Integer.parseInt(r.substring(0, 4), 16);
			short_max = Integer.parseInt(r.substring(4, 8), 16);
			medium_min = Integer.parseInt(r.substring(16, 20), 16);
			medium_max = Integer.parseInt(r.substring(20, 24), 16);
			long_min = Integer.parseInt(r.substring(32, 36), 16);
			long_max = Integer.parseInt(r.substring(36, 40), 16);
		}
		catch(NumberFormatException e) {
			throw new ParsingException(
				"Invalid classification lengths: " + r);
		}
	}

	/** Is the classification set to the default values? */
	public boolean isDefault() {
		return short_min == VehLengthClass.MOTORCYCLE.bound + 1 &&
		       short_max == VehLengthClass.SHORT.bound &&
		       medium_min == VehLengthClass.SHORT.bound + 1 &&
		       medium_max == VehLengthClass.MEDIUM.bound &&
		       long_min == VehLengthClass.MEDIUM.bound + 1 &&
		       long_max == VehLengthClass.LONG.bound;
	}

	/** Get a string representation */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Classification: ");
		sb.append(short_min);
		sb.append('-');
		sb.append(short_max);
		sb.append(',');
		sb.append(medium_min);
		sb.append('-');
		sb.append(medium_max);
		sb.append(',');
		sb.append(long_min);
		sb.append('-');
		sb.append(long_max);
		return sb.toString();
	}
}
