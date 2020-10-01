/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2020  Minnesota Department of Transportation
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
 * Road class enumeration.  The ordinal values correspond to the records in the
 * iris.road_class look-up table.
 *
 * @author Douglas Lau
 */
public enum RoadClass {

	/** Enumerated road class values */
	NONE(" ", "", 1f),                   // 0
	RESIDENTIAL("Residential", "A", 2f), // 1
	BUSINESS("Business", "B", 3f),       // 2
	COLLECTOR("Collector", "C", 3f),     // 3
	ARTERIAL("Arterial", "D", 4f),       // 4
	EXPRESSWAY("Expressway", "E", 4f),   // 5
	FREEWAY("Freeway", "F", 6f),         // 6
	CD_ROAD("CD Road", "", 3.5f);        // 7

	/** Description of road class */
	public final String description;

	/** Letter Grade */
	public final String grade;

	/** Scale for map display */
	public final float scale;

	/** Create a new road class */
	private RoadClass(String d, String g, float s) {
		description = d;
		grade = g;
		scale = s;
	}

	/** Get the string representation of a road class */
	@Override
	public String toString() {
		return description;
	}

	/** Get a road class from an ordinal value */
	static public RoadClass fromOrdinal(short o) {
		if (o >= 0 && o < values().length)
			return values()[o];
		else
			return NONE;
	}

	/** Check if an ordinal value is valid */
	static public boolean isValid(short o) {
		return fromOrdinal(o).ordinal() == o;
	}
}
