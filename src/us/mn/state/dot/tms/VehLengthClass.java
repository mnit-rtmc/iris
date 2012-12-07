/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012  Minnesota Department of Transportation
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
 * Vehicle length class.
 *
 * @author Douglas Lau
 */
public enum VehLengthClass {
	MOTORCYCLE(7),	/* 0 to 7 feet */
	SHORT(20),	/* 7 to 20 feet */
	MEDIUM(43),	/* 20 to 43 feet */
	LONG(255);	/* 43+ feet */

	/** Upper bound of vehicle length (feet) */
	public final int bound;

	/** Create a new vehicle length class */
	private VehLengthClass(int b) {
		bound = b;
	}

	/** Size of enum */
	static public final int size = values().length;

	/** Get a vehicle length class from an ordinal */
	static public VehLengthClass fromOrdinal(int o) {
		for(VehLengthClass vc: VehLengthClass.values()) {
			if(vc.ordinal() == o)
				return vc;
		}
		return SHORT;
	}
}
