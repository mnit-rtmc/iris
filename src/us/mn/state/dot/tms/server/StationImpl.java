/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2024  Minnesota Department of Transportation
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
import java.util.Map;
import java.util.NavigableMap;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.sonar.server.Server;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.Station;
import us.mn.state.dot.tms.SystemAttrEnum;
import static us.mn.state.dot.tms.server.Constants.MISSING_DATA;
import static us.mn.state.dot.tms.server.XmlWriter.createAttribute;

/**
 * A station is a group of related detectors.
 *
 * @author Douglas Lau
 */
public class StationImpl implements Station, VehicleSampler {

	/** Breakdown speed (should be system attribute?) */
	static private final int VSA_BREAKDOWN_SPEED_MPH = 25;

	/** Bottleneck debug log */
	static private final DebugLog BOTTLENECK_LOG =
		new DebugLog("bottleneck");

	/** Calculate the rolling average of some samples.
	 * @param samples Array of samples to average.
	 * @param n_smp Number of samples to average.
	 * @return Average of samples, or MISSING_DATA. */
	static private float average(float[] samples, int n_smp) {
		float total = 0;
		int count = 0;
		for (int i = 0; i < n_smp; i++) {
			float s = samples[i];
			if (s > 0) {
				total += s;
				count += 1;
			}
		}
		return average(total, count);
	}

	/** Calculate the average from a total and sample count.
	 * @param total Total of all sample data.
	 * @param count Count of samples.
	 * @return Average of samples, or MISSING_DATA. */
	static private float average(float total, int count) {
		return (count > 0)
		      ? total / count
		      : MISSING_DATA;
	}

	/** Check if a detector is a valid station detector */
	static private boolean isValidStation(DetectorImpl det) {
		return det.isSampling()
		    && det.isStationOrCD()
		    && !det.getAbandoned();
	}

	/** Staiton name */
	private final String name;

	/** Get the station name */
	@Override
	public String getName() {
		return name;
	}

	/** Get notes (including hashtags) */
	@Override
	public String getNotes() {
		return null;
	}

	/** Roadway node */
	private final R_NodeImpl r_node;

	/** Get the roadway node */
	@Override
	public R_Node getR_Node() {
		return r_node;
	}

	/** Create a new station */
	public StationImpl(String station_id, R_NodeImpl n) {
		name = station_id;
		r_node = n;
	}

	/** Destroy a station */
	@Override
	public void destroy() {
		// Nothing to do
	}

	/** Get a string representation of the station */
	@Override
	public String toString() {
		return name;
	}

	/** Is this station active? */
	public boolean getActive() {
		for (DetectorImpl det: r_node.getDetectors()) {
			if (!det.getAbandoned())
				return true;
		}
		return false;
	}

	/** Current average station occupancy */
	private float occupancy = MISSING_DATA;

	/** Get a vehicle count */
	@Override
	public int getVehCount(long stamp, int per_ms) {
		int total = 0;
		int n_count = 0;
		for (DetectorImpl det: r_node.getDetectors()) {
			if (isValidStation(det)) {
				int c = det.getVehCount(stamp, per_ms);
				if (c >= 0) {
					total += c;
					n_count++;
				}
			}
		}
		return Math.round(average(total, n_count));
	}

	/** Get the average station flow */
	@Override
	public int getFlow(long stamp, int per_ms) {
		int t_flow = 0;
		int n_flow = 0;
		for (DetectorImpl det: r_node.getDetectors()) {
			if (isValidStation(det)) {
				int f = det.getFlow(stamp, per_ms);
				if (f >= 0) {
					t_flow += f;
					n_flow++;
				}
			}
		}
		return Math.round(average(t_flow, n_flow));
	}

	/** Current average station density */
	private float density = MISSING_DATA;

	/** Get the average station density */
	@Override
	public float getDensity(long stamp, int per_ms) {
		return density;
	}

	/** Current average station density, ignoring auto-fail */
	private float density_ig = MISSING_DATA;

	/** Current average station speed */
	private float speed = MISSING_DATA;

	/** Get the average station speed */
	@Override
	public float getSpeed(long stamp, int per_ms) {
		return speed;
	}

	/** Get the station speed limit */
	public int getSpeedLimit() {
		return r_node.getSpeedLimit();
	}

	/** Rolling station speeds */
	private final SpeedSmoother speeds = new SpeedSmoother();

