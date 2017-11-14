/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2016  Minnesota Department of Transportation
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

/**
 * Road lane type enumeration.   The ordinal values correspond to the records
 * in the iris.lane_type look-up table.
 *
 * @author Douglas Lau
 */
public enum LaneType {

	/** Undefined lane type (0) */
	NONE(" ", "", new Interval(0), new Interval(0)),

	/** Mainline lane type (1) */
	MAINLINE("Mainline", "", new Interval(12,HOURS),new Interval(3,MINUTES)),

	/** Auxiliary lane type (2) */
	AUXILIARY("Auxiliary", "A", new Interval(24, HOURS), new Interval(3,
		MINUTES)),

	/** Collector/Distributor lane type (3) */
	CD_LANE("CD Lane", "CD", new Interval(12, HOURS), new Interval(3,
		MINUTES)),

	/** Reversible lane type (4) */
	REVERSIBLE("Reversible", "R", new Interval(72, HOURS), new Interval(3,
		MINUTES)),

	/** Merge lane type (5) */
	MERGE("Merge", "M", new Interval(12, HOURS), new Interval(20, MINUTES)),

	/** Queue detector lane type (6) */
	QUEUE("Queue", "Q", new Interval(12, HOURS), new Interval(30, MINUTES)),

	/** Exit lane type (7) */
	EXIT("Exit", "X", new Interval(12, HOURS), new Interval(20, MINUTES)),

	/** Meter bypass (HOV) lane type (8) */
	BYPASS("Bypass", "B", new Interval(72,HOURS), new Interval(20,MINUTES)),

	/** Passage lane type (9) */
	PASSAGE("Passage", "P", new Interval(12, HOURS), new Interval(20,
		MINUTES)),

	/** Velocity (mainline) lane type (10) */
	VELOCITY("Velocity", "V", new Interval(12, HOURS), new Interval(3,
		MINUTES)),

	/** Omnibus (ok, bus) lane type (11) */
	OMNIBUS("Omnibus", "O", new Interval(72, HOURS), new Interval(20,
		MINUTES)),

	/** Green count lane type (12) */
	GREEN("Green", "G", new Interval(72, HOURS), new Interval(20, MINUTES)),

	/** Wrong way (exit) lane type (13) */
	WRONG_WAY("Wrong Way", "Y", new Interval(12, HOURS), new Interval(20,
		MINUTES)),

	/** High-Occupancy-Vehicle (HOV) lane type (14) */
	HOV("HOV", "H", new Interval(12, HOURS), new Interval(3, MINUTES)),

	/** High Occupancy / Toll (HOT) lane type (15) */
	HOT("HOT", "HT", new Interval(72, HOURS), new Interval(3, MINUTES)),

	/** Dynamic shoulder lane type (16) */
	SHOULDER("Shoulder", "D", new Interval(72, HOURS), new Interval(3,
		MINUTES));

	/** Create a new lane type */
	private LaneType(String d, String s, Interval nht, Interval lot) {
		description = d;
		suffix = s;
		no_hit_threshold = nht;
		lock_on_threshold = lot;
	}

	/** Description */
	public final String description;

	/** Suffic (for detector labels) */
	public final String suffix;

	/** No hit threshold */
	public final Interval no_hit_threshold;

	/** Lock on threshold */
	public final Interval lock_on_threshold;

	/** Get the string description */
	@Override
	public String toString() {
		return description;
	}

	/** Check if the lane type is a mainline lane */
	public boolean isMainline() {
		return this == MAINLINE ||
		       this == AUXILIARY ||
		       this == CD_LANE ||
		       this == REVERSIBLE ||
		       this == VELOCITY ||
		       this == HOV ||
		       this == HOT ||
		       this == SHOULDER;
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
		return this == MERGE ||
		       this == QUEUE ||
		       this == EXIT ||
		       this == BYPASS ||
		       this == PASSAGE ||
		       this == OMNIBUS;
	}

	/** Check if the lane type is an on-ramp */
	public boolean isOnRamp() {
		return this == MERGE ||
		       this == QUEUE ||
		       this == BYPASS ||
		       this == PASSAGE ||
		       this == OMNIBUS ||
		       this == GREEN;
	}

	/** Check if the lane type is an off-ramp */
	public boolean isOffRamp() {
		return this == EXIT;
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
