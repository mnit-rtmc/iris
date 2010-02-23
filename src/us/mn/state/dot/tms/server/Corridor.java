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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import us.mn.state.dot.tms.CorridorBase;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.R_NodeHelper;

/**
 * A corridor is a collection of all R_Node objects for one freeway corridor.
 *
 * @author Douglas Lau
 */
public class Corridor extends CorridorBase {

	/** Create a new corridor */
	public Corridor(GeoLoc loc) {
		super(loc);
	}

	/** Arrange the nodes in the corridor */
	public void arrangeNodes() {
		super.arrangeNodes();
		linkDownstream();
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

	/** Create a mapping from mile points to stations */
	public TreeMap<Float, StationImpl> createStationMap() {
		TreeMap<Float, StationImpl> stations =
			new TreeMap<Float, StationImpl>();
		for(Float m: n_points.keySet()) {
			R_NodeImpl n = (R_NodeImpl)n_points.get(m);
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
		Float origin = calculateMilePoint(od.getOrigin());
		Float destination = calculateMilePoint(od.getDestination());
		if(origin == null || destination == null)
			throw new BadRouteException("No nodes on corridor");
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
		Float m = calculateMilePoint(loc);
		if(m == null)
			throw new BadRouteException("No nodes on corridor");
		for(Float mile: n_points.keySet()) {
			if(mile > m)
				return (R_NodeImpl)n_points.get(mile);
		}
		throw new BadRouteException("No downstream nodes");
	}

	/** Interface to find a node on the corridor */
	static public interface NodeFinder {
		public boolean check(R_NodeImpl r_node);
	}

	/** Find a node using a node finder callback interface */
	public R_NodeImpl findNode(NodeFinder finder) {
		for(R_Node n: n_points.values()) {
			R_NodeImpl r_node = (R_NodeImpl)n;
			if(finder.check(r_node))
				return r_node;
		}
		return null;
	}

	/** Get a reversed list of nodes in the corridor */
	protected List<R_NodeImpl> getReversedList() {
		LinkedList<R_NodeImpl> rev = new LinkedList<R_NodeImpl>();
		for(R_Node r_node: n_points.values())
			rev.addFirst((R_NodeImpl)r_node);
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
		for(R_Node r_node: n_points.values()) {
			if(R_NodeHelper.isCD(r_node)) {
				GeoLoc l = r_node.getGeoLoc();
				String c = GeoLocHelper.getLinkedCorridor(l);
				if(c != null)
					return c;
			}
		}
		return null;
	}

	/** Print out the corridor to an XML file */
	public void printXml(PrintWriter out) {
		out.println("<corridor route='" + freeway + "' dir='" +
			GeoLocHelper.getDirection(free_dir) + "'>");
		for(R_Node n: r_nodes)
			((R_NodeImpl)n).printXml(out);
		out.println("</corridor>");
	}
}
