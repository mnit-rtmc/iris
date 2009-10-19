/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.R_NodeHelper;
import us.mn.state.dot.tms.R_NodeTransition;
import us.mn.state.dot.tms.R_NodeType;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.client.proxy.MapGeoLoc;

/**
 * A segment is the shape of a roadway segment on a map.
 *
 * @author Douglas Lau
 */
public class Segment {

	/** R_Node for segment */
	protected final R_Node r_node;

	/** Get the segment r_node */
	public R_Node getR_Node() {
		return r_node;
	}

	/** Get the station ID */
	public String getStationID() {
		if(r_node != null)
			return r_node.getStationID();
		else
			return "";
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

	/** Get the count of lanes */
	public int getLaneCount() {
		if(r_node != null)
			return r_node.getLanes();
		else
			return lane_sensors.size();
	}

	/** Create a new segment */
	public Segment() {
		this(null);
	}

	/** Create a new segment */
	public Segment(R_Node n) {
		r_node = n;
		if(r_node != null) {
			DetectorHelper.find(new Checker<Detector>() {
				public boolean check(Detector d) {
					if(d.getR_Node() == r_node) {
						String id = "D" + d.getName();
						int n = d.getLaneNumber();
						lane_sensors.put(id, n);
					}
					return false;
				}
			});
		}
	}

	/** Add a point to the segment */
	public void addNode(MapGeoLoc loc) {
		if(loc != null)
			locs.add(loc);
	}

	/** Check if a node is at a segment break */
	public boolean isBreak(R_Node n) {
		return isStationBreak(n) || isLaneChange(n) || !isJoined(n);
	}

	/** Check if a node is at a station break */
	public boolean isStationBreak(R_Node n) {
		return n.getNodeType() == R_NodeType.STATION.ordinal() &&
		       R_NodeHelper.hasDetection(n);
	}

	/** Check if a node has a change in lane count */
	protected boolean isLaneChange(R_Node n) {
		return n.getNodeType() == R_NodeType.STATION.ordinal() &&
		       n.getLanes() != getLaneCount();
	}

	/** Check if a node should be joined with a segment */
	public boolean isJoined(R_Node n) {
		return n.getTransition() != R_NodeTransition.COMMON.ordinal() ||
		       n.getNodeType() != R_NodeType.ENTRANCE.ordinal();
	}

	/** Check if a location is beyond distance threshold */
	public boolean isTooDistant(MapGeoLoc loc) {
		if(r_node == null)
			return false;
		else {
			if(loc == null)
				return true;
			GeoLoc g0 = r_node.getGeoLoc();
			GeoLoc g1 = loc.getGeoLoc();
			return GeoLocHelper.metersTo(g0, g1) >
			       SystemAttrEnum.MAP_SEGMENT_MAX_METERS.getInt();
		}
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
