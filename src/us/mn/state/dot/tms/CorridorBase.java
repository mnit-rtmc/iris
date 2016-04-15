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
package us.mn.state.dot.tms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import static us.mn.state.dot.tms.GeoLocHelper.distanceTo;
import static us.mn.state.dot.tms.GeoLocHelper.segmentDistance;
import us.mn.state.dot.tms.geo.Position;
import us.mn.state.dot.tms.geo.SphericalMercatorPosition;
import us.mn.state.dot.tms.units.Distance;
import static us.mn.state.dot.tms.units.Distance.Units.MILES;

/**
 * A corridor is a collection of all R_Node objects for one roadway corridor.
 *
 * @author Douglas Lau
 */
public class CorridorBase<T extends R_Node> implements Iterable<T> {

	/** Adjustment for r_node milepoints falling on exact same spot */
	static protected float calculateEpsilon(float v) {
		return (v != 0) ? (v * 0.0000001f) : 0.0000001f;
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
	static private boolean hasLocation(R_Node n) {
		return !GeoLocHelper.isNull(n.getGeoLoc());
	}

	/** Corridor name */
	private final String name;

	/** Get the corridor name */
	public String getName() {
		return name;
	}

	/** Get a string representation of the corridor */
	@Override
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
	private final Set<T> unsorted = new HashSet<T>();

	/** Roadway node list */
	private final ArrayList<T> r_nodes = new ArrayList<T>();

	/** Mapping from milepoint to r_node */
	protected final TreeMap<Float, T> n_points =
		new TreeMap<Float, T>();

	/** Create a new corridor */
	public CorridorBase(GeoLoc loc) {
		name = GeoLocHelper.getCorridorName(loc);
		roadway = loc.getRoadway();
		road_dir = loc.getRoadDir();
	}

	/** Add a roadway node to the corridor */
	public void addNode(T r_node) {
		if (hasLocation(r_node) && !r_node.getAbandoned()) {
			unsorted.add(r_node);
			unsorted.addAll(r_nodes);
			r_nodes.clear();
			n_points.clear();
		}
	}

	/** Remove a roadway node from the corridor */
	public void removeNode(T r_node) {
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
	private void sortNodes() {
		assert r_nodes.isEmpty();
		beginList();
		while (!unsorted.isEmpty())
			linkNearestNode();
		if (isReversed())
			Collections.reverse(r_nodes);
	}

	/** Put one r_node into the list */
	private void beginList() {
		// Only way to get one Set element is to get iterator
		Iterator<T> it = unsorted.iterator();
		if (it.hasNext()) {
			r_nodes.add(it.next());
			it.remove();
		}
	}

	/** Link the nearest node */
	private void linkNearestNode() {
		assert r_nodes.size() > 0;
		T first = r_nodes.get(0);
		T last = r_nodes.get(r_nodes.size() - 1);
		NodeDistance fnear = findNearest(first);
		NodeDistance lnear = findNearest(last);
		if (fnear == null || lnear == null)
			unsorted.clear();
		else if (fnear.meters < lnear.meters) {
			r_nodes.add(0, fnear.node);
			unsorted.remove(fnear.node);
		} else {
			r_nodes.add(lnear.node);
			unsorted.remove(lnear.node);
		}
	}

	/** Simple structure to hold a node and distance */
	private class NodeDistance {
		private final double meters;
		private final T node;
		public NodeDistance(Double m, T n) {
			meters = m;
			node = n;
		}
	}

	/** Find the nearest unsorted node to the given node */
	private NodeDistance findNearest(T end) {
		NodeDistance near = null;
		for (T r_node: unsorted) {
			Distance m = nodeDistance(r_node, end);
			if (m != null && (near == null || m.m() < near.meters))
				near = new NodeDistance(m.m(), r_node);
		}
		return near;
	}

	/** Check if the roadway nodes are in reverse order */
	private boolean isReversed() {
		return r_nodes.size() > 1 && !isUpstreamToDownstream();
	}

	/** Check if the nodes are in upstream-to-downstream order */
	private boolean isUpstreamToDownstream() {
		assert r_nodes.size() > 1;
		T first = r_nodes.get(0);
		T last = r_nodes.get(r_nodes.size() - 1);
		Position pf = GeoLocHelper.getWgs84Position(first.getGeoLoc());
		Position pl = GeoLocHelper.getWgs84Position(last.getGeoLoc());
		if (pf == null || pl == null)
			return false;
		switch (Direction.fromOrdinal(road_dir)) {
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

	/** Calculate the mile points for all nodes on the corridor */
	private void calculateNodeMilePoints() {
		assert n_points.isEmpty();
		float miles = 0;
		T previous = null;
		for (T n: r_nodes) {
			if (previous != null) {
				Distance m = nodeDistance(previous, n);
				if (m == null)
					continue;
				miles += m.asFloat(MILES);
			}
			while (n_points.containsKey(miles))
				miles += calculateEpsilon(miles);
			n_points.put(miles, n);
			previous = n;
		}
	}

	/** Calculate the mile point for a location.
	 * @param loc Location to calculate.
	 * @return Mile point for location, or null if no r_nodes exist. */
	public Float calculateMilePoint(GeoLoc loc) {
		if (n_points.isEmpty())
			return null;
		T nearest = null;
		T n_after = null;
		float n_mile = 0;
		double n_meters = 0;
		for (Float mile: n_points.keySet()) {
			T n = n_points.get(mile);
			Distance m = nodeDistance(n, loc);
			if (m != null) {
				double ms = m.m();
				if (nearest == null || ms < n_meters) {
					nearest = n;
					n_after = n;
					n_mile = mile;
					n_meters = ms;
				} else if (n_after == nearest)
					n_after = n;
			}
		}
		if (nearest == null || n_after == null)
			return null;
		float mi = new Distance(n_meters).asFloat(MILES);
		Distance m0 = nodeDistance(n_after, nearest);
		Distance m1 = nodeDistance(n_after, loc);
		if (m0 != null && m1 != null && m0.m() > m1.m())
			return n_mile + mi;
		else
			return n_mile - mi;
	}

	/** Get the mile point for a specified node */
	public Float getMilePoint(T r_node) {
		for (Float mile: n_points.keySet()) {
			T n = n_points.get(mile);
			if (n == r_node)
				return mile;
		}
		return null;
	}

	/** Create a r_node iterator */
	@Override
	public Iterator<T> iterator() {
		return r_nodes.iterator();
	}

	/** Find the nearest node to the given location */
	public T findNearest(GeoLoc loc) {
		Position pos = GeoLocHelper.getWgs84Position(loc);
		return (pos != null) ? findNearest(pos) : null;
	}

	/** Find the nearest node to the given position */
	public T findNearest(Position pos) {
		T nearest = null;
		double n_meters = 0;
		for (T n: r_nodes) {
			Distance m = distanceTo(n.getGeoLoc(), pos);
			if (m != null && (nearest == null || m.m() < n_meters)) {
				nearest = n;
				n_meters = m.m();
			}
		}
		return nearest;
	}

	/** Find the nearest node to the given location with given type */
	private T findNearest(Position pos, R_NodeType nt) {
		T nearest = null;
		double n_meters = 0;
		for (T n: r_nodes) {
			if (n.getNodeType() != nt.ordinal())
				continue;
			Distance m = distanceTo(n.getGeoLoc(), pos);
			if (m != null && (nearest == null || m.m() < n_meters)) {
				nearest = n;
				n_meters = m.m();
			}
		}
		return nearest;
	}

	/** Find the nearest node to the given location with given type.
	 * @param pos Location to search.
	 * @param nt Node type.
	 * @param pickable Pickable flag.
	 * @return Nearest matching node. */
	public T findNearest(Position pos, R_NodeType nt, boolean pickable) {
		T nearest = null;
		double n_meters = 0;
		for (T n: r_nodes) {
			if (n.getNodeType() != nt.ordinal())
				continue;
			if (n.getPickable() != pickable)
				continue;
			Distance m = distanceTo(n.getGeoLoc(), pos);
			if (m != null && (nearest == null || m.m() < n_meters)) {
				nearest = n;
				n_meters = m.m();
			}
		}
		return nearest;
	}

	/** Fint the last node before the given location */
	public T findLastBefore(Position pos) {
		T nearest = null;
		T n_before = null;
		T n_after = null;
		double n_meters = 0;
		for (T n: r_nodes) {
			Distance m = distanceTo(n.getGeoLoc(), pos);
			if (m != null) {
				double ms = m.m();
				if (nearest == null || ms < n_meters) {
					n_before = nearest;
					nearest = n;
					n_after = n;
					n_meters = ms;
				} else if (ms == n_meters) {
					// coincident points
					nearest = n;
					n_after = n;
				} else if (n_after == nearest)
					n_after = n;
			}
		}
		if (nearest == null)
			return null;
		GeoLoc ga = n_after.getGeoLoc();
		Distance m0 = distanceTo(ga, nearest.getGeoLoc());
		Distance m1 = distanceTo(ga, pos);
		if (m0 != null && m1 != null && m0.m() > m1.m())
			return nearest;
		else
			return n_before;
	}

	/** Get the lane configuration at the given location */
	public LaneConfiguration laneConfiguration(Position pos) {
		T node = findLastBefore(pos);
		if (node != null)
			return laneConfiguration(node);
		else
			return new LaneConfiguration(0, 0);
	}

	/** Get the lane configuration at the given node */
	private LaneConfiguration laneConfiguration(T node) {
		int left = 0;
		int right = 0;
		for (T n: r_nodes) {
			if (n.getAttachSide())
				left = n.getShift();
			else
				right = n.getShift();
			if (n.getNodeType() == R_NodeType.STATION.ordinal()) {
				if (n.getAttachSide())
					right = left + n.getLanes();
				else
					left = right - n.getLanes();
			}
			if (n == node)
				return new LaneConfiguration(left, right);
		}
		// Node not found on corridor
		return new LaneConfiguration(0, 0);
	}

	/** Get the lane count a given location */
	public int getLaneCount(LaneType lt, GeoLoc loc) {
		Position pos = GeoLocHelper.getWgs84Position(loc);
		if (pos == null)
			return 0;
		switch (lt) {
		case EXIT:
			R_Node n = findNearest(pos, R_NodeType.EXIT);
			return (n != null) ? n.getLanes() : 0;
		case MERGE:
			R_Node mn = findNearest(pos, R_NodeType.ENTRANCE);
			return (mn != null) ? mn.getLanes() : 0;
		default:
			return laneConfiguration(pos).getLanes();
		}
	}

	/** Snap a point to the corridor.
	 * @param smp Selected point (spherical mercator position).
	 * @param lt Lane type (MAINLINE, EXIT, MERGE or CD_LANE).
	 * @param max_dist Maximum distance to snap.
	 * @return GeoLocDist snapped to corridor, or null if not found. */
	public GeoLocDist snapGeoLoc(SphericalMercatorPosition smp, LaneType lt,
		Distance max_dist)
	{
		switch (lt) {
		case EXIT:
		case MERGE:
		case MAINLINE:
		case CD_LANE:
			if (checkLaneType(lt))
				return snapGeoLoc2(smp, lt, max_dist);
		}
		return null;
	}

	/** Check if the road class matches a lane type */
	private boolean checkLaneType(LaneType lt) {
		RoadClass rc = RoadClass.fromOrdinal(roadway.getRClass());
		boolean cd_cls = (rc == RoadClass.CD_ROAD);
		boolean cd_typ = (lt == LaneType.CD_LANE);
		return cd_cls == cd_typ;
	}

	/** Snap a point to the corridor.
	 * @param smp Selected point (spherical mercator position).
	 * @param max_dist Maximum distance to snap.
	 * @return GeoLocDist snapped to corridor, or null if not found. */
	private GeoLocDist snapGeoLoc2(SphericalMercatorPosition smp,
		LaneType lt, Distance max_dist)
	{
		double dist = max_dist.m();
		GeoLoc l0 = null;
		GeoLoc l1 = null;
		GeoLoc lp = null;	/* previous location */
		T np = null;		/* previous node */
		for (T n: r_nodes) {
			if (R_NodeHelper.isContinuityBreak(n)) {
				np = null;
				lp = null;
				continue;
			}
			GeoLoc l = n.getGeoLoc();
			if ((lp != null) &&
			   (!skipExit(lt, np)) &&
			   (!skipEntrance(lt, n)))
			{
				double m = segmentDistance(lp, l, smp);
				if (m < dist) {
					l0 = lp;
					l1 = l;
					dist = m;
				}
			}
			np = n;
			lp = l;
		}
		if (l0 != null) {
			GeoLoc loc = GeoLocHelper.snapSegment(l0, l1, smp);
			if (loc != null)
				return new GeoLocDist(loc, new Distance(dist));
		}
		return null;
	}

	/** Check if exit lane type should be skipped */
	private boolean skipExit(LaneType lt, T n) {
		return (n != null) &&
		       (lt == LaneType.EXIT) &&
		       !R_NodeHelper.isExit(n);
	}

	/** Check if entrance lane type should be skipped */
	private boolean skipEntrance(LaneType lt, T n) {
		assert (n != null);
		return (lt == LaneType.MERGE) &&
		       !R_NodeHelper.isEntrance(n);
	}

	/** GeoLoc / distance pair */
	static public class GeoLocDist {
		public final GeoLoc loc;
		public final Distance dist;
		private GeoLocDist(GeoLoc l, Distance d) {
			loc = l;
			dist = d;
		}
	}
}
