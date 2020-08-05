/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2020  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.EventType;
import static us.mn.state.dot.tms.EventType.TT_LINK_TOO_LONG;
import static us.mn.state.dot.tms.EventType.TT_NO_DESTINATION_DATA;
import static us.mn.state.dot.tms.EventType.TT_NO_ORIGIN_DATA;
import us.mn.state.dot.tms.units.Interval;

/**
 * A route leg timer calculates travel times for one leg of a route.
 *
 * @author Douglas Lau
 */
public class RouteLegTimer {

	/** Distance to use low station speed at end of route (miles) */
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

	/** Route leg */
	private final RouteLeg leg;

	/** Milepoint after which to use low station speed */
	private final float low_mi;

	/** Create a new route leg timer.
	 * @param lg Route leg.
	 * @param fd Final destination flag. */
	public RouteLegTimer(RouteLeg lg, boolean fd) {
		leg = lg;
		low_mi = (fd) ? leg.d_mi - LOW_SPEED_DISTANCE : leg.d_mi;
	}

	/** Calculate the current travel time.
	 * @return Travel time interval. */
	public Interval calculateTime() throws BadRouteException {
		float hours = 0;
		StationData pd = null;	// previous station data
		for (StationData sd: lookupStationData()) {
			if (pd != null) {
				if (isSegmentTooLong(pd.mile, sd.mile))
					throwException(TT_LINK_TOO_LONG, pd);
				hours += sd.timeFrom(pd);
			}
			pd = sd;
		}
		return new Interval(hours, Interval.Units.HOURS);
	}

	/** Lookup station data for the route leg */
	private ArrayList<StationData> lookupStationData()
		throws BadRouteException
	{
		final ArrayList<StationData> s_data =
			new ArrayList<StationData>();
		// NOTE: stations are checked in mile point order
		leg.corridor.findStation(new Corridor.StationFinder() {
			public boolean check(float m, StationImpl s) {
				if (isWithinTrip(m)) {
					float a = s.getSmoothedAverageSpeed();
					float l = s.getSmoothedLowSpeed();
					if (a > 0 && l > 0) {
						s_data.add(new StationData(
							s.getName(), m, a, l));
					}
				}
				return false;
			}
		});
		extendStationData(s_data);
		return s_data;
	}

	/** Add station data for beginning / end of leg */
	private void extendStationData(ArrayList<StationData> s_data)
		throws BadRouteException
	{
		if (s_data.isEmpty()) {
			// If leg has no data, use minimum travel time speed at
			// the midpoint.  This allows short legs to be used (CD
			// roads with no detection, for example).
			float m = leg.getMidPoint();
			int mph = SystemAttrEnum.TRAVEL_TIME_MIN_MPH.getInt();
			s_data.add(new StationData("LEG", m, mph, mph));
		}
		StationData sd = s_data.get(0);
		if (sd.mile > leg.o_mi) {
			sd = sd.linkBefore();
			if (sd.mile > leg.o_mi)
				throwException(TT_NO_ORIGIN_DATA, sd);
			s_data.add(0, sd);
		}
		sd = s_data.get(s_data.size() - 1);
		if (sd.mile < leg.d_mi) {
			sd = sd.linkAfter();
			if (sd.mile < leg.d_mi)
				throwException(TT_NO_DESTINATION_DATA, sd);
			s_data.add(sd);
		}
	}

	/** Throw a BadRouteException with the specified message */
	private void throwException(EventType et, StationData sd)
		throws BadRouteException
	{
		throw new BadRouteException(et, leg.toString(), sd.sid);
	}

	/** Check if a milepoint is within the leg "bounds" */
	private boolean isWithinTrip(float m) {
		// NOTE: isSegmentTooLong never returns true if start > end
		return !(isSegmentTooLong(m, leg.o_mi) ||
		         isSegmentTooLong(leg.d_mi, m));
	}

	/** Station data */
	private class StationData {
		private final String sid;
		private final float mile;
		private final float avg;
		private final float low;
		private StationData(String sid, float m, float a, float l) {
			this.sid = sid;
			mile = m;
			avg = a;
			low = l;
		}
		private StationData linkBefore() {
			return new StationData(sid + "-",
				mile - MAX_LINK_LENGTH, avg, low);
		}
		private StationData linkAfter() {
			return new StationData(sid + "+",
				mile + MAX_LINK_LENGTH, avg, low);
		}
		private float timeFrom(StationData pd) {
			assert mile > pd.mile;
			return timeFromAvg(pd) + timeFromLow(pd);
		}
		private float timeFromAvg(StationData pd) {
			return time_segment(leg.o_mi, low_mi, pd.mile, mile,
			                    pd.avg, avg);
		}
		private float timeFromLow(StationData pd) {
			return time_segment(low_mi, leg.d_mi, pd.mile, mile,
			                    pd.low, low);
		}
	}
}
