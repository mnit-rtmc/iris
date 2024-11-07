/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2024  Minnesota Department of Transportation
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
 * Ramp meter queue state
 *
 * @author Douglas Lau
 */
public enum MeterQueueState {

	/** Queue unknown state */
	UNKNOWN("Unknown"),

	/** Queue empty state */
	EMPTY("Empty"),

	/** Queue exists state */
	EXISTS("Exists"),

	/** Queue full state */
	FULL("Full");

	/** Create a new meter queue state */
	private MeterQueueState(String d) {
		description = d;
	}

	/** Get the string representation */
	@Override
	public String toString() {
		return description;
	}

	/** Description of the queue state */
	public final String description;

	/** Get a meter queue state from an ordinal value */
	static public MeterQueueState fromOrdinal(int o) {
		if (o >= 0 && o < values().length)
			return values()[o];
		else
			return UNKNOWN;
	}
}
