/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2009  Minnesota Department of Transportation
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

/**
 * A corridor is a collection of all R_Node objects for one freeway corridor.
 *
 * @author Douglas Lau
 */
public class CorridorBase {

	/** Calculate the distance to another roadway node (in meters) */
	static public Double metersTo(R_Node a, R_Node b) {
		return GeoLocHelper.metersTo(a.getGeoLoc(), b.getGeoLoc());
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

	/** Corridor freeway */
	protected final Road freeway;

	/** Get the corridor freeway */
	public Road getFreeway() {
		return freeway;
	}

	/** Corridor direction */
	protected final short free_dir;

	/** Get the corridor direction */
	public short getFreeDir() {
		return free_dir;
	}

	/** Get the corridor ID */
	public String getID() {
		return GeoLocHelper.getCorridorID(freeway, free_dir);
	}

	/** Flag for downstream-to-upstream (backwards) order */
	protected final boolean order_down_up;

	/** Set of unsorted roadway nodes */
	protected final Set<R_Node> unsorted = new HashSet<R_Node>();

	/** Roadway node list */
	protected final LinkedList<R_Node> r_nodes = new LinkedList<R_Node>();

	/** Create a new corridor */
	public CorridorBase(GeoLoc loc, boolean order) {
		name = GeoLocHelper.getCorridorName(loc);
		freeway = loc.getFreeway();
		free_dir = loc.getFreeDir();
		order_down_up = order;
	}

	/** Create a new corridor */
	protected CorridorBase(GeoLoc loc) {
		this(loc, false);
	}

	/** Add a roadway node to the corridor */
	public void addNode(R_Node r_node) {
		assert r_nodes.isEmpty();
		if(hasLocation(r_node))
			unsorted.add(r_node);
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

	/** Check if the nodes are in upstream-to-downstream order */
	protected boolean isUpstreamToDownstream() {
		R_Node first = r_nodes.getFirst();
		R_Node last = r_nodes.getLast();
		switch(free_dir) {
			case Road.NORTH:
				return getTrueNorthing(first) <
					getTrueNorthing(last);
			case Road.SOUTH:
				return getTrueNorthing(first) >
					getTrueNorthing(last);
			case Road.EAST:
				return getTrueEasting(first) <
					getTrueEasting(last);
			case Road.WEST:
				return getTrueEasting(first) >
					getTrueEasting(last);
			case Road.INNER_LOOP:
				// FIXME: this might be tricky
				return false;
			case Road.OUTER_LOOP:
				// FIXME: this might be tricky
				return false;
		}
		return false;
	}

	/** Check if the roadway nodes are in reverse order */
	protected boolean isReversed() {
		return r_nodes.size() > 1 &&
			(order_down_up == isUpstreamToDownstream());
	}

	/** Reverse the list of roadway nodes */
	protected void reverseList() {
		LinkedList<R_Node> tmp = new LinkedList<R_Node>(r_nodes);
		r_nodes.clear();
		for(R_Node r_node: tmp)
			r_nodes.addFirst(r_node);
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

	/** Sort the roadway nodes for the corridor */
	protected void sortNodes() {
		assert r_nodes.isEmpty();
		beginList();
		while(!unsorted.isEmpty())
			linkNearestNode();
		if(isReversed())
			reverseList();
	}

	/** Arrange the nodes in the corridor */
	public void arrangeNodes() {
		sortNodes();
	}

	/** Get the list of r_nodes */
	public List<R_Node> getNodes() {
		return r_nodes;
	}

	/** Find the nearest node to the given location */
	public R_Node findNearest(GeoLoc loc) {
		R_Node nearest = null;
		double n_meters = 0;
		for(R_Node n: r_nodes) {
			double m = GeoLocHelper.metersTo(n.getGeoLoc(), loc);
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
}
