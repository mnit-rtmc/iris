/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2009  Minnesota Department of Transportation
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

import java.io.PrintStream;
import java.util.LinkedList;

/**
 * A route is a list of "corridor trips" from an origin to a destination on a
 * freeway network.
 *
 * @author Douglas Lau
 */
public class Route implements Comparable<Route> {

	/** Penalty (in goodness) for each trip in a route */
	static protected final float TRIP_PENALTY = 0.25f;

	/** Convert minutes to hours */
	static protected float minutesToHours(float minutes) {
		return minutes / 60;
	}

	/** List of corridor trips */
	protected final LinkedList<CorridorTrip> trips =
		new LinkedList<CorridorTrip>();

	/** Number of turns in route */
	protected int turns;

	/** Create a new route */
	public Route() {
		turns = 0;
	}

	/** Add a corridor trip to the route */
	public void addTrip(CorridorTrip trip) {
		trips.add(trip);
	}

	/** Set the number of turns in the route */
	public void setTurns(int t) {
		turns = t;
	}

	/** Get the number of turns in the route */
	public int getTurns() {
		return turns;
	}

	/** Get the "only" corridor (if the route is just a single corridor) */
	public Corridor getOnlyCorridor() {
		if(trips.size() == 1)
			return trips.getFirst().getCorridor();
		else
			return null;
	}

	/** Get the length of the route (in miles) */
	public float getLength() {
		float l = 0;
		for(CorridorTrip trip: trips)
			l += trip.getLength();
		return l;
	}

	/** Get the goodness rating (lower is better) */
	public float getGoodness() {
		return getLength() + TRIP_PENALTY * trips.size();
	}

	/** Compare to another route (for sorting) */
	public int compareTo(Route o) {
		return (int)Math.signum(getGoodness() - o.getGoodness());
	}

	/** Get the current travel time (in hours) */
	public float getTravelTime(boolean final_dest)
		throws BadRouteException
	{
		if(trips.isEmpty())
			throw new BadRouteException("Route is empty");
		float hours = minutesToHours(turns);
		for(CorridorTrip trip: trips)
			hours += trip.getTravelTime(final_dest);
		return hours;
	}

	/** Print the route to stderr */
	public void print(PrintStream out) {
		out.println("Route:" + getLength());
		for(CorridorTrip trip: trips)
			trip.print(out);
	}
}
