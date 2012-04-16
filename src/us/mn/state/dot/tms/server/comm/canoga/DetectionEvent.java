/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2012  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.canoga;

import java.util.Calendar;
import static us.mn.state.dot.tms.server.Constants.FEET_PER_MILE;
import us.mn.state.dot.tms.server.DetectorImpl;

/**
 * Canoga vehicle detection event
 *
 * @author Douglas Lau
 */
public class DetectionEvent {

	/** Conversion factor from milliseconds to seconds */
	static protected final int MS_PER_SECOND = 1000;

	/** Conversion factor from seconds to hours */
	static protected final int SEC_PER_HOUR = 3600;

	/** Minimum spacing (feet) between speed pair detectors */
	static protected final int MIN_SPACING = 6;

	/** Minimum duration needed to calculate speed */
	static protected final int MIN_DURATION = 20;

	/** Percent of duration to match vehicle on upstream sensor */
	static protected final float MATCH_DURATION = 0.10f;

	/** Minimum time (ms) elapsed (per foot) between speed events */
	static protected final int MIN_ELAPSED = 5;

	/** Minimum speed (mph) to estimate */
	static protected final int MIN_SPEED = 5;

	/** Maximum speed (mph) to estimate */
	static protected final int MAX_SPEED = 120;

	/** Maximum valid duration (120 seconds) */
	static protected final int MAX_DURATION = 120 * 1000;

	/** Maximum valid headway (12 hours) */
	static protected final int MAX_HEADWAY = 12 * 60 * 60 * 1000;

	/** Duration of vehicle (in milliseconds) */
	protected final int duration;

	/** Time since detector reset (in milliseconds) */
	protected final int start;

	/** Vehicle count (increments for each event) */
	protected final int count;

	/** Event state */
	protected final int state;

	/** Create a new detection event */
	protected DetectionEvent(int d, int s, int c, int st) {
		duration = d;
		start = s;
		count = c;
		state = st;
	}

	/** Test if two detection events are the same */
	public boolean equals(Object o) {
		if(o instanceof DetectionEvent) {
			DetectionEvent other = (DetectionEvent)o;
			return (duration == other.duration) &&
			       (start == other.start) &&
			       (count == other.count) &&
			       (state == other.state);
		} else
			return false;
	}

	/** Calculate a hash code for the detection event */
	public int hashCode() {
		return (duration << 16) ^ (start ^ count);
	}

	/** Get a string representation of the detection event */
	public String toString() {
		return "duration:" + duration + ",start:" + start +
			",count:" + count + ",state:" + state;
	}

	/** Check if the Canoga has been reset */
	public boolean isReset() {
		return (duration == 0) && (start == 0) && (count == 0);
	}

	/** Check for transmission errors in event data */
	public boolean hasErrors(DetectionEvent prev) {
		if(prev == null || isReset())
			return false;
		if(start == prev.start)
			return !equals(prev);
		if(count == prev.count)
			return !equals(prev);
		if(duration > MAX_DURATION)
			return true;
		else
			return hasDataErrors(prev);
	}

	/** Check for a specific type of data error which can escape detection
	 * by the simple XOR checksum.  It can happen if the first byte of the
	 * response is lost.  We test for it by shifting duration by one byte
	 * and comparing it to the previous duration.  If they match, it's
	 * likely that the error has occurred. */
	private boolean hasDataErrors(DetectionEvent prev) {
		return ((duration >> 8) == prev.duration) &&
		       !isHeadwayValid(prev);
	}

	/** Calculate the time elapsed since another event */
	private int calculateElapsed(DetectionEvent other) {
		long e = start - other.start;
		// Test for rollover (about once every 50 days)
		if(e < 0)
			e += (1 << 32);
		return (int)e;
	}

	/** Log the current event in the detection log */
	public void logEvent(Calendar stamp, DetectorImpl det,
		DetectionEvent prev, int speed)
	{
		int headway = 0;
		if(isHeadwayValid(prev)) {
			int missed = calculateMissed(prev);
			for(int i = 0; i < missed; i++)
				det.logVehicle(stamp, 0, 0, 0);
			// If no vehicles were missed, log headway
			if(missed == 0)
				headway = calculateElapsed(prev);
		} else {
			// There is a gap in vehicle event log
			det.logGap();
		}
		det.logVehicle(stamp, duration, headway, speed);
	}

	/** Test if headway from previous event is valid */
	private boolean isHeadwayValid(DetectionEvent prev) {
		if(prev == null || prev.isReset() || isReset())
			return false;
		else {
			int headway = calculateElapsed(prev);
			return headway > 0 && headway < MAX_HEADWAY;
		}
	}

	/** Calculate the number of missed vehicles */
	private int calculateMissed(DetectionEvent prev) {
		int n_vehicles = count - prev.count;
		if(n_vehicles < 0)
			n_vehicles += 256;
		return n_vehicles > 0 ? n_vehicles - 1 : 0;
	}

	/** Calculate speed (mph) from upstream event/spacing (ft).
	 * Upstream detection event must be from same detector card
	 * for time stamps to be comparable. */
	public int calculateSpeed(DetectionEvent upstream, float spacing) {
		if(spacing < MIN_SPACING)
			return 0;
		if(duration < MIN_DURATION)
			return 0;
		int d = Math.round(duration * MATCH_DURATION);
		if(upstream.duration < duration - d)
			return 0;
		if(upstream.duration > duration + d)
			return 0;
		float elapsed = calculateAvgElapsed(upstream);
		if(elapsed < MIN_ELAPSED * spacing)
			return 0;
		float fps = spacing * MS_PER_SECOND / elapsed;
		float mph = fps * SEC_PER_HOUR / FEET_PER_MILE;
		if(mph < MIN_SPEED || mph > MAX_SPEED)
			return 0;
		else
			return Math.round(mph);
	}

	/** Calculate the average elapsed time between events */
	protected float calculateAvgElapsed(DetectionEvent upstream) {
		int e1 = calculateElapsed(upstream);
		int d = duration - upstream.duration;
		int e2 = e1 + d;
		return (e1 + e2) / 2.0f;
	}
}
