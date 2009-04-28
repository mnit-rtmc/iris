/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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
 * A lane-use indication is one of a set of possible states for a lane-use
 * control sign.
 *
 * @author Douglas Lau
 */
public enum LaneUseIndication {

	/** Dark (no indication) */
	DARK("Dark"),

	/** Lane open (green arrow) */
	LANE_OPEN("Lane open"),

	/** Use caution (flashing yellow arrow, not in MUTCD) */
	USE_CAUTION("Use caution"),

	/** Lane closed ahead (Yellow X) */
	LANE_CLOSED_AHEAD("Lane closed ahead"),

	/** Lane closed (red X) */
	LANE_CLOSED("Lane closed"),

	/** HOV / HOT vehicles only (white diamond) */
	HOV("HOV"),

	/** Merge left (not in MUTCD) */
	MERGE_LEFT("Merge Left"),

	/** Merge right (not in MUTCD) */
	MERGE_RIGHT("Merge Right"),

	/** Advisory variable speed limit (amber on black) */
	AVSL("Advisory variable speed limit"),

	/** Variable speed limit (black on white) */
	VSL("Variable Speed Limit");

	/** Create a new lane use indication */
	private LaneUseIndication(String d) {
		description = d;
	}

	/** Description of the lane use indication */
	public final String description;

	/** Get a lane use indication from an ordinal value */
	static public LaneUseIndication fromOrdinal(Integer o) {
		if(o != null && o > 0 && o < values().length)
			return values()[o];
		else
			return null;
	}

	/** Get an array of lock descriptions */
	static public String[] getDescriptions() {
		LinkedList<String> d = new LinkedList<String>();
		for(LaneUseIndication i: LaneUseIndication.values())
			d.add(i.description);
		return d.toArray(new String[0]);
	}
}
