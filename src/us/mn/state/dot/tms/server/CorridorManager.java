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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.R_Node;

/**
 * This is a class to manage roadway network corridors.
 *
 * @author Douglas Lau
 */
public class CorridorManager {

	/** SONAR namespace */
	protected final Namespace namespace;

	/** Map to hold all corridors */
	protected final Map<String, Corridor> corridors =
		new TreeMap<String, Corridor>();

	/** Create all corridors from the existing r_nodes */
	public CorridorManager() {
		namespace = BaseObjectImpl.namespace;
		namespace.findObject(R_Node.SONAR_TYPE,
			new Checker<R_NodeImpl>()
		{
			public boolean check(R_NodeImpl r_node) {
				findDownstreamLinks(r_node);
				GeoLoc loc = r_node.getGeoLoc();
				String cid = GeoLocHelper.getCorridorName(loc);
				if(cid != null) {
					Corridor c = corridors.get(cid);
					if(c == null) {
						c = new Corridor(loc);
						corridors.put(cid, c);
					}
					c.addNode(r_node);
				}
				return false;
			}
		});
		for(Corridor c: corridors.values())
			c.arrangeNodes();
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
		namespace.findObject(R_Node.SONAR_TYPE,
			new Checker<R_NodeImpl>()
		{
			public boolean check(R_NodeImpl other) {
				if(r_node.isExitLink(other))
					links.add(other);
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
			double m = Corridor.metersTo(r_node, other);
			if(nearest == null || m < distance) {
				nearest = other;
				distance = m;
			}
		}
		return nearest;
	}

	/** Link an access node with all corresponding entrance nodes */
	protected void linkAccessToEntrance(final R_NodeImpl r_node) {
		namespace.findObject(R_Node.SONAR_TYPE,
			new Checker<R_NodeImpl>()
		{
			public boolean check(R_NodeImpl other) {
				if(r_node.isAccessLink(other))
					r_node.addDownstream(other);
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
	public synchronized void printXmlBody(PrintWriter out) {
		for(Corridor c: corridors.values())
			c.printXml(out);
	}
}
