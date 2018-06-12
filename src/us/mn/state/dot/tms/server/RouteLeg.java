/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016-2018  Minnesota Department of Transportation
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

import java.util.ArrayList;
import us.mn.state.dot.tms.LaneType;
import us.mn.state.dot.tms.units.Distance;

/**
 * A RouteLeg is one "leg" (on a single corridor) of a Route.
 *
 * @author Douglas Lau
 */
public class RouteLeg {

	/** Maximum distance from origin to a corridor node (in meters) */
	static private final float MAX_ORIGIN_M = 1000;

	/** Create a new route leg.
	 * @param c Corridor.
	 * @param od Origin-destination pair.
	 * @param pr Previous leg. */
	static public RouteLeg create(Corridor c, ODPair od, RouteLeg pr) {
		Float o_mi = c.calculateMilePoint(od.getOrigin());
		Float d_mi = c.calculateMilePoint(od.getDestination());
		if (o_mi != null && d_mi != null) {
			RouteLeg rl = new RouteLeg(c, od, o_mi, d_mi, pr);
			return rl.isValid() ? rl : null;
		} else
			return null;
	}

	/** Corridor for route leg */
	public final Corridor corridor;

	/** O/D pair */
	private final ODPair od_pair;

	/** Previous leg */
	public final RouteLeg prev;

	/** Leg origin milepoint */
	public final float o_mi;

	/** Leg destination milepoint */
	public final float d_mi;

	/** Create a new route leg.
	 * @param c Corridor.
	 * @param od Origin-destination pair.
	 * @param omi Origin milepoint.
	 * @param dmi Destination milepoint.
	 * @param pr Previous leg. */
	private RouteLeg(Corridor c, ODPair od, float omi, float dmi,
		RouteLeg pr)
	{
		corridor = c;
		od_pair = od;
		prev = pr;
		o_mi = omi;
		d_mi = dmi;
	}

	/** Check if the route leg is valid */
	private boolean isValid() {
		return isOriginValid() && (o_mi < d_mi) && isContinuous();
	}

	/** Check if origin is within distance limit to corridor */
	private boolean isOriginValid() {
		Distance d = corridor.distanceTo(od_pair.getOrigin());
		return (d != null) && (d.m() < MAX_ORIGIN_M);
	}

	/** Check if the route leg is continuous */
	private boolean isContinuous() {
		return (null == corridor.findActiveNode(
			new Corridor.NodeFinder()
		{
			public boolean check(float m, R_NodeImpl rn) {
				return isWithinTrip(m) && rn.isCommonExit();
			}
		}));
	}

	/** Check if a milepoint is within the trip */
	private boolean isWithinTrip(float m) {
		// Cannot include d_mi -- it might be a common exit
		return (m > o_mi) && (m < d_mi);
	}

	/** Get the leg distance.
	 * @return Distance of the leg. */
	public Distance getDistance() {
		float d = d_mi - o_mi;
		return new Distance(d, Distance.Units.MILES);
	}

	/** Check if the leg ends in a "turn" */
	public boolean hasTurn() {
		return od_pair.hasTurn();
	}

	/** Get the middle mile point of the route leg */
	public float getMidPoint() {
		return o_mi + (0.5f * (d_mi - o_mi));
	}

	/** Lookup samplers on a corridor trip */
	public ArrayList<VehicleSampler> lookupSamplers(final LaneType lt) {
		final ArrayList<VehicleSampler> samplers =
			new ArrayList<VehicleSampler>();
		corridor.findStation(new Corridor.StationFinder() {
			public boolean check(float m, StationImpl s) {
				if (isWithinTrip(m))
					samplers.addAll(lookupSamplers(s, lt));
				return false;
			}
		});
		return samplers;
	}

	/** Lookup the samplers for one station and lane type */
	private ArrayList<VehicleSampler> lookupSamplers(StationImpl s,
		LaneType lt)
	{
		SamplerSet ss = s.getSamplerSet();
		ArrayList<VehicleSampler> dets = ss.filter(lt);
		// Create sampler set combining all detectors in station.
		// This is needed to average densities over multiple HOT lanes.
		ArrayList<VehicleSampler> arr = new ArrayList<VehicleSampler>();
		if (dets.size() > 0)
			arr.add(new SamplerSet(dets));
		return arr;
	}

	/** Get a string representation */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("leg: ");
		sb.append(od_pair);
		sb.append(", o_mi: ");
		sb.append(o_mi);
		sb.append(", d_mi: ");
		sb.append(d_mi);
		return sb.toString();
	}
}
