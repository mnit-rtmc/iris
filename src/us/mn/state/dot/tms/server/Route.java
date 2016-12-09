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

import java.util.ArrayList;
import java.util.List;
import us.mn.state.dot.tms.LaneType;
import us.mn.state.dot.tms.units.Distance;

/**
 * A route is a list of "corridor trips" from an origin to a destination on a
 * roadway network.
 *
 * @author Douglas Lau
 */
public class Route implements Comparable<Route> {

	/** Penalty (in goodness) for each trip in a route */
	static private final float TRIP_PENALTY = 0.25f;

	/** List of corridor trips */
	private final List<CorridorTrip> trips = new ArrayList<CorridorTrip>();

	/** Number of turns in route */
	private int turns;

	/** Create a new route.
	 * @param n Name (for debugging). */
	public Route() {
		turns = 0;
	}

	/** Add a corridor trip to the route */
	public void addTrip(CorridorTrip trip, boolean is_turn) {
		trips.add(trip);
		if (is_turn)
			turns++;
	}

	/** Get list of trips in route */
	public List<CorridorTrip> getTrips() {
		return trips;
	}

	/** Get the number of turns in the route */
	public int getTurns() {
		return turns;
	}

	/** Get the "only" corridor (if the route is just a single corridor) */
	public Corridor getOnlyCorridor() {
		if (trips.size() == 1)
			return trips.get(0).corridor;
		else
			return null;
	}

	/** Get the route distance.
	 * @return Total route distance. */
	public Distance getDistance() {
		Distance d = new Distance(0);
		for (CorridorTrip trip: trips)
			d = d.add(trip.getDistance());
		return d;
	}

	/** Get the goodness rating (lower is better) */
	public float getGoodness() {
		return getDistance().asFloat(Distance.Units.MILES) +
			TRIP_PENALTY * trips.size();
	}

	/** Get a set of vehicle samplers on route */
	public SamplerSet getSamplerSet(LaneType lt) {
		ArrayList<VehicleSampler> vs = new ArrayList<VehicleSampler>();
		for (CorridorTrip trip: trips)
			vs.addAll(trip.lookupSamplers(lt));
		return new SamplerSet(vs);
	}

	/** Compare to another route (for sorting) */
	@Override
	public int compareTo(Route o) {
		return (int)Math.signum(getGoodness() - o.getGoodness());
	}

	/** Get a string representation of the route */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getDistance());
		sb.append(", ");
		sb.append(getTurns());
		sb.append(" turns, ");
		sb.append(getGoodness());
		sb.append(" goodness, ");
		for (CorridorTrip trip: trips)
			sb.append(trip.toString());
		return sb.toString();
	}
}
