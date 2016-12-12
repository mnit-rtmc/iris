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

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.tms.units.Distance;
import us.mn.state.dot.tms.units.Interval;

/**
 * A trip timer calculates travel times for a corridor trip.
 *
 * @author Douglas Lau
 */
public class TripTimer {

	/** Distance to use low station speed at end of trip (miles) */
	static private final float LOW_SPEED_DISTANCE = 1.0f;

	/** Maximum allowed length of a travel time link (miles) */
	static private final float MAX_LINK_LENGTH = 0.6f;

	/** Check if a segment is too long.
	 * @param m0 Milepoint at start of segment.
	 * @param m1 Milepoint at end of segment.
	 * @return true if segment is too long. */
	static private boolean isSegmentTooLong(float m0, float m1) {
		return (m1 - m0) > (3 * MAX_LINK_LENGTH);
	}

	/** Calculate the travel time for one segment.
	 * @param o Milepoint at start of segment.
	 * @param d Milepoint at end of segment.
	 * @param m0 Milepoint at first station.
	 * @param m1 Milepoint at second station.
	 * @param s0 Speed at first station.
	 * @param s1 Speed at second station.
	 * @return Travel time (hours). */
	static private float time_segment(float o, float d, float m0, float m1,
		float s0, float s1)
	{
		assert m1 > m0;
		float t = (m1 - m0) / 3;
		float ma = m0 + t;
		float mb = m1 - t;
		return time_link(o, d, m0, ma, s0)
		     + time_link(o, d, ma, mb, (s0 + s1) / 2)
		     + time_link(o, d, mb, m1, s1);
	}

	/** Calculate the travel time for one link.
	 * @param o Milepoint at start of segment.
	 * @param d Milepoint at end of segment.
	 * @param l0 Milepoint at start of link.
	 * @param l1 Milepoint at end of link.
	 * @param sp Speed of link.
	 * @return Travel time (hours). */
	static private float time_link(float o, float d, float l0, float l1,
		float sp)
	{
		float link = Math.min(l1, d) - Math.max(l0, o);
		return (link > 0) ? (link / sp) : 0;
	}

	/** Debug log */
	private final DebugLog dlog;

	/** Name to use for debugging purposes */
	private final String name;

	/** Corridor trip */
	private final CorridorTrip trip;

	/** Milepoint after which to use low station speed */
	private final float low_mile;

	/** Create a new trip timer.
	 * @param dl Debug log.
	 * @param n Name (for debugging).
	 * @param ct Corridor trip.
	 * @param fd Final destination flag. */
	public TripTimer(DebugLog dl, String n, CorridorTrip ct, boolean fd) {
		dlog = dl;
		name = n;
		trip = ct;
		low_mile = (fd)
		          ? trip.destination - LOW_SPEED_DISTANCE
		          : trip.destination;
	}

	/** Calculate the current trip time.
	 * @return Travel time interval. */
	public Interval calculate() throws BadRouteException {
		float hours = 0;
		StationData pd = null;	// previous station data
		for (StationData sd: lookupStationData()) {
			if (pd != null) {
				if (isSegmentTooLong(pd.mile, sd.mile)) {
					float llen = sd.mile - pd.mile;
					trip.throwException("Link too long (" +
						llen + ") " + sd.station);
				}
				hours += sd.timeFrom(pd);
			}
			pd = sd;
		}
		return new Interval(hours, Interval.Units.HOURS);
	}

	/** Lookup station data for the trip */
	private Collection<StationData> lookupStationData()
		throws BadRouteException
	{
		final TreeMap<Float, StationData> s_data =
			new TreeMap<Float, StationData>();
		trip.corridor.findStation(new Corridor.StationFinder() {
			public boolean check(float m, StationImpl s) {
				if (isWithinTrip(m)) {
					float a = s.getSmoothedAverageSpeed();
					float l = s.getSmoothedLowSpeed();
					if (a > 0 && l > 0) {
						s_data.put(m, new StationData(s,
						           m, a, l));
					}
				}
				return false;
			}
		});
		if (s_data.size() > 0) {
			Map.Entry<Float, StationData> me = s_data.firstEntry();
			if (me.getKey() > trip.origin) {
				StationData sd = me.getValue();
				float mm = sd.mile - MAX_LINK_LENGTH;
				if (mm > trip.origin)
					trip.throwException("Start > origin");
				s_data.put(mm, new StationData(sd.station, mm,
				           sd.avg, sd.low));
			}
			me = s_data.lastEntry();
			if (me.getKey() < trip.destination) {
				StationData sd = me.getValue();
				float mm = sd.mile + MAX_LINK_LENGTH;
				if (mm < trip.destination)
					trip.throwException("End < destin");
				s_data.put(mm, new StationData(sd.station, mm,
				           sd.avg, sd.low));
			}
		} else
			trip.throwException("No speed data");
		return s_data.values();
	}

	/** Check if a milepoint is within the trip "bounds" */
	private boolean isWithinTrip(float m) {
		// NOTE: isSegmentTooLong never returns true if start > end
		return !(isSegmentTooLong(m, trip.origin) ||
		         isSegmentTooLong(trip.destination, m));
	}

	/** Station data */
	private class StationData {
		private final StationImpl station;
		private final float mile;
		private final float avg;
		private final float low;
		private StationData(StationImpl s, float m, float a, float l) {
			station = s;
			mile = m;
			avg = a;
			low = l;
		}
		private float timeFrom(StationData pd) {
			assert mile > pd.mile;
			float hours = timeFromAvg(pd) + timeFromLow(pd);
			if (dlog.isOpen()) {
				dlog.log(name + " st: " + mile + ", " + avg +
				         ", " + low + ", h: " + hours);
			}
			return hours;
		}
		private float timeFromAvg(StationData pd) {
			return time_segment(trip.origin, low_mile,
			                    pd.mile, mile, pd.avg, avg);
		}
		private float timeFromLow(StationData pd) {
			return time_segment(low_mile, trip.destination,
			                    pd.mile, mile, pd.low, low);
		}
	}
}
