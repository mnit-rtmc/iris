/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016-2019  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
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
	 * @param org Origin location.
	 * @param dst Destination location.
	 * @param t Turn penalty.
	 * @param pr Previous leg. */
	static public RouteLeg create(Corridor c, GeoLoc org, GeoLoc dst,
		boolean t, RouteLeg pr)
	{
		Float o_mi = c.calculateMilePoint(org);
		Float d_mi = c.calculateMilePoint(dst);
		if (o_mi != null && d_mi != null) {
			RouteLeg rl = new RouteLeg(c, org, o_mi, dst,d_mi,t,pr);
			return rl.isValid() ? rl : null;
		} else
			return null;
	}

	/** Corridor for route leg */
	public final Corridor corridor;

	/** Location of leg origin */
	private final GeoLoc orig;

	/** Milepoint of leg origin */
	public final float o_mi;

	/** Location of leg destination */
	private final GeoLoc dest;

	/** Milepoint of leg destination */
	public final float d_mi;

	/** Turn penalty */
	private final boolean turn;

	/** Previous leg */
	public final RouteLeg prev;

	/** Create a new route leg.
	 * @param c Corridor.
	 * @param org Origin location.
	 * @param omi Origin milepoint.
	 * @param dst Destination location.
	 * @param dmi Destination milepoint.
	 * @param t Turn penalty.
	 * @param pr Previous leg. */
	private RouteLeg(Corridor c, GeoLoc org, float omi, GeoLoc dst,
		float dmi, boolean t, RouteLeg pr)
	{
		corridor = c;
		orig = org;
		o_mi = omi;
		dest = dst;
		d_mi = dmi;
		turn = t;
		prev = pr;
	}

	/** Check if the route leg is valid */
	private boolean isValid() {
		return isOriginValid() && (o_mi < d_mi) && isContinuous();
	}

	/** Check if origin is within distance limit to corridor */
	private boolean isOriginValid() {
		Distance d = corridor.distanceTo(orig);
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
		return turn;
	}

	/** Get the middle mile point of the route leg */
	public float getMidPoint() {
		return o_mi + (0.5f * (d_mi - o_mi));
	}

	/** Lookup samplers on a corridor trip.
	 * @param samplers Array to add samplers.
	 * @param lt Detector lane type to include. */
	public void lookupSamplers(final ArrayList<VehicleSampler> samplers,
		final LaneType lt)
	{
		corridor.findStation(new Corridor.StationFinder() {
			public boolean check(float m, StationImpl s) {
				if (isWithinTrip(m)) {
					// Detectors within station should be
					// averaged at the station level
					SamplerSet dets = s.getSamplerSet()
					                   .filter(lt);
					if (dets.size() > 0)
						samplers.add(dets);
				}
				return false;
			}
		});
	}

	/** Get a string representation */
	@Override
	public String toString() {
		return "leg orig: " + GeoLocHelper.getLocation(orig)
		       + ", o_mi: " + o_mi
		       + ", dest: " + GeoLocHelper.getLocation(dest)
		       + ", d_mi: " + d_mi
		       + ", turn: " + turn;
	}
}
