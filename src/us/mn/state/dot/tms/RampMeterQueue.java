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
 * Ramp meter queue status
 *
 * @author Douglas Lau
 */
public enum RampMeterQueue {

	/** Queue unknown status */
	UNKNOWN("Unknown"),

	/** Queue empty status */
	EMPTY("Empty"),

	/** Queue exists status */
	EXISTS("Exists"),

	/** Queue full status */
	FULL("Full");

	/** Create a new meter queue status */
	private RampMeterQueue(String d) {
		description = d;
	}

	/** Get the string representation */
	@Override
	public String toString() {
		return description;
	}

	/** Description of the queue status */
	public final String description;

	/** Get a ramp meter queue status from an ordinal value */
	static public RampMeterQueue fromOrdinal(int o) {
		if (o >= 0 && o < values().length)
			return values()[o];
		else
			return UNKNOWN;
	}
}
