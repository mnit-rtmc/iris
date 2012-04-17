/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2012  Minnesota Department of Transportation
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
import static us.mn.state.dot.tms.Interval.hour;
import static us.mn.state.dot.tms.Interval.minute;

/**
 * Road lane type enumeration.   The ordinal values correspond to the records
 * in the iris.lane_type look-up table.
 *
 * @author Douglas Lau
 */
public enum LaneType {

	/** Undefined lane type (0) */
	NONE(" ", "", minute(0), minute(0)),

	/** Mainline lane type (1) */
	MAINLINE("Mainline", "", hour(4), minute(3)),

	/** Auxiliary lane type (2) */
	AUXILIARY("Auxiliary", "A", hour(24), minute(3)),

	/** Collector/Distributor lane type (3) */
	CD_LANE("CD Lane", "CD", hour(4), minute(3)),

	/** Reversible lane type (4) */
	REVERSIBLE("Reversible", "R", hour(72), minute(3)),

	/** Merge lane type (5) */
	MERGE("Merge", "M", hour(12), minute(20)),

	/** Queue detector lane type (6) */
	QUEUE("Queue", "Q", hour(12), minute(30)),

	/** Exit lane type (7) */
	EXIT("Exit", "X", hour(8), minute(20)),

	/** Meter bypass (HOV) lane type (8) */
	BYPASS("Bypass", "B", hour(72), minute(20)),

	/** Passage lane type (9) */
	PASSAGE("Passage", "P", hour(12), minute(20)),

	/** Velocity (mainline) lane type (10) */
	VELOCITY("Velocity", "V", hour(4), minute(3)),

	/** Omnibus (ok, bus) lane type (11) */
	OMNIBUS("Omnibus", "O", hour(72), minute(20)),

	/** Green count lane type (12) */
	GREEN("Green", "G", hour(72), minute(20)),

	/** Wrong way (exit) lane type (13) */
	WRONG_WAY("Wrong Way", "Y", hour(8), minute(20)),

	/** High-Occupancy-Vehicle (HOV) lane type (14) */
	HOV("HOV", "H", hour(8), minute(3)),

	/** High Occupancy / Toll (HOT) lane type (15) */
	HOT("HOT", "HT", hour(8), minute(3)),

	/** Dynamic shoulder lane type (16) */
	SHOULDER("Shoulder", "D", hour(72), minute(3));

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

	/** Get the string description of the lane type */
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
