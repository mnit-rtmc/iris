/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms;

import java.io.PrintStream;
import java.util.TreeMap;

/**
 * A CorridorTrip is one "leg" (on a single corridor) of a Route.
 *
 * @author Douglas Lau
 */
public class CorridorTrip implements Constants {

	/** Distance to use low station speed at end of trip (miles) */
	static protected final float LOW_SPEED_DISTANCE = 1.0f;

	/** Maximum allowed length of a travel time link (miles) */
	static protected final float MAX_LINK_LENGTH = 0.6f;

	/** Name to use for debugging purposes */
	protected final String name;

	/** Corridor for the trip */
	protected final Corridor corridor;

	/** Get the corridor */
	public Corridor getCorridor() {
		return corridor;
	}

	/** Origin/destination pair */
	protected final ODPair od_pair;

	/** Throw a BadRouteException with the specified message */
	protected void throwException(String message) throws BadRouteException {
		throw new BadRouteException(message + " (" +
			corridor.getName() + ", origin: " + origin +
			", destination: " + destination + ")");
	}

	/** Milepoint of the trip origin */
	protected final float origin;

	/** Milepoint of the trip destination */
	protected final float destination;

	/** Mapping from mile point to station */
	protected final TreeMap<Float, StationImpl> stations;

	/** Create a new corridor trip */
	public CorridorTrip(String n, Corridor c, ODPair od)
		throws BadRouteException
	{
		name = n;
		corridor = c;
		od_pair = od;
		if(!c.getName().equals(od.getCorridor()))
			throwException("Bad trip");
		origin = c.calculateMilePoint(od.getOrigin());
		destination = c.calculateMilePoint(od.getDestination());
		if(origin > destination)
			throwException("Origin > destination");
		stations = c.createStationMap();
		if(stations.isEmpty())
			throwException("No stations");
	}

	/** Get the length of the corridor trip (in miles) */
	public float getLength() {
		return destination - origin;
	}

	/** Calculate the travel time for one link */
	static protected float link_time(float start, float end, float o,
		float d, float speed)
	{
		float link = Math.min(end, d) - Math.max(start, o);
		if(link > 0)
			return link / speed;
		else
			return 0;
	}

	/** Calculate the travel time between two stations */
	static protected float station_time(float m0, float m1, float[] spd,
		float o, float d)
	{
		float h = 0;
		float t = (m1 - m0) / 3;
		h += link_time(m0, m0 + t, o, d, spd[0]);
		h += link_time(m0 + t, m1 - t, o, d, (spd[0] + spd[1]) / 2);
		h += link_time(m1 - t, m1, o, d, spd[1]);
		return h;
	}

	/** Trip timer */
	protected class TripTimer {

		float low_mile = destination;
		float[] low = { MISSING_DATA, MISSING_DATA };
		float[] avg = { MISSING_DATA, MISSING_DATA };
		float smile = 0;
		float hours = 0;

		TripTimer(boolean final_destin) {
			if(final_destin)
				low_mile -= LOW_SPEED_DISTANCE;
		}

		void firstStation(float mile, float _avg, float _low) {
			avg[1] = _avg;
			low[1] = _low;
			smile = mile;
		}

		void nextStation(float mile, float _avg, float _low) {
			avg[0] = avg[1];
			low[0] = low[1];
			avg[1] = _avg;
			low[1] = _low;
			hours += station_time(smile, mile, avg, origin,
				low_mile);
			hours += station_time(smile, mile, low, low_mile,
				destination);
			smile = mile;
		}
	}

	/** Check the length of a link between two milepoints */
	static protected boolean checkLinkLength(float start, float end) {
		return (end - start) > (3 * MAX_LINK_LENGTH);
	}

	/** Find the speeds for a trip timer */
	protected void findTripSpeeds(TripTimer tt) throws BadRouteException {
		float avg = 0;
		float low = 0;
		float pmile = 0;
		boolean first = true;

		for(Float mile: stations.keySet()) {
			if(checkLinkLength(mile, origin))
				continue;
			if(checkLinkLength(destination, mile))
				break;
			StationImpl s = stations.get(mile);
			avg = s.getTravelSpeed(false);
			low = s.getTravelSpeed(true);
			if(avg > 0 && low > 0) {
				if(first) {
					float mm = mile - MAX_LINK_LENGTH;
					if(mm > origin)
						throwException("Start > origin");
					tt.firstStation(mm, avg, low);
					first = false;
				} else if(checkLinkLength(pmile, mile))
					throwException("Link too long: " + s);
				else
					tt.nextStation(mile, avg, low);
			}
			pmile = mile;
		}
		if(first)
			throwException("No speed data");
		else if(pmile < destination) {
			float mm = pmile + MAX_LINK_LENGTH;
			if(mm < destination)
				throwException("End < destin");
			tt.nextStation(mm, avg, low);
		}
	}

	/** Get the current travel time (in hours) */
	public float getTravelTime(boolean final_dest)
		throws BadRouteException
	{
		TripTimer tt = new TripTimer(final_dest);
		findTripSpeeds(tt);
		return tt.hours;
	}

	/** Print the trip to a print stream */
	public void print(PrintStream out) {
		out.println("\tTrip origin: " + origin + ", destin: " +
			destination);
		for(StationImpl s: stations.values())
			out.println("\t\tStation: " + s.getName());
	}
}
