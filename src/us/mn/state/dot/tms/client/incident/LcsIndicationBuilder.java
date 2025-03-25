/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2025  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.CorridorBase;
import us.mn.state.dot.tms.CorridorFinder;
import us.mn.state.dot.tms.Incident;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.LaneConfiguration;
import us.mn.state.dot.tms.LaneImpact;
import static us.mn.state.dot.tms.LaneImpact.*;
import us.mn.state.dot.tms.Lcs;
import us.mn.state.dot.tms.LcsHelper;
import us.mn.state.dot.tms.LcsIndication;
import static us.mn.state.dot.tms.R_Node.MAX_SHIFT;
import us.mn.state.dot.tms.geo.Position;
import us.mn.state.dot.tms.units.Distance;
import static us.mn.state.dot.tms.units.Distance.Units.MILES;

/**
 * LcsIndicationBuilder builds LCS indications for an incident.
 *
 * @author Douglas Lau
 */
public class LcsIndicationBuilder {

	/** Short distance upstream of incident to deploy devices */
	static private final Distance DIST_SHORT = new Distance(0.6f, MILES);

	/** Medium distance upstream of incident to deploy devices */
	static private final Distance DIST_MEDIUM = new Distance(0.9f, MILES);

	/** Long distance upstream of incident to deploy devices */
	static private final Distance DIST_LONG = new Distance(1.2f, MILES);

	/** Assign a requested indication to an available indication.
	 * @param li Requested indication.
	 * @param avail Array of available lane use indications.
	 * @return "Best" lane use indication in available array. */
	static private LcsIndication assignIndication(LcsIndication li,
		LcsIndication[] avail)
	{
		for (LcsIndication a: avail) {
			if (li == a)
				return li;
		}
		LcsIndication alt = altIndication(li);
		for (LcsIndication a: avail) {
			if (alt == a)
				return alt;
		}
		return LcsIndication.DARK;
	}

	/** Get alternate indication for "changeable" LCS devices. */
	static private LcsIndication altIndication(LcsIndication li) {
		switch (li) {
		case LANE_CLOSED_AHEAD:
		case MERGE_RIGHT:
		case MERGE_LEFT:
			return LcsIndication.USE_CAUTION;
		case MUST_EXIT_RIGHT:
		case MUST_EXIT_LEFT:
			return LcsIndication.LANE_CLOSED;
		default:
			return LcsIndication.LANE_OPEN;
		}
	}

	/** Check if a set of indications should be deployed */
	static private boolean shouldDeploy(int[] ind) {
		for (int i: ind) {
			LcsIndication li = LcsIndication.fromOrdinal(i);
			switch (LcsIndication.fromOrdinal(i)) {
			case DARK:
			case LANE_OPEN:
				continue;
			default:
				return true;
			}
		}
		return false;
	}

	/** Corridor finder */
	private final CorridorFinder finder;

	/** Incident in question */
	private final Incident incident;

	/** Lane configuration at incident */
	private final LaneConfiguration config;

	/** Create a new LCS indication builder.
	 * @param cf Corridor finder.
	 * @param inc Incident for deployment. */
	public LcsIndicationBuilder(CorridorFinder cf, Incident inc) {
		finder = cf;
		incident = inc;
		config = laneConfiguration(new IncidentLoc(inc));
	}

	/** Get lane configuration at a location */
	private LaneConfiguration laneConfiguration(GeoLoc loc) {
		CorridorBase cb = finder.lookupCorridor(loc);
		return (cb != null)
		      ? cb.laneConfiguration(new Position(loc.getLat(),
				loc.getLon()))
		      : null;
	}

	/** Get lane configuration at LCS array */
	private LaneConfiguration laneConfiguration(Lcs lcs) {
		GeoLoc loc = lcs.getGeoLoc();
		return (loc != null) ? laneConfiguration(loc) : null;
	}

	/** Create proposed indications for an LCS array.
	 * @param lcs LCS array.
	 * @param up Distance upstream from incident (miles).
	 * @return Array of LcsIndication ordinal values, or null. */
	public int[] createIndications(Lcs lcs, Distance up) {
		LaneConfiguration cfg = laneConfiguration(lcs);
		return (cfg != null)
		      ? createIndications(lcs, up, cfg)
		      : null;
	}

	/** Create proposed indications for an LCS array.
	 * @param lcs LCS array.
	 * @param up Distance upstream from incident (miles).
	 * @param cfg Lane configuration at LCS array location.
	 * @return Array of LcsIndication ordinal values, or null. */
	private int[] createIndications(Lcs lcs, Distance up,
		LaneConfiguration cfg)
	{
		int n_lanes = LcsHelper.countLanes(lcs);
		LcsIndication[] ind = createIndications(cfg, up, n_lanes,
			lcs.getShift());
		int[] oin = new int[ind.length];
		for (int ln = 0; ln < ind.length; ln++) {
			LcsIndication[] avail =
				LcsHelper.lookupIndications(lcs, ln + 1);
			oin[ln] = assignIndication(ind[ln], avail).ordinal();
		}
		return shouldDeploy(oin) ? oin : null;
	}

