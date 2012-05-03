/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012  Minnesota Department of Transportation
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

import static us.mn.state.dot.tms.server.Constants.MISSING_DATA;

/**
 * An occupancy sample data value.  The value can range from 0 to 100%, in
 * 1/100th percent increments (using int values).
 *
 * @author Douglas Lau
 */
public class OccupancySample extends PeriodicSample {

	/** Maximum (100%) occupancy */
	static public final int MAX = 10000;

	/** Convert protocol-specific scans to occupancy (0 - 10000) */
	static private int calculateOccupancy(int n_scans, int max_scans) {
		assert(max_scans > 0);
		if(n_scans >= 0)
			return Math.round((float)n_scans / max_scans * MAX);
		else
			return MISSING_DATA;
	}

	/** Create a new occupancy sample.
	 * @param s Time stamp after end of sample period.
	 * @param p Sample period in seconds.
	 * @param n_scans Sample scan count.
	 * @param max_scans Maximum scan value (representing 100%). */
	public OccupancySample(long s, int p, int n_scans, int max_scans) {
		super(s, p, calculateOccupancy(n_scans, max_scans));
	}

	/** Float value for 60 Hz samples */
	static private final float HZ_60 = 60f;

	/** Get sample as 60 Hz scan count */
	public int as60HzScans() {
		if(value >= 0)
			return Math.round(value * period * HZ_60 / MAX);
		else
			return MISSING_DATA;
	}
}
