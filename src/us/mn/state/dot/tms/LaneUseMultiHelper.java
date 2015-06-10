/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2015  Minnesota Department of Transportation
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
 * Helper class for LaneUseMulti.
 *
 * @author Douglas Lau
 */
public class LaneUseMultiHelper extends BaseHelper {

	/** Prevent object creation */
	private LaneUseMultiHelper() {
		assert false;
	}

	/** Lookup the lane-use MULTI with the specified name */
	static public LaneUseMulti lookup(String name) {
		return (LaneUseMulti)namespace.lookupObject(
			LaneUseMulti.SONAR_TYPE, name);
	}

	/** Get a lane-use MULTI iterator */
	static public Iterator<LaneUseMulti> iterator() {
		return new IteratorWrapper<LaneUseMulti>(namespace.iterator(
			LaneUseMulti.SONAR_TYPE));
	}

	/** Get a lane-use MULTI for a given indication.
	 * @param ind LaneUseIndication ordinal value.
	 * @param w Sign width (pixels).
	 * @param h Sign height (pixels).
	 * @return A lane-use MULTI. */
	static public LaneUseMulti find(int ind, int w, int h) {
		Iterator<LaneUseMulti> it = iterator();
		while (it.hasNext()) {
			LaneUseMulti lum = it.next();
			if (lum.getIndication() == ind &&
			    lum.getWidth() == w &&
			    lum.getHeight() == h)
				return lum;
		}
		return null;
	}
}
