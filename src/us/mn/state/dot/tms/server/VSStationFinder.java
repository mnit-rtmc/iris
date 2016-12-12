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

import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.tms.SystemAttrEnum;

/**
 * Variable Speed Advisory Start Station (VSS) Finder.
 *
 * @author Douglas Lau
 */
public class VSStationFinder implements Corridor.StationFinder {

	/** Get the control deceleration threshold */
	static private int getControlThreshold() {
		return SystemAttrEnum.VSA_CONTROL_THRESHOLD.getInt();
	}

	/** Mile point to search for VSS */
	protected final float ma;

	/** Upstream station */
	protected StationImpl su;

	/** Upstream mile point */
	protected Float mu;

	/** Downstream station */
	protected StationImpl sd;

	/** Downstream mile point */
	protected Float md;

	/** Found VSS */
	protected StationImpl vss;

	/** Mile point at found VSS */
	protected Float vss_mp;

	/** Create a new VSS finder */
	public VSStationFinder(float m) {
		ma = m;
	}

	/** Check if a station should be a VSS.  From StationFinder.check.
	 * @param m Milepoint of station.
	 * @param s Station to check.
	 * @return true to stop checking (never). */
	@Override
	public boolean check(float m, StationImpl s) {
		if (m < ma) {
			su = s;
			mu = m;
		} else if(md == null || md > m) {
			sd = s;
			md = m;
		}
		if((vss_mp == null || vss_mp > m) && s.isBottleneckFor(m - ma)){
			vss = s;
			vss_mp = m;
		}
		return false;
	}

	/** Check if a valid VSS was found */
	public boolean foundVSS() {
		return vss != null;
	}

	/** Get the speed limit */
	public Integer getSpeedLimit() {
		if(su != null && sd != null)
			return Math.min(su.getSpeedLimit(), sd.getSpeedLimit());
		else if(su != null)
			return su.getSpeedLimit();
		else if(sd != null)
			return sd.getSpeedLimit();
		else
			return null;
	}

	/** Calculate the advisory speed */
	public Float calculateSpeedAdvisory() {
		if(vss != null && vss_mp != null) {
			float spd = vss.getRollingAverageSpeed();
			if(spd > 0)
				return calculateSpeedAdvisory(spd, vss_mp - ma);
		}
		return null;
	}

	/** Calculate a speed advisory.
	 * @param spd Average speed at VSS.
	 * @param d Distance upstream of station.
	 * @return Speed advisory. */
	private float calculateSpeedAdvisory(float spd, float d) {
		if(d > 0) {
			int acc = -getControlThreshold();
			double s2 = spd * spd + 2.0 * acc * d;
			assert s2 > 0;
			return (float)Math.sqrt(s2);
		} else
			return spd;
	}

	/** Debug the finder */
	public void debug(DebugLog VSA_LOG) {
		Float a = calculateSpeedAdvisory();
		VSA_LOG.log("adv: " + a +
		            ", upstream: " + su +
		            ", downstream: " + sd +
		            ", vss: " + vss +
		            ", speed: " + getSpeed() +
		            ", limit: " + getSpeedLimit());
	}

	/** Get the speed */
	private Float getSpeed() {
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
}
