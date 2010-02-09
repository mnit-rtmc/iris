/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2010  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.roads;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.tdxml.SensorSample;
import us.mn.state.dot.tms.Detector;
import us.mn.state.dot.tms.DetectorHelper;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.R_NodeHelper;
import us.mn.state.dot.tms.client.proxy.MapGeoLoc;

/**
 * A segment is the shape of a roadway segment on a map.
 *
 * @author Douglas Lau
 */
public class Segment {

	/** Upstream mainline node */
	protected final R_Node upstream;

	/** Upstream node */
	protected final R_Node node_a;

	/** Downstream node */
	protected final R_Node node_b;

	/** Get the station ID */
	public String getStationID() {
		return upstream.getStationID();
	}

	/** List of map geo locations */
	protected final List<MapGeoLoc> locs = new LinkedList<MapGeoLoc>();

	/** Get the list of map geo locations */
	public List<MapGeoLoc> getLocations() {
		return locs;
	}

	/** Mapping of sensor ID to lane number */
	protected final HashMap<String, Integer> lane_sensors =
		new HashMap<String, Integer>();

	/** Mapping of sensor ID to sample data */
	protected final HashMap<String, SensorSample> samples =
		new HashMap<String, SensorSample>();

	/** Mapping of sensor ID to sample data for next interval */
	protected final HashMap<String, SensorSample> next_samples =
		new HashMap<String, SensorSample>();

	/** Get the count of lanes through the segment */
	public int getLaneCount() {
		return getRightShift() - getLeftShift();
	}

	/** Get the left side shift */
	protected int getLeftShift() {
		return Math.max(getLeftShift(node_a), getLeftShift(node_b));
	}

	/** Get the left shift for the specified node */
	protected int getLeftShift(R_Node n) {
		if(n.getAttachSide())
			return n.getShift();
		else if(R_NodeHelper.isStation(n))
			return n.getShift() - n.getLanes();
		else {
			return upstream.getAttachSide() ? upstream.getShift() :
			       upstream.getShift() - upstream.getLanes();
		}
	}

	/** Get the right side shift */
	protected int getRightShift() {
		return Math.min(getRightShift(node_a), getRightShift(node_b));
	}

	/** Get the right shift for the specified node */
	protected int getRightShift(R_Node n) {
		if(!n.getAttachSide())
			return n.getShift();
		else if(R_NodeHelper.isStation(n))
			return n.getShift() + n.getLanes();
		else {
			return upstream.getAttachSide() ? upstream.getShift() +
			       upstream.getLanes() : upstream.getShift();
		}
	}

	/** Create a new segment */
	public Segment(R_Node u, R_Node a, R_Node b) {
		upstream = u;
		node_a = a;
		node_b = b;
		final int shift = getRightShift() - getRightShift(upstream);
		DetectorHelper.find(new Checker<Detector>() {
			public boolean check(Detector d) {
				if(d.getR_Node() == upstream) {
					String id = "D" + d.getName();
					int n = d.getLaneNumber() + shift;
					lane_sensors.put(id, n);
				}
				return false;
			}
		});
	}

	/** Add a point to the segment */
	public void addNode(MapGeoLoc loc) {
		if(loc != null)
			locs.add(loc);
	}

	/** Update one sample */
	public void updateSample(SensorSample s) {
		if(lane_sensors.containsKey(s.id))
			next_samples.put(s.id, s);
	}

	/** Swap the samples */
	public void swapSamples() {
		synchronized(samples) {
			samples.clear();
			samples.putAll(next_samples);
		}
		next_samples.clear();
	}

	/** Get the flow for the given lane */
	public Integer getFlow(Integer lane) {
		int total = 0;
		int count = 0;
		synchronized(samples) {
			for(String sid: samples.keySet()) {
				SensorSample s = samples.get(sid);
				if(s != null) {
					Integer f = s.getFlow();
					if(f != null && (lane == null ||
					   lane == lane_sensors.get(sid)))
					{
						total += f;
						count++;
					}
				}
			}
		}
		if(count > 0)
			return total / count;
		else
			return null;
	}

	/** Get the speed for the given lane */
	public Integer getSpeed(Integer lane) {
		int total = 0;
		int count = 0;
		synchronized(samples) {
			for(String sid: samples.keySet()) {
				SensorSample s = samples.get(sid);
				if(s != null) {
					Integer spd = s.getSpeed();
					if(spd != null && (lane == null ||
					   lane == lane_sensors.get(sid)))
					{
						total += spd;
						count++;
					}
				}
			}
		}
		if(count > 0)
			return total / count;
		else
			return null;
	}

	/** Get the density for the given lane */
	public Integer getDensity(Integer lane) {
		int total = 0;
		int count = 0;
		synchronized(samples) {
			for(String sid: samples.keySet()) {
				SensorSample s = samples.get(sid);
				if(s != null) {
					Integer d = s.getDensity();
					if(d != null && (lane == null ||
					   lane == lane_sensors.get(sid)))
					{
						total += d;
						count++;
					}
				}
			}
		}
		if(count > 0)
			return total / count;
		else
			return null;
	}
}
