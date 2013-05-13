/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2013  Minnesota Department of Transportation
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

/**
 * IncidentPolicy determines which LCS indications to propose for an incident.
 *
 * @author Douglas Lau
 */
public class IncidentPolicy {

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
	 * @param n_lcs Number of lanes on LCS array.
	 * @param shift Lane shift relative to incident.
	 * @param n_lanes Number of full lanes at LCS array.
	 * @return Array of LaneUseIndication ordinal values. */
	public Integer[] createIndications(float up, int n_lcs, int shift,
		int n_lanes)
	{
		Integer[] ind = new Integer[n_lcs];
		for(int i = 0; i < ind.length; i++) {
			if(i < n_lcs - n_lanes) {
				// Don't deploy shoulder lanes
				ind[i] = LaneUseIndication.DARK.ordinal();
				continue;
			}
			ind[i] = getIndication(up, n_lcs, shift, i).ordinal();
		}
		return ind;
	}

	/** Get an indication for one lane.
	 * @param up Distance upstream from incident (miles).
	 * @param n_lcs Number of lanes on LCS array.
	 * @param shift Lane shift relative to incident.
	 * @param i Lane number.
	 * @return LaneUseIndication value. */
	protected LaneUseIndication getIndication(float up, int n_lcs,
		int shift, int i)
	{
		if(up < 0)
			return LaneUseIndication.DARK;
		if(up < DIST_UPSTREAM_1_MILES)
			return getIndication1(n_lcs, shift, i);
		if(up < DIST_UPSTREAM_2_MILES)
			return getIndication2(n_lcs, shift, i);
		if(up < DIST_UPSTREAM_3_MILES)
			return getIndication3(n_lcs, shift, i);
		else
			return LaneUseIndication.DARK;
	}

	/** Get the first indication for one lane.
	 * @param n_lcs Number of lanes on LCS array.
	 * @param shift Lane shift relative to incident.
	 * @param i Lane number.
	 * @return LaneUseIndication value. */
	protected LaneUseIndication getIndication1(int n_lcs, int shift,
		int i)
	{
		int ln = shift + n_lcs - i;
		IncidentImpact ii = getImpact(ln);
		if(ii == BLOCKED)
			return LaneUseIndication.LANE_CLOSED;
		if(ii == PARTIALLY_BLOCKED)
			return LaneUseIndication.USE_CAUTION;
		IncidentImpact def = FREE_FLOWING;
		IncidentImpact right = getAdjacentImpact(ln + 1, def);
		if(right == BLOCKED)
			return LaneUseIndication.USE_CAUTION;
		IncidentImpact left = getAdjacentImpact(ln - 1, def);
		if(left == BLOCKED)
			return LaneUseIndication.USE_CAUTION;
		return LaneUseIndication.LANE_OPEN;
	}

	/** Get the impact at the specified lane */
	private IncidentImpact getImpact(int ln) {
		String impact = incident.getImpact();
		// Don't look at the shoulder lanes
		if(ln <= 0 || ln >= impact.length() - 1)
			return FREE_FLOWING;
		else
			return IncidentImpact.fromChar(impact.charAt(ln));
	}

	/** Get the impact at the specified adjacent lane */
	private IncidentImpact getAdjacentImpact(int ln, IncidentImpact def) {
		String impact = incident.getImpact();
		if(ln < 0 || ln >= impact.length())
			return def;
		else
			return IncidentImpact.fromChar(impact.charAt(ln));
	}

	/** Get the second indication for one lane.
	 * @param n_lcs Number of lanes in array.
	 * @param shift Lane shift relative to incident.
	 * @param i Lane number.
	 * @return LaneUseIndication value. */
	protected LaneUseIndication getIndication2(int n_lcs, int shift,
		int i)
	{
		int ln = shift + n_lcs - i;
		IncidentImpact ii = getImpact(ln);
		if(ii != BLOCKED)
			return LaneUseIndication.LANE_OPEN;
		IncidentImpact def = BLOCKED;
		IncidentImpact right = getAdjacentImpact(ln + 1, def);
		if(i - 1 < 0)
			right = BLOCKED;
		IncidentImpact left = getAdjacentImpact(ln - 1, def);
		if(i + 1 >= n_lcs)
			left = BLOCKED;
		if(left == BLOCKED && right == BLOCKED)
			return LaneUseIndication.LANE_CLOSED;
		if(left != BLOCKED && right != BLOCKED)
			return LaneUseIndication.MERGE_BOTH;
		if(left == BLOCKED)
			return LaneUseIndication.MERGE_RIGHT;
		else
			return LaneUseIndication.MERGE_LEFT;
	}

	/** Get the third indication for one lane.
	 * @param n_lcs Number of lanes in array.
	 * @param shift Lane shift relative to incident.
	 * @param i Lane number.
	 * @return LaneUseIndication value. */
	protected LaneUseIndication getIndication3(int n_lcs, int shift,
		int i)
	{
		int ln = shift + n_lcs - i;
		IncidentImpact ii = getImpact(ln);
		if(ii != BLOCKED)
			return LaneUseIndication.LANE_OPEN;
		IncidentImpact def = BLOCKED;
		IncidentImpact right = getAdjacentImpact(ln + 1, def);
		if(i - 1 < 0)
			right = BLOCKED;
		IncidentImpact left = getAdjacentImpact(ln - 1, def);
		if(i + 1 >= n_lcs)
			left = BLOCKED;
		if(left == BLOCKED && right == BLOCKED) {
			// NOTE: adjacent lanes are also BLOCKED, so find the
			//       impact 2 lanes to the left and right
			right = getAdjacentImpact(ln + 2, def);
			if(i - 2 < 0)
				right = BLOCKED;
			left = getAdjacentImpact(ln - 2, def);
			if(i + 2 >= n_lcs)
				left = BLOCKED;
			if(left != BLOCKED && right != BLOCKED)
				return LaneUseIndication.MERGE_BOTH;
			if(left == BLOCKED)
				return LaneUseIndication.MERGE_RIGHT;
			else
				return LaneUseIndication.MERGE_LEFT;
		} else
			return LaneUseIndication.LANE_CLOSED_AHEAD;
	}
}
