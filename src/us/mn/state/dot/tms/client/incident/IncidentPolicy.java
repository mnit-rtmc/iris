/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.LaneUseIndication;

/**
 * IncidentPolicy determines which LCS indications to propose for an incident.
 *
 * @author Douglas Lau
 */
public class IncidentPolicy {

	/** Impact codes */
	static private enum ImpactCode {
		FREE_FLOWING, PARTIALLY_BLOCKED, BLOCKED;
		static protected ImpactCode fromChar(char im) {
			switch(im) {
			case '?':
				return PARTIALLY_BLOCKED;
			case '!':
				return BLOCKED;
			default:
				return FREE_FLOWING;
			}
		}
	};

	/** Distance 1 upstream of incident to deploy devices */
	static protected final float DIST_UPSTREAM_1_MILES = 0.5f;

	/** Distance 2 upstream of incident to deploy devices */
	static protected final float DIST_UPSTREAM_2_MILES = 1.0f;

	/** Distance 3 upstream of incident to deploy devices */
	static protected final float DIST_UPSTREAM_3_MILES = 1.5f;

	/** Incident in question */
	protected final Incident incident;

	/** Create a new incident policy */
	public IncidentPolicy(Incident inc) {
		incident = inc;
	}

	/** Create proposed indications for an LCS array.
	 * @param up Distance upstream from incident (miles).
	 * @param n_lanes Number of lanes on LCS array.
	 * @param shift Lane shift relative to incident.
	 * @return Array of LaneUseIndication ordinal values. */
	public Integer[] createIndications(float up, int n_lanes, int shift) {
		Integer[] ind = new Integer[n_lanes];
		for(int i = 0; i < ind.length; i++)
			ind[i] = getIndication(up, n_lanes, shift, i).ordinal();
		return ind;
	}

	/** Get an indication for one lane.
	 * @param up Distance upstream from incident (miles).
	 * @param n_lanes Number of lanes on LCS array.
	 * @param shift Lane shift relative to incident.
	 * @param i Lane number.
	 * @return LaneUseIndication value. */
	protected LaneUseIndication getIndication(float up, int n_lanes,
		int shift, int i)
	{
		if(up < 0)
			return LaneUseIndication.DARK;
		if(up < DIST_UPSTREAM_1_MILES)
			return getIndication1(n_lanes, shift, i);
		if(up < DIST_UPSTREAM_2_MILES)
			return getIndication2(n_lanes, shift, i);
		if(up < DIST_UPSTREAM_3_MILES)
			return getIndication3(n_lanes, shift, i);
		else
			return LaneUseIndication.DARK;
	}

	/** Get the first indication for one lane.
	 * @param n_lanes Number of lanes on LCS array.
	 * @param shift Lane shift relative to incident.
	 * @param i Lane number.
	 * @return LaneUseIndication value. */
	protected LaneUseIndication getIndication1(int n_lanes, int shift,
		int i)
	{
		int ln = shift + n_lanes - i;
		ImpactCode ic = getImpact(ln);
		if(ic == ImpactCode.BLOCKED)
			return LaneUseIndication.LANE_CLOSED;
		if(ic == ImpactCode.PARTIALLY_BLOCKED)
			return LaneUseIndication.USE_CAUTION;
		ImpactCode def = ImpactCode.FREE_FLOWING;
		ImpactCode right = getAdjacentImpact(ln + 1, def);
		if(right == ImpactCode.BLOCKED)
			return LaneUseIndication.USE_CAUTION;
		ImpactCode left = getAdjacentImpact(ln - 1, def);
		if(left == ImpactCode.BLOCKED)
			return LaneUseIndication.USE_CAUTION;
		return LaneUseIndication.LANE_OPEN;
	}

	/** Get the impact at the specified lane */
	protected ImpactCode getImpact(int ln) {
		String impact = incident.getImpact();
		// Don't look at the shoulder lanes
		if(ln <= 0 || ln >= impact.length() - 1)
			return ImpactCode.FREE_FLOWING;
		else
			return ImpactCode.fromChar(impact.charAt(ln));
	}

	/** Get the impact at the specified adjacent lane */
	protected ImpactCode getAdjacentImpact(int ln, ImpactCode def) {
		String impact = incident.getImpact();
		if(ln < 0 || ln >= impact.length())
			return def;
		else
			return ImpactCode.fromChar(impact.charAt(ln));
	}

	/** Get the second indication for one lane.
	 * @param n_lanes Number of lanes in array.
	 * @param shift Lane shift relative to incident.
	 * @param i Lane number.
	 * @return LaneUseIndication value. */
	protected LaneUseIndication getIndication2(int n_lanes, int shift,
		int i)
	{
		int ln = shift + n_lanes - i;
		ImpactCode ic = getImpact(ln);
		if(ic != ImpactCode.BLOCKED)
			return LaneUseIndication.LANE_OPEN;
		ImpactCode def = ImpactCode.BLOCKED;
		ImpactCode right = getAdjacentImpact(ln + 1, def);
		if(i - 1 < 0)
			right = ImpactCode.BLOCKED;
		ImpactCode left = getAdjacentImpact(ln - 1, def);
		if(i + 1 >= n_lanes)
			left = ImpactCode.BLOCKED;
		if(left == ImpactCode.BLOCKED && right == ImpactCode.BLOCKED)
			return LaneUseIndication.LANE_CLOSED;
		if(left != ImpactCode.BLOCKED && right != ImpactCode.BLOCKED)
			return LaneUseIndication.MERGE_BOTH;
		if(left == ImpactCode.BLOCKED)
			return LaneUseIndication.MERGE_RIGHT;
		else
			return LaneUseIndication.MERGE_LEFT;
	}

	/** Get the third indication for one lane.
	 * @param n_lanes Number of lanes in array.
	 * @param shift Lane shift relative to incident.
	 * @param i Lane number.
	 * @return LaneUseIndication value. */
	protected LaneUseIndication getIndication3(int n_lanes, int shift,
		int i)
	{
		int ln = shift + n_lanes - i;
		ImpactCode ic = getImpact(ln);
		if(ic != ImpactCode.BLOCKED)
			return LaneUseIndication.LANE_OPEN;
		ImpactCode def = ImpactCode.BLOCKED;
		ImpactCode right = getAdjacentImpact(ln + 1, def);
		if(i - 1 < 0)
			right = ImpactCode.BLOCKED;
		ImpactCode left = getAdjacentImpact(ln - 1, def);
		if(i + 1 >= n_lanes)
			left = ImpactCode.BLOCKED;
		if(left == ImpactCode.BLOCKED && right == ImpactCode.BLOCKED) {
			// NOTE: adjacent lanes are also blocked, so find the
			//       impact 2 lanes to the left and right
			right = getAdjacentImpact(ln + 2, def);
			if(i - 2 < 0)
				right = ImpactCode.BLOCKED;
			left = getAdjacentImpact(ln - 2, def);
			if(i + 2 >= n_lanes)
				left = ImpactCode.BLOCKED;
			if(left != ImpactCode.BLOCKED &&
			   right != ImpactCode.BLOCKED)
				return LaneUseIndication.MERGE_BOTH;
			if(left == ImpactCode.BLOCKED)
				return LaneUseIndication.MERGE_RIGHT;
			else
				return LaneUseIndication.MERGE_LEFT;
		} else
			return LaneUseIndication.LANE_CLOSED_AHEAD;
	}
}
