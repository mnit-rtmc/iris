/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.dr500;

/**
 * Enum of radar variable names.
 *
 * @author Douglas Lau
 */
public enum VarName {
	UNITS("UN"),		/* 0: mph, 1: kph, 2: fps, 3: mps */
	BIN_MINUTES("BN"),	/* binning interval (1-??) */
	SENSITIVITY("ST"),	/* percentage of max range (10-99) */
	LO_SPEED("LO"),		/* low speed cutoff */
	THRESHOLD_SPEED("SP"),	/* violator threshold */
	HI_SPEED("HI"),		/* high speed cutoff */
	TARGET("SF");		/* fastest target if 1, else strongest */

	/** Create a new variable name */
	private VarName(String n) {
		assert 2 == n.length();
		name = n;
	}

	/** Variable name */
	public final String name;
}
