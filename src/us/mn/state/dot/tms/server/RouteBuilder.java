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

import java.util.LinkedList;
import java.util.TreeSet;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.units.Distance;

/**
 * A route builder builds a route from an origin to a destination.
 *
 * @author Douglas Lau
 */
public class RouteBuilder {

	/** Maximum distance from origin to a corridor node (in meters) */
	static private final float MAX_ORIGIN_M = 1000;

	/** Maximum number of R_Nodes to follow on a corridor */
	static private final int MAX_R_NODE_LIMIT = 100;

	/** Debug log */
	private final DebugLog dlog;

	/** Name to use for debugging purposes */
	private final String name;

	/** Check if we're logging */
	private boolean isLogging() {
		return dlog.isOpen();
	}

	/** Log a message to the debug log */
	private void log(String msg) {
		dlog.log(name + ": " + msg);
	}

	/** Corridor manager */
	private final CorridorManager corridors;

	/** Maximum number of corridor legs */
	private final int legs = SystemAttrEnum.ROUTE_MAX_LEGS.getInt();

	/** Maximum route distance (miles) */
	private float max_mi = SystemAttrEnum.ROUTE_MAX_MILES.getInt();

	/** Working path */
	private final LinkedList<ODPair> path = new LinkedList<ODPair>();

	/** Set of all routes built */
	private final TreeSet<Route> routes = new TreeSet<Route>();

	/** Create a new route builder.
	 * @param dl Debug log.
	 * @param n Name (for debugging).
	 * @param c Corridor manager. */
	public RouteBuilder(DebugLog dl, String n, CorridorManager c) {
		dlog = dl;
		name = n;
		corridors = c;
	}

	/** Find the best route from an origin to a destination.
	 * @param o Route origin.
	 * @param d Route destination.
	 * @return Best route found. */
	public Route findBestRoute(GeoLoc o, GeoLoc d) {
		routes.clear();
		path.clear();
		findPaths(0, o, d);
		if (routes.size() > 0)
			return routes.first();
		else
			return null;
	}

	/** Find all paths from an origin to a destination.
	 * @param distance Distance.
	 * @param origin Route origin.
	 * @param destination Route destination. */
	private void findPaths(float distance, final GeoLoc origin,
		final GeoLoc destination)
	{
		ODPair od = new ODPair(origin, destination, false);
		Corridor c = corridors.getCorridor(od);
		if (c != null) {
			try {
				float d = c.calculateDistance(od);
				if (distance + d < max_mi)
					buildRoute(od);
			}
			catch (BadRouteException e) {
				debugRouteException(e);
			}
		}
		if (path.size() < legs) {
			try {
				searchCorridor(distance, origin, destination);
			}
			catch (BadRouteException e) {
				debugRouteException(e);
			}
		}
	}

	/** Debug a route exception.
	 * @param e Bad route exception. */
	private void debugRouteException(BadRouteException e) {
		if (isLogging())
			log("BAD ROUTE: " + e.getMessage());
	}

	/** Build a route from the current path.
	 * @param odf Origin / destination pair.
	 * @throws BadRouteException on route error. */
	private void buildRoute(ODPair odf) throws BadRouteException {
		Route r = new Route();
		for (ODPair od: path)
			r.addTrip(createTrip(od), od.hasTurn());
		r.addTrip(createTrip(odf), false);
		routes.add(r);
		// NOTE: this optimisation will prevent us from finding some
		// secondary routes; we're only interested in the best route.
		max_mi = Math.min(max_mi, r.getGoodness());
		if (isLogging()) {
			GeoLoc dest = odf.getDestination();
			log("FOUND ROUTE TO " + GeoLocHelper.getDescription(
			    dest) + ", " + r);
			if (max_mi == r.getGoodness())
				log("LOWERED MAX DIST TO " + max_mi);
		}
	}

	/** Create one corridor trip */
	private CorridorTrip createTrip(ODPair od) throws BadRouteException {
		Corridor c = corridors.getCorridor(od);
		if (c != null)
			return new CorridorTrip(c, od);
		else
			throw new BadRouteException("MISSING CORRIDOR");
	}

	/** Search a corridor for branching paths to a destination.
	 * @param distance Distance.
	 * @param origin Route origin.
	 * @param destination Route destination.
	 * @throws BadRouteException if route cannot be found. */
	private void searchCorridor(float distance, GeoLoc origin,
		GeoLoc destination) throws BadRouteException
	{
		Corridor c = corridors.getCorridor(origin);
		if (c == null) {
			log("BAD ORIGIN: " + origin.getName());
			return;
		}
		R_NodeImpl r_node = c.findDownstreamNode(origin);
		Distance m = Corridor.nodeDistance(r_node, origin);
		if (m == null || m.m() > MAX_ORIGIN_M) {
			throw new BadRouteException("ORIGIN OFF MAINLINE: " +
				GeoLocHelper.getDescription(origin));
		}
		int i = 0;
		while (r_node != null) {
			GeoLoc dest = r_node.getGeoLoc();
			ODPair od = new ODPair(origin, dest, false);
			float dist = distance + c.calculateDistance(od);
			if (isLogging()) {
				log("SEARCH FOR " + GeoLocHelper.getDescription(
				    destination) + " (" + i + ", " + dist +
				    " miles) " + od);
			}
			if (dist > max_mi) {
				if (isLogging()) {
					log("MAX DISTANCE (" + max_mi +
					    ") EXCEEDED");
				}
				break;
			}
			i++;
			if (i > MAX_R_NODE_LIMIT) {
				if (isLogging()) {
					log("BREAKING R_NODE LOOP AT " +
					    r_node.getName());
				}
				break;
			}
			r_node = findNextNode(c, r_node, dist, origin,
				destination);
		}
	}

	/** Find the next node on the corridor.
	 * @param c Corridor.
	 * @param r_node Roadway node.
	 * @param distance Distance.
	 * @param origin Route origin.
	 * @param destination Route destination.
	 * @return Next roadway node.
	 * @throws BadRouteException on route error. */
	private R_NodeImpl findNextNode(Corridor c, R_NodeImpl r_node,
		float distance, GeoLoc origin, GeoLoc destination)
		throws BadRouteException
	{
		GeoLoc dest = r_node.getGeoLoc();
		R_NodeImpl next = null;
		for (R_NodeImpl n: r_node.getDownstream()) {
			if (!r_node.isCorridorType())
				continue;
			GeoLoc down = n.getGeoLoc();
			if (GeoLocHelper.isSameCorridor(down, origin))
				next = n;
			else {
				boolean turn = r_node.hasTurnPenalty()
					&& n.hasTurnPenalty();
				path.add(new ODPair(origin, dest, turn));
				findPaths(distance, down, destination);
				path.removeLast();
			}
		}
		return next;
	}
}
