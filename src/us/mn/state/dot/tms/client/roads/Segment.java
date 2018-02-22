/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2018  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.R_NodeHelper;
import us.mn.state.dot.tms.Station;
import us.mn.state.dot.tms.StationHelper;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.client.proxy.MapGeoLoc;
import us.mn.state.dot.tms.geo.SphericalMercatorPosition;
import us.mn.state.dot.tms.units.Distance;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A segment is the shape of a roadway segment on a map.
 *
 * @author Douglas Lau
 */
public class Segment {

	/** Check if two locations are within segment distance */
	static private boolean isWithinSegmentDist(MapGeoLoc g0, MapGeoLoc g1) {
		GeoLoc l0 = (g0 != null) ? g0.getGeoLoc() : null;
		GeoLoc l1 = (g1 != null) ? g1.getGeoLoc() : null;
		Distance d = GeoLocHelper.distanceTo(l0, l1);
		return d != null
		    && d.m() <= SystemAttrEnum.MAP_SEGMENT_MAX_METERS.getInt();
	}

	/** Get position of a MapGeoLoc (may be null) */
	static private SphericalMercatorPosition getPosition(MapGeoLoc loc) {
		return (loc != null)
		     ? GeoLocHelper.getPosition(loc.getGeoLoc())
		     : null;
	}

	/** Get tangent of a MapGeoLoc (may be null) */
	static private double getTangent(MapGeoLoc loc) {
		return (loc != null)
		     ? loc.getTangent()
		     : MapGeoLoc.northTangent();
	}

	/** R_Node model */
	private final R_NodeModel model;

	/** Get the r_node model */
	public R_NodeModel getModel() {
		return model;
	}

	/** Shift from station node to downstream end */
	private final int shift;

	/** Flag indicating whether the segment contains parking detection */
	public final boolean parking;

	/** Flag indicating whether the segment is good */
	private final boolean good;

	/** Get flag indicating whether the segment is good */
	public boolean isGood() {
		return good;
	}

	/** Get the segment label */
	public String getLabel(Integer lane) {
		return labels.get(lane);
	}

	/** Position at upstream end */
	public final SphericalMercatorPosition pos_a;

	/** Position at downstream end */
	public final SphericalMercatorPosition pos_b;

	/** Tangent at upstream end */
	public final double tangent_a;

	/** Tangent at downstream end */
	public final double tangent_b;

	/** Sample data set */
	private final SampleDataSet samples;

	/** Mapping of lane numbers to labels */
	private final HashMap<Integer, String> labels =
		new HashMap<Integer, String>();

	/** Mapping of sensor ID to lane number */
	private final HashMap<String, Integer> lane_sensors =
		new HashMap<String, Integer>();

	/** Create a new segment.
	 * @param m Upstream node model.
	 * @param al Location of node at upstream end of segment.
	 * @param b Node at downstream end of segment.
	 * @param bl Location of node at downstream end of segment.
	 * @param s Station node containing detectors for segment.
	 * @param sl Location of station node.
	 * @param sds Sample data set.
	 * @param dhash Detector hash. */
	public Segment(R_NodeModel m, MapGeoLoc al, R_Node b, MapGeoLoc bl,
		R_Node s, MapGeoLoc sl, SampleDataSet sds, DetectorHash dhash)
	{
		model = new R_NodeModel(b, m);
		pos_a = getPosition(al);
		pos_b = getPosition(bl);
		tangent_a = getTangent(al);
		tangent_b = getTangent(bl);
		samples = sds;
		shift = model.getShift(s);
		parking = R_NodeHelper.isParking(b);
		good = (s != null) && R_NodeHelper.isJoined(b) &&
			isWithinSegmentDist(al, bl);
		if (isWithinSegmentDist(sl, bl)) {
			labels.put(null, getStationLabel(s));
			addDetection(dhash.getDetectors(s));
		}
	}

	/** Get label for a station segment */
	private String getStationLabel(R_Node s) {
		StringBuilder sb = new StringBuilder();
		sb.append(I18N.get("detector.station"));
		sb.append(" ");
		if (s != null) {
			String sid = s.getStationID();
			if (sid != null && sid.length() > 0) {
				sb.append(sid);
				sb.append(": ");
				Station sta = StationHelper.lookup(sid);
				if (sta != null)
					sb.append(StationHelper.getLabel(sta));
			}
		}
		return sb.toString().trim();
	}

	/** Add detection to the segment */
	private void addDetection(Set<Detector> dets) {
		for (Detector d: dets) {
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
		if (lbl != null) {
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
		for (Map.Entry<String, Integer> ent: lane_sensors.entrySet()) {
			if (lane == null || lane == ent.getValue()) {
				String sid = ent.getKey();
				SensorSample s = samples.getSample(sid);
				if (s != null) {
					Integer f = s.getFlow();
					if (f != null) {
						total += f;
						count++;
					}
				}
			}
		}
		if (count > 0)
			return total / count;
		else
			return null;
	}

	/** Get the speed for the given lane */
	public Integer getSpeed(Integer lane) {
		int total = 0;
		int count = 0;
		for (Map.Entry<String, Integer> ent: lane_sensors.entrySet()) {
			if (lane == null || lane == ent.getValue()) {
				String sid = ent.getKey();
				SensorSample s = samples.getSample(sid);
				if (s != null) {
					Integer spd = s.getSpeed();
					if (spd != null) {
						total += spd;
						count++;
					}
				}
			}
		}
		if (count > 0)
			return total / count;
		else
			return null;
	}

	/** Get the density for the given lane */
	public Integer getDensity(Integer lane) {
		int total = 0;
		int count = 0;
		for (Map.Entry<String, Integer> ent: lane_sensors.entrySet()) {
			if (lane == null || lane == ent.getValue()) {
				String sid = ent.getKey();
				SensorSample s = samples.getSample(sid);
				if (s != null) {
					Integer d = s.getDensity();
					if (d != null) {
						total += d;
						count++;
					}
				}
			}
		}
		if (count > 0)
			return total / count;
		else
			return null;
	}

	/** Get the occupancy for the given lane */
	public Float getOcc(Integer lane) {
		float total = 0;
		int count = 0;
		for (Map.Entry<String, Integer> ent: lane_sensors.entrySet()) {
			if (lane == null || lane == ent.getValue()) {
				String sid = ent.getKey();
				SensorSample s = samples.getSample(sid);
				if (s != null) {
					Float o = s.getOcc();
					if (o != null) {
						total += o;
						count++;
					}
				}
			}
		}
		return (count > 0) ? total / count : null;
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
