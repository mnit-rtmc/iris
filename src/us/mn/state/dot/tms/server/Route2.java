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

import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.units.Distance;

/**
 * A route is a list of "route legs" from an origin to a destination on a
 * roadway network.
 *
 * @author Douglas Lau
 */
public class Route2 {

	/** Penalty (in goodness) for each leg in a route */
	static private final float LEG_PENALTY = 0.25f;

	/** Route destination */
	private final GeoLoc dest;

	/** Final leg of route */
	private final RouteLeg leg;

	/** Create a new route */
	private Route2(GeoLoc dst, RouteLeg lg) {
		dest = dst;
		leg = lg;
	}

	/** Create a new route */
	public Route2(GeoLoc dst) {
		this(dst, null);
	}

	/** Create an extended route.
	 * @param c Corridor of leg.
	 * @param od O/D pair of leg.
	 * @return Extended route with new leg. */
	public Route2 createExtended(Corridor c, ODPair od) {
		RouteLeg lg = new RouteLeg(c, od, leg);
		return lg.isValid() ? new Route2(dest, lg) : null;
	}

	/** Get the destination */
	public GeoLoc getDestination() {
		return dest;
	}

	/** Get the number of legs */
	public int legCount() {
		int i = 0;
		for (RouteLeg lg = leg; lg != null; lg = lg.prev)
			i++;
		return i;
	}

	/** Get the number of turns in the route */
	public int getTurns() {
		int t = 0;
		for (RouteLeg lg = leg; lg != null; lg = lg.prev) {
			if (lg.hasTurn())
				t++;
		}
		return t;
	}

	/** Get the route distance.
	 * @return Total route distance. */
	public Distance getDistance() {
		Distance d = new Distance(0);
		for (RouteLeg lg = leg; lg != null; lg = lg.prev)
			d = d.add(lg.getDistance());
		return d;
	}

	/** Get the goodness rating (lower is better) */
	public float getGoodness() {
		return getDistance().asFloat(Distance.Units.MILES) +
		       legCount() * LEG_PENALTY;
	}

	/** Check if the route matches an older-style route */
	public boolean matches(Route r) {
		return (r != null)
		    && (legCount() == r.getTrips().size())
		    && (getGoodness() == r.getGoodness());
	}

	/** Get a string representation of the route */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getDistance());
		sb.append(", ");
		sb.append(getTurns());
		sb.append(" turns, ");
		sb.append(getGoodness());
		sb.append(" goodness, ");
		for (RouteLeg lg = leg; lg != null; lg = lg.prev)
			sb.append(lg.toString());
		return sb.toString();
	}
}
