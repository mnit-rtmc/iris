/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2025  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.utils.I18N;

/**
 * An LCS indication is one of a set of possible states for a lane-use
 * control sign.  The ordinal values correspond to the records in the
 * iris.lcs_indication look-up table.
 *
 * @author Douglas Lau
 */
public enum LcsIndication {

	/** Unknown (0) */
	UNKNOWN(I18N.get("lcs.unknown")),

	/** Dark (no indication) (1) */
	DARK(I18N.get("lcs.dark")),

	/** Lane open (green arrow) (2) */
	LANE_OPEN(I18N.get("lcs.lane.open")),

	/** Use caution (flashing yellow arrow, not in MUTCD) (3) */
	USE_CAUTION(I18N.get("lcs.use.caution")),

	/** Lane closed ahead (Yellow X) (4) */
	LANE_CLOSED_AHEAD(I18N.get("lcs.lane.closed.ahead")),

	/** Lane closed (red X) (5) */
	LANE_CLOSED(I18N.get("lcs.lane.closed")),

	/** Merge right (not in MUTCD) (6) */
	MERGE_RIGHT(I18N.get("lcs.merge.right")),

	/** Merge left (not in MUTCD) (7) */
	MERGE_LEFT(I18N.get("lcs.merge.left")),

	/** Merge left or right (not in MUTCD) (8) */
	MERGE_BOTH(I18N.get("lcs.merge.both")),

	/** Must exit right (not in MUTCD) (9) */
	MUST_EXIT_RIGHT(I18N.get("lcs.must.exit.right")),

	/** Must exit left (not in MUTCD) (10) */
	MUST_EXIT_LEFT(I18N.get("lcs.must.exit.left")),

	/** HOV / HOT vehicles only (white diamond) (11) */
	HOV(I18N.get("lcs.hov.hot")),

	/** HOV / HOT begins (white diamond) (12) */
	HOV_BEGINS(I18N.get("lcs.hov.hot.begins")),

	/** Variable speed advisory (amber on black) (13) */
	VSA(I18N.get("lcs.vsa")),

	/** Variable speed limit (black on white) (14) */
	VSL(I18N.get("lcs.vsl")),

	/** Low visibility (small green arrow) (15) */
	LOW_VISIBILITY(I18N.get("lcs.low.visibility"));

	/** Create a new LCS indication */
	private LcsIndication(String d) {
		description = d;
	}

	/** Description of the LCS indication */
	public final String description;

	/** Get the string representation */
	@Override
	public String toString() {
		return description;
	}

	/** Get a LCS indication from an ordinal value */
	static public LcsIndication fromOrdinal(int o) {
		return (o >= 0 && o < values().length) ? values()[o] : UNKNOWN;
	}
}
