/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2021  Minnesota Department of Transportation
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

/**
 * A periodic sample data value, such as vehicle counts, scans, speed, etc.
 *
 * @author Douglas Lau
 */
public class PeriodicSample implements Comparable<PeriodicSample> {

	/** Time stamp after end of sample period */
	public final long stamp;

	/** Sample period in seconds */
	public final int per_sec;

	/** Sample data value */
	public final int value;

	/** Create a new periodic sample.
	 * @param s Time stamp after end of sample period.
	 * @param p Sample period in seconds.
	 * @param v Sample data value. */
	public PeriodicSample(long s, int p, int v) {
		assert p > 0;
		stamp = s;
		per_sec = p;
		value = v;
	}

	/** Compare the sample to another */
	@Override
	public int compareTo(PeriodicSample other) {
		long ms = stamp + periodMillis();
		long oms = other.stamp + other.periodMillis();
		return (int) (ms - oms);
	}

	/** Get a time stamp at the start of the sampling period.
	 * This time stamp can be used to calculate the sample number. */
	public long start() {
		return end() - periodMillis();
	}

	/** Get a time stamp at the end of the sampling period. */
	public long end() {
		long per_ms = periodMillis();
		return stamp / per_ms * per_ms;
	}

	/** Get the sampling period in milliseconds */
	private long periodMillis() {
		return per_sec * 1000;
	}
}
