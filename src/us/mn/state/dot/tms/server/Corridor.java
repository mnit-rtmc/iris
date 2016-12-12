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
import us.mn.state.dot.tms.units.Distance;
import static us.mn.state.dot.tms.units.Distance.Units.MILES;

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

	/** Arrange the nodes in the corridor */
	@Override
	public void arrangeNodes() {
		super.arrangeNodes();
		linkDownstream();
	}

	/** Link each node with the next downstream node in the corridor */
	private void linkDownstream() {
		Iterator<R_NodeImpl> down = iterator();
		// Throw away first r_node in downstream iterator
		if (down.hasNext())
			down.next();
		for (R_NodeImpl n: this) {
			if (down.hasNext()) {
				R_NodeImpl d = down.next();
				if (n.hasDownstreamLink())
					n.addDownstream(d);
			}
		}
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
	protected StationImpl findStation(StationFinder finder) {
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

	/** Create a mapping from mile points to stations */
	public TreeMap<Float, StationImpl> createStationMap() {
		final TreeMap<Float, StationImpl> stations =
			new TreeMap<Float, StationImpl>();
		findStation(new StationFinder() {
			public boolean check(float m, StationImpl s) {
				stations.put(m, s);
				return false;
			}
		});
		return stations;
	}

	/** Calculate the distance for the given O/D pair (miles) */
	public float calculateDistance(ODPair od) throws BadRouteException {
		Float origin = calculateMilePoint(od.getOrigin());
		Float destination = calculateMilePoint(od.getDestination());
		if (origin == null || destination == null)
			throw new BadRouteException("No nodes on corridor");
		if (origin > destination) {
			throw new BadRouteException("Origin (" + origin +
				") > destin (" + destination + "), " + od);
		}
		return destination - origin;
	}

	/** Calculate the distance to the nearest node */
	public Distance distanceTo(GeoLoc loc) {
		Float m = calculateMilePoint(loc);
		if (m != null) {
			Float mile = findDownstreamPoint(m);
			if (mile != null)
				return new Distance(mile - m, MILES);
		}
		return null;
	}

	/** Find the nearest node downstream from the given location */
	public R_NodeImpl findDownstreamNode(GeoLoc loc)
		throws BadRouteException
	{
		Float m = calculateMilePoint(loc);
		if (m == null)
			throw new BadRouteException("No nodes on corridor");
		Float mile = findDownstreamPoint(m);
		if (mile != null)
			return n_points.get(mile);
		throw new BadRouteException("No downstream nodes");
	}

	/** Find the nearest milepoint downstream from the given milepoint */
	private Float findDownstreamPoint(float m) {
		for (Float mile: n_points.keySet()) {
			if (mile > m)
				return mile;
		}
		return null;
	}

	/** Get the IDs of all linked CD roads */
	public Iterator<String> getLinkedCDRoads() {
		HashSet<String> cds = new HashSet<String>();
		for (R_NodeImpl r_node: n_points.values()) {
			if (R_NodeHelper.isCD(r_node)) {
				GeoLoc l = r_node.getGeoLoc();
				String c = GeoLocHelper.getLinkedCorridor(l);
				if (c != null)
					cds.add(c);
			}
		}
		return cds.iterator();
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
				if (s.getRollingAverageSpeed() > 0) {
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
