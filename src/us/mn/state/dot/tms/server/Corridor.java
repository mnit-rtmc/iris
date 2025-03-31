/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2025  Minnesota Department of Transportation
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import us.mn.state.dot.tms.CorridorBase;
import us.mn.state.dot.tms.Direction;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.R_NodeHelper;
import us.mn.state.dot.tms.Road;

/**
 * A corridor is a collection of all R_Node objects for one roadway corridor.
 *
 * @author Douglas Lau
 */
public class Corridor extends CorridorBase<R_NodeImpl> {

	/** Create a new corridor */
	public Corridor(GeoLoc loc) {
		super(loc);
	}

	/** Interface to find a node on the corridor */
	static public interface NodeFinder {
		public boolean check(float m, R_NodeImpl r_node);
	}

	/** Find an active node using a node finder callback interface */
	public R_NodeImpl findActiveNode(NodeFinder finder) {
		for (Float m: n_points.keySet()) {
			assert m != null;
			R_NodeImpl n = n_points.get(m);
			if (n.getActive() && finder.check(m, n))
				return n;
		}
		return null;
	}

	/** Interface to find a station on the corridor */
	static public interface StationFinder {
		public boolean check(float m, StationImpl s);
	}

	/** Find a station using a station finder callback interface */
	public StationImpl findStation(StationFinder finder) {
		for (Float m: n_points.keySet()) {
			assert m != null;
			R_NodeImpl n = n_points.get(m);
			if (n.getActive() && R_NodeHelper.isStation(n)) {
				StationImpl s = n.getStation();
				if (s != null && finder.check(m, s))
					return s;
			}
		}
		return null;
	}

	/** Get the corridor IDs of all linked CD roads */
	public Iterator<String> getLinkedCDRoads() {
		HashSet<String> cds = new HashSet<String>();
		for (R_NodeImpl n: n_points.values()) {
			if (R_NodeHelper.isEntrance(n)) {
				GeoLoc l = n.getGeoLoc();
				if (matchesCD(l)) {
					cds.add(
						GeoLocHelper.getLinkedName(l)
					);
				}
			}
		}
		return cds.iterator();
	}

	/** Check if a location matches as a CD road */
	private boolean matchesCD(GeoLoc loc) {
		Road xs = (loc != null) ? loc.getCrossStreet() : null;
		String nm = (xs != null) ? xs.getName() : "";
		return nm.startsWith(roadway.toString())
		    && nm.matches(".*\\bCD\\b.*");
	}

	/** Write out the corridor to an XML file */
	public void writeXml(Writer w, Map<String, RampMeterImpl> m_nodes)
		throws IOException
	{
		w.write("<corridor route='" + roadway + "' dir='" +
			Direction.fromOrdinal(road_dir).abbrev + "'>\n");
		for (R_NodeImpl n: this)
			n.writeXml(w, m_nodes);
		w.write("</corridor>\n");
	}

	/** Find the current bottlenecks on the corridor */
	public void findBottlenecks() {
		final TreeMap<Float, StationImpl> upstream =
			new TreeMap<Float, StationImpl>();
		findStation(new StationFinder() {
			public boolean check(float m, StationImpl s) {
				if (s.getSpeedAvg(10) > 0) {
					s.calculateBottleneck(m, upstream);
					upstream.put(m, s);
				} else
					s.clearBottleneck();
				s.debug();
				return false;
			}
		});
	}
}
