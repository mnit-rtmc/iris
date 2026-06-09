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
 * Alert condition for phase actions
 *
 * @author Douglas Lau
 */
public class AlertCondition {

	/** Data field */
	static public enum Field {
		BEFORE, DURING, AFTER, EXPIRED
	}

	/** Alert ID */
	public final String id;

	/** Data field */
	public final Field field;

	/** Create a new alert condition */
	private AlertCondition(String i, Field f) {
		id = i;
		field = f;
	}

	/** Get the string description */
	@Override
	public String toString() {
		return "" + id + "," + field;
	}

	/** Parse an alert condition parameter */
	static public AlertCondition parse(String p) {
		String[] v = p.split(",", 2);
		if (v.length == 2) {
			String i = v[0].trim();
			if (!i.isEmpty()) {
				Field f = parseField(v[1]);
				if (f != null)
					return new AlertCondition(i, f);
			}
		}
		return null;
	}

	/** Parse a data field */
	static private Field parseField(String f) {
		f = f.trim().toLowerCase();
		if ("before".startsWith(f))
			return Field.BEFORE;
		else if ("during".startsWith(f))
			return Field.DURING;
		else if ("after".startsWith(f))
			return Field.AFTER;
		else if ("expired".startsWith(f))
			return Field.EXPIRED;
		else
			return null;
	}
}
