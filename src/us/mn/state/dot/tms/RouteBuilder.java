/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007  Minnesota Department of Transportation
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

	/** Maximum distance from origin to a corridor node (in meters) */
	static protected final float MAX_ORIGIN_DISTANCE = 1000;

	/** Name to use for debugging purposes */
	protected final String name;

	/** Map of all R_Nodes */
	protected final R_NodeMapImpl node_map;

	/** Maximum number of corridor legs */
	protected final int legs;

	/** Maximum route distance */
	protected final float max_dist;

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

	/** Search for branching paths to a destination */
	protected void searchPaths(float distance, final LocationImpl origin,
		final LocationImpl destination) throws BadRouteException
	{
		Corridor c = node_map.getCorridor(origin.getCorridor());
		R_NodeImpl r_node = c.getNearestNode(origin);
		if(r_node.metersTo(origin) > MAX_ORIGIN_DISTANCE) {
			throw new BadRouteException("Origin off mainline: " +
				origin.getDescription());
		}
int i = 0;
		while(r_node != null) {
			LocationImpl dest = (LocationImpl)r_node.getLocation();
			R_NodeImpl next = null;
			for(R_NodeImpl n: r_node.getDownstream()) {
				if(!r_node.isCorridorType())
					continue;
				LocationImpl down =
					(LocationImpl)n.getLocation();
				if(down.isSameCorridor(origin))
					next = n;
				else {
					ODPair p = new ODPair(origin, dest);
					float d = c.calculateDistance(p);
					if(distance + d < max_dist) {
						path.add(p);
						findPaths(distance + d, down,
							destination);
						path.removeLast();
					}
				}
			}
			r_node = next;
i++;
if(i > 100) {
	System.err.println("Breaking r_node loop for " + name);
	break;
}
		}
	}

	/** Debug a route exception */
	protected void debugRouteException(BadRouteException e) {
		DMSImpl.TRAVEL_LOG.log(name + ": bad route: " + e.getMessage());
	}

	/** Find all paths from an origin to a destination */
	protected void findPaths(float distance, final LocationImpl origin,
		final LocationImpl destination)
	{
		ODPair od = new ODPair(origin, destination);
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
				searchPaths(distance, origin, destination);
			}
			catch(BadRouteException e) {
				debugRouteException(e);
			}
		}
	}

	/** Build a route from the current path */
	protected void buildRoute(ODPair odf) throws BadRouteException {
		Route r = new Route();
		for(ODPair od: path) {
			Corridor c = node_map.getCorridor(od);
			r.addTrip(new CorridorTrip(name, c, od));
		}
		Corridor c = node_map.getCorridor(odf);
		r.addTrip(new CorridorTrip(name, c, odf));
		routes.add(r);
	}

	/** Find all the routes from an origin to a destination */
	public SortedSet<Route> findRoutes(LocationImpl o, LocationImpl d) {
		routes.clear();
		path.clear();
		findPaths(0, o, d);
		return routes;
	}
}
