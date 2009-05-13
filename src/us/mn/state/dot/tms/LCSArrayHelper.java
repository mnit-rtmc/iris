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

import java.util.TreeMap;
import us.mn.state.dot.sonar.Checker;

/**
 * Helper class for LCSArrays.
 *
 * @author Douglas Lau
 */
public class LCSArrayHelper extends BaseHelper {

	/** Prevent object creation */
	private LCSArrayHelper() {
		assert false;
	}

	/** Lookup the LCS objects for an array */
	static public LCS[] lookupLCSs(LCSArray lcs_array) {
		final TreeMap<Integer, LCS> lanes = new TreeMap<Integer, LCS>();
		lookupLCS(lcs_array, new Checker<LCS>() {
			public boolean check(LCS lcs) {
				lanes.put(lcs.getLane(), lcs);
			}
		});
		int n_lanes = 0;
		if(lanes.size() > 0)
			n_lanes = lanes.lastKey();
		LCS[] lcss = new LCS[n_lanes];
		for(int i = 0; i < n_lanes; i++)
			lcss[i] = lanes.get(i);
		return lcss;
	}

	/** Lookup the LCS in the specified lane */
	static public LCS lookupLCS(LCSArray lcs_array, final int lane) {
		return lookupLCS(lcs_array, new Checker<LCS>() {
			public boolean check(LCS lcs) {
				return lcs.getLane() == lane;
			}
		});
	}

	/** Lookup the LCS objects for an array */
	static public LCS lookupLCS(final LCSArray lcs_array,
		final Checker<LCS> checker)
	{
		return (LCS)namespace.findObject(LCS.SONAR_TYPE,
			new Checker<LCS>()
		{
			public boolean check(LCS lcs) {
				if(lcs.getArray() == lcs_array)
					return checker.check(lcs);
				else
					return false;
			}
		});
	}
}
