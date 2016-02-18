/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2016  Minnesota Department of Transportation
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
 * Ramp meter type enumeration.
 *
 * @author Douglas Lau
 */
public enum RampMeterType {

	/** Single lane */
	SINGLE("One Lane", 1),

	/** Two-lane alternate release */
	DUAL_ALTERNATE("Two Lane, Alternate Release", 2),

	/** Two-lane simultaneous release (drag race) */
	DUAL_SIMULTANEOUS("Two Lane, Simultaneous Release", 2);

	/** Create a new ramp meter type */
	private RampMeterType(String d, int l) {
		description = d;
		lanes = l;
	}

	/** Description of the type */
	public final String description;

	/** Get the string representation */
	@Override
	public String toString() {
		return description;
	}

	/** Number of lanes */
	public final int lanes;

	/** Get a ramp meter type from an ordinal value */
	static public RampMeterType fromOrdinal(int o) {
		if (o >= 0 && o < values().length)
			return values()[o];
		else
			return DUAL_ALTERNATE;
	}
}
