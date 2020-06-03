/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2020  Minnesota Department of Transportation
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
 * Impact for one mainline lane (or shoulder).
 *
 * @author Douglas Lau
 */
public enum LaneImpact {
	FREE_FLOWING('.'),
	AFFECTED('?'),
	BLOCKED('!');

	/** Character for encoding impact */
	public final char _char;

	/** Create a new lane impact */
	private LaneImpact(char c) {
		_char = c;
	}

	/** Lookup a lane impact from a character code */
	static public LaneImpact fromChar(char c) {
		for (LaneImpact v: LaneImpact.values()) {
			if (v._char == c)
				return v;
		}
		return null;
	}

	/** Create an array of lane impacts from a coded string.
	 * @param im Coded string of impact by lane.
	 * @return Array of impact values, one per lane. */
	static public LaneImpact[] fromString(String im) {
		LaneImpact[] imp = new LaneImpact[im.length()];
		for (int i = 0; i < imp.length; i++) {
			LaneImpact v = fromChar(im.charAt(i));
			imp[i] = (v != null) ? v : FREE_FLOWING;
		}
		return imp;
	}

	/** Create a coded impact string from an array.
	 * @param imp Array of impact values, one per lane.
	 * @return Coded string of impact by lane. */
	static public String fromArray(LaneImpact[] imp) {
		StringBuilder sb = new StringBuilder();
		for (LaneImpact v: imp)
			sb.append((v != null) ? v._char : FREE_FLOWING._char);
		return sb.toString();
	}

	/** Create an impact string for the given number of lanes.
	 * @param n_lanes Number of lanes.
	 * @return Coded string of impact by lane. */
	static public String fromLanes(int n_lanes) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < n_lanes + 2; i++)
			sb.append(FREE_FLOWING._char);
		return sb.toString();
	}
}
