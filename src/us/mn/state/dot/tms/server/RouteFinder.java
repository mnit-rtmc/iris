/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server;

import java.util.TreeMap;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.units.Distance;
import static us.mn.state.dot.tms.units.Distance.Units.MILES;

/**
 * A route finder builds a route from an origin to a destination.
 *
 * @author Douglas Lau
 */
public class RouteFinder {

	/** Determine the best of two routes */
	static private Route bestRoute(Route r1, Route r2) {
		if (null == r1)
			return r2;
		else if (null == r2)
			return r1;
		else {
			float g1 = r1.getGoodness();
			float g2 = r2.getGoodness();
			return (g1 <= g2) ? r1 : r2;
		}
	}

	/** Maximum route distance */
	private final Distance dist_max = new Distance(
		SystemAttrEnum.ROUTE_MAX_MILES.getInt(), MILES);

	/** Maximum number of corridor legs */
	private final int legs_max = SystemAttrEnum.ROUTE_MAX_LEGS.getInt();

	/** Corridor manager */
	private final CorridorManager corridors;

	/** Create a new route finder.
	 * @param c Corridor manager. */
	public RouteFinder(CorridorManager c) {
		corridors = c;
	}

	/** Find the best route from an origin to a destination.
	 * @param orig Route origin.
	 * @param dest Route destination.
	 * @return Best route found, or null. */
	public Route findRoute(GeoLoc orig, GeoLoc dest) {
		Route r = findRoute(orig, new Route(dest));
		return (r != null && r.getDistance().compareTo(dist_max) < 0)
		      ? r
		      : null;
	}

	/** Find the best route from an origin to a destination.
	 * @param orig Corridor origin.
	 * @param r Partial route.
	 * @return Completed route, or null if none found. */
	private Route findRoute(GeoLoc orig, Route r) {
		ODPair od = new ODPair(orig, r.getDestination(), false);
		Corridor c = corridors.getCorridor(od);
		if (c != null) {
			Route re = r.createExtended(c, od);
			if (re != null)
				return re;
		}
		if (r.legCount() < legs_max)
			return findBranching(orig, r);
		else
			return null;
	}

	/** Find the best branching route to a destination.
	 * @param orig Corridor origin.
	 * @param r Partial route.
	 * @return Completed route, or null if none found. */
	private Route findBranching(GeoLoc orig, Route r) {
		Corridor c = corridors.getCorridor(orig);
		if (null == c)
			return null;
		Float o_mi = c.calculateMilePoint(orig);
		if (null == o_mi)
			return null;
		Distance rd = dist_max.sub(r.getDistance());
		BranchFinder bf = new BranchFinder(o_mi, r.getDestination(),rd);
		c.findActiveNode(bf);
		Route rb = null;	// best route
		for (R_NodeImpl rn: bf.branches.values()) {
			GeoLoc cd = rn.getGeoLoc();
			for (R_NodeImpl f: rn.getForks()) {
				boolean turn = rn.hasTurnPenalty()
				             && f.hasTurnPenalty();
				ODPair od = new ODPair(orig, cd, turn);
				Route re = r.createExtended(c, od);
				if (re != null) {
					GeoLoc o = f.getGeoLoc();
					rb = bestRoute(rb, findRoute(o, re));
				}
			}
		}
		return rb;
	}

	/** Route branch finder */
	static private class BranchFinder implements Corridor.NodeFinder {
		private final float o_mi;	// corridor origin milepoint
		private final GeoLoc dest;	// destination
		private final Distance r_dist;	// remaining distance
		private final TreeMap<Distance, R_NodeImpl> branches =
			new TreeMap<Distance, R_NodeImpl>();
		private BranchFinder(float o, GeoLoc dst, Distance rd) {
			o_mi = o;
			dest = dst;
			r_dist = rd;
		}
		public boolean check(float m, R_NodeImpl rn) {
			if (m > o_mi) {
				if (rn.isExit())
					checkExit(rn, m - o_mi);
				if (rn.isCommonExit())
					return true;
			}
			return false;
		}
		private void checkExit(R_NodeImpl rn, float cd) {
			// distance from this exit to the destination
			Distance d = GeoLocHelper.distanceTo(rn.getGeoLoc(),
				dest);
			if (d != null) {
				// add distance from corridor origin to exit
				Distance td = d.add(new Distance(cd, MILES));
				if (td.compareTo(r_dist) < 0)
					branches.put(td, rn);
			}
		}
	}
}
