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
 * Alarm condition for phase actions
 *
 * @author Douglas Lau
 */
public class AlarmCondition {

	/** Data field */
	static public enum Field {
		TRIGGERED, CLEARED
	}

	/** Alarm ID */
	public final String id;

	/** Data field */
	public final Field field;

	/** Create a new alarm condition */
	private AlarmCondition(String i, Field f) {
		id = i;
		field = f;
	}

	/** Get the string description */
	@Override
	public String toString() {
		return "" + id + "," + field;
	}

	/** Parse an alarm condition parameter */
	static public AlarmCondition parse(String p) {
		String[] v = p.split(",", 2);
		if (v.length == 2) {
			String i = v[0].trim();
			if (!i.isEmpty()) {
				Field f = parseField(v[1]);
				if (f != null)
					return new AlarmCondition(i, f);
			}
		}
		return null;
	}

	/** Parse a data field */
	static private Field parseField(String f) {
		f = f.trim().toLowerCase();
		if ("triggered".startsWith(f))
			return Field.TRIGGERED;
		else if ("cleared".startsWith(f))
			return Field.CLEARED;
		else
			return null;
	}
}
