/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017  Iteris Inc.
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
 * Pavement sensor errors as defined by essPavementSensorError in NTCIP 1204.
 *
 * @author Michael Darter
 */
public enum PavementSensorError {

	/** Pavement sensor errors defined by essPavementSensorError */
	UNDEFINED("???"),
	OTHER("Other"),	
	NONE("None"),
	NO_RESPONSE("noResponse"),
	CUT_CABLE("cutCable"),
	SHORT_CIRCUIT("shortCircuit"),
	DIRTY_LENSE("dirtyLense");

	/** Description string */
	public final String description;

	/** Constructor */
	private PavementSensorError(String d) {
		description = d;
	}

	/** Get an enum from an ordinal value */
	static public PavementSensorError fromOrdinal(Integer o) {
		if (o != null && o >= 0 && o < values().length)
			return values()[o];
		else
			return UNDEFINED;
	}
}
