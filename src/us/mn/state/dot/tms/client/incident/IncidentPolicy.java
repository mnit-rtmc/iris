/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2014  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.incident;

import us.mn.state.dot.tms.Incident;
import us.mn.state.dot.tms.IncidentImpact;
import static us.mn.state.dot.tms.IncidentImpact.FREE_FLOWING;
import static us.mn.state.dot.tms.IncidentImpact.PARTIALLY_BLOCKED;
import static us.mn.state.dot.tms.IncidentImpact.BLOCKED;
import us.mn.state.dot.tms.LaneUseIndication;
import us.mn.state.dot.tms.LCS;
import us.mn.state.dot.tms.LCSArray;
import us.mn.state.dot.tms.LCSArrayHelper;
import us.mn.state.dot.tms.LCSHelper;
import static us.mn.state.dot.tms.R_Node.MAX_SHIFT;
import us.mn.state.dot.tms.units.Distance;
import static us.mn.state.dot.tms.units.Distance.Units.MILES;

/**
 * IncidentPolicy determines which LCS indications to propose for an incident.
 *
 * @author Douglas Lau
 */
public class IncidentPolicy {

	/** Short distance upstream of incident to deploy devices */
	static private final Distance DIST_SHORT = new Distance(0.5f, MILES);

	/** Medium distance upstream of incident to deploy devices */
	static private final Distance DIST_MEDIUM = new Distance(1.0f, MILES);

	/** Long distance upstream of incident to deploy devices */
	static private final Distance DIST_LONG = new Distance(1.5f, MILES);

	/** Incident in question */
	private final Incident incident;

	/** Create a new incident policy */
	public IncidentPolicy(Incident inc) {
		incident = inc;
	}

	/** Create proposed indications for an LCS array.
	 * @param up Distance upstream from incident (miles).
	 * @param lcs_array LCS array.
	 * @param shift Lane shift relative to incident.
	 * @param n_lanes Number of full lanes at incident.
	 * @return Array of LaneUseIndication ordinal values. */
	public Integer[] createIndications(Distance up, LCSArray lcs_array,
		int shift, int n_lanes)
	{
		int n_lcs = lcs_array.getIndicationsCurrent().length;
		LCS[] lcss = LCSArrayHelper.lookupLCSs(lcs_array);
		if(n_lcs != lcss.length)
			return new Integer[0];
		LaneUseIndication[] ind = createIndications(up, n_lcs, shift);
		Integer[] oin = new Integer[ind.length];
		for(int i = 0; i < ind.length; i++) {
			LaneUseIndication[] available =
				LCSHelper.lookupIndications(lcss[i]);
			oin[i] = assignIndication(ind[i], available).ordinal();
		}
		return oin;
	}

	/** Create proposed indications for an LCS array.
	 * @param up Distance upstream from incident (miles).
	 * @param n_lcs Number of lanes at LCS array.
	 * @param shift Lane shift relative to incident.
	 * @return Array of LaneUseIndication values. */
	LaneUseIndication[] createIndications(Distance up, int n_lcs,
		int shift)
	{
		LaneUseIndication[] ind = new LaneUseIndication[n_lcs];
		for(int i = 0; i < ind.length; i++) {
			int ln = shift + n_lcs - i;
			ind[i] = createIndication(up, ln);
		}
		return ind;
	}

	/** Create an LCS indication for one lane.
	 * @param up Distance upstream from incident.
	 * @param ln Lane number (0 for left shoulder, increasing to right).
	 * @return LaneUseIndication value. */
	private LaneUseIndication createIndication(Distance up, int ln) {
		if(isShoulder(ln))
			return LaneUseIndication.DARK;
		double m = up.m();
		if(m < 0)
			return LaneUseIndication.DARK;
		if(m < DIST_SHORT.m())
			return createIndicationShort(ln);
		if(m < DIST_MEDIUM.m())
			return createIndicationMedium(ln);
		if(m < DIST_LONG.m())
			return createIndicationLong(ln);
		else
			return LaneUseIndication.DARK;
	}

