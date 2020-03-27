/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2020  Minnesota Department of Transportation
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
import java.util.Map;
import java.util.TreeMap;
import us.mn.state.dot.tms.Direction;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.LaneType;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.R_NodeHelper;
import us.mn.state.dot.tms.geo.SphericalMercatorPosition;
import us.mn.state.dot.tms.units.Distance;

/**
 * This is a class to manage roadway network corridors.
 *
 * @author Douglas Lau
 */
public class CorridorManager {

	/** Map to hold all corridors */
	private final Map<String, Corridor> corridors =
		new TreeMap<String, Corridor>();

	/** Create all corridors from the existing r_nodes */
	public synchronized void createCorridors() {
		corridors.clear();
		Iterator<R_Node> it = R_NodeHelper.iterator();
		while (it.hasNext()) {
			R_Node r_node = it.next();
			if (r_node instanceof R_NodeImpl) {
				R_NodeImpl n = (R_NodeImpl) r_node;
				n.updateFork();
				addCorridorNode(n);
			}
		}
		for (Corridor c: corridors.values())
			c.arrangeNodes();
	}

	/** Add an r_node to the proper corridor */
	private void addCorridorNode(R_NodeImpl r_node) {
		String cid = R_NodeHelper.getCorridorName(r_node);
		if (cid != null)
			addCorridorNode(cid, r_node);
	}

	/** Add an r_node to the specified corridor */
	private void addCorridorNode(String cid, R_NodeImpl r_node) {
		Corridor c = corridors.get(cid);
		if (c == null) {
			c = new Corridor(r_node.getGeoLoc());
			corridors.put(cid, c);
		}
		c.addNode(r_node);
	}

	/** Lookup the named corridor */
	public synchronized Corridor getCorridor(String cid) {
		return (cid != null) ? corridors.get(cid) : null;
	}

	/** Write the body of the r_node configuration XML file */
	public synchronized void writeXmlBody(Writer w,
		Map<String, RampMeterImpl> m_nodes) throws IOException
	{
		for (Corridor c: corridors.values())
			c.writeXml(w, m_nodes);
	}

	/** Find the current bottlenecks for all corridors */
	public synchronized void findBottlenecks() {
		for (Corridor c: corridors.values())
			c.findBottlenecks();
	}

	/** Lookup the corridor for a location */
	public Corridor getCorridor(GeoLoc loc) {
		String cid = GeoLocHelper.getCorridorName(loc);
		return (cid != null) ? getCorridor(cid) : null;
	}

	/** Create a GeoLoc snapped to nearest r_node segment.
	 * NOTE: copied from client/roads/R_NodeManager. */
	public synchronized GeoLoc snapGeoLoc(SphericalMercatorPosition smp,
		LaneType lt, Distance max_dist, Direction dir)
	{
		GeoLoc loc = null;
		Distance dist = max_dist;
		for (Corridor c: corridors.values()) {
			if (dir != Direction.UNKNOWN &&
			    dir.ordinal() != c.getRoadDir())
				continue;
			Corridor.GeoLocDist ld = c.snapGeoLoc(smp, lt, dist);
			if (ld != null && ld.dist.m() < dist.m()) {
				loc = ld.loc;
				dist = ld.dist;
			}
		}
		return loc;
	}
}
