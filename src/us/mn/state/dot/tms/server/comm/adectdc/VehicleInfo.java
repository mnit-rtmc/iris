/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2025  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.adectdc;

import us.mn.state.dot.tms.units.Distance;
import us.mn.state.dot.tms.units.Speed;
import us.mn.state.dot.tms.server.DetectorImpl;

/**
 * Detector vehicle information
 *
 * @author Douglas Lau
 */
public class VehicleInfo {

	/** Speed (km/h units) */
	private final int speed;

	/** Vehicle class and lane info */
	private final int class_lane;

	/** Occupancy (duration) in 10 ms units */
	private final int occupancy;

	/** Gap from previous vehicle (10 ms units).
	 *
	 * NOTE: the protocol document is unclear, but it is assumed that
	 *       this is the gap between two vehicles, and not headway */
	private final int gap;

	/** Vehicle length (0.1 m units) */
	private final int length;

	/** Peer synchronization stamp / tick (2.5 ms units) */
	private final int sync_stamp;

	/** Construct vehicle information */
	public VehicleInfo(int sp, int cl, int o, int g, int l, int st) {
		speed = sp;
		class_lane = cl;
		occupancy = o;
		gap = g;
		length = l;
		sync_stamp = st;
	}

	/** Get vehicle info as a string */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("veh");
		if (speed > 0) {
			sb.append(" spd:");
			sb.append(speed);
		}
		if (class_lane != 0) {
			sb.append(" cl_ln:");
			sb.append(class_lane);
		}
		if (occupancy > 0) {
			sb.append(" occ:");
			sb.append(occupancy);
		}
		if (gap > 0) {
			sb.append(" gap:");
			sb.append(gap);
		}
		if (length > 0) {
			sb.append(" len:");
			sb.append(length);
		}
		if (sync_stamp > 0) {
			sb.append(" sync_stamp:");
			sb.append(sync_stamp);
		}
		return sb.toString();
	}

	/** Get the headway from previous vehicle (ms) */
	public int getHeadway() {
		// Technically, for headway it should be previous vehicle's
		// occupancy + gap -- this is more like "tailway".
		if (gap > 0 && occupancy > 0)
			return 10 * (gap + occupancy);
		else
			return 0;
	}

	/** Get the vehicle speed (mph) */
	private int getSpeedMph() {
		Speed s = new Speed(speed, Speed.Units.KPH);
		return s.round(Speed.Units.MPH);
	}

	/** Get the vehicle length (ft) */
	private int getLengthFt() {
		Distance len = new Distance(length, Distance.Units.DECIMETERS);
		return len.round(Distance.Units.FEET);
	}

	/** Log vehicle info */
	public void logVehicle(DetectorImpl det, long stamp) {
		int duration = 10 * occupancy;
		int headway = getHeadway();
		det.logVehicle(duration, headway, stamp, getSpeedMph(),
			getLengthFt());
	}
}