	/** Create an LCS indication for one lane within a short distance of
	 * the incident.
	 * @param ln Lane number (0 for left shoulder, increasing to right).
	 * @return LaneUseIndication value. */
	private LaneUseIndication createIndicationShort(int ln) {
		IncidentImpact ii = getImpact(ln);
		if(ii == BLOCKED)
			return LaneUseIndication.LANE_CLOSED;
		else if(ii == PARTIALLY_BLOCKED || isAdjacentLaneBlocked(ln))
			return LaneUseIndication.USE_CAUTION;
		else
			return LaneUseIndication.LANE_OPEN;
	}

	/** Get the impact at the specified lane.
	 * @param ln Lane number (0 for left shoulder, increasing to right).
	 * @return IcidentImpact for given lane. */
	private IncidentImpact getImpact(int ln) {
		String impact = incident.getImpact();
		// Don't look at the shoulder lanes
		if(ln <= 0 || ln >= impact.length() - 1)
			return FREE_FLOWING;
		else
			return IncidentImpact.fromChar(impact.charAt(ln));
	}

	/** Check if an adjacent lane is blocked.
	 * @param ln Lane number (0 for left shoulder, increasing to right).
	 * @return true if an adjacent lane is blocked, false otherwise. */
	private boolean isAdjacentLaneBlocked(int ln) {
		IncidentImpact left = getAdjacentImpact(ln - 1, FREE_FLOWING);
		IncidentImpact right = getAdjacentImpact(ln + 1, FREE_FLOWING);
		return left == BLOCKED || right == BLOCKED;
	}

	/** Get the impact at the specified adjacent lane.
	 * @param ln Lane number (0 for left shoulder, increasing to right).
	 * @param def Default impact for invalid lanes.
	 * @return IncidentImpact for given adjacent lane. */
	private IncidentImpact getAdjacentImpact(int ln, IncidentImpact def) {
		String impact = incident.getImpact();
		if(ln < 0 || ln >= impact.length())
			return def;
		else
			return IncidentImpact.fromChar(impact.charAt(ln));
	}

	/** Create an LCS indication for one lane at a medium distance to
	 * the incident.
	 * @param ln Lane number (0 for left shoulder, increasing to right).
	 * @return LaneUseIndication value. */
	private LaneUseIndication createIndicationMedium(int ln) {
		if(!isLaneBlocked(ln))
			return LaneUseIndication.LANE_OPEN;
		int n_left = unblockedLeftMainline(ln);
		int n_right = unblockedRightMainline(ln);
		if(n_left < n_right)
			return LaneUseIndication.MERGE_LEFT;
		else if(n_right < n_left)
			return LaneUseIndication.MERGE_RIGHT;
		else if(n_left < MAX_SHIFT)
			return LaneUseIndication.MERGE_BOTH;
		else
			return indication2MainlineBlocked(ln);
	}

	/** Check if a lane is blocked.
	 * @param ln Lane number (0 for left shoulder, increasing to right).
	 * @return true if lane is blocked, false otherwise. */
	private boolean isLaneBlocked(int ln) {
		String impact = incident.getImpact();
		if(ln > 0 && ln < impact.length()) {
			char c = impact.charAt(ln);
			IncidentImpact ii = IncidentImpact.fromChar(c);
			return ii == BLOCKED;
		} else
			return true;
	}

	/** Check if a lane is blocked or a shoulder lane.
	 * @param ln Lane number (0 for left shoulder, increasing to right).
	 * @return true if lane is blocked, false otherwise. */
	private boolean isLaneBlockedOrShoulder(int ln) {
		return isShoulder(ln) || isLaneBlocked(ln);
	}

	/** Check if a lane is a shoulder lane.
	 * @param ln Lane number (0 for left shoulder, increasing to right).
	 * @return true if lane is a shoulder, false otherwise. */
	private boolean isShoulder(int ln) {
		String impact = incident.getImpact();
		return ln <= 0 || ln >= impact.length() - 1;
	}

