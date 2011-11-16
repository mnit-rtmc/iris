/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2011  Minnesota Department of Transportation
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

import java.util.regex.Pattern;
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
	 * @param w Sign width (pixels).
	 * @param h Sign height (pixels).
	 * @return A lane-use MULTI. */
	static public LaneUseMulti find(final int ind, final int w,
		final int h)
	{
		return find(new Checker<LaneUseMulti>() {
			public boolean check(LaneUseMulti lum) {
				return lum.getIndication() == ind &&
				       lum.getWidth() == w &&
				       lum.getHeight() == h;
			}
		});
	}

	/** Lookup the lane-use MULTI with the specified name */
	static public LaneUseMulti lookup(String name) {
		return (LaneUseMulti)namespace.lookupObject(
			LaneUseMulti.SONAR_TYPE, name);
	}

	/** Find a lane-use MULTI which matches a MULTI string */
	static public LaneUseMulti find(final String multi) {
		return find(new Checker<LaneUseMulti>() {
			public boolean check(LaneUseMulti lum) {
				QuickMessage qm = lum.getQuickMessage();
				return qm != null &&
				       match(qm.getMulti(), multi);
			}
		});
	}

	/** Test if a quick message matches a multi string */
	static protected boolean match(String qm, String multi) {
		return Pattern.matches(createRegex(qm), multi);
	}

	/** Create a regex which matches any speed advisory values */
	static protected String createRegex(String qm) {
		MultiString re = new MultiString() {
			public void addSpeedAdvisory() {
				// Add unquoted regex to match 2 digits
				addSpan("\\E[0-9].\\Q");
			}
		};
		// Start quoting for regex
		re.addSpan("\\Q");
		MultiParser.parse(qm, re);
		// End quoting for regex
		re.addSpan("\\E");
		return re.toString();
	}
}
