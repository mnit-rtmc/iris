/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2010  Minnesota Department of Transportation
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

import java.util.LinkedList;
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

	/** Find LCS arrays using a Checker */
	static public LCSArray find(Checker<LCSArray> checker) {
		return (LCSArray)namespace.findObject(LCSArray.SONAR_TYPE,
			checker);
	}

	/** Lookup the LCS objects for an array */
	static public LCS[] lookupLCSs(LCSArray lcs_array) {
		final TreeMap<Integer, LCS> lanes = new TreeMap<Integer, LCS>();
		lookupLCS(lcs_array, new Checker<LCS>() {
			public boolean check(LCS lcs) {
				lanes.put(lcs.getLane(), lcs);
				return false;
			}
		});
		int n_lanes = 0;
		if(lanes.size() > 0)
			n_lanes = lanes.lastKey();
		LCS[] lcss = new LCS[n_lanes];
		for(int i = 0; i < n_lanes; i++)
			lcss[i] = lanes.get(i + 1);
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

	/** Lookup the DMS for the LCS in the specified lane */
	static public DMS lookupDMS(LCSArray lcs_array, int lane) {
		LCS lcs = lookupLCS(lcs_array, lane);
		if(lcs != null)
			return DMSHelper.lookup(lcs.getName());
		else
			return null;
	}

	/** Lookup the camera for an LCS array */
	static public Camera getCamera(LCSArray lcs_array) {
		if(lcs_array != null)
			return DMSHelper.getCamera(lookupDMS(lcs_array, 1));
		else
			return null;
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

	/** Lookup the location of the LCS array */
	static public String lookupLocation(LCSArray lcs_array) {
		return GeoLocHelper.getDescription(lookupGeoLoc(lcs_array));
	}

	/** Lookup the location of the LCS array */
	static public GeoLoc lookupGeoLoc(LCSArray lcs_array) {
		DMS dms = lookupDMS(lcs_array, 1);
		if(dms != null)
			return dms.getGeoLoc();
		else
			return null;
	}

	/** Get the controller status */
	static public String lookupStatus(LCSArray lcs_array) {
		final LinkedList<String> status = new LinkedList<String>();
		status.add("???");
		lookupLCS(lcs_array, new Checker<LCS>() {
			public boolean check(LCS lcs) {
				DMS dms = DMSHelper.lookup(lcs.getName());
				if(dms != null) {
					Controller c = dms.getController();
					if(c != null) {
						String s = c.getStatus();
						status.add(s);
						return !"".equals(s);
					}
				}
				return false;
			}
		});
		return status.getLast();
	}

	/** Check if an LCS array is failed */
	static public boolean isFailed(final LCSArray lcs_array) {
		final LinkedList<LCS> lcss = new LinkedList<LCS>();
		lookupLCS(lcs_array, new Checker<LCS>() {
			public boolean check(LCS lcs) {
				lcss.add(lcs);
				return false;
			}
		});
		for(LCS lcs: lcss) {
			if(LCSHelper.isFailed(lcs))
				return true;
		}
		return false;
	}

	/** Check if all LCSs in an array are failed */
	static public boolean isAllFailed(final LCSArray lcs_array) {
		final LinkedList<LCS> lcss = new LinkedList<LCS>();
		lookupLCS(lcs_array, new Checker<LCS>() {
			public boolean check(LCS lcs) {
				lcss.add(lcs);
				return false;
			}
		});
		for(LCS lcs: lcss) {
			if(!LCSHelper.isFailed(lcs))
				return false;
		}
		return true;
	}

	/** Check if any LCSs in an array need maintenance */
	static public boolean needsMaintenance(final LCSArray lcs_array) {
		final LinkedList<LCS> lcss = new LinkedList<LCS>();
		lookupLCS(lcs_array, new Checker<LCS>() {
			public boolean check(LCS lcs) {
				lcss.add(lcs);
				return false;
			}
		});
		for(LCS lcs: lcss) {
			if(LCSHelper.needsMaintenance(lcs))
				return true;
		}
		return false;
	}
}
