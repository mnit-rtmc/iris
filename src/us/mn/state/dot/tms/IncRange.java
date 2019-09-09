/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016-2019  Minnesota Department of Transportation
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
 * Incident range enumeration.  The ordinal values correspond to the records in
 * the iris.inc_range look-up table.
 *
 * @author Douglas Lau
 */
public enum IncRange {
	ahead,  // 0
	near,   // 1
	middle, // 2
	far;    // 3

	/** Values array */
	static private final IncRange[] VALUES = values();

	/** Get a range from an ordinal value */
	static public IncRange fromOrdinal(int o) {
		return (o >= 0 && o < VALUES.length) ? VALUES[o] : null;
	}

	/** Get range to an incident.
	 * @param exits number of exits.
	 * @param ahead_dist Distance below `ahead` threshold. */
	static public IncRange fromExits(int exits, boolean ahead_dist) {
		for (IncRange range: VALUES) {
			if (range.isWithin(exits, ahead_dist))
				return range;
		}
		return null;
	}

	/** Check if a number of exits is within range */
	private boolean isWithin(int exits, boolean ahead_dist) {
		// Only within `ahead` range if distance is below threshold
		return (exits <= getMaxExits()) &&
		       (ahead_dist || this != ahead);
	}

	/** Get a maximum number of exits */
	public int getMaxExits() {
		switch (this) {
			case ahead:  return 0;
			case near:   return 1;
			case middle: return 5;
			case far:    return 9;
		}
		return 0; // unreachable
	}
}
