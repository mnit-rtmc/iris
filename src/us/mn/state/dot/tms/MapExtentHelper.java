/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010  AHMCT, University of California
 * Copyright (C) 2011  Minnesota Department of Transportation
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
 * Helper class for MapExtent.
 *
 * @author Michael Darter
 */
public class MapExtentHelper extends BaseHelper {

	/** Don't instantiate */
	private MapExtentHelper() {
		assert false;
	}

	/** Lookup the MapExtent with the specified name */
	static public MapExtent lookup(String name) {
		return (MapExtent)namespace.lookupObject(MapExtent.SONAR_TYPE,
			name);
	}
}