	/** Rolling station speeds (ignoring auto-fail) */
	private final SpeedSmoother speeds_ig = new SpeedSmoother();

	/** Low station speeds */
	private final SpeedSmoother speeds_low = new SpeedSmoother();

	/** Get smoothed speed using density rank mode */
	public float getSpeedAvg(int limit_adj) {
		return getSpeedAvg(RankMode.DENSITY, limit_adj);
	}

	/** Get smoothed speed using the given rank mode */
	private float getSpeedAvg(RankMode mode, int limit_adj) {
		int limit = getSpeedLimit();
		return speeds.value(mode, limit + limit_adj);
	}

	/** Get smoothed speed using the given rank mode */
	public float getSpeedAvg(RankMode mode) {
		return getSpeedAvg(mode, 0);
	}

	/** Get smoothed speed (ignoring auto-fail) using density rank mode */
	public float getSpeedAvgIg(int limit_adj) {
		int limit = getSpeedLimit();
		return speeds_ig.value(RankMode.DENSITY, limit + limit_adj);
	}

	/** Get smoothed low speed using speed rank mode */
	public float getSpeedLow() {
		int limit = getSpeedLimit();
		return speeds_low.value(RankMode.SPEED, limit);
	}

	/** Calculate the current station data */
	public void calculateData(long stamp, int per_ms) {
		speeds.setDensity(density);
		speeds_ig.setDensity(density_ig);
		float t_occ = 0;
		int n_occ = 0;
		float t_density = 0;
		int n_density = 0;
		float t_density_ig = 0; /* ignore auto-fail */
		int n_density_ig = 0; /* ignore auto-fail */
		float t_speed = 0;
		int n_speed = 0;
		float low = MISSING_DATA;
		float t_speed_ig = 0; /* ignore auto-fail */
		int n_speed_ig = 0; /* ignore auto-fail */
		for (DetectorImpl det: r_node.getDetectors()) {
			if (!isValidStation(det))
				continue;
			float f = det.getOccupancy(stamp, per_ms);
			if (f != MISSING_DATA) {
				t_occ += f;
				n_occ++;
			}
			f = det.getDensity(stamp, per_ms);
			if (f != MISSING_DATA) {
				t_density += f;
				n_density++;
			}
			f = det.getDensity(stamp, per_ms, true);
			if (f != MISSING_DATA) {
				t_density_ig += f;
				n_density_ig++;
			}
			f = det.getSpeed(stamp, per_ms);
			if (f > 0) {
				t_speed += f;
				n_speed++;
				low = (low == MISSING_DATA)
				    ? f
				    : Math.min(f, low);
			}
			f = det.getSpeed(stamp, per_ms, true);
			if (f > 0) {
				t_speed_ig += f;
				n_speed_ig++;
			}
		}
		occupancy = average(t_occ, n_occ);
		density = average(t_density, n_density);
		density_ig = average(t_density_ig, n_density_ig);
		speed = average(t_speed, n_speed);
		speeds.push(speed);
		float speed_ig = average(t_speed_ig, n_speed_ig);
		speeds_ig.push(speed_ig);
		speeds_low.push(low);
	}

	/** Write the current sample as an XML element */
	public void writeSampleXml(Writer w, long stamp, int per_ms)
		throws IOException
	{
		if (!getActive())
			return;
		int f = getFlow(stamp, per_ms);
		int s = Math.round(getSpeed(stamp, per_ms));
		float o = occupancy;
		w.write("\t<sample");
		w.write(createAttribute("sensor", name));
		if (f > MISSING_DATA)
			w.write(createAttribute("flow", f));
		if (s > 0)
			w.write(createAttribute("speed", s));
		if (o >= 0) {
			w.write(createAttribute("occ",
				BaseObjectImpl.formatFloat(o, 2)));
		}
		w.write("/>\n");
	}

	/** Write the current sample as a JSON object */
	public boolean writeSampleJson(long stamp, int per_ms, Writer writer,
		boolean first) throws IOException
	{
		int f = getFlow(stamp, per_ms);
		int s = Math.round(getSpeed(stamp, per_ms));
		if (f > MISSING_DATA || s > 0) {
			if (!first)
				writer.write(',');
			writeSampleJson(f, s, writer);
			return true;
		} else
			return false;
	}

