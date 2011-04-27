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
import java.util.Map;
import java.util.TreeMap;
import us.mn.state.dot.tms.CorridorBase;
import us.mn.state.dot.tms.Direction;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.R_NodeHelper;
import us.mn.state.dot.tms.SystemAttrEnum;

/**
 * A corridor is a collection of all R_Node objects for one roadway corridor.
 *
 * @author Douglas Lau
 */
public class Corridor extends CorridorBase {

	/** VSA debug log */
	static protected final IDebugLog VSA_LOG = new IDebugLog("vsa");

	/** Round up to the nearest 5 mph */
	static protected int round5Mph(float mph) {
		return Math.round(mph / 5) * 5;
	}

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

	/** Interface to find a station on the corridor */
	static public interface StationFinder {
		public boolean check(Float m, StationImpl s);
	}

	/** Find a station using a station finder callback interface */
	public StationImpl findStation(StationFinder finder) {
		for(Float m: n_points.keySet()) {
			assert m != null;
			R_NodeImpl n = (R_NodeImpl)n_points.get(m);
			if(R_NodeHelper.isStation(n)) {
				StationImpl s = n.getStation();
				if(s != null && finder.check(m, s))
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
			public boolean check(Float m, StationImpl s) {
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

	/** Find a node using a node finder callback (reverse order) */
	public R_NodeImpl findNodeReverse(NodeFinder finder) {
		for(R_Node n: n_points.descendingMap().values()) {
			R_NodeImpl r_node = (R_NodeImpl)n;
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
	public void printXml(PrintWriter out,
		Map<String, RampMeterImpl> m_nodes)
	{
		out.println("<corridor route='" + roadway + "' dir='" +
			Direction.fromOrdinal(road_dir).abbrev + "'>");
		for(R_Node n: r_nodes) {
			R_NodeImpl r_node = (R_NodeImpl)n;
			r_node.printXml(out);
			String key = r_node.getName();
			if(m_nodes.containsKey(key)) {
				RampMeterImpl meter = m_nodes.get(key);
				meter.printXml(out);
			}
		}
		out.println("</corridor>");
	}

	/** Find the current bottlenecks on the corridor */
	public void findBottlenecks() {
		final TreeMap<Float, StationImpl> upstream =
			new TreeMap<Float, StationImpl>();
		findStation(new StationFinder() {
			public boolean check(Float m, StationImpl s) {
				if(s.getRollingAverageSpeed() > 0) {
					s.calculateBottleneck(m, upstream);
					upstream.put(m, s);
				} else
					s.clearBottleneck();
				s.debug();
				return false;
			}
		});
	}

	/** Calculate the speed advisory */
	public Integer calculateSpeedAdvisory(GeoLoc loc) {
		Float m = calculateMilePoint(loc);
		if(VSA_LOG.isOpen())
			VSA_LOG.log(loc.getName() + ", mp: " + m);
		if(m != null)
			return calculateSpeedAdvisory(m);
		else
			return null;
	}

	/** Calculate the speed advisory */
	protected Integer calculateSpeedAdvisory(float m) {
		BottleneckFinder bf = new BottleneckFinder(m);
		findStation(bf);
		bf.debug();
		if(bf.foundBottleneck()) {
			Integer lim = bf.getSpeedLimit();
			if(lim != null) {
				Float a = bf.calculateSpeedAdvisory();
				if(a != null) {
					a = Math.max(a, getMinDisplay());
					int sa = round5Mph(a);
					if(sa < lim)
						return sa;
					else
						return null;
				}
			}
		}
		return null;
	}

	/** Get the minimum speed to display for advisory */
	protected int getMinDisplay() {
		return SystemAttrEnum.VSA_MIN_DISPLAY_MPH.getInt();
	}

	/** Class to find a bottleneck near a point */
	static protected class BottleneckFinder implements StationFinder {
		protected final float ma;	// mile point
		protected StationImpl su;	// upstream station
		protected Float mu;		// upstream mile pt
		protected StationImpl sd;	// downstream station
		protected Float md;		// downstream mile pt
		protected StationImpl sb;	// bottleneck station
		protected Float mb;		// bottleneck mile pt
		protected BottleneckFinder(float m) {
			ma = m;
		}
		public boolean check(Float m, StationImpl s) {
			if(m < ma) {
				su = s;
				mu = m;
			} else if(md == null || md > m) {
				sd = s;
				md = m;
			}
			if((mb == null || mb > m) && s.isBottleneckFor(m - ma)){
				sb = s;
				mb = m;
			}
			return false;
		}
		protected boolean foundBottleneck() {
			return sb != null;
		}
		protected Float getSpeed() {
			if(su != null && sd != null) {
				float u0 = su.getRollingAverageSpeed();
				float u1 = sd.getRollingAverageSpeed();
				if(u0 > 0 && u1 > 0)
					return Math.min(u0, u1);
				if(u0 > 0)
					return u0;
				if(u1 > 0)
					return u1;
			}
			return null;
		}
		protected Integer getSpeedLimit() {
			if(su != null && sd != null) {
				return Math.min(su.getSpeedLimit(),
					sd.getSpeedLimit());
			} else if(su != null)
				return su.getSpeedLimit();
			else if(sd != null)
				return sd.getSpeedLimit();
			else
				return null;
		}
		protected Float calculateSpeedAdvisory() {
			if(sb != null && mb != null)
				return sb.calculateSpeedAdvisory(mb - ma);
			else
				return null;
		}
		protected void debug() {
			if(VSA_LOG.isOpen()) {
				Float a = calculateSpeedAdvisory();
				VSA_LOG.log("adv: " + a +
				            ", upstream: " + su +
				            ", downstream: " + sd +
				            ", bottleneck: " + sb +
				            ", speed: " + getSpeed() +
				            ", limit: " + getSpeedLimit());
			}
		}
	}
}
