/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2013  Minnesota Department of Transportation
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
import java.util.Map;
import java.util.Set;
import us.mn.state.dot.tms.Detector;
import us.mn.state.dot.tms.DetectorHelper;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.Station;
import us.mn.state.dot.tms.StationHelper;
import us.mn.state.dot.tms.client.proxy.MapGeoLoc;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A segment is the shape of a roadway segment on a map.
 *
 * @author Douglas Lau
 */
public class Segment {

	/** R_Node model */
	private final R_NodeModel model;

	/** Get the r_node model */
	public R_NodeModel getModel() {
		return model;
	}

	/** Upstream mainline node */
	private final R_Node upstream;

	/** Shift from upstream node to end of segment */
	private final int shift;

	/** Get the segment label */
	public String getLabel(Integer lane) {
		return labels.get(lane);
	}

	/** Location at upstream end of segment */
	public final MapGeoLoc loc_up;

	/** Location at downstream end of segment */
	public final MapGeoLoc loc_dn;

	/** Sample data set */
	private final SampleDataSet samples;

	/** Flag to indicate too distant from upstream node */
	private final boolean too_distant;

	/** Mapping of lane numbers to labels */
	private final HashMap<Integer, String> labels =
		new HashMap<Integer, String>();

	/** Mapping of sensor ID to lane number */
	private final HashMap<String, Integer> lane_sensors =
		new HashMap<String, Integer>();

	/** Create a new segment */
	public Segment(R_NodeModel m, R_Node u, MapGeoLoc lu, MapGeoLoc ld,
		SampleDataSet sds, boolean td)
	{
		assert m != null;
		assert u != null;
		assert lu != null;
		assert ld != null;
		model = m;
		upstream = u;
		loc_up = lu;
		loc_dn = ld;
		samples = sds;
		too_distant = td;
		shift = model.getShift(upstream);
		if(!too_distant)
			labels.put(null, getStationLabel());
	}

	/** Get label for a station segment */
	private String getStationLabel() {
		StringBuilder sb = new StringBuilder();
		sb.append(I18N.get("detector.station"));
		sb.append(" ");
		String sid = upstream.getStationID();
		if(sid != null && sid.length() > 0) {
			sb.append(sid);
			sb.append(": ");
			Station sta = StationHelper.lookup(sid);
			if(sta != null)
				sb.append(StationHelper.getLabel(sta));
		}
		return sb.toString().trim();
	}

	/** Add detection to the segment */
	public void addDetection(Set<Detector> dets) {
		for(Detector d: dets) {
			String sid = d.getName();
			int ln = d.getLaneNumber();
			lane_sensors.put(sid, ln);
			addDetectorLabel(d);
		}
	}

	/** Add a detector label */
	private void addDetectorLabel(Detector det) {
		int ln = det.getLaneNumber();
		StringBuilder sb = new StringBuilder();
		String lbl = labels.get(ln);
		if(lbl != null) {
			sb.append(lbl);
			sb.append('\n');
		}
		sb.append(getDetectorLabel(det));
		labels.put(ln, sb.toString());
	}

	/** Get label for a detector segment */
	private String getDetectorLabel(Detector det) {
		StringBuilder sb = new StringBuilder();
		sb.append('D');
		sb.append(det.getName());
		sb.append(' ');
		sb.append(DetectorHelper.getLabel(det));
		return sb.toString().trim();
	}

	/** Get the flow for the given lane */
	public Integer getFlow(Integer lane) {
		int total = 0;
		int count = 0;
		for(Map.Entry<String, Integer> ent: lane_sensors.entrySet()) {
			if(lane == null || lane == ent.getValue()) {
				String sid = ent.getKey();
				SensorSample s = samples.getSample(sid);
				if(s != null) {
					Integer f = s.getFlow();
					if(f != null) {
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
		for(Map.Entry<String, Integer> ent: lane_sensors.entrySet()) {
			if(lane == null || lane == ent.getValue()) {
				String sid = ent.getKey();
				SensorSample s = samples.getSample(sid);
				if(s != null) {
					Integer spd = s.getSpeed();
					if(spd != null) {
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
		for(Map.Entry<String, Integer> ent: lane_sensors.entrySet()) {
			if(lane == null || lane == ent.getValue()) {
				String sid = ent.getKey();
				SensorSample s = samples.getSample(sid);
				if(s != null) {
					Integer d = s.getDensity();
					if(d != null) {
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

	/** Get the minimum left shift */
	public int getLeftMin() {
		return Math.min(model.getUpstreamLane(true),
				model.getDownstreamLane(true));
	}

	/** Get the maximum right shift */
	public int getRightMax() {
		return Math.max(model.getUpstreamLane(false),
				model.getDownstreamLane(false));
	}

	/** Get the lane for the given shift.
	 * @param sh Absolute shift.
	 * @return Lane number (1 for right lane) */
	public int getLane(int sh) {
		return model.getDownstreamLane(false) - sh - shift;
	}
}
