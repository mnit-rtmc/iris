/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2012  Minnesota Department of Transportation
 * Copyright (C) 2012  Iteris Inc.
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
package us.mn.state.dot.tms.server.comm.g4;

import us.mn.state.dot.tms.Constants;
import static us.mn.state.dot.tms.Constants.MISSING_DATA;

/**
 * All samples for one lane.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class LaneSample {

	/** Constants */
	static private final int MAX_PERCENT = 1024;

	/** Maximum number of lanes */
	static public final int MAX_NUM_LANES = 8;

	/** Lane number, 1 based */
	public final int lane_num;

	/** Volume: number of vehicles in sample period */
	public int volume = MISSING_DATA;

	/** Speed in either KPH or MPH */
	public int speed = MISSING_DATA;

	/** Occupancy, ranges 0 - 100 */
	public double occupancy = MISSING_DATA;

	/** Constructor */
	protected LaneSample(int ln) {
		assert ln >= 1 : "error ln < 1";
		assert ln <= MAX_NUM_LANES : "error ln > max_num_lanes";
		lane_num = ln;
	}

	/** Get scans, which is an integer 0 - 1800 */
	public int getScans() {
		if(occupancy == MISSING_DATA)
			return MISSING_DATA;
		double o = occupancy / 100d;
		return (int)Math.round(o * Constants.MAX_SCANS);
	}

	/** Get the speed adjusted for different unit systems */
	public int getSpeed(boolean si) {
		if(speed == MISSING_DATA)
			return MISSING_DATA;
		return (si ? kphToMph(speed) : speed);
	}

	/** Convert KPH to MPH. TODO: this should go into a utils class. */
	static private int kphToMph(int kph) {
		return (int)Math.round(.621371192 * (double)kph);
	}

	/** To string */
	public String toString() {
		return lane_num + ": v=" + volume + ", s=" + 
			speed + ", o=" + occupancy;
	}
}