	/** Write the current sample as a JSON object */
	private void writeSampleJson(int f, int s, Writer writer)
		throws IOException
	{
		writer.write("\n\"");
		writer.write(name);
		writer.write("\":[");
		writer.write((f > MISSING_DATA) ? String.valueOf(f) : "null");
		writer.write(',');
		writer.write((s > 0) ? String.valueOf(s) : "null");
		writer.write(']');
	}

	/** Acceleration from previous station */
	private Float acceleration = null;

	/** Count of iterations when station was a bottleneck candidate */
	private int n_candidate = 0;

	/** Bottleneck exists flag */
	private boolean bottleneck = false;

	/** Bottleneck flag from previous time step.  This is needed when
	 * checking if a bottleneck should be adjusted downstream. */
	private boolean p_bottle = false;

	/** Set the bottleneck flag */
	private void setBottleneck(boolean b) {
		p_bottle = bottleneck;
		bottleneck = b;
	}

	/** Calculate whether the station is a bottleneck.
	 * @param m Mile point of this station.
	 * @param upstream Mapping of mile points to upstream stations. */
	public void calculateBottleneck(float m,
		NavigableMap<Float, StationImpl> upstream)
	{
		Float mp = upstream.lowerKey(m);
		while (mp != null && isTooClose(m - mp))
			mp = upstream.lowerKey(mp);
		if (mp != null) {
			StationImpl sp = upstream.get(mp);
			float d = m - mp;
			acceleration = calculateAcceleration(sp, d);
			checkCandidate();
			if (isAboveBottleneckSpeed())
				setBottleneck(false);
			else if (isBeforeStartCount()) {
				setBottleneck(false);
				adjustDownstream(upstream);
			} else {
				setBottleneck(true);
				adjustUpstream(upstream);
			}
		} else
			clearBottleneck();
	}

	/** Test if upstream station is too close for bottleneck calculation */
	private boolean isTooClose(float d) {
		return d < SystemAttrEnum.VSA_MIN_STATION_MILES.getFloat();
	}

	/** Calculate the acceleration from previous station.
	 * @param sp Previous station.
	 * @param d Distance to previous station (miles).
	 * @return acceleration in mphph */
	private Float calculateAcceleration(StationImpl sp, float d) {
		float u = getSpeedAvg(10);
		float up = sp.getSpeedAvg(10);
		return calculateAcceleration(u, up, d);
	}

	/** Calculate the acceleration between two stations.
	 * @param u Downstream speed (mph).
	 * @param up Upstream speed (mph).
	 * @param d Distance between stations (miles).
	 * @return acceleration in mphph */
	private Float calculateAcceleration(float u, float up, float d) {
		assert d > 0;
		return (u > 0 && up > 0)
		      ? (u * u - up * up) / (2 * d)
		      : null;
	}

	/** Check if station is a bottleneck candidate */
	private void checkCandidate() {
		if (isBelowBreakdownSpeed()) {
			n_candidate++;
			bumpCandidateCount();
		} else if (isBottleneckCandidate())
			n_candidate++;
		else
			n_candidate = 0;
	}

	/** Test if station speed is below the breakdown speed */
	private boolean isBelowBreakdownSpeed() {
		float s = getSpeedAvg(10);
		return s > 0 && s < VSA_BREAKDOWN_SPEED_MPH;
	}

	/** Bump the candidate count up to the start count */
	private void bumpCandidateCount() {
		if (isBeforeStartCount())
		   n_candidate = SystemAttrEnum.VSA_START_INTERVALS.getInt();
	}

	/** Check if station is a bottleneck candidate */
	private boolean isBottleneckCandidate() {
		return isBelowBottleneckSpeed() &&
		       isAccelerationBelowThreshold();
	}

	/** Test if station speed is below the bottleneck id speed */
	private boolean isBelowBottleneckSpeed() {
		float s = getSpeedAvg(10);
		return s > 0 &&
		       s < SystemAttrEnum.VSA_BOTTLENECK_ID_MPH.getInt();
	}

	/** Test if acceleration is below bottleneck threshold */
	private boolean isAccelerationBelowThreshold() {
		Float a = acceleration;
		return a != null && a < getThreshold();
	}

	/** Get the current deceleration threshold */
	private int getThreshold() {
		return isBeforeStartCount()
		      ? getStartThreshold()
		      : getStopThreshold();
	}

