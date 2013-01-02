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
package us.mn.state.dot.tms.server;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.R_NodeHelper;
import us.mn.state.dot.tms.units.Distance;

/**
 * This is a class to manage roadway network corridors.
 *
 * @author Douglas Lau
 */
public class CorridorManager {

	/** Map to hold all corridors */
	protected final Map<String, Corridor> corridors =
		new TreeMap<String, Corridor>();

	/** Create all corridors from the existing r_nodes */
	public synchronized void createCorridors() {
		corridors.clear();
		Iterator<R_Node> it = R_NodeHelper.iterator();
		while(it.hasNext()) {
			R_Node r_node = it.next();
			findDownstreamLinks((R_NodeImpl)r_node);
			addCorridorNode(r_node);
		}
		for(Corridor c: corridors.values())
			c.arrangeNodes();
	}

	/** Add an r_node to the proper corridor */
	private void addCorridorNode(R_Node r_node) {
		String cid = R_NodeHelper.getCorridorName(r_node);
		if(cid != null)
			addCorridorNode(cid, r_node);
	}

	/** Add an r_node to the specified corridor */
	private void addCorridorNode(String cid, R_Node r_node) {
		Corridor c = corridors.get(cid);
		if(c == null) {
			c = new Corridor(r_node.getGeoLoc());
			corridors.put(cid, c);
		}
		c.addNode(r_node);
	}

	/** Find downstream links (not in corridor) for the given node */
	private void findDownstreamLinks(R_NodeImpl r_node) {
		r_node.clearDownstream();
		if(r_node.isExit())
			linkExitToEntrance(r_node);
		else if(r_node.isAccess())
			linkAccessToEntrance(r_node);
		// FIXME: link intersections together
	}

	/** Link an exit node with a corresponding entrance node */
	private void linkExitToEntrance(R_NodeImpl r_node) {
		LinkedList<R_NodeImpl> links = new LinkedList<R_NodeImpl>();
		Iterator<R_Node> it = R_NodeHelper.iterator();
		while(it.hasNext()) {
			R_Node other = it.next();
			if(R_NodeHelper.isExitLink(r_node, other))
				links.add((R_NodeImpl)other);
		}
		R_NodeImpl link = findNearest(r_node, links);
		if(link != null)
			r_node.addDownstream(link);
	}

	/** Find the nearest r_node in a list */
	static protected R_NodeImpl findNearest(R_NodeImpl r_node,
		List<R_NodeImpl> others)
	{
		R_NodeImpl nearest = null;
		Distance d = new Distance(0);
		for(R_NodeImpl other: others) {
			Distance m = Corridor.nodeDistance(r_node, other);
			if(m != null && (nearest == null || m.m() < d.m())) {
				nearest = other;
				d = m;
			}
		}
		return nearest;
	}

	/** Link an access node with all corresponding entrance nodes */
	private void linkAccessToEntrance(R_NodeImpl r_node) {
		Iterator<R_Node> it = R_NodeHelper.iterator();
		while(it.hasNext()) {
			R_Node n = it.next();
			if(R_NodeHelper.isAccessLink(r_node, n))
				r_node.addDownstream((R_NodeImpl)n);
		}
	}

	/** Lookup the named corridor */
	public synchronized Corridor getCorridor(String c) {
		if(c != null)
			return corridors.get(c);
		else
			return null;
	}

	/** Lookup the corridor for an O/D pair */
	protected Corridor getCorridor(ODPair od) {
		return getCorridor(od.getCorridorName());
	}

	/** Write the body of the r_node configuration XML file */
	public synchronized void writeXmlBody(Writer w,
		Map<String, RampMeterImpl> m_nodes) throws IOException
	{
		for(Corridor c: corridors.values())
			c.writeXml(w, m_nodes);
	}

	/** Find the current bottlenecks for all corridors */
	public synchronized void findBottlenecks() {
		for(Corridor c: corridors.values())
			c.findBottlenecks();
	}
}
