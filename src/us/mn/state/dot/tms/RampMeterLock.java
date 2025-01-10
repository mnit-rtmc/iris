/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2003-2025  Minnesota Department of Transportation
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
 * A ramp meter lock specifies the reason for locking a meter.
 *
 * @author Douglas Lau
 */
public enum RampMeterLock {

	/** Placeholder for lock-off status (0) */
	OFF(" "),

	/** Lock for maintenance status (1) */
	MAINTENANCE("Maintenance"),

	/** Lock for incident status (2) */
	INCIDENT("Incident"),

	/** Lock for construction status (3) */
	CONSTRUCTION("Construction"),

	/** Lock for testing status (4) */
	TESTING("Testing"),

	/** Lock for knocked-down status (5) */
	KNOCKED_DOWN("Knocked Down");

	/** Create a new meter lock */
	private RampMeterLock(String d) {
		description = d;
	}

	/** Description of the lock reason */
	public final String description;

	/** Get the string representation */
	@Override
	public String toString() {
		return description;
	}

	/** Get a ramp meter lock from an ordinal value */
	static public RampMeterLock fromOrdinal(Integer o) {
		return (o != null && o > 0 && o < values().length)
		      ? values()[o]
		      : null;
	}
}
