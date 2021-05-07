/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2019  Minnesota Department of Transportation
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
 * Direction-of-travel enumeration.  The ordinal values correspond to the
 * records in the iris.direction look-up table.
 *
 * @author Douglas Lau
 */
public enum Direction {

	/** Enumerated direction values */
	UNKNOWN(" ", "", ""),                   // 0
	NORTH("Northbound", "NB", "N"),         // 1
	SOUTH("Southbound", "SB", "S"),         // 2
	EAST("Eastbound", "EB", "E"),           // 3
	WEST("Westbound", "WB", "W"),           // 4
	NORTH_SOUTH("North-South", "NS", "NS"), // 5
	EAST_WEST("East-West", "EW", "EW"),     // 6
	SURFACE("Surface", "SUR", "SUR");       // 7 Roadway Surface

	/** Direction description */
	public final String description;

	/** Direction abbreviation */
	public final String abbrev;

	/** Detector abbreviation */
	public final String det_dir;

	/** Create a new direction */
	private Direction(String d, String a, String dt) {
		description = d;
		abbrev = a;
		det_dir = dt;
	}

	/** Get the string representation */
	@Override
	public String toString() {
		return description;
	}

	/** Check if an ordinal value is valid */
	static public boolean isValid(short o) {
		return fromOrdinal(o).ordinal() == o;
	}

	/** Get a direction from an ordinal value */
	static public Direction fromOrdinal(short o) {
		if (o >= 0 && o < values().length)
			return values()[o];
		else
			return UNKNOWN;
	}

	/** Check if two directions are opposite */
	static public boolean isOpposite(short o0, short o1) {
		Direction d0 = fromOrdinal(o0);
		Direction d1 = fromOrdinal(o1);
		switch (d0) {
			case NORTH: return d1 == SOUTH;
			case SOUTH: return d1 == NORTH;
			case EAST: return d1 == WEST;
			case WEST: return d1 == EAST;
			default: return false;
		}
	}
}
