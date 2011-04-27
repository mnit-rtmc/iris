/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2011  Minnesota Department of Transportation
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.R_NodeHelper;

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
		R_NodeHelper.find(new Checker<R_Node>() {
			public boolean check(R_Node r_node) {
				findDownstreamLinks((R_NodeImpl)r_node);
				addCorridorNode(r_node);
				return false;
			}
		});
		for(Corridor c: corridors.values())
			c.arrangeNodes();
	}

	/** Add an r_node to the proper corridor */
	protected void addCorridorNode(R_Node r_node) {
		String cid = R_NodeHelper.getCorridorName(r_node);
		if(cid != null)
			addCorridorNode(cid, r_node);
	}

	/** Add an r_node to the specified corridor */
	protected void addCorridorNode(String cid, R_Node r_node) {
		Corridor c = corridors.get(cid);
		if(c == null) {
			c = new Corridor(r_node.getGeoLoc());
			corridors.put(cid, c);
		}
		c.addNode(r_node);
	}

	/** Find downstream links (not in corridor) for the given node */
	protected void findDownstreamLinks(R_NodeImpl r_node) {
		r_node.clearDownstream();
		if(r_node.isExit())
			linkExitToEntrance(r_node);
		else if(r_node.isAccess())
			linkAccessToEntrance(r_node);
		// FIXME: link intersections together
	}

	/** Link an exit node with a corresponding entrance node */
	protected void linkExitToEntrance(final R_NodeImpl r_node) {
		final LinkedList<R_NodeImpl> links =
			new LinkedList<R_NodeImpl>();
		R_NodeHelper.find(new Checker<R_Node>() {
			public boolean check(R_Node other) {
				if(R_NodeHelper.isExitLink(r_node, other))
					links.add((R_NodeImpl)other);
				return false;
			}
		});
		R_NodeImpl link = findNearest(r_node, links);
		if(link != null)
			r_node.addDownstream(link);
	}

	/** Find the nearest r_node in a list */
	static protected R_NodeImpl findNearest(R_NodeImpl r_node,
		List<R_NodeImpl> others)
	{
		R_NodeImpl nearest = null;
		double distance = 0;
		for(R_NodeImpl other: others) {
			Double m = Corridor.metersTo(r_node, other);
			if(m != null && (nearest == null || m < distance)) {
				nearest = other;
				distance = m;
			}
		}
		return nearest;
	}

	/** Link an access node with all corresponding entrance nodes */
	protected void linkAccessToEntrance(final R_NodeImpl r_node) {
		R_NodeHelper.find(new Checker<R_Node>() {
			public boolean check(R_Node n) {
				if(R_NodeHelper.isAccessLink(r_node, n))
					r_node.addDownstream((R_NodeImpl)n);
				return false;
			}
		});
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

	/** Print the body of the r_node configuration XML file */
	public synchronized void printXmlBody(PrintWriter out,
		Map<String, RampMeterImpl> m_nodes)
	{
		for(Corridor c: corridors.values())
			c.printXml(out, m_nodes);
	}

	/** Find the current bottlenecks for all corridors */
	public synchronized void findBottlenecks() {
		for(Corridor c: corridors.values())
			c.findBottlenecks();
	}
}
