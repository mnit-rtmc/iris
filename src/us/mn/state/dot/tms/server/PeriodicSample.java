/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2012  Minnesota Department of Transportation
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
 * A periodic sample data value, such as volume, scans, speed, etc.
 *
 * @author Douglas Lau
 */
public class PeriodicSample implements Comparable<PeriodicSample> {

	/** Time stamp after end of sample period */
	public final long stamp;

	/** Sample period in seconds */
	public final int period;

	/** Sample data value */
	public final int value;

	/** Create a new periodic sample.
	 * @param s Time stamp after end of sample period.
	 * @param p Sample period in seconds.
	 * @param v Sample data value. */
	public PeriodicSample(long s, int p, int v) {
		assert p > 0;
		stamp = s;
		period = p;
		value = v;
	}

	/** Compare the sample to another */
	public int compareTo(PeriodicSample other) {
		return (int)((stamp + period) - (other.stamp + other.period));
	}

	/** Get a time stamp at the start of the sampling period.
	 * This time stamp can be used to calculate the sample number. */
	public long start() {
		return end() - periodMillis();
	}

	/** Get a time stamp at the end of the sampling period. */
	public long end() {
		int pms = periodMillis();
		return stamp / pms * pms;
	}

	/** Get the sampling period in milliseconds */
	private int periodMillis() {
		return period * 1000;
	}
}
