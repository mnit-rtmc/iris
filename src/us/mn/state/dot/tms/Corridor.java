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

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

/**
 * A corridor is a collection of all R_Node objects for one freeway corridor.
 *
 * @author Douglas Lau
 */
public class Corridor {

	/** Conversion value from meter to mile length units */
	static protected final float METERS_PER_MILE = 1609.344f;

	/** Convert meters to miles */
	static protected float metersToMiles(double meters) {
		return (float)(meters / METERS_PER_MILE);
	}

	/** Corridor name */
	protected final String name;

	/** Get the corridor name */
	public String getName() {
		return name;
	}

	/** Flag for downstream-to-upstream (backwards) order */
	protected final boolean order_down_up;

	/** Corridor freeway */
	protected final RoadwayImpl freeway;

	/** Get the corridor freeway */
	public RoadwayImpl getFreeway() {
		return freeway;
	}

	/** Corridor direction */
	protected final short free_dir;

	/** Get the corridor direction */
	public short getFreeDir() {
		return free_dir;
	}

	/** Set of unsorted roadway nodes */
	protected final Set<R_NodeImpl> unsorted = new HashSet<R_NodeImpl>();

	/** Roadway node list */
	protected final LinkedList<R_NodeImpl> r_nodes =
		new LinkedList<R_NodeImpl>();

	/** Mapping from milepoint to r_node */
	protected final TreeMap<Float, R_NodeImpl> n_points =
		new TreeMap<Float, R_NodeImpl>();

	/** Create a new corridor */
	public Corridor(boolean order, LocationImpl loc) {
		name = loc.getCorridor();
		order_down_up = order;
		freeway = (RoadwayImpl)loc.getFreeway();
		free_dir = loc.getFreeDir();
	}

	/** Add a roadway node to the corridor */
	public synchronized void addNode(R_NodeImpl r_node) {
		if(r_node.hasLocation()) {
			unsorted.add(r_node);
			n_points.clear();
		}
	}

	/** Find the nearest unsorted node to the given node */
	protected R_NodeImpl findNearest(R_NodeImpl end) {
		R_NodeImpl nearest = null;
		double n_meters = 0;
		for(R_NodeImpl r_node: unsorted) {
			double m = r_node.metersTo(end);
			if(nearest == null || m < n_meters) {
				nearest = r_node;
				n_meters = m;
			}
		}
		return nearest;
	}

	/** Link the nearest node */
	protected void linkNearestNode() {
		R_NodeImpl first = r_nodes.getFirst();
		R_NodeImpl last = r_nodes.getLast();
		R_NodeImpl fnear = findNearest(first);
		R_NodeImpl lnear = findNearest(last);
		if(first.metersTo(fnear) < last.metersTo(lnear)) {
			r_nodes.addFirst(fnear);
			unsorted.remove(fnear);
		} else {
			r_nodes.addLast(lnear);
			unsorted.remove(lnear);
		}
	}

	/** Check if the nodes are in upstream-to-downstream order */
	protected boolean isUpstreamToDownstream() {
		R_NodeImpl first = r_nodes.getFirst();
		R_NodeImpl last = r_nodes.getLast();
		switch(free_dir) {
			case Roadway.NORTH:
				return first.getTrueNorthing() <
					last.getTrueNorthing();
			case Roadway.SOUTH:
				return first.getTrueNorthing() >
					last.getTrueNorthing();
			case Roadway.EAST:
				return first.getTrueEasting() <
					last.getTrueEasting();
			case Roadway.WEST:
				return first.getTrueEasting() >
					last.getTrueEasting();
			case Roadway.INNER_LOOP:
				// FIXME: this might be tricky
				return false;
			case Roadway.OUTER_LOOP:
				// FIXME: this might be tricky
				return false;
		}
		return false;
	}

	/** Check if the roadway nodes are in reverse order */
	protected boolean isReversed() {
		if(order_down_up)
			return isUpstreamToDownstream();
		else
			return !isUpstreamToDownstream();
	}

	/** Reverse the list of roadway nodes */
	protected void reverseList() {
		LinkedList<R_NodeImpl> tmp =
			new LinkedList<R_NodeImpl>(r_nodes);
		r_nodes.clear();
		for(R_NodeImpl r_node: tmp)
			r_nodes.addFirst(r_node);
	}

	/** Put one r_node into the list */
	protected void beginList() {
		// Only way to get one Set element is to get iterator
		Iterator<R_NodeImpl> it = unsorted.iterator();
		if(it.hasNext()) {
			r_nodes.add(it.next());
			it.remove();
		}
	}

	/** Sort the roadway nodes for the corridor */
	protected void sort() {
		if(r_nodes.isEmpty())
			beginList();
		while(!unsorted.isEmpty())
			linkNearestNode();
		// Reverse the list if necessary
		if(r_nodes.size() > 1) {
			if(isReversed())
				reverseList();
		}
	}

	/** Get the list of r_nodes for the corridor */
	protected List<R_NodeImpl> getNodes() {
		if(unsorted.size() > 0) {
			if(r_nodes.size() > 0) {
				unsorted.addAll(r_nodes);
				r_nodes.clear();
			}
			sort();
		}
		return r_nodes;
	}