	/** Create proposed indications for an LCS array.
	 * @param cfg Lane configuration at LCS array.
	 * @param up Distance upstream from incident (miles).
	 * @param n_lanes Number of lanes at LCS array.
	 * @param lcs_shift Lane shift at LCS array.
	 * @return Array of LcsIndication values. */
	private LcsIndication[] createIndications(LaneConfiguration cfg,
		Distance up, int n_lanes, int lcs_shift)
	{
		LcsIndication[] ind = new LcsIndication[n_lanes];
		for (int i = 0; i < ind.length; i++) {
			int shift = lcs_shift + n_lanes - i - 1;
			ind[i] = createIndication(cfg, up, shift);
		}
		return ind;
	}

	/** Create an LCS indication for one lane.
	 * @param cfg Lane configuration at LCS array.
	 * @param up Distance upstream from incident.
	 * @param shift Lane shift from left origin.
	 * @return LcsIndication value. */
	private LcsIndication createIndication(LaneConfiguration cfg,
		Distance up, int shift)
	{
		if (isShoulder(cfg, shift))
			return LcsIndication.DARK;
		if (config != null && isShoulder(config, shift))
			return LcsIndication.LANE_OPEN;
		double m = up.m();
		if (m < 0)
			return LcsIndication.DARK;
		if (m < DIST_SHORT.m())
			return createIndicationShort(cfg, shift);
		if (m < DIST_MEDIUM.m())
			return createIndicationMedium(cfg, shift);
		if (m < DIST_LONG.m())
			return createIndicationLong(shift);
		else
			return LcsIndication.DARK;
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
	 * @return LcsIndication value. */
	private LcsIndication createIndicationShort(LaneConfiguration cfg,
		int shift)
	{
		LaneImpact ii = getImpact(shift);
		if (ii == BLOCKED)
			return LcsIndication.LANE_CLOSED;
		else if (ii == AFFECTED || isAdjacentLaneBlocked(shift))
			return LcsIndication.USE_CAUTION;
		else
			return LcsIndication.LANE_OPEN;
	}

	/** Get the impact at the specified lane.
	 * @param shift Lane shift from left origin.
	 * @return LaneImpact for given lane, or null. */
	private LaneImpact getImpact(int shift) {
		// FIXME: check lane continuity to incident
		String impact = incident.getImpact();
		int ln = shift;
		if (config != null)
			ln = ln - config.leftShift + 1;
		if (ln >= 0 && ln < impact.length())
			return LaneImpact.fromChar(impact.charAt(ln));
		else
			return null;
	}

	/** Check if an adjacent lane is blocked.
	 * @param shift Lane shift from left origin.
	 * @return true if an adjacent lane is blocked, false otherwise. */
	private boolean isAdjacentLaneBlocked(int shift) {
		LaneImpact left = getImpact(shift - 1);
		LaneImpact right = getImpact(shift + 1);
		return left == BLOCKED || right == BLOCKED;
	}

	/** Create an LCS indication for one lane at a medium distance to
	 * the incident.
	 * @param cfg Lane configuration at LCS array.
	 * @param shift Lane shift from left origin.
	 * @return LcsIndication value. */
	private LcsIndication createIndicationMedium(LaneConfiguration cfg,
		int shift)
	{
		if (getImpact(shift) != BLOCKED)
			return LcsIndication.LANE_OPEN;
		int n_left = unblockedLeftMainline(cfg, shift);
		int n_right = unblockedRightMainline(cfg, shift);
		if (n_left < n_right)
			return LcsIndication.MERGE_LEFT;
		else if (n_right < n_left)
			return LcsIndication.MERGE_RIGHT;
		else if (n_left < MAX_SHIFT)
			return LcsIndication.LANE_CLOSED_AHEAD;
		else
			return createIndicationMediumBlocked(cfg, shift);
	}

	/** Check if a lane is open.
	 * @param cfg Lane configuration at LCS array.
	 * @param shift Lane shift from left origin.
	 * @return true if lane is open, false otherwise. */
	private boolean isLaneOpen(LaneConfiguration cfg, int shift) {
		LaneImpact ii = getImpact(shift);
		return (ii == FREE_FLOWING) || (ii == AFFECTED);
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
	 * @return LcsIndication value. */
	private LcsIndication createIndicationMediumBlocked(
		LaneConfiguration cfg, int shift)
	{
		int n_left = unblockedLeftShoulder(cfg, shift);
		int n_right = unblockedRightShoulder(cfg, shift);
		if (n_left < n_right)
			return LcsIndication.MERGE_LEFT;
		else if (n_right < n_left)
			return LcsIndication.MERGE_RIGHT;
		else
			return LcsIndication.LANE_CLOSED;
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
		LaneImpact ii = getImpact(shift);
		return (ii == null) || (ii == BLOCKED);
	}

	/** Create an LCS indication for one lane at a long distance to
	 * the incident.
	 * @param shift Lane shift from left origin.
	 * @return LcsIndication value. */
	private LcsIndication createIndicationLong(int shift) {
		if (isLaneBlocked(shift))
			return LcsIndication.LANE_CLOSED_AHEAD;
		else
			return LcsIndication.LANE_OPEN;
	}
}
