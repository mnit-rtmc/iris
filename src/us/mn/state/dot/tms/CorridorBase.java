/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2010  Minnesota Department of Transportation
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
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

/**
 * A corridor is a collection of all R_Node objects for one roadway corridor.
 *
 * @author Douglas Lau
 */
public class CorridorBase {

	/** Conversion value from meter to mile length units */
	static protected final float METERS_PER_MILE = 1609.344f;

	/** Convert meters to miles */
	static protected float metersToMiles(double meters) {
		return (float)(meters / METERS_PER_MILE);
	}

	/** Adjustment for r_node milepoints falling on exact same spot */
	static protected float calculateEpsilon(float v) {
		if(v != 0)
			return v * 0.0000001f;
		else
			return 0.0000001f;
	}

	/** Calculate the distance to another roadway node (in meters) */
	static public Double metersTo(R_Node a, R_Node b) {
		return GeoLocHelper.metersTo(a.getGeoLoc(), b.getGeoLoc());
	}

	/** Calculate the distance to a location (in meters) */
	static public Double metersTo(R_Node n, GeoLoc l) {
		return GeoLocHelper.metersTo(n.getGeoLoc(), l);
	}

	/** Get the true UTM Northing (without offset) */
	static protected Integer getTrueNorthing(R_Node n) {
		return GeoLocHelper.getTrueNorthing(n.getGeoLoc());
	}

	/** Get the true UTM Easting (without offset) */
	static protected Integer getTrueEasting(R_Node n) {
		return GeoLocHelper.getTrueEasting(n.getGeoLoc());
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
		assert r_nodes.isEmpty();
		if(hasLocation(r_node))
			unsorted.add(r_node);
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
		R_Node fnear = findNearest(first);
		R_Node lnear = findNearest(last);
		if(metersTo(first, fnear) < metersTo(last, lnear)) {
			r_nodes.addFirst(fnear);
			unsorted.remove(fnear);
		} else {
			r_nodes.addLast(lnear);
			unsorted.remove(lnear);
		}
	}

	/** Find the nearest unsorted node to the given node */
	protected R_Node findNearest(R_Node end) {
		R_Node nearest = null;
		double n_meters = 0;
		for(R_Node r_node: unsorted) {
			double m = metersTo(r_node, end);
			if(nearest == null || m < n_meters) {
				nearest = r_node;
				n_meters = m;
			}
		}
		return nearest;
	}

	/** Check if the roadway nodes are in reverse order */
	protected boolean isReversed() {
		return r_nodes.size() > 1 && !isUpstreamToDownstream();
	}

	/** Check if the nodes are in upstream-to-downstream order */
	protected boolean isUpstreamToDownstream() {
		R_Node first = r_nodes.getFirst();
		R_Node last = r_nodes.getLast();
		Integer nf = getTrueNorthing(first);
		Integer nl = getTrueNorthing(last);
		Integer ef = getTrueEasting(first);
		Integer el = getTrueEasting(last);
		if(nf == null || nl == null || ef == null || el == null)
			return false;
		switch(Direction.fromOrdinal(road_dir)) {
		case NORTH:
			return nf < nl;
		case SOUTH:
			return nf > nl;
		case EAST:
			return ef < el;
		case WEST:
			return ef > el;
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
			if(previous != null)
				miles += metersToMiles(metersTo(previous, n));
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
			double m = metersTo(n, loc);
			if(nearest == null || m < n_meters) {
				nearest = n;
				n_after = n;
				n_mile = mile;
				n_meters = m;
			} else if(n_after == nearest)
				n_after = n;
		}
		float mi = metersToMiles(n_meters);
		if(metersTo(n_after, nearest) > metersTo(n_after, loc))
			return n_mile + mi;
		else
			return n_mile - mi;
	}

	/** Get the list of r_nodes */
	public List<R_Node> getNodes() {
		return r_nodes;
	}

	/** Find the nearest node to the given location */
	public R_Node findNearest(GeoLoc loc) {
		Integer easting = GeoLocHelper.getTrueEasting(loc);
		Integer northing = GeoLocHelper.getTrueNorthing(loc);
		if(easting == null || northing == null)
			return null;
		else
			return findNearest(easting, northing);
	}

	/** Find the nearest node to the given location */
	public R_Node findNearest(int easting, int northing) {
		R_Node nearest = null;
		double n_meters = 0;
		for(R_Node n: r_nodes) {
			double m = GeoLocHelper.metersTo(n.getGeoLoc(), easting,
				northing);
			if(nearest == null || m < n_meters) {
				nearest = n;
				n_meters = m;
			}
		}
		return nearest;
	}

	/** Find the nearest node to the given location with given type */
	public R_Node findNearest(int easting, int northing, R_NodeType nt) {
		R_Node nearest = null;
		double n_meters = 0;
		for(R_Node n: r_nodes) {
			if(n.getNodeType() != nt.ordinal())
				continue;
			double m = GeoLocHelper.metersTo(n.getGeoLoc(), easting,
				northing);
			if(nearest == null || m < n_meters) {
				nearest = n;
				n_meters = m;
			}
		}
		return nearest;
	}

	/** Get the lane count at the given location */
	public int getLaneCount(int easting, int northing) {
		R_Node last = findLastBefore(easting, northing);
		if(last == null)
			return 0;
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
			if(n == last)
				break;
		}
		return right - left;
	}

	/** Fint the last node before the given location */
	protected R_Node findLastBefore(int easting, int northing) {
		R_Node nearest = null;
		R_Node n_before = null;
		R_Node n_after = null;
		double n_meters = 0;
		for(R_Node n: r_nodes) {
			double m = GeoLocHelper.metersTo(n.getGeoLoc(),
				easting, northing);
			if(nearest == null || m < n_meters) {
				n_before = nearest;
				nearest = n;
				n_after = n;
				n_meters = m;
			} else if(n_after == nearest)
				n_after = n;
		}
		if(nearest == null)
			return null;
		GeoLoc ga = n_after.getGeoLoc();
		double m0 = GeoLocHelper.metersTo(ga, nearest.getGeoLoc());
		double m1 = GeoLocHelper.metersTo(ga, easting, northing);
		if(m0 > m1)
			return nearest;
		else
			return n_before;
	}

	/** Get the left lane shift at the given location */
	public int getShift(int easting, int northing) {
		R_Node last = findLastBefore(easting, northing);
		if(last == null)
			return 0;
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
			if(n == last)
				return left;
		}
		// This should never happen -- we didn't find last node?
		return 0;
	}
}