	/** Get the list of r_nodes linked with downstream nodes */
	protected List<R_NodeImpl> getNodesLinked() {
		List<R_NodeImpl> r_nodes = getNodes();
		Iterator<R_NodeImpl> down = r_nodes.iterator();
		// Throw away first r_node in downstream iterator
		if(down.hasNext())
			down.next();
		for(R_NodeImpl r_node: r_nodes) {
			if(down.hasNext()) {
				R_NodeImpl d = down.next();
				if(r_node.hasDownstreamLink())
					r_node.addDownstream(d);
			}
		}
		return r_nodes;
	}

	/** Print out the corridor to an XML file */
	public synchronized void printXml(PrintWriter out) {
		out.println("<corridor route='" + freeway.getName() +
			"' dir='" + Roadway.DIRECTION[free_dir] + "'>");
		List<R_NodeImpl> r_nodes = getNodesLinked();
		for(R_NodeImpl r_node: r_nodes)
			r_node.printXml(out);
		out.println("</corridor>");
	}

	/** Adjustment for r_node milepoints falling on exact same spot */
	static protected final float MILES_EPSILON = 0.0001f;

	/** Calculate the mile points for all nodes on the corridor */
	protected void calculateNodeMilePoints() {
		float miles = 0;
		R_NodeImpl previous = null;
		for(R_NodeImpl n: getNodes()) {
			if(previous != null)
				miles += metersToMiles(previous.metersTo(n));
			while(n_points.containsKey(miles))
				miles += MILES_EPSILON;
			n_points.put(miles, n);
			previous = n;
		}
	}

	/** Calculate the mile point for a location */
	public synchronized float calculateMilePoint(LocationImpl loc)
		throws BadRouteException
	{
		if(n_points.isEmpty())
			calculateNodeMilePoints();
		if(n_points.isEmpty())
			throw new BadRouteException("No nodes on corridor");
		R_NodeImpl nearest = null;
		R_NodeImpl n_after = null;
		float n_mile = 0;
		double n_meters = 0;
		for(Float mile: n_points.keySet()) {
			R_NodeImpl n = n_points.get(mile);
			double m = n.metersTo(loc);
			if(nearest == null || m < n_meters) {
				nearest = n;
				n_after = n;
				n_mile = mile;
				n_meters = m;
			} else if(n_after == nearest)
				n_after = n;
		}
		float mi = metersToMiles(n_meters);
		if(n_after.metersTo(nearest) > n_after.metersTo(loc))
			return n_mile + mi;
		else
			return n_mile - mi;
	}

	/** Create a mapping from mile points to stations */
	public synchronized TreeMap<Float, StationImpl> createStationMap() {
		if(n_points.isEmpty())
			calculateNodeMilePoints();
		TreeMap<Float, StationImpl> stations =
			new TreeMap<Float, StationImpl>();
		for(Float m: n_points.keySet()) {
			R_NodeImpl n = n_points.get(m);
			if(n.isStation()) {
				StationImpl s = (StationImpl)n.getStation();
				if(s != null)
					stations.put(m, s);
			}
		}
		return stations;
	}

	/** Calculate the distance for the given O/D pair (miles) */
	public float calculateDistance(ODPair od) throws BadRouteException {
		float origin = calculateMilePoint(od.getOrigin());
		float destination = calculateMilePoint(od.getDestination());
		if(origin > destination) {
			throw new BadRouteException("Origin (" + origin +
				") > destin (" + destination + "), " + od);
		}
		return destination - origin;
	}

	/** Find the nearest node downstream from the given location */
	public synchronized R_NodeImpl findDownstreamNode(LocationImpl loc)
		throws BadRouteException
	{
		float m = calculateMilePoint(loc);
		for(Float mile: n_points.keySet()) {
			if(mile > m)
				return n_points.get(mile);
		}
		throw new BadRouteException("No downstream nodes");
	}

	/** Interface to find a node on the corridor */
	static public interface NodeFinder {
		public boolean check(R_NodeImpl r_node);
	}

	/** Find a node using a node finder callback interface */
	public synchronized R_NodeImpl findNode(NodeFinder finder) {
		if(n_points.isEmpty())
			calculateNodeMilePoints();
		for(R_NodeImpl r_node: n_points.values()) {
			R_NodeImpl n = checkNode(finder, r_node);
			if(n != null)
				return n;
		}
		return null;
	}

	/** Check a node and any downstream CD road using a finder interface */
	protected R_NodeImpl checkNode(NodeFinder finder, R_NodeImpl r_node) {
		if(finder.check(r_node))
			return r_node;
		if(r_node.isCDExit()) {
			R_NodeImpl cd = findNextCD(r_node);
			while(cd != null) {
				if(finder.check(cd))
					return cd;
				cd = findNextCD(cd);
			}
		}
		return null;
	}

	/** Find the next downstream CD node */
	protected R_NodeImpl findNextCD(R_NodeImpl r_node) {
		for(R_NodeImpl down: r_node.getDownstream()) {
			LocationImpl loc = (LocationImpl)down.getLocation();
			if(loc.isMatchingCD(this))
				return down;
		}
		return null;
	}
}
