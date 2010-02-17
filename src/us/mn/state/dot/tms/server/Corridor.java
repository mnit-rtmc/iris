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
package us.mn.state.dot.tms.server;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import us.mn.state.dot.tms.CorridorBase;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.R_Node;

/**
 * A corridor is a collection of all R_Node objects for one freeway corridor.
 *
 * @author Douglas Lau
 */
public class Corridor extends CorridorBase {

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

	/** Calculate the distance to a location (in meters) */
	static public Double metersTo(R_Node n, GeoLoc l) {
		return GeoLocHelper.metersTo(n.getGeoLoc(), l);
	}

	/** Mapping from milepoint to r_node */
	protected final TreeMap<Float, R_NodeImpl> n_points =
		new TreeMap<Float, R_NodeImpl>();

	/** Create a new corridor */
	public Corridor(GeoLoc loc) {
		super(loc);
	}

	/** Link each node with the next downstream node in the corridor */
	protected void linkDownstream() {
		Iterator<R_Node> down = r_nodes.iterator();
		// Throw away first r_node in downstream iterator
		if(down.hasNext())
			down.next();
		for(R_Node n: r_nodes) {
			R_NodeImpl r_node = (R_NodeImpl)n;
			if(down.hasNext()) {
				R_NodeImpl d = (R_NodeImpl)down.next();
				if(r_node.hasDownstreamLink())
					r_node.addDownstream(d);
			}
		}
	}

	/** Print out the corridor to an XML file */
	public void printXml(PrintWriter out) {
		out.println("<corridor route='" + freeway + "' dir='" +
			GeoLocHelper.getDirection(free_dir) + "'>");
		for(R_Node n: r_nodes)
			((R_NodeImpl)n).printXml(out);
		out.println("</corridor>");
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
			n_points.put(miles, (R_NodeImpl)n);
			previous = n;
		}
	}

	/** Arrange the nodes in the corridor */
	public void arrangeNodes() {
		sortNodes();
		linkDownstream();
		calculateNodeMilePoints();
	}

	/** Calculate the mile point for a location */
	public float calculateMilePoint(GeoLoc loc) throws BadRouteException {
		if(n_points.isEmpty())
			throw new BadRouteException("No nodes on corridor");
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

	/** Create a mapping from mile points to stations */
	public TreeMap<Float, StationImpl> createStationMap() {
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
	public R_NodeImpl findDownstreamNode(GeoLoc loc)
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
	public R_NodeImpl findNode(NodeFinder finder) {
		for(R_NodeImpl r_node: n_points.values()) {
			if(finder.check(r_node))
				return r_node;
		}
		return null;
	}

	/** Get a reversed list of nodes in the corridor */
	protected List<R_NodeImpl> getReversedList() {
		LinkedList<R_NodeImpl> rev = new LinkedList<R_NodeImpl>();
		for(R_NodeImpl r_node: n_points.values())
			rev.addFirst(r_node);
		return rev;
	}

	/** Find a node using a node finder callback (reverse order) */
	public R_NodeImpl findNodeReverse(NodeFinder finder) {
		for(R_NodeImpl r_node: getReversedList()) {
			if(finder.check(r_node))
				return r_node;
		}
		return null;
	}

	/** Get the ID of a linked CD road */
	public String getLinkedCDRoad() {
		// FIXME: there may be more than one linked CD road
		for(R_NodeImpl r_node: n_points.values()) {
			if(r_node.isCD()) {
				GeoLoc l = r_node.getGeoLoc();
				String c = GeoLocHelper.getLinkedCorridor(l);
				if(c != null)
					return c;
			}
		}
		return null;
	}
}
