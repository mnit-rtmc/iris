/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2025  Minnesota Department of Transportation
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
import static us.mn.state.dot.tms.units.Interval.Units.SECONDS;
import static us.mn.state.dot.tms.units.Interval.Units.MINUTES;
import static us.mn.state.dot.tms.units.Interval.Units.HOURS;
import static us.mn.state.dot.tms.units.Interval.Units.WEEKS;

/**
 * Road lane enumeration.
 *
 * @author Douglas Lau
 */
public enum LaneCode {

	/** Mainline lane code */
	MAINLINE("", "Mainline", new Interval(4, HOURS), true, false),

	/** Auxiliary lane code */
	AUXILIARY("A", "Auxiliary", new Interval(24, HOURS), true, false),

	/** Meter bypass (HOV) lane code */
	BYPASS("B", "Bypass", new Interval(72,HOURS), false, true),

	/** Collector/Distributor lane code */
	CD_LANE("C", "CD Lane", new Interval(4, HOURS), true, false),

	/** Dynamic shoulder lane code */
	SHOULDER("D", "Shoulder", new Interval(72, HOURS), true, false),

	/** Green count lane code */
	GREEN("G", "Green", new Interval(72, HOURS), false, true),

	/** High-Occupancy-Vehicle (HOV) lane code */
	HOV("H", "HOV", new Interval(8, HOURS), true, false),

	/** Parking lane code */
	PARKING("K", "Parking", new Interval(2, WEEKS), false, false),

	/** Merge lane code */
	MERGE("M", "Merge", new Interval(12, HOURS), false, true),

	/** Omnibus (ok, bus) lane code */
	OMNIBUS("O", "Omnibus", new Interval(72, HOURS), false, true),

	/** Passage lane code */
	PASSAGE("P", "Passage", new Interval(12, HOURS), false, true),

	/** Queue detector lane code */
	QUEUE("Q", "Queue", new Interval(12, HOURS), false, true),

	/** Reversible lane code */
	REVERSIBLE("R", "Reversible", new Interval(72, HOURS), true, false),

	/** High Occupancy / Toll (HOT) lane code */
	HOT("T", "HOT", new Interval(72, HOURS), true, false),

	/** Velocity (mainline) lane code */
	VELOCITY("V", "Velocity", new Interval(4, HOURS), true, false),

	/** Exit lane code */
	EXIT("X", "Exit", new Interval(8, HOURS), false, true),

	/** Wrong way (exit) lane code */
	WRONG_WAY("Y", "Wrong Way", new Interval(8, HOURS), false, true);

	/** Create a new lane code */
	private LaneCode(String c, String d, Interval nht, boolean ml,
		boolean rm)
	{
		lcode = c;
		description = d;
		no_hit_threshold = nht;
		is_mainline = ml;
		is_ramp = rm;
	}

	/** Lock-on threshold for mainline lane sensors */
	static private final Interval LOCK_ON_THRESHOLD_MAINLINE =
		new Interval(2, MINUTES);

	/** Lock-on threshold for ramp lane sensors */
	static private final Interval LOCK_ON_THRESHOLD_RAMP =
		new Interval(30, MINUTES);

	/** Lock-on threshold for other lane sensors */
	static private final Interval LOCK_ON_THRESHOLD_FALLBACK =
		new Interval(2, WEEKS);

	/** Scan "no change" threshold for mainline / ramp sensors */
	static private final Interval NO_CHANGE_THRESHOLD =
		new Interval(24, HOURS);

	/** Scan "no change" threshold for other sensors */
	static private final Interval NO_CHANGE_THRESHOLD_FALLBACK =
		new Interval(2, WEEKS);

	/** Scan "occ spike" threshold for regular sensors */
	static private final Interval OCC_SPIKE_THRESHOLD =
		new Interval(29, SECONDS);

	/** Scan "occ spike" threshold for parking sensors */
	static private final Interval OCC_SPIKE_THRESHOLD_PARKING =
		new Interval(2, WEEKS);

	/** Description */
	public final String description;

	/** Lane code (for detector labels) */
	public final String lcode;

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

	/** Get the no-change threshold */
	public final Interval getNoChangeThreshold() {
		if (is_mainline || is_ramp)
			return NO_CHANGE_THRESHOLD;
		else
			return NO_CHANGE_THRESHOLD_FALLBACK;
	}

	/** Get the occ spike threshold */
	public final Interval getOccSpikeThreshold() {
		if (this != PARKING)
			return OCC_SPIKE_THRESHOLD;
		else
			return OCC_SPIKE_THRESHOLD_PARKING;
	}

	/** Mainline lane */
	public final boolean is_mainline;

	/** Ramp lane */
	public final boolean is_ramp;

	/** Get the string description */
	@Override
	public String toString() {
		return description;
	}

	/** Check if the lane is a mainline lane */
	public boolean isMainline() {
		return is_mainline;
	}

	/** Check if the lane is a station lane */
	public boolean isStation() {
		return this == MAINLINE;
	}

	/** Check if the lane is a CD lane */
	public boolean isCD() {
		return this == CD_LANE;
	}

	/** Check if the lane is a station or CD lane */
	public boolean isStationOrCD() {
		return isStation() || isCD();
	}

	/** Check if the lane is a ramp lane */
	public boolean isRamp() {
		return is_ramp;
	}

	/** Check if the lane is velocity */
	public boolean isVelocity() {
		return this == VELOCITY;
	}

	/** Get a lane code from an lcode */
	static public LaneCode fromCode(String lc) {
		for (LaneCode lcd: values()) {
			if (lcd.lcode.equals(lc))
				return lcd;
		}	
		return LaneCode.MAINLINE;
	}
}
