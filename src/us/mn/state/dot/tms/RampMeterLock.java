/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2003-2009  Minnesota Department of Transportation
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

	/** Placeholder for lock-off status */
	OFF(" "),

	/** Lock knock down status */
	KNOCK_DOWN("Knocked down"),

	/** Lock for incident status */
	INCIDENT("Incident"),

	/** Lock testing status */
	TESTING("Testing"),

	/** Lock by police panel status */
	POLICE_PANEL("Police panel"),

	/** Lock by manual metering status */
	MANUAL("Manual mode"),

	/** Lock other status */
	OTHER("Other reason");

	/** Create a new meter lock */
	private RampMeterLock(String d) {
		description = d;
	}

	/** Description of the lock reason */
	public final String description;

	/** Get a ramp meter lock from an ordinal value */
	static public RampMeterLock fromOrdinal(Integer o) {
		if(o != null && o > 0 && o < values().length)
			return values()[o];
		else
			return null;
	}

	/** Get an array of lock descriptions */
	static public String[] getDescriptions() {
		LinkedList<String> d = new LinkedList<String>();
		for(RampMeterLock lock: RampMeterLock.values())
			d.add(lock.description);
		return d.toArray(new String[0]);
	}

	/** Check if a lock value is a "controller-only" lock */
	static public boolean isControllerLock(Integer l) {
		if(l == RampMeterLock.POLICE_PANEL.ordinal())
			return true;
		if(l == RampMeterLock.MANUAL.ordinal())
			return true;
		return false;
	}
}
