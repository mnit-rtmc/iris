/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2009  Minnesota Department of Transportation
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

import java.util.LinkedList;

/**
 * Road lane type enumeration.
 *
 * @author Douglas Lau
 */
public enum LaneType {

	/** Undefined lane type (0) */
	NONE(" ", "", 0, 0),

	/** Mainline lane type (1) */
	MAINLINE("Mainline", "", 4 * Interval.HOUR, 3 * Interval.MINUTE),

	/** Auxiliary lane type (2) */
	AUXILIARY("Auxiliary", "A", 24 * Interval.HOUR, 3 * Interval.MINUTE),

	/** Collector/Distributor lane type (3) */
	CD_LANE("CD Lane", "CD", 4 * Interval.HOUR, 3 * Interval.MINUTE),

	/** Reversible lane type (4) */
	REVERSIBLE("Reversible", "R", 72 * Interval.HOUR, 3 * Interval.MINUTE),

	/** Merge lane type (5) */
	MERGE("Merge", "M", 12 * Interval.HOUR, 20 * Interval.MINUTE),

	/** Queue detector lane type (6) */
	QUEUE("Queue", "Q", 12 * Interval.HOUR, 30 * Interval.MINUTE),

	/** Exit lane type (7) */
	EXIT("Exit", "X", 8 * Interval.HOUR, 20 * Interval.MINUTE),

	/** Meter bypass (HOV) lane type (8) */
	BYPASS("Bypass", "B", 72 * Interval.HOUR, 20 * Interval.MINUTE),

	/** Passage lane type (9) */
	PASSAGE("Passage", "P", 12 * Interval.HOUR, 20 * Interval.MINUTE),

	/** Velocity (mainline) lane type (10) */
	VELOCITY("Velocity", "V", 4 * Interval.HOUR, 3 * Interval.MINUTE),

	/** Omnibus (ok, bus) lane type (11) */
	OMNIBUS("Omnibus", "O", 72 * Interval.HOUR, 20 * Interval.MINUTE),

	/** Green count lane type (12) */
	GREEN("Green", "G", 72 * Interval.HOUR, 20 * Interval.MINUTE),

	/** Wrong way (exit) lane type (13) */
	WRONG_WAY("Wrong Way", "Y", 8 * Interval.HOUR, 20 * Interval.MINUTE),

	/** High-Occupancy-Vehicle (HOV) lane type (14) */
	HOV("HOV", "H", 8 * Interval.HOUR, 3 * Interval.MINUTE),

	/** High Occupancy / Toll (HOT) lane type (15) */
	HOT("HOT", "HT", 8 * Interval.HOUR, 3 * Interval.MINUTE),

	/** Dynamic shoulder lane type (16) */
	SHOULDER("Shoulder", "D", 72 * Interval.HOUR, 3 * Interval.MINUTE);

	/** Create a new lane type */
	private LaneType(String d, String s, int nht, int lot) {
		description = d;
		suffix = s;
		no_hit_threshold = nht;
		lock_on_threshold = lot;
	}

	/** Description */
	public final String description;

	/** Suffic (for detector labels) */
	public final String suffix;

	/** No hit threshold (seconds) */
	public final int no_hit_threshold;

	/** Lock on threshold (seconds) */
	public final int lock_on_threshold;

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
		for(LaneType lt: LaneType.values()) {
			if(lt.ordinal() == o)
				return lt;
		}
		return NONE;
	}

	/** Get an array of lane type descriptions */
	static public String[] getDescriptions() {
		LinkedList<String> d = new LinkedList<String>();
		for(LaneType lt: LaneType.values())
			d.add(lt.description);
		return d.toArray(new String[0]);
	}
}
