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

import us.mn.state.dot.sonar.Checker;

/**
 * Lane marking helper methods.
 *
 * @author Douglas Lau
 */
public class LaneMarkingHelper extends BaseHelper {

	/** Disallow instantiation */
	protected LaneMarkingHelper() {
		assert false;
	}

	/** Find lane markings using a Checker */
	static public LaneMarking find(final Checker<LaneMarking> checker) {
		return (LaneMarking)namespace.findObject(LaneMarking.SONAR_TYPE,
			checker);
	}

	/** Lookup the lane marking with the specified name */
	static public LaneMarking lookup(String name) {
		return (LaneMarking)namespace.lookupObject(
			LaneMarking.SONAR_TYPE, name);
	}
}
