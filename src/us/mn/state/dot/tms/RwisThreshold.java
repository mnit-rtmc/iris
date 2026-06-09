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
 * RWIS data threshold for phase actions
 *
 * @author Douglas Lau
 */
public class RwisThreshold {

	/** Data field */
	static public enum Field {
		FRICTION, SURFACE_TEMP, WIND_GUST, VISIBILITY, PRECIPITATION
	}

	/** Weather sensor ID */
	public final String id;

	/** Data field */
	public final Field field;

	/** Greater than (vs less than) */
	public final boolean greater;

	/** Threshold value */
	public final int value;

	/** Create a new RWIS threshold */
	private RwisThreshold(String i, Field f, boolean g, int v) {
		id = i;
		field = f;
		greater = g;
		value = v;
	}

	/** Get the string description */
	@Override
	public String toString() {
		if (greater)
			return "" + id + "," + field + ">" + value;
		else
			return "" + id + "," + field + "<" + value;
	}

	/** Parse an RWIS threshold parameter */
	static public RwisThreshold parse(String p) {
		String[] v = p.split(",", 2);
		if (v.length == 2) {
			String i = v[0].trim();
			if (i.isEmpty())
				return null;
			String[] lt = v[1].split("<", 2);
			if (lt.length == 2) {
				Field f = parseField(lt[0]);
				if (f != null) {
					Integer t = parseValue(lt[1]);
					return new RwisThreshold(i, f, false, t);
				}
			}
			String[] gt = v[1].split(">", 2);
			if (gt.length == 2) {
				Field f = parseField(gt[0]);
				if (f != null) {
					Integer t = parseValue(gt[1]);
					return new RwisThreshold(i, f, true, t);
				}
			}
		}
		return null;
	}

	/** Parse a data field */
	static private Field parseField(String f) {
		f = f.trim().toLowerCase();
		if ("friction".startsWith(f))
			return Field.FRICTION;
		else if ("surface_temp".startsWith(f))
			return Field.SURFACE_TEMP;
		else if ("wind_gust".startsWith(f))
			return Field.WIND_GUST;
		else if ("visibility".startsWith(f))
			return Field.VISIBILITY;
		else if ("precipitation".startsWith(f))
			return Field.PRECIPITATION;
		else
			return null;
	}

	/** Parse a threshold value */
	static private Integer parseValue(String v) {
		try {
			return Integer.parseUnsignedInt(v.trim());
		}
		catch (NumberFormatException e) {
			return null;
		}
	}
}
