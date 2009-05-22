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
import us.mn.state.dot.tms.utils.I18N;

/**
 * A lane-use indication is one of a set of possible states for a lane-use
 * control sign.
 *
 * @author Douglas Lau
 */
public enum LaneUseIndication {

	/** Dark (no indication) */
	DARK(I18N.get("lane.use.dark")),

	/** Lane open (green arrow) */
	LANE_OPEN(I18N.get("lane.use.lane.open")),

	/** Use caution (flashing yellow arrow, not in MUTCD) */
	USE_CAUTION(I18N.get("lane.use.use.caution")),

	/** Lane closed ahead (Yellow X) */
	LANE_CLOSED_AHEAD(I18N.get("lane.use.lane.closed.ahead")),

	/** Lane closed (red X) */
	LANE_CLOSED(I18N.get("lane.use.lane.closed")),

	/** HOV / HOT vehicles only (white diamond) */
	HOV(I18N.get("lane.use.hov.hot")),

	/** Merge right (not in MUTCD) */
	MERGE_RIGHT(I18N.get("lane.use.merge.right")),

	/** Merge left (not in MUTCD) */
	MERGE_LEFT(I18N.get("lane.use.merge.left")),

	/** Merge left or right (not in MUTCD) */
	MERGE_BOTH(I18N.get("lane.use.merge.both")),

	/** Must exit right (not in MUTCD) */
	MUST_EXIT_RIGHT(I18N.get("lane.use.must.exit.right")),

	/** Must exit left (not in MUTCD) */
	MUST_EXIT_LEFT(I18N.get("lane.use.must.exit.left")),

	/** Advisory variable speed limit (amber on black) */
	AVSL(I18N.get("lane.use.avsl")),

	/** Variable speed limit (black on white) */
	VSL(I18N.get("lane.use.vsl"));

	/** Create a new lane use indication */
	private LaneUseIndication(String d) {
		description = d;
	}

	/** Description of the lane use indication */
	public final String description;

	/** Get a lane use indication from an ordinal value */
	static public LaneUseIndication fromOrdinal(Integer o) {
		if(o != null && o >= 0 && o < values().length)
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
