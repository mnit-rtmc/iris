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

	/** R_Node model */
	protected final R_NodeModel model;

	/** Get the r_node model */
	public R_NodeModel getModel() {
		return model;
	}

	/** Upstream mainline node */
	protected final R_Node upstream;

	/** Shift from upstream node to end of segment */
	protected final int shift;

	/** Get the station ID */
	public String getStationID() {
		return upstream.getStationID();
	}

	/** Location at upstream end of segment */
	public final MapGeoLoc loc_up;

	/** Location at downstream end of segment */
	public final MapGeoLoc loc_dn;

	/** Mapping of sensor ID to lane number */
	protected final HashMap<String, Integer> lane_sensors =
		new HashMap<String, Integer>();

	/** Mapping of sensor ID to sample data */
	protected final HashMap<String, SensorSample> samples =
		new HashMap<String, SensorSample>();

	/** Mapping of sensor ID to sample data for next interval */
	protected final HashMap<String, SensorSample> next_samples =
		new HashMap<String, SensorSample>();

	/** Create a new segment */
	public Segment(R_NodeModel m, R_Node u, MapGeoLoc lu, MapGeoLoc ld) {
		model = m;
		upstream = u;
		loc_up = lu;
		loc_dn = ld;
		shift = model.getShift(upstream);
	}

	/** Add detection to the segment */
	public void addDetection() {
		DetectorHelper.find(new Checker<Detector>() {
			public boolean check(Detector d) {
				if(d.getR_Node() == upstream) {
					String id = "D" + d.getName();
					int ln = d.getLaneNumber();
					lane_sensors.put(id, ln);
				}
				return false;
			}
		});
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

	/** Get the left line for the segment */
	public int getLeftLine() {
		return Math.min(model.getUpstreamLane(true),
				model.getDownstreamLane(true));
	}

	/** Get the right side line for the segment */
	public int getRightLine() {
		return Math.max(model.getUpstreamLane(false),
				model.getDownstreamLane(false));
	}

	/** Get the lane shift */
	public int getLaneShift() {
		return model.getDownstreamLane(false) - shift;
	}
}
