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

	/** Get a range from an ordinal value */
	static public IncRange fromOrdinal(int o) {
		if (o >= 0 && o < values().length)
			return values()[o];
		else
			return null;
	}

	/** Get a range from a number of exits */
	static public IncRange fromExits(int exits) {
		switch (exits) {
		case 0: return IncRange.ahead;
		case 1: return IncRange.near;
		case 2:
		case 3:
		case 4: return IncRange.middle;
		case 5:
		case 6:
		case 7:
		case 8: return IncRange.far;
		default: return null;
		}
	}

	/** Get a maximum number of exits */
	public int getMaxExits() {
		switch (this) {
		case near: return 1;
		case middle: return 4;
		case far: return 8;
		default: return 0;
		}
	}
}
