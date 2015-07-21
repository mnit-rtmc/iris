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
package us.mn.state.dot.tms;

import java.util.Iterator;

/**
 * Helper class for toll zones.
 *
 * @author Douglas Lau
 */
public class TollZoneHelper extends BaseHelper {

	/** don't instantiate */
	private TollZoneHelper() {
		assert false;
	}

	/** Lookup the toll zone with the specified name */
	static public TollZone lookup(String name) {
		return (TollZone)namespace.lookupObject(TollZone.SONAR_TYPE,
			name);
	}

	/** Get a toll zone iterator */
	static public Iterator<TollZone> iterator() {
		return new IteratorWrapper<TollZone>(namespace.iterator(
			TollZone.SONAR_TYPE));
	}
}
