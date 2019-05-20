/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2019  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.units.Interval;
import static us.mn.state.dot.tms.units.Interval.Units.MINUTES;
import static us.mn.state.dot.tms.units.Interval.Units.HOURS;
import static us.mn.state.dot.tms.units.Interval.Units.WEEKS;

/**
 * Road lane type enumeration.   The ordinal values correspond to the records
 * in the iris.lane_type look-up table.
 *
 * @author Douglas Lau
 */
public enum LaneType {

	/** Undefined lane type (0) */
	NONE(" ", "", new Interval(0), false, false),

	/** Mainline lane type (1) */
	MAINLINE("Mainline", "", new Interval(4, HOURS), true, false),

	/** Auxiliary lane type (2) */
	AUXILIARY("Auxiliary", "A", new Interval(24, HOURS), true, false),

	/** Collector/Distributor lane type (3) */
	CD_LANE("CD Lane", "CD", new Interval(4, HOURS), true, false),

	/** Reversible lane type (4) */
	REVERSIBLE("Reversible", "R", new Interval(72, HOURS), true, false),

	/** Merge lane type (5) */
	MERGE("Merge", "M", new Interval(12, HOURS), false, true),

	/** Queue detector lane type (6) */
	QUEUE("Queue", "Q", new Interval(12, HOURS), false, true),

	/** Exit lane type (7) */
	EXIT("Exit", "X", new Interval(8, HOURS), false, true),

	/** Meter bypass (HOV) lane type (8) */
	BYPASS("Bypass", "B", new Interval(72,HOURS), false, true),

	/** Passage lane type (9) */
	PASSAGE("Passage", "P", new Interval(12, HOURS), false, true),

	/** Velocity (mainline) lane type (10) */
	VELOCITY("Velocity", "V", new Interval(4, HOURS), true, false),

	/** Omnibus (ok, bus) lane type (11) */
	OMNIBUS("Omnibus", "O", new Interval(72, HOURS), false, true),

	/** Green count lane type (12) */
	GREEN("Green", "G", new Interval(72, HOURS), false, true),

	/** Wrong way (exit) lane type (13) */
	WRONG_WAY("Wrong Way", "Y", new Interval(8, HOURS), false, true),

	/** High-Occupancy-Vehicle (HOV) lane type (14) */
	HOV("HOV", "H", new Interval(8, HOURS), true, false),

	/** High Occupancy / Toll (HOT) lane type (15) */
	HOT("HOT", "HT", new Interval(72, HOURS), true, false),

	/** Dynamic shoulder lane type (16) */
	SHOULDER("Shoulder", "D", new Interval(72, HOURS), true, false),

	/** Parking lane type (17) */
	PARKING("Parking", "PK", new Interval(2, WEEKS), false, false);

	/** Create a new lane type */
	private LaneType(String d, String s, Interval nht, boolean ml,
		boolean rm)
	{
		description = d;
		suffix = s;
		no_hit_threshold = nht;
		is_mainline = ml;
		is_ramp = rm;
	}

	/** Lock-on threshold for mainline lane type sensors */
	static private final Interval LOCK_ON_THRESHOLD_MAINLINE =
		new Interval(2, MINUTES);

	/** Lock-on threshold for ramp lane type sensors */
	static private final Interval LOCK_ON_THRESHOLD_RAMP =
		new Interval(30, MINUTES);

	/** Lock-on threshold for other lane type sensors */
	static private final Interval LOCK_ON_THRESHOLD_FALLBACK =
		new Interval(2, WEEKS);

	/** Description */
	public final String description;

	/** Suffic (for detector labels) */
	public final String suffix;

	/** No hit threshold */
	public final Interval no_hit_threshold;

	/** Get the lock-on threshold */
	public final Interval getLockedOnThreshold() {
		if (is_mainline)
			return LOCK_ON_THRESHOLD_MAINLINE;
		else if (is_ramp)
			return LOCK_ON_THRESHOLD_RAMP;
		else
			return LOCK_ON_THRESHOLD_FALLBACK;
	}

	/** Mainline lane type */
	public final boolean is_mainline;

	/** Ramp lane type */
	public final boolean is_ramp;

	/** Get the string description */
	@Override
	public String toString() {
		return description;
	}

	/** Check if the lane type is a mainline lane */
	public boolean isMainline() {
		return is_mainline;
	}

	/** Check if the lane type is a station lane */
	public boolean isStation() {
		return this == MAINLINE;
	}

	/** Check if the lane type is a CD lane */
	public boolean isCD() {
		return this == CD_LANE;
	}

	/** Check if the lane type is a station or CD lane */
	public boolean isStationOrCD() {
		return isStation() || isCD();
	}

	/** Check if the lane type is a ramp lane */
	public boolean isRamp() {
		return is_ramp;
	}

	/** Check if the lane type is velocity */
	public boolean isVelocity() {
		return this == VELOCITY;
	}

	/** Get a lane type from an ordinal value */
	static public LaneType fromOrdinal(short o) {
		if (o >= 0 && o < values().length)
			return values()[o];
		else
			return NONE;
	}
}