	/** Get the starting deceleration threshold */
	private int getStartThreshold() {
		return SystemAttrEnum.VSA_START_THRESHOLD.getInt();
	}

	/** Get the stopping deceleration threshold */
	private int getStopThreshold() {
		return SystemAttrEnum.VSA_STOP_THRESHOLD.getInt();
	}

	/** Test if the number of intervals is lower than start count */
	private boolean isBeforeStartCount() {
		   return n_candidate < SystemAttrEnum.VSA_START_INTERVALS.getInt();
	}

	/** Test if station speed is above the bottleneck id speed */
	private boolean isAboveBottleneckSpeed() {
		return getSpeedAvg(10) >
			SystemAttrEnum.VSA_BOTTLENECK_ID_MPH.getInt();
	}

	/** Adjust a bottleneck downstream if necessary.  Check the immediately
	 * upstream station for bottleneck.
	 * @param upstream Map of upstream stations. */
	private void adjustDownstream(NavigableMap<Float, StationImpl> upstream)
	{
		Map.Entry<Float, StationImpl> entry = upstream.lastEntry();
		if (entry != null)
			adjustDownstream(entry.getValue());
	}

	/** Adjust the bottleneck downstream if necessary.
	 * @param sp Immediately upstream station */
	private void adjustDownstream(StationImpl sp) {
		Float ap = sp.acceleration;
		Float a = acceleration;
		if (a != null && ap != null && a < ap && sp.p_bottle)
			sp.moveBottleneck(this);
	}

	/** Adjust the bottleneck upstream if necessary.  Check if bottleneck
	 * should be moved upstream from current station.
	 * @param upstream Map of upstream stations. */
	private void adjustUpstream(NavigableMap<Float, StationImpl> upstream) {
		StationImpl s = this;
		Map.Entry<Float, StationImpl> entry = upstream.lastEntry();
		while (entry != null) {
			StationImpl sp = entry.getValue();
			Float ap = sp.acceleration;
			Float a = s.acceleration;
			if (a == null || ap == null || a <= ap)
				break;
			s.moveBottleneck(sp);
			s = sp;
			entry = upstream.lowerEntry(entry.getKey());
		}
	}

	/** Clear the station as a bottleneck */
	public void clearBottleneck() {
		n_candidate = 0;
		setBottleneck(false);
		acceleration = null;
	}

	/** Move bottleneck to an adjacent station.
	 * @param s Station to move bottleneck to. */
	private void moveBottleneck(StationImpl s) {
		// Don't use setBottleneck; p_bottle should not be updated
		s.bottleneck = true;
		s.n_candidate = Math.max(s.n_candidate, n_candidate);
		bottleneck = false;
		n_candidate = 0;
	}

	/** Debug the bottleneck calculation */
	public void debug() {
		if (BOTTLENECK_LOG.isOpen()) {
			BOTTLENECK_LOG.log(name +
				", spd: " + getSpeedAvg(10) +
				", acc: " + acceleration +
				", n_can: " + n_candidate +
				", bneck: " + bottleneck);
		}
	}

	/** Check if the station is a bottleneck for the given distance */
	public boolean isBottleneckFor(float d) {
		return bottleneck && isBottleneckInRange(d);
	}

	/** Check if the (bottleneck) station is in range */
	private boolean isBottleneckInRange(float d) {
		return (d > 0)
		      ? d < getUpstreamDistance()
		      : -d < getDownstreamDistance();
	}

	/** Get the upstream bottleneck distance */
	private float getUpstreamDistance() {
		float lim = getSpeedLimit();
		float sp = getSpeedAvg(10);
		if (sp > 0 && sp < lim) {
			int acc = -getControlThreshold();
			return (lim * lim - sp * sp) / (2 * acc);
		} else
			return 0;
	}

	/** Get the control deceleration threshold */
	private int getControlThreshold() {
		return SystemAttrEnum.VSA_CONTROL_THRESHOLD.getInt();
	}

	/** Get the downstream bottleneck distance */
	private float getDownstreamDistance() {
		return SystemAttrEnum.VSA_DOWNSTREAM_MILES.getFloat();
	}

	/** Notify SONAR clients of an object removed */
	public void notifyRemove() {
		Server s = MainServer.server;
		if (s != null)
			s.removeObject(this);
	}

	/** Lookup the samplers */
	public SamplerSet getSamplerSet() {
		return r_node.getSamplerSet();
	}
}
