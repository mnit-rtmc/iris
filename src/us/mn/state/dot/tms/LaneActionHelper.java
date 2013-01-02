/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2013  Minnesota Department of Transportation
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
 * Helper class for lane actions.
 *
 * @author Douglas Lau
 */
public class LaneActionHelper extends BaseHelper {

	/** Don't allow instances to be created */
	private LaneActionHelper() {
		assert false;
	}

	/** Lookup the lane action with the specified name */
	static public LaneAction lookup(String name) {
		return (LaneAction)namespace.lookupObject(LaneAction.SONAR_TYPE,
			name);
	}

	/** Get a lane action iterator */
	static public Iterator<LaneAction> iterator() {
		return new IteratorWrapper<LaneAction>(namespace.iterator(
			LaneAction.SONAR_TYPE));
	}
}
