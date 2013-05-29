/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2013  Minnesota Department of Transportation
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

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeMap;
import us.mn.state.dot.geokit.Position;
import static us.mn.state.dot.tms.GeoLocHelper.distanceTo;
import us.mn.state.dot.tms.units.Distance;
import static us.mn.state.dot.tms.units.Distance.Units.MILES;

/**
 * A corridor is a collection of all R_Node objects for one roadway corridor.
 *
 * @author Douglas Lau
 */
public class CorridorBase implements Iterable<R_Node> {

	/** Adjustment for r_node milepoints falling on exact same spot */
	static protected float calculateEpsilon(float v) {
		if(v != 0)
			return v * 0.0000001f;
		else
			return 0.0000001f;
	}

	/** Calculate the distance to another roadway node */
	static public Distance nodeDistance(R_Node a, R_Node b) {
		return distanceTo(a.getGeoLoc(), b.getGeoLoc());
	}

	/** Calculate the distance to a location */
	static public Distance nodeDistance(R_Node n, GeoLoc l) {
		return distanceTo(n.getGeoLoc(), l);
	}

	/** Check if the r_node location is valid */
	static protected boolean hasLocation(R_Node n) {
		return !GeoLocHelper.isNull(n.getGeoLoc());
	}

	/** Corridor name */
	protected final String name;

	/** Get the corridor name */
	public String getName() {
		return name;
	}

	/** Get a string representation of the corridor */
	public String toString() {
		return name;
	}

	/** Corridor roadway */
	protected final Road roadway;

	/** Get the corridor roadway */
	public Road getRoadway() {
		return roadway;
	}

	/** Corridor direction */
	protected final short road_dir;

	/** Get the corridor direction */
	public short getRoadDir() {
		return road_dir;
	}

	/** Get the corridor ID */
	public String getID() {
		return GeoLocHelper.getCorridorID(roadway, road_dir);
	}

	/** Set of unsorted roadway nodes */
	protected final Set<R_Node> unsorted = new HashSet<R_Node>();

	/** Roadway node list */
	protected final LinkedList<R_Node> r_nodes = new LinkedList<R_Node>();

	/** Mapping from milepoint to r_node */
	protected final TreeMap<Float, R_Node> n_points =
		new TreeMap<Float, R_Node>();

	/** Create a new corridor */
	public CorridorBase(GeoLoc loc) {
		name = GeoLocHelper.getCorridorName(loc);
		roadway = loc.getRoadway();
		road_dir = loc.getRoadDir();
	}

	/** Add a roadway node to the corridor */
	public void addNode(R_Node r_node) {
		if(hasLocation(r_node) && !r_node.getAbandoned()) {
			unsorted.add(r_node);
			unsorted.addAll(r_nodes);
			r_nodes.clear();
			n_points.clear();
		}
	}

	/** Remove a roadway node from the corridor */
	public void removeNode(R_Node r_node) {
		unsorted.addAll(r_nodes);
		unsorted.remove(r_node);
		r_nodes.clear();
		n_points.clear();
	}

	/** Arrange the nodes in the corridor */
	public void arrangeNodes() {
		sortNodes();
		calculateNodeMilePoints();
	}

	/** Sort the roadway nodes for the corridor */
	protected void sortNodes() {
		assert r_nodes.isEmpty();
		beginList();
		while(!unsorted.isEmpty())
			linkNearestNode();
		if(isReversed())
			reverseList();
	}

	/** Put one r_node into the list */
	protected void beginList() {
		// Only way to get one Set element is to get iterator
		Iterator<R_Node> it = unsorted.iterator();
		if(it.hasNext()) {
			r_nodes.add(it.next());
			it.remove();
		}
	}

	/** Link the nearest node */
	protected void linkNearestNode() {
		R_Node first = r_nodes.getFirst();
		R_Node last = r_nodes.getLast();
		NodeDistance fnear = findNearest(first);
		NodeDistance lnear = findNearest(last);
		if(fnear == null || lnear == null)
			unsorted.clear();
		else if(fnear.meters < lnear.meters) {
			r_nodes.addFirst(fnear.node);
			unsorted.remove(fnear.node);
		} else {
			r_nodes.addLast(lnear.node);
			unsorted.remove(lnear.node);
		}
	}

	/** Simple structure to hold a node and distance */
	protected class NodeDistance {
		protected final double meters;
		protected final R_Node node;
		public NodeDistance(Double m, R_Node n) {
			meters = m;
			node = n;
		}
	}

	/** Find the nearest unsorted node to the given node */
	protected NodeDistance findNearest(R_Node end) {
		NodeDistance near = null;
		for(R_Node r_node: unsorted) {
			Distance m = nodeDistance(r_node, end);
			if(m != null && (near == null || m.m() < near.meters))
				near = new NodeDistance(m.m(), r_node);
		}
		return near;
	}

	/** Check if the roadway nodes are in reverse order */
	protected boolean isReversed() {
		return r_nodes.size() > 1 && !isUpstreamToDownstream();
	}

	/** Check if the nodes are in upstream-to-downstream order */
	protected boolean isUpstreamToDownstream() {
		R_Node first = r_nodes.getFirst();
		R_Node last = r_nodes.getLast();
		Position pf = GeoLocHelper.getWgs84Position(first.getGeoLoc());
		Position pl = GeoLocHelper.getWgs84Position(last.getGeoLoc());
		if(pf == null || pl == null)
			return false;
		switch(Direction.fromOrdinal(road_dir)) {
		case NORTH:
			return pf.getLatitude() < pl.getLatitude();
		case SOUTH:
			return pf.getLatitude() > pl.getLatitude();
		case EAST:
			return pf.getLongitude() < pl.getLongitude();
		case WEST:
			return pf.getLongitude() > pl.getLongitude();
		case INNER_LOOP:
			// FIXME: this might be tricky
			return false;
		case OUTER_LOOP:
			// FIXME: this might be tricky
			return false;
		}
		return false;
	}