	/** Get number of lanes to the next unblocked mainline lanes left.
	 * @param ln Lane number (0 for left shoulder, increasing to right).
	 * @return Number of lanes to left until a lane is not blocked. */
	private int unblockedLeftMainline(int ln) {
		for(int i = 0; i < MAX_SHIFT; i++) {
			if(!isLaneBlockedOrShoulder(ln - i))
				return i;
		}
		return MAX_SHIFT;
	}

	/** Get number of lanes to the next unblocked mainline lane right.
	 * @param ln Lane number (0 for left shoulder, increasing to right).
	 * @return Number of lanes to right until a lane is not blocked. */
	private int unblockedRightMainline(int ln) {
		for(int i = 0; i < MAX_SHIFT; i++) {
			if(!isLaneBlockedOrShoulder(ln + i))
				return i;
		}
		return MAX_SHIFT;
	}

	/** Get the second indication when all mainline lanes are blocked.
	 * @param ln Lane number (0 for left shoulder, increasing to right).
	 * @return LaneUseIndication value. */
	private LaneUseIndication indication2MainlineBlocked(int ln) {
		int n_left = unblockedLeft(ln);
		int n_right = unblockedRight(ln);
		if(n_left < n_right)
			return LaneUseIndication.MERGE_LEFT;
		else if(n_right < n_left)
			return LaneUseIndication.MERGE_RIGHT;
		else if(n_left < MAX_SHIFT)
			return LaneUseIndication.MERGE_BOTH;
		else
			return LaneUseIndication.LANE_CLOSED;
	}

	/** Get number of lanes to the next unblocked lane left.
	 * @param ln Lane number (0 for left shoulder, increasing to right).
	 * @return Number of lanes to left until a lane is not blocked. */
	private int unblockedLeft(int ln) {
		for(int i = 0; i < MAX_SHIFT; i++) {
			if(!isLaneBlocked(ln - i))
				return i;
		}
		return MAX_SHIFT;
	}

	/** Get number of lanes to the next unblocked lane right.
	 * @param ln Lane number (0 for left shoulder, increasing to right).
	 * @return Number of lanes to right until a lane is not blocked. */
	private int unblockedRight(int ln) {
		for(int i = 0; i < MAX_SHIFT; i++) {
			if(!isLaneBlocked(ln + i))
				return i;
		}
		return MAX_SHIFT;
	}

	/** Create an LCS indication for one lane at a long distance to
	 * the incident.
	 * @param ln Lane number (0 for left shoulder, increasing to right).
	 * @return LaneUseIndication value. */
	private LaneUseIndication createIndicationLong(int ln) {
		if(isLaneBlocked(ln))
			return LaneUseIndication.LANE_CLOSED_AHEAD;
		else
			return LaneUseIndication.LANE_OPEN;
	}

	/** Assign a requested indication to an available indication.
	 * @param lui Requested lane use indication.
	 * @param available Array of available lane use indications.
	 * @return "Best" lane use indication in available array. */
	static private LaneUseIndication assignIndication(LaneUseIndication lui,
		LaneUseIndication[] available)
	{
		for(LaneUseIndication a: available) {
			if(lui == a)
				return lui;
		}
		LaneUseIndication alt = altIndication(lui);
		for(LaneUseIndication a: available) {
			if(alt == a)
				return alt;
		}
		return LaneUseIndication.DARK;
	}

	/** Get alternate indication for "dumb" LCS devices. */
	static private LaneUseIndication altIndication(LaneUseIndication lui) {
		switch(lui) {
		case LOW_VISIBILITY:
		case LANE_CLOSED_AHEAD:
		case MERGE_RIGHT:
		case MERGE_LEFT:
		case MERGE_BOTH:
			return LaneUseIndication.USE_CAUTION;
		case MUST_EXIT_RIGHT:
		case MUST_EXIT_LEFT:
			return LaneUseIndication.LANE_CLOSED;
		default:
			return LaneUseIndication.LANE_OPEN;
		}
	}
}
