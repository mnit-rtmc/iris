/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2008  Minnesota Department of Transportation
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
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A route builder builds a set of routes
 *
 * @author Douglas Lau
 */
public class RouteBuilder {

	/** Travel time debug log */
	static protected final DebugLog TRAVEL_LOG = new DebugLog("travel");

	/** Maximum distance from origin to a corridor node (in meters) */
	static protected final float MAX_ORIGIN_DISTANCE = 1000;

	/** Maximum number of R_Nodes to follow on a corridor */
	static protected final int MAX_R_NODE_LIMIT = 100;

	/** Name to use for debugging purposes */
	protected final String name;

	/** Corridor manager */
	protected final CorridorManager corridors;

	/** Maximum number of corridor legs */
	protected final int legs;

	/** Maximum route distance */
	protected float max_dist;

	/** Working path */
	protected final LinkedList<ODPair> path = new LinkedList<ODPair>();

	/** Set of all routes built */
	protected final TreeSet<Route> routes = new TreeSet<Route>();

	/** Create a new route builder */
	public RouteBuilder(String n, CorridorManager c, int l, float d) {
		name = n;
		corridors = c;
		legs = l;
		max_dist = d;
	}

	/** Search a corridor for branching paths to a destination */
	protected void searchCorridor(float distance, GeoLoc origin,
		GeoLoc destination) throws BadRouteException
	{
		String cid = GeoLocHelper.getCorridor(origin);
		if(cid == null) {
			TRAVEL_LOG.log(name + ": BAD ORIGIN: " +
				origin.getName());
			return;
		}
		Corridor c = corridors.getCorridor(cid);
		if(c == null) {
			TRAVEL_LOG.log(name + ": MISSING CORRIDOR: " + cid);
			return;
		}
		R_NodeImpl r_node = c.findDownstreamNode(origin);
		if(r_node.metersTo(origin) > MAX_ORIGIN_DISTANCE) {
			throw new BadRouteException("ORIGIN OFF MAINLINE: " +
				GeoLocHelper.getDescription(origin));
		}
		int i = 0;
		while(r_node != null) {
			GeoLoc dest = r_node.getGeoLoc();
			ODPair od = new ODPair(origin, dest, false);
			float dist = distance + c.calculateDistance(od);
			if(TRAVEL_LOG.isOpen()) {
				TRAVEL_LOG.log(name + ": SEARCH FOR " +
					GeoLocHelper.getDescription(destination)
					+ " (" + i + ", " + dist + " miles) " +
					od);
			}
			if(dist > max_dist) {
				if(TRAVEL_LOG.isOpen()) {
					TRAVEL_LOG.log(name +
						": MAX DISTANCE (" + max_dist +
						") EXCEEDED");
				}
				break;
			}
			i++;
			if(i > MAX_R_NODE_LIMIT) {
				if(TRAVEL_LOG.isOpen()) {
					TRAVEL_LOG.log(name +
						": BREAKING R_NODE LOOP AT " +
						r_node.getName());
				}
				break;
			}
			r_node = findNextNode(c, r_node, dist, origin,
				destination);
		}
	}

	/** Find the next node on the corridor */
	protected R_NodeImpl findNextNode(Corridor c, R_NodeImpl r_node,
		float distance, GeoLoc origin, GeoLoc destination)
		throws BadRouteException
	{
		GeoLoc dest = r_node.getGeoLoc();
		R_NodeImpl next = null;
		for(R_NodeImpl n: r_node.getDownstream()) {
			if(!r_node.isCorridorType())
				continue;
			GeoLoc down = n.getGeoLoc();
			if(GeoLocHelper.isSameCorridor(down, origin))
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

	/** Debug a route exception */
	protected void debugRouteException(BadRouteException e) {
		if(TRAVEL_LOG.isOpen())
			TRAVEL_LOG.log(name + ": BAD ROUTE: " + e.getMessage());
	}

	/** Find all paths from an origin to a destination */
	protected void findPaths(float distance, final GeoLoc origin,
		final GeoLoc destination)
	{
		ODPair od = new ODPair(origin, destination, false);
		Corridor c = corridors.getCorridor(od);
		if(c != null) {
			try {
				float d = c.calculateDistance(od);
				if(distance + d < max_dist)
					buildRoute(od);
			}
			catch(BadRouteException e) {
				debugRouteException(e);
			}
		}
		if(path.size() < legs) {
			try {
				searchCorridor(distance, origin, destination);
			}
			catch(BadRouteException e) {
				debugRouteException(e);
			}
		}
	}

	/** Build a route from the current path */
	protected void buildRoute(ODPair odf) throws BadRouteException {
		Route r = new Route();
		int turns = 0;
		for(ODPair od: path) {
			Corridor c = corridors.getCorridor(od);
			r.addTrip(new CorridorTrip(name, c, od));
			if(od.hasTurn())
				turns++;
		}
		r.setTurns(turns);
		Corridor c = corridors.getCorridor(odf);
		r.addTrip(new CorridorTrip(name, c, odf));
		routes.add(r);
		// NOTE: this optimisation will prevent us from finding some
		// secondary routes; we're only interested in the best route.
		max_dist = Math.min(max_dist, r.getGoodness());
		if(TRAVEL_LOG.isOpen()) {
			GeoLoc dest = odf.getDestination();
			TRAVEL_LOG.log(name + ": FOUND ROUTE TO " +
				GeoLocHelper.getDescription(dest) + ", " +
				r.getLength() + " miles, " + r.getTurns() +
				" turns, goodness: " + r.getGoodness());
			if(max_dist == r.getGoodness()) {
				TRAVEL_LOG.log(name + ": LOWERED MAX DIST TO " +
					max_dist);
			}
		}
	}

	/** Find all the routes from an origin to a destination */
	public SortedSet<Route> findRoutes(GeoLoc o, GeoLoc d) {
		routes.clear();
		path.clear();
		findPaths(0, o, d);
		return routes;
	}
}
