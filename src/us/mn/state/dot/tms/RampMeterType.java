/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008  Minnesota Department of Transportation
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

import java.util.LinkedList;

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

	/** Number of lanes */
	public final int lanes;

	/** Get a ramp meter type from an ordinal value */
	static public RampMeterType fromOrdinal(int o) {
		for(RampMeterType t: RampMeterType.values()) {
			if(t.ordinal() == o)
				return t;
		}
		return DUAL_ALTERNATE;
	}

	/** Get an array of ramp meter type descriptions */
	static public String[] getDescriptions() {
		LinkedList<String> d = new LinkedList<String>();
		for(RampMeterType t: RampMeterType.values())
			d.add(t.description);
		return d.toArray(new String[0]);
	}
}
