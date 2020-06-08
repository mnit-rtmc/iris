/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2019-2020  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.incident;

import us.mn.state.dot.tms.CorridorBase;
import us.mn.state.dot.tms.Device;
import us.mn.state.dot.tms.IncRange;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.units.Distance;
import static us.mn.state.dot.tms.units.Distance.Units.MILES;

/**
 * Device upstream of an incident.
 *
 * @author Douglas Lau
 */
public class UpstreamDevice implements Comparable<UpstreamDevice> {

	/** Max distance threshold for `ahead` range */
	static private final float AHEAD_DIST_MI = 1.5f;

	/** Max gap between r_nodes in corridor */
	static public final float MAX_GAP_MI = 10f;

	/** Get the maximum distance threshold for `ahead` range */
	static private float getAheadDistMi(boolean picked) {
		return picked ? AHEAD_DIST_MI / 2f : AHEAD_DIST_MI;
	}

	/** Create upstream device on a corridor.
	 * @param dev Device.
	 * @param cb Freeway corridor.
	 * @param mp Milepoint at incident.
	 * @param loc Location of device. */
	static public UpstreamDevice create(Device dev, CorridorBase<R_Node> cb,
		float mp, GeoLoc loc)
	{
		Float p = cb.calculateMilePoint(loc);
		if (p != null && mp > p) {
			Integer exits = cb.countExits(p, mp, MAX_GAP_MI);
			if (exits != null) {
				Distance up = new Distance(mp - p, MILES);
				return new UpstreamDevice(dev, exits, up);
			}
		}
		return null;
	}

	/** Upstream device */
	public final Device device;

	/** Number of exits between device and incident */
	public final int exits;

	/** Distance from device to incident */
	public final Distance distance;

	/** Create a new upstream device */
	private UpstreamDevice(Device dev, int ex, Distance dist) {
		device = dev;
		exits = ex;
		distance = dist;
	}

	/** Create an adjusted upstream device */
	public UpstreamDevice adjusted(int ex, Distance dist) {
		return new UpstreamDevice(device, exits + ex,
			distance.add(dist));
	}

	/** Compare with another upstream device */
	@Override
	public int compareTo(UpstreamDevice other) {
		return (exits != other.exits)
		      ? exits - other.exits
		      : distance.compareTo(other.distance);
	}

	/** Get the incident range */
	public IncRange range(boolean picked) {
		// If distance is less than threshold, use `ahead` range
		return (distance.asFloat(MILES) < getAheadDistMi(picked))
		      ? IncRange.ahead
		      : IncRange.fromExits(exits);
	}
}