	/** Reverse the list of roadway nodes */
	protected void reverseList() {
		LinkedList<R_Node> tmp = new LinkedList<R_Node>(r_nodes);
		r_nodes.clear();
		for(R_Node r_node: tmp)
			r_nodes.addFirst(r_node);
	}

	/** Calculate the mile points for all nodes on the corridor */
	protected void calculateNodeMilePoints() {
		assert n_points.isEmpty();
		float miles = 0;
		R_Node previous = null;
		for(R_Node n: r_nodes) {
			if(previous != null) {
				Distance m = nodeDistance(previous, n);
				if(m == null)
					continue;
				miles += m.asFloat(MILES);
			}
			while(n_points.containsKey(miles))
				miles += calculateEpsilon(miles);
			n_points.put(miles, n);
			previous = n;
		}
	}

	/** Calculate the mile point for a location.
	 * @param loc Location to calculate.
	 * @return Mile point for location, or null if no r_nodes exist. */
	public Float calculateMilePoint(GeoLoc loc) {
		if(n_points.isEmpty())
			return null;
		R_Node nearest = null;
		R_Node n_after = null;
		float n_mile = 0;
		double n_meters = 0;
		for(Float mile: n_points.keySet()) {
			R_Node n = n_points.get(mile);
			Distance m = nodeDistance(n, loc);
			if(m != null) {
				double ms = m.m();
				if(nearest == null || ms < n_meters) {
					nearest = n;
					n_after = n;
					n_mile = mile;
					n_meters = ms;
				} else if(n_after == nearest)
					n_after = n;
			}
		}
		if(nearest == null || n_after == null)
			return null;
		float mi = new Distance(n_meters).asFloat(MILES);
		Distance m0 = nodeDistance(n_after, nearest);
		Distance m1 = nodeDistance(n_after, loc);
		if(m0 != null && m1 != null && m0.m() > m1.m())
			return n_mile + mi;
		else
			return n_mile - mi;
	}

	/** Get the mile point for a specified node */
	public Float getMilePoint(R_Node r_node) {
		for(Float mile: n_points.keySet()) {
			R_Node n = n_points.get(mile);
			if(n == r_node)
				return mile;
		}
		return null;
	}

	/** Create a r_node iterator */
	public Iterator<R_Node> iterator() {
		return r_nodes.iterator();
	}

	/** Find the nearest node to the given location */
	public R_Node findNearest(GeoLoc loc) {
		Position pos = GeoLocHelper.getWgs84Position(loc);
		if(pos != null)
			return findNearest(pos);
		else
			return null;
	}

	/** Find the nearest node to the given position */
	public R_Node findNearest(Position pos) {
		R_Node nearest = null;
		double n_meters = 0;
		for(R_Node n: r_nodes) {
			Distance m = distanceTo(n.getGeoLoc(), pos);
			if(m != null && (nearest == null || m.m() < n_meters)) {
				nearest = n;
				n_meters = m.m();
			}
		}
		return nearest;
	}

	/** Find the nearest node to the given location with given type */
	public R_Node findNearest(Position pos, R_NodeType nt) {
		R_Node nearest = null;
		double n_meters = 0;
		for(R_Node n: r_nodes) {
			if(n.getNodeType() != nt.ordinal())
				continue;
			Distance m = distanceTo(n.getGeoLoc(), pos);
			if(m != null && (nearest == null || m.m() < n_meters)) {
				nearest = n;
				n_meters = m.m();
			}
		}
		return nearest;
	}

	/** Fint the last node before the given location */
	public R_Node findLastBefore(Position pos) {
		R_Node nearest = null;
		R_Node n_before = null;
		R_Node n_after = null;
		double n_meters = 0;
		for(R_Node n: r_nodes) {
			Distance m = distanceTo(n.getGeoLoc(), pos);
			if(m != null) {
				double ms = m.m();
				if(nearest == null || ms < n_meters) {
					n_before = nearest;
					nearest = n;
					n_after = n;
					n_meters = ms;
				} else if(ms == n_meters) {
					// coincident points
					nearest = n;
					n_after = n;
				} else if(n_after == nearest)
					n_after = n;
			}
		}
		if(nearest == null)
			return null;
		GeoLoc ga = n_after.getGeoLoc();
		Distance m0 = distanceTo(ga, nearest.getGeoLoc());
		Distance m1 = distanceTo(ga, pos);
		if(m0 != null && m1 != null && m0.m() > m1.m())
			return nearest;
		else
			return n_before;
	}

	/** Get the lane configuration at the given location */
	public LaneConfiguration laneConfiguration(Position pos) {
		R_Node node = findLastBefore(pos);
		if(node != null)
			return laneConfiguration(node);
		else
			return new LaneConfiguration(0, 0);
	}

	/** Get the lane configuration at the given node */
	private LaneConfiguration laneConfiguration(R_Node node) {
		int left = 0;
		int right = 0;
		for(R_Node n: r_nodes) {
			if(n.getAttachSide())
				left = n.getShift();
			else
				right = n.getShift();
			if(n.getNodeType() == R_NodeType.STATION.ordinal()) {
				if(n.getAttachSide())
					right = left + n.getLanes();
				else
					left = right - n.getLanes();
			}
			if(n == node)
				return new LaneConfiguration(left, right);
		}
		// Node not found on corridor
		return new LaneConfiguration(0, 0);
	}
}
