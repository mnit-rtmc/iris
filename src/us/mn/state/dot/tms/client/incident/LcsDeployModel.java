/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2016  Minnesota Department of Transportation
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
import static us.mn.state.dot.tms.IncidentImpact.*;
import us.mn.state.dot.tms.LaneConfiguration;
import us.mn.state.dot.tms.LaneUseIndication;
import us.mn.state.dot.tms.LCS;
import us.mn.state.dot.tms.LCSArray;
import us.mn.state.dot.tms.LCSArrayHelper;
import us.mn.state.dot.tms.LCSHelper;
import static us.mn.state.dot.tms.R_Node.MAX_SHIFT;
import us.mn.state.dot.tms.units.Distance;
import static us.mn.state.dot.tms.units.Distance.Units.MILES;

/**
 * LcsDeployModel determines which LCS indications to propose for an incident.
 *
 * @author Douglas Lau
 */
public class LcsDeployModel {

	/** Short distance upstream of incident to deploy devices */
	static private final Distance DIST_SHORT = new Distance(0.5f, MILES);

	/** Medium distance upstream of incident to deploy devices */
	static private final Distance DIST_MEDIUM = new Distance(1.0f, MILES);

	/** Long distance upstream of incident to deploy devices */
	static private final Distance DIST_LONG = new Distance(1.5f, MILES);

	/** Assign a requested indication to an available indication.
	 * @param lui Requested lane use indication.
	 * @param avail Array of available lane use indications.
	 * @return "Best" lane use indication in available array. */
	static private LaneUseIndication assignIndication(LaneUseIndication lui,
		LaneUseIndication[] avail)
	{
		for (LaneUseIndication a: avail) {
			if (lui == a)
				return lui;
		}
		LaneUseIndication alt = altIndication(lui);
		for (LaneUseIndication a: avail) {
			if (alt == a)
				return alt;
		}
		return LaneUseIndication.DARK;
	}

