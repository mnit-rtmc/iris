/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2026  Minnesota Department of Transportation
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

/**
 * Toll mode for phase actions
 *
 * @author Douglas Lau
 */
public class TollMode {

	/** Data field */
	static public enum Field {
		PRICED, OPEN, CLOSED
	}

	/** Toll zone ID */
	public final String id;

	/** Data field */
	public final Field field;

	/** Create a new toll mode */
	private TollMode(String i, Field f) {
		id = i;
		field = f;
	}

	/** Get the string description */
	@Override
	public String toString() {
		return "" + id + "," + field;
	}

	/** Parse a toll mode parameter */
	static public TollMode parse(String p) {
		String[] v = p.split(",", 2);
		if (v.length == 2) {
			String i = v[0].trim();
			if (!i.isEmpty()) {
				Field f = parseField(v[1]);
				if (f != null)
					return new TollMode(i, f);
			}
		}
		return null;
	}

	/** Parse a data field */
	static private Field parseField(String f) {
		f = f.trim().toLowerCase();
		if ("priced".startsWith(f))
			return Field.PRICED;
		else if ("open".startsWith(f))
			return Field.OPEN;
		else if ("closed".startsWith(f))
			return Field.CLOSED;
		else
			return null;
	}
}
