/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2010  Minnesota Department of Transportation
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
 * Location modifier enumeration.  The ordinal values correspond to the
 * records in the iris.road_modifier look-up table.
 *
 * @author Douglas Lau
 */
public enum LocModifier {

	/** Enumerated location modifier values */
	AT("@", ""),				// 0
	NORTH_OF("N of", "N"),			// 1
	SOUTH_OF("S of", "S"),			// 2
	EAST_OF("E of", "E"),			// 3
	WEST_OF("W of", "W"),			// 4
	NORTH_JUNCTION("N Jct", "Nj"),		// 5
	SOUTH_JUNCTION("S Jct", "Sj"),		// 6
	EAST_JUNCTION("E Jct", "Ej"),		// 7
	WEST_JUNCTION("W Jct", "Wj");		// 8

	/** Description of location modifier */
	public final String description;

	/** Abbreviated description */
	public final String abbrev;

	/** Create a new location modifier */
	private LocModifier(String d, String a) {
		description = d;
		abbrev = a;
	}

	/** Get the string representation of a location modifier */
	public String toString() {
		return description;
	}

	/** Get a location modifier from an ordinal value */
	static public LocModifier fromOrdinal(short o) {
		for(LocModifier m: LocModifier.values()) {
			if(m.ordinal() == o)
				return m;
		}
		return AT;
	}

	/** Check if an ordinal value is valid */
	static public boolean isValid(short o) {
		return fromOrdinal(o).ordinal() == o;
	}
}
