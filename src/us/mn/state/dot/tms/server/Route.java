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
import java.util.LinkedList;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.tms.LaneType;
import us.mn.state.dot.tms.units.Distance;
import us.mn.state.dot.tms.units.Interval;

/**
 * A route is a list of "corridor trips" from an origin to a destination on a
 * roadway network.
 *
 * @author Douglas Lau
 */
public class Route implements Comparable<Route> {

	/** Penalty (in goodness) for each trip in a route */
	static private final float TRIP_PENALTY = 0.25f;

	/** Debug log */
	private final DebugLog dlog;

	/** Name for route debugging */
	private final String name;

	/** List of corridor trips */
	private final LinkedList<CorridorTrip> trips =
		new LinkedList<CorridorTrip>();

	/** Number of turns in route */
	private int turns;

	/** Create a new route.
	 * @param dl Debug log.
	 * @param n Name (for debugging). */
	public Route(DebugLog dl, String n) {
		dlog = dl;
		name = n;
		turns = 0;
	}

	/** Add a corridor trip to the route */
	public void addTrip(CorridorTrip trip, boolean is_turn) {
		trips.add(trip);
		if (is_turn)
			turns++;
	}

	/** Get the number of turns in the route */
	public int getTurns() {
		return turns;
	}

	/** Get the "only" corridor (if the route is just a single corridor) */
	public Corridor getOnlyCorridor() {
		if (trips.size() == 1)
			return trips.getFirst().corridor;
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

	/** Get the current travel time */
	public Interval getTravelTime(boolean final_dest)
		throws BadRouteException
	{
		if (trips.isEmpty())
			throw new BadRouteException("Route is empty");
		Interval t = new Interval(turns, Interval.Units.MINUTES);
		for (CorridorTrip trip: trips) {
			TripTimer tt = new TripTimer(dlog, name, trip,
				final_dest);
			t = t.add(tt.calculate());
		}
		if (isLogging())
			log("TRAVEL TIME " + t);
		return t;
	}

	/** Check if we're logging */
	private boolean isLogging() {
		return dlog.isOpen();
	}

	/** Log a message */
	private void log(String m) {
		dlog.log(name + ": " + m);
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
