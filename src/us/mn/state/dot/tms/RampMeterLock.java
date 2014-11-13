/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2003-2014  Minnesota Department of Transportation
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
 * A ramp meter lock specifies the reason for locking a meter.
 *
 * @author Douglas Lau
 */
public enum RampMeterLock {

	/** Placeholder for lock-off status (0) */
	OFF(" ", true),

	/** Lock for maintenance status (1) */
	MAINTENANCE("Maintenance", false),

	/** Lock for incident status (2) */
	INCIDENT("Incident", false),

	/** Lock for construction status (3) */
	CONSTRUCTION("Construction", false),

	/** Lock for testing status (4) */
	TESTING("Testing", false),

	/** Lock by police panel status (5) */
	POLICE_PANEL("Police panel", true),

	/** Lock by manual metering status (6) */
	MANUAL("Manual mode", true);

	/** Create a new meter lock */
	private RampMeterLock(String d, boolean cl) {
		description = d;
		controller_lock = cl;
	}

	/** Description of the lock reason */
	public final String description;

	/** Flag to indicate lock triggered by controller */
	public final boolean controller_lock;

	/** Get a ramp meter lock from an ordinal value */
	static public RampMeterLock fromOrdinal(Integer o) {
		return (o != null && o > 0 && o < values().length)
		      ? values()[o]
		      : null;
	}

	/** Get an array of lock descriptions */
	static public String[] getDescriptions() {
		LinkedList<String> d = new LinkedList<String>();
		for (RampMeterLock lock: values())
			d.add(lock.description);
		return d.toArray(new String[0]);
	}
}
