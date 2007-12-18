/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007  Minnesota Department of Transportation
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
import java.util.Iterator;
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
	static protected final float MAX_LINK_LENGTH = 0.5f;

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

// FIXME: temporary stuff
protected final DMSImpl dms;

	/** Add buffer stations into station map */
	protected void addBufferStations() {
		Float m = stations.firstKey();
		StationImpl stat = stations.get(m);
		stations.put(m - MAX_LINK_LENGTH, stat);
		m = stations.lastKey();
		stat = stations.get(m);
		stations.put(m + MAX_LINK_LENGTH, stat);
	}

	/** Create a new corridor trip */
	public CorridorTrip(Corridor c, ODPair od, DMSImpl d)
		throws BadRouteException
	{
		corridor = c;
		od_pair = od;
dms = d;
		if(!c.getName().equals(od.getCorridor()))
			throwException("Bad trip");
		origin = c.calculateMilePoint(od.getOrigin());
		destination = c.calculateMilePoint(od.getDestination());
		if(origin > destination)
			throwException("Origin > destination");
		stations = c.createStationMap();
		if(stations.isEmpty())
			throwException("No stations");
		addBufferStations();
		if(origin < stations.firstKey())
			throwException("Origin < first link");
		if(destination > stations.lastKey())
			throwException("Destin > last link");
	}

	/** Get the length of the corridor trip (in miles) */
	public float getLength() {
		return destination - origin;
	}

	/** Calculate the travel time for one link */
	protected float link_time(float start, float end, float o,
		float d, float speed) throws BadRouteException
	{
		float link = Math.min(end, d) - Math.max(start, o);
		if(link > MAX_LINK_LENGTH)
			throwException("Link too long: " + link);
		if(link > 0)
			return link / speed;
		else
			return 0;
	}

	/** Calculate the travel time between two stations */
	protected float station_time(float m0, float m1, float[] spd,
		float o, float d) throws BadRouteException
	{
		float h = 0;
		float t = (m1 - m0) / 3;
		h += link_time(m0, m0 + t, o, d, spd[0]);
		h += link_time(m0 + t, m1 - t, o, d, (spd[0] + spd[1]) / 2);
		h += link_time(m1 - t, m1, o, d, spd[1]);
		return h;
	}

	/** Get the current travel time (in hours) */
	public float getTravelTime(boolean final_destin)
		throws BadRouteException
	{
		float low_mile = destination;
		if(final_destin)
			low_mile -= LOW_SPEED_DISTANCE;
		float[] low = { MISSING_DATA, MISSING_DATA };
		float[] avg = { MISSING_DATA, MISSING_DATA };
		Float fmile = null;
		float smile = 0;
		float hours = 0;
		for(Float m: stations.keySet()) {
			StationImpl s = stations.get(m);
			low[1] = s.getTravelSpeed(true);
			avg[1] = s.getTravelSpeed(false);
			if(avg[1] <= 0 || low[1] <= 0)
				continue;
			if(fmile != null) {
				hours += station_time(smile, m, low, origin,
					low_mile);
				hours += station_time(smile, m, avg, low_mile,
					destination);

// FIXME: temporary debugging code
float _h = station_time(smile, m, low, origin, low_mile);
if(_h > 0) DMSImpl.TRAVEL_LOG.log("dms: " + dms.getId() + "route: " + od_pair + ", station: " + s.getName() + ", mile: " + m + ", time: " + _h);
_h = station_time(smile, m, avg, low_mile, destination);
if(_h > 0) DMSImpl.TRAVEL_LOG.log("dms: " + dms.getId() + "route: " + od_pair + ", station: " + s.getName() + ", mile: " + m + ", time: " + _h);
// FIXME: temporary debugging code

			} else
				fmile = m;
			low[0] = low[1];
			avg[0] = avg[1];
			smile = m;
		}
		if(fmile == null || fmile > origin)
			throwException("Start > origin");
		if(smile < destination)
			throwException("End < destin");
		return hours;
	}

	/** Print the trip to a print stream */
	public void print(PrintStream out) {
		out.println("\tTrip origin: " + origin + ", destin: " +
			destination);
		for(StationImpl s: stations.values())
			out.println("\t\tStation: " + s.getName());
	}
}
