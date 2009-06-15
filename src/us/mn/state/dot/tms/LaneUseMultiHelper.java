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
 * Helper class for LaneUseMulti.
 *
 * @author Douglas Lau
 */
public class LaneUseMultiHelper extends BaseHelper {

	/** Prevent object creation */
	private LaneUseMultiHelper() {
		assert false;
	}

	/** Find LaneUseMulti using a Checker */
	static public LaneUseMulti find(Checker<LaneUseMulti> checker) {
		return (LaneUseMulti)namespace.findObject(
			LaneUseMulti.SONAR_TYPE, checker);
	}

	/** Get a lane-use MULTI for a given indication.
	 * @param ind LaneUseIndication ordinal value.
	 * @return A lane-use MULTI. */
	static public LaneUseMulti find(final int ind) {
		return find(new Checker<LaneUseMulti>() {
			public boolean check(LaneUseMulti lum) {
				return lum.getIndication() == ind;
			}
		});
	}
}
