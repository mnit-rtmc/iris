/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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
 * An LCS array lock specifies the reason for locking an LCS array.
 *
 * @author Douglas Lau
 */
public enum LCSArrayLock {

	/** Placeholder for lock-off status */
	OFF(" "),

	/** Lock for incident status */
	INCIDENT("Incident"),

	/** Lock maintenance status */
	MAINTENANCE("Maintenance"),

	/** Lock testing status */
	TESTING("Testing"),

	/** Lock other status */
	OTHER("Other reason");

	/** Create a new LCS array lock */
	private LCSArrayLock(String d) {
		description = d;
	}

	/** Description of the lock reason */
	public final String description;

	/** Get an LCS array lock from an ordinal value */
	static public LCSArrayLock fromOrdinal(Integer o) {
		if(o != null && o > 0 && o < values().length)
			return values()[o];
		else
			return null;
	}

	/** Get an array of lock descriptions */
	static public String[] getDescriptions() {
		LinkedList<String> d = new LinkedList<String>();
		for(LCSArrayLock lock: LCSArrayLock.values())
			d.add(lock.description);
		return d.toArray(new String[0]);
	}
}
