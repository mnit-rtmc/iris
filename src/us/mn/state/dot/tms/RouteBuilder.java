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

	/** Map of all R_Nodes */
	protected final R_NodeMapImpl node_map;

	/** Maximum number of corridor legs */
	protected final int legs;

	/** Maximum route distance */
	protected float max_dist;

	/** Working path */
	protected final LinkedList<ODPair> path = new LinkedList<ODPair>();

	/** Set of all routes built */
	protected final TreeSet<Route> routes = new TreeSet<Route>();

	/** Create a new route builder */
	public RouteBuilder(String n, R_NodeMapImpl map, int l, float d) {
		name = n;
		node_map = map;
		legs = l;
		max_dist = d;
	}

	/** Search a corridor for branching paths to a destination */
	protected void searchCorridor(float distance, LocationImpl origin,
		LocationImpl destination) throws BadRouteException
	{
		Corridor c = node_map.getCorridor(origin.getCorridor());
		R_NodeImpl r_node = c.findDownstreamNode(origin);
		if(r_node.metersTo(origin) > MAX_ORIGIN_DISTANCE) {
			throw new BadRouteException("ORIGIN OFF MAINLINE: " +
				origin.getDescription());
		}
		int i = 0;
		while(r_node != null) {
			LocationImpl dest = (LocationImpl)r_node.getLocation();
			ODPair od = new ODPair(origin, dest, false);
			float dist = distance + c.calculateDistance(od);
			if(TRAVEL_LOG.isOpen()) {
				TRAVEL_LOG.log(name + ": SEARCH FOR " +
					destination.getDescription() + " (" +
					i + ", " + dist + " miles) " + od);
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
						r_node.getOID());
				}
				break;
			}
			r_node = findNextNode(c, r_node, dist, origin,
				destination);
		}
	}

	/** Find the next node on the corridor */
	protected R_NodeImpl findNextNode(Corridor c, R_NodeImpl r_node,
		float distance, LocationImpl origin, LocationImpl destination)
		throws BadRouteException
	{
		LocationImpl dest = (LocationImpl)r_node.getLocation();
		R_NodeImpl next = null;
		for(R_NodeImpl n: r_node.getDownstream()) {
			if(!r_node.isCorridorType())
				continue;
			LocationImpl down = (LocationImpl)n.getLocation();
			if(down.isSameCorridor(origin))
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
	protected void findPaths(float distance, final LocationImpl origin,
		final LocationImpl destination)
	{
		ODPair od = new ODPair(origin, destination, false);
		Corridor c = node_map.getCorridor(od);
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
			Corridor c = node_map.getCorridor(od);
			r.addTrip(new CorridorTrip(name, c, od));
			if(od.hasTurn())
				turns++;
		}
		r.setTurns(turns);
		Corridor c = node_map.getCorridor(odf);
		r.addTrip(new CorridorTrip(name, c, odf));
		routes.add(r);
		// NOTE: this optimisation will prevent us from finding some
		// secondary routes; we're only interested in the best route.
		max_dist = Math.min(max_dist, r.getGoodness());
		if(TRAVEL_LOG.isOpen()) {
			TRAVEL_LOG.log(name + ": FOUND ROUTE TO " +
				odf.getDestination().getDescription() + ", " +
				r.getLength() + " miles, " + r.getTurns() +
				" turns, goodness: " + r.getGoodness());
			if(max_dist == r.getGoodness()) {
				TRAVEL_LOG.log(name + ": LOWERED MAX DIST TO " +
					max_dist);
			}
		}
	}

	/** Find all the routes from an origin to a destination */
	public SortedSet<Route> findRoutes(LocationImpl o, LocationImpl d) {
		routes.clear();
		path.clear();
		findPaths(0, o, d);
		return routes;
	}
}
