/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2014  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.server.DetectorImpl;
import us.mn.state.dot.tms.units.Distance;
import static us.mn.state.dot.tms.units.Distance.Units.FEET;
import us.mn.state.dot.tms.units.Interval;
import static us.mn.state.dot.tms.units.Interval.Units.HOURS;
import static us.mn.state.dot.tms.units.Interval.Units.MILLISECONDS;
import us.mn.state.dot.tms.units.Speed;
import static us.mn.state.dot.tms.units.Speed.Units.MPH;

/**
 * Canoga vehicle detection event
 *
 * @author Douglas Lau
 */
public class DetectionEvent {

	/** Minimum spacing between speed pair detectors */
	static private final Distance MIN_SPACING = new Distance(6, FEET);

	/** Minimum time (ms) elapsed (per foot) between speed events */
	static private final int MIN_ELAPSED_PER_FOOT = 5;

	/** Minimum speed (mph) to estimate */
	static private final int MIN_SPEED_MPH = 5;

	/** Maximum speed (mph) to estimate */
	static private final int MAX_SPEED_MPH = 120;

	/** Percent of duration to match vehicle on upstream sensor */
	static private final float MATCH_DURATION = 0.10f;

	/** Minimum duration needed to calculate speed (ms) */
	static private final int MIN_DURATION_MS = 20;

	/** Maximum valid duration (ms) */
	static private final long MAX_DURATION_MS = new Interval(120).ms();

	/** Maximum valid headway (ms) */
	static private final long MAX_HEADWAY_MS = new Interval(12, HOURS).ms();

	/** Calculate the minimum elapsed interval for a given detector spacing.
	 * @param spacing Spacing between a detector pair.
	 * @return Minimum elapsed time interval for a valid vehicle event. */
	static private Interval calculateMinElapsed(Distance spacing) {
		return new Interval(MIN_ELAPSED_PER_FOOT *
			spacing.convert(FEET).value, MILLISECONDS);
	}

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
	@Override
	public boolean equals(Object o) {
		if (o instanceof DetectionEvent) {
			DetectionEvent other = (DetectionEvent)o;
			return (duration == other.duration) &&
			       (start == other.start) &&
			       (count == other.count) &&
			       (state == other.state);
		} else
			return false;
	}

	/** Calculate a hash code for the detection event */
	@Override
	public int hashCode() {
		return (duration << 16) ^ (start ^ count);
	}

	/** Get a string representation of the detection event */
	@Override
	public String toString() {
		return "dur:" + duration + ",start:" + start +
			",count:" + count + ",state:" + state;
	}

	/** Check if the Canoga has been reset */
	public boolean isReset() {
		return (duration == 0) && (start == 0) && (count == 0);
	}

	/** Check for transmission errors in event data */
	public boolean hasErrors(DetectionEvent prev) {
		if (prev == null || isReset())
			return false;
		if (start == prev.start)
			return !equals(prev);
		if (count == prev.count)
			return !equals(prev);
		if (duration > MAX_DURATION_MS)
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
		if (e < 0)
			e += (1 << 32);
		return (int)e;
	}

	/** Log the current event in the detection log */
	public void logEvent(Calendar stamp, DetectorImpl det,
		DetectionEvent prev, int speed)
	{
		int headway = 0;
		if (isHeadwayValid(prev)) {
			int missed = calculateMissed(prev);
			for (int i = 0; i < missed; i++)
				det.logVehicle(stamp, 0, 0, 0);
			// If no vehicles were missed, log headway
			if (missed == 0)
				headway = calculateElapsed(prev);
		} else {
			// There is a gap in vehicle event log
			det.logGap();
		}
		det.logVehicle(stamp, duration, headway, speed);
	}

	/** Test if headway from previous event is valid */
	private boolean isHeadwayValid(DetectionEvent prev) {
		if (prev == null || prev.isReset() || isReset())
			return false;
		else {
			int headway = calculateElapsed(prev);
			return headway > 0 && headway < MAX_HEADWAY_MS;
		}
	}

	/** Calculate the number of missed vehicles */
	private int calculateMissed(DetectionEvent prev) {
		int n_vehicles = count - prev.count;
		if (n_vehicles < 0)
			n_vehicles += 256;
		return n_vehicles > 0 ? n_vehicles - 1 : 0;
	}

	/** Calculate speed (mph) from upstream event/spacing (ft).
	 * Upstream detection event must be from same detector card
	 * for time stamps to be comparable.
	 * @param upstream Upstream (pair) detection event.
	 * @param spacing Distance spacing of detector pair.
	 * @return Speed of vehicle, or 0 if speed is invalid. */
	public int calculateSpeed(DetectionEvent upstream, Distance spacing) {
		if (spacing.compareTo(MIN_SPACING) < 0)
			return 0;
		if (!isDurationValid(upstream))
			return 0;
		Interval min_elapsed = calculateMinElapsed(spacing);
		Interval elapsed = calculateAvgElapsed(upstream);
		if (elapsed.compareTo(min_elapsed) < 0)
			return 0;
		Speed spd = new Speed(spacing, elapsed);
		int mph = spd.round(MPH);
		if (mph < MIN_SPEED_MPH || mph > MAX_SPEED_MPH)
			return 0;
		else
			return mph;
	}

	/** Check if the event duration is valid.
	 * @param upstream Upstream (pair) detection event.
	 * @return true if event duration is valid. */
	private boolean isDurationValid(DetectionEvent upstream) {
		if (duration < MIN_DURATION_MS)
			return false;
		int d = Math.round(duration * MATCH_DURATION);
		if (upstream.duration < duration - d)
			return false;
		if (upstream.duration > duration + d)
			return false;
		return true;
	}

	/** Calculate the average elapsed time between events.  This averages
	 * the elapsed times between arrival and departure at each detector. */
	private Interval calculateAvgElapsed(DetectionEvent upstream) {
		int e1 = calculateElapsed(upstream);
		int d = duration - upstream.duration;
		int e2 = e1 + d;
		return new Interval((e1 + e2) / 2.0f, MILLISECONDS);
	}
}
