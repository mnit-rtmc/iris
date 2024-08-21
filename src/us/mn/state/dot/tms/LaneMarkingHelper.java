/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2024  Minnesota Department of Transportation
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
 * Lane marking helper methods.
 *
 * @author Douglas Lau
 */
public class LaneMarkingHelper extends BaseHelper {

	/** Disallow instantiation */
	private LaneMarkingHelper() {
		assert false;
	}

	/** Lookup the lane marking with the specified name */
	static public LaneMarking lookup(String name) {
		return (LaneMarking) namespace.lookupObject(
			LaneMarking.SONAR_TYPE, name);
	}

	/** Get a lane marking iterator */
	static public Iterator<LaneMarking> iterator() {
		return new IteratorWrapper<LaneMarking>(namespace.iterator(
			LaneMarking.SONAR_TYPE));
	}
}