	/** Get alternate indication for "dumb" LCS devices. */
	static private LaneUseIndication altIndication(LaneUseIndication lui) {
		switch (lui) {
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

	/** Check if a set of indications should be deployed */
	static private boolean shouldDeploy(Integer[] ind) {
		for (int i: ind) {
			LaneUseIndication li = LaneUseIndication.fromOrdinal(i);
			switch (LaneUseIndication.fromOrdinal(i)) {
			case DARK:
			case LANE_OPEN:
				continue;
			default:
				return true;
			}
		}
		return false;
	}

	/** Incident in question */
	private final Incident incident;

	/** Lane configuration at incident */
	private final LaneConfiguration config;

	/** Create a new LCS deploy model.
	 * @param inc Incident for deployment.
	 * @param conf Lane configuration at incident location. */
	public LcsDeployModel(Incident inc, LaneConfiguration conf) {
		incident = inc;
		config = conf;
	}

	/** Create proposed indications for an LCS array.
	 * @param cfg Lane configuration at LCS array location.
	 * @param up Distance upstream from incident (miles).
	 * @param lcs_array LCS array.
	 * @return Array of LaneUseIndication ordinal values, or null. */
	public Integer[] createIndications(LaneConfiguration cfg, Distance up,
		LCSArray lcs_array)
	{
		int n_lcs = lcs_array.getIndicationsCurrent().length;
		LCS[] lcss = LCSArrayHelper.lookupLCSs(lcs_array);
		if (n_lcs != lcss.length)
			return null;
		LaneUseIndication[] ind = createIndications(cfg, up, n_lcs,
			lcs_array.getShift());
		Integer[] oin = new Integer[ind.length];
		for (int i = 0; i < ind.length; i++) {
			LaneUseIndication[] avail =
				LCSHelper.lookupIndications(lcss[i]);
			oin[i] = assignIndication(ind[i], avail).ordinal();
		}
		return shouldDeploy(oin) ? oin : null;
	}

	/** Create proposed indications for an LCS array.
	 * @param cfg Lane configuration at LCS array.
	 * @param up Distance upstream from incident (miles).
	 * @param n_lcs Number of lanes at LCS array.
	 * @param lcs_shift Lane shift at LCS array.
	 * @return Array of LaneUseIndication values. */
	LaneUseIndication[] createIndications(LaneConfiguration cfg,
		Distance up, int n_lcs, int lcs_shift)
	{
		LaneUseIndication[] ind = new LaneUseIndication[n_lcs];
		for (int i = 0; i < ind.length; i++) {
			int shift = lcs_shift + n_lcs - i - 1;
			ind[i] = createIndication(cfg, up, shift);
		}
		return ind;
	}

	/** Create an LCS indication for one lane.
	 * @param cfg Lane configuration at LCS array.
	 * @param up Distance upstream from incident.
	 * @param shift Lane shift from left origin.
	 * @return LaneUseIndication value. */
	private LaneUseIndication createIndication(LaneConfiguration cfg,
		Distance up, int shift)
	{
		if (isShoulder(cfg, shift))
			return LaneUseIndication.DARK;
		if (isShoulder(config, shift))
			return LaneUseIndication.LANE_OPEN;
		double m = up.m();
		if (m < 0)
			return LaneUseIndication.DARK;
		if (m < DIST_SHORT.m())
			return createIndicationShort(cfg, shift);
		if (m < DIST_MEDIUM.m())
			return createIndicationMedium(cfg, shift);
		if (m < DIST_LONG.m())
			return createIndicationLong(shift);
		else
			return LaneUseIndication.DARK;
	}

	/** Check if a lane is a shoulder lane.
	 * @param cfg Lane configuration at LCS array.
	 * @param shift Lane shift from left origin.
	 * @return true if lane is a shoulder, false otherwise. */
	private boolean isShoulder(LaneConfiguration cfg, int shift) {
		return (shift < cfg.leftShift || shift >= cfg.rightShift);
	}

	/** Create an LCS indication for one lane within a short distance of
	 * the incident.
	 * @param cfg Lane configuration at LCS array.
	 * @param shift Lane shift from left origin.
	 * @return LaneUseIndication value. */
	private LaneUseIndication createIndicationShort(LaneConfiguration cfg,
		int shift)
	{
		IncidentImpact ii = getImpact(shift);
		if (ii == BLOCKED)
			return LaneUseIndication.LANE_CLOSED;
		else if (ii == PARTIALLY_BLOCKED ||isAdjacentLaneBlocked(shift))
			return LaneUseIndication.USE_CAUTION;
		else
			return LaneUseIndication.LANE_OPEN;
	}

	/** Get the impact at the specified lane.
	 * @param shift Lane shift from left origin.
	 * @return IncidentImpact for given lane, or null. */
	private IncidentImpact getImpact(int shift) {
		// FIXME: check lane continuity to incident
		String impact = incident.getImpact();
		int ln = shift - config.leftShift + 1;
		if (ln >= 0 && ln < impact.length())
			return IncidentImpact.fromChar(impact.charAt(ln));
		else
			return null;
	}

	/** Check if an adjacent lane is blocked.
	 * @param shift Lane shift from left origin.
	 * @return true if an adjacent lane is blocked, false otherwise. */
	private boolean isAdjacentLaneBlocked(int shift) {
		IncidentImpact left = getImpact(shift - 1);
		IncidentImpact right = getImpact(shift + 1);
		return left == BLOCKED || right == BLOCKED;
	}

	/** Create an LCS indication for one lane at a medium distance to
	 * the incident.
	 * @param cfg Lane configuration at LCS array.
	 * @param shift Lane shift from left origin.
	 * @return LaneUseIndication value. */
	private LaneUseIndication createIndicationMedium(LaneConfiguration cfg,
		int shift)
	{
		if (getImpact(shift) != BLOCKED)
			return LaneUseIndication.LANE_OPEN;
		int n_left = unblockedLeftMainline(cfg, shift);
		int n_right = unblockedRightMainline(cfg, shift);
		if (n_left < n_right)
			return LaneUseIndication.MERGE_LEFT;
		else if (n_right < n_left)
			return LaneUseIndication.MERGE_RIGHT;
		else if (n_left < MAX_SHIFT)
			return LaneUseIndication.MERGE_BOTH;
		else
			return createIndicationMediumBlocked(cfg, shift);
	}

	/** Check if a lane is open.
	 * @param cfg Lane configuration at LCS array.
	 * @param shift Lane shift from left origin.
	 * @return true if lane is open, false otherwise. */
	private boolean isLaneOpen(LaneConfiguration cfg, int shift) {
		IncidentImpact ii = getImpact(shift);
		return (ii == FREE_FLOWING) || (ii == PARTIALLY_BLOCKED);
	}

	/** Check if a lane is open and not a shoulder.
	 * @param cfg Lane configuration at LCS array.
	 * @param shift Lane shift from left origin.
	 * @return true if lane is open, false otherwise. */
	private boolean isMainlineLaneOpen(LaneConfiguration cfg, int shift) {
		return isLaneOpen(cfg, shift) && !isShoulder(cfg, shift);
	}

	/** Get number of lanes to the next unblocked mainline lane left.
	 * @param cfg Lane configuration at LCS array.
	 * @param shift Lane shift from left origin.
	 * @return Number of lanes to left until a lane is not blocked. */
	private int unblockedLeftMainline(LaneConfiguration cfg, int shift) {
		for (int i = 1; i <= shift; i++) {
			if (isMainlineLaneOpen(cfg, shift - i))
				return i;
		}
		return MAX_SHIFT;
	}

	/** Get number of lanes to the next unblocked mainline lane right.
	 * @param cfg Lane configuration at LCS array.
	 * @param shift Lane shift from left origin.
	 * @return Number of lanes to right until a lane is not blocked. */
	private int unblockedRightMainline(LaneConfiguration cfg, int shift) {
		for (int i = 1; i < MAX_SHIFT; i++) {
			if (isMainlineLaneOpen(cfg, shift + i))
				return i;
		}
		return MAX_SHIFT;
	}

	/** Get a medium distance indication when all lanes are blocked.
	 * @param cfg Lane configuration at LCS array.
	 * @param shift Lane shift from left origin.
	 * @return LaneUseIndication value. */
	private LaneUseIndication createIndicationMediumBlocked(
		LaneConfiguration cfg, int shift)
	{
		int n_left = unblockedLeftShoulder(cfg, shift);
		int n_right = unblockedRightShoulder(cfg, shift);
		if (n_left < n_right)
			return LaneUseIndication.MERGE_LEFT;
		else if (n_right < n_left)
			return LaneUseIndication.MERGE_RIGHT;
		else if (n_left < MAX_SHIFT)
			return LaneUseIndication.MERGE_BOTH;
		else
			return LaneUseIndication.LANE_CLOSED;
	}

	/** Get number of lanes to the unblocked left shoulder.
	 * @param cfg Lane configuration at LCS array.
	 * @param shift Lane shift from left origin.
	 * @return Number of lanes to left shoulder, or MAX_SHIFT. */
	private int unblockedLeftShoulder(LaneConfiguration cfg, int shift) {
		int shoulder = cfg.leftShift - 1;
		if (isLaneOpen(cfg, shoulder))
			return shift - shoulder;
		else
			return MAX_SHIFT;
	}

	/** Get number of lanes to the unblocked right shoulder.
	 * @param cfg Lane configuration at LCS array.
	 * @param shift Lane shift from left origin.
	 * @return Number of lanes to right shoulder, or MAX_SHIFT. */
	private int unblockedRightShoulder(LaneConfiguration cfg, int shift) {
		if (isLaneOpen(cfg, cfg.rightShift))
			return cfg.rightShift - shift;
		else
			return MAX_SHIFT;
	}

	/** Check if a lane is blocked.
	 * @param shift Lane shift from left origin.
	 * @return true if lane is blocked, false otherwise. */
	private boolean isLaneBlocked(int shift) {
		IncidentImpact ii = getImpact(shift);
		return (ii == null) || (ii == BLOCKED);
	}

	/** Create an LCS indication for one lane at a long distance to
	 * the incident.
	 * @param shift Lane shift from left origin.
	 * @return LaneUseIndication value. */
	private LaneUseIndication createIndicationLong(int shift) {
		if (isLaneBlocked(shift))
			return LaneUseIndication.LANE_CLOSED_AHEAD;
		else
			return LaneUseIndication.LANE_OPEN;
	}
}
