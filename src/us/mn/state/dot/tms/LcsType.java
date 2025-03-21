/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2025  Minnesota Department of Transportation
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
 * LCS type enumeration.  The ordinal values correspond to the records in the
 * iris.lcs_type look-up table.
 *
 * @author Douglas Lau
 */
public enum LcsType {

	/** Over lane dedicated (0) */
	OVER_LANE_DEDICATED("Over lane dedicated"),

	/** Over lane DMS (1) */
	OVER_LANE_DMS("Over lane DMS"),

	/** Pavement LED (2) */
	PAVEMENT_LED("Pavement LED");

	/** Create a new LCS type */
	private LcsType(String d) {
		description = d;
	}

	/** Description of the type */
	public final String description;

	/** Get the string representation */
	@Override
	public String toString() {
		return description;
	}

	/** Get LCS type from an ordinal value */
	static public LcsType fromOrdinal(int o) {
		if (o >= 0 && o < values().length)
			return values()[o];
		else
			return OVER_LANE_DEDICATED;
	}
}
