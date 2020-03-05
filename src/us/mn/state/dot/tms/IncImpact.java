/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2019  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.LaneType;

/**
 * An incident impact is the full impact of an incident across all lanes.  The
 * ordinal values correspond to the records in the iris.inc_impact look-up
 * table.
 *
 * @author Douglas Lau
 */
public enum IncImpact {
	/* lanes blocked */
	lanes_blocked,           // 0
	left_lanes_blocked,      // 1
	right_lanes_blocked,     // 2
	center_lanes_blocked,    // 3
	/* lanes affected */
	lanes_affected,          // 4
	left_lanes_affected,     // 5
	right_lanes_affected,    // 6
	center_lanes_affected,   // 7
	/* shoulders blocked */
	both_shoulders_blocked,  // 8
	left_shoulder_blocked,   // 9
	right_shoulder_blocked,  // 10
	/* shoulders affected */
	both_shoulders_affected, // 11
	left_shoulder_affected,  // 12
	right_shoulder_affected, // 13
	/* nothing blocked or affected */
	free_flowing;            // 14

	/** Get an impact from an ordinal value */
	static public IncImpact fromOrdinal(int o) {
		if (o >= 0 && o < values().length)
			return values()[o];
		else
			return free_flowing;
	}

	/** Get the impact of an incident */
	static public IncImpact getImpact(Incident inc) {
		LaneImpact[] li = LaneImpact.fromString(inc.getImpact());
		// Check for BLOCKED lanes
		if (isLeftLane(li, LaneImpact.BLOCKED) &&
		    isRightLane(li, LaneImpact.BLOCKED))
			return IncImpact.lanes_blocked;
		else if (isLeftLane(li, LaneImpact.BLOCKED))
			return IncImpact.left_lanes_blocked;
		else if (isRightLane(li, LaneImpact.BLOCKED))
			return IncImpact.right_lanes_blocked;
		else if (isAnyLane(li, LaneImpact.BLOCKED))
			return IncImpact.center_lanes_blocked;
		// Check for AFFECTED lanes
		else if (isLeftLane(li, LaneImpact.AFFECTED) &&
		         isRightLane(li, LaneImpact.AFFECTED))
			return IncImpact.lanes_affected;
		else if (isLeftLane(li, LaneImpact.AFFECTED))
			return IncImpact.left_lanes_affected;
		else if (isRightLane(li, LaneImpact.AFFECTED))
			return IncImpact.right_lanes_affected;
		else if (isAnyLane(li, LaneImpact.AFFECTED))
			return IncImpact.center_lanes_affected;
		// Check for BLOCKED shoulders
		else if (isBothShoulders(li, LaneImpact.BLOCKED))
			return IncImpact.both_shoulders_blocked;
		else if (isLeftShoulder(li, LaneImpact.BLOCKED))
			return IncImpact.left_shoulder_blocked;
		else if (isRightShoulder(li, LaneImpact.BLOCKED))
			return IncImpact.right_shoulder_blocked;
		// Check for AFFECTED shoulders
		else if (isBothShoulders(li, LaneImpact.AFFECTED))
			return IncImpact.both_shoulders_affected;
		else if (isLeftShoulder(li, LaneImpact.AFFECTED))
			return IncImpact.left_shoulder_affected;
		else if (isRightShoulder(li, LaneImpact.AFFECTED))
			return IncImpact.right_shoulder_affected;
		else
			return IncImpact.free_flowing;
	}

	/** Check if any lane or shoulder has given impact */
	static private boolean isAny(LaneImpact[] li, LaneImpact v) {
		for (int i = 0; i < li.length; i++) {
			if (li[i] == v)
				return true;
		}
		return false;
	}

	/** Check if any lane has given impact */
	static private boolean isAnyLane(LaneImpact[] li, LaneImpact v) {
		for (int i = 1; i < li.length - 1; i++) {
			if (li[i] == v)
				return true;
		}
		return false;
	}

	/** Check if left lane has given impact */
	static private boolean isLeftLane(LaneImpact[] li, LaneImpact v) {
		return (li.length > 3) && (li[1] == v);
	}

	/** Check if right lane has given impact */
	static private boolean isRightLane(LaneImpact[] li, LaneImpact v) {
		return (li.length > 3) && (li[li.length - 2] == v);
	}

	/** Check if both shoulders have given impact */
	static private boolean isBothShoulders(LaneImpact[] li, LaneImpact v) {
		return isLeftShoulder(li, v) && isRightShoulder(li, v);
	}

	/** Check if left shoulder has given impact */
	static private boolean isLeftShoulder(LaneImpact[] li, LaneImpact v) {
		return (li.length > 1) && (li[0] == v);
	}

	/** Check if right shoulder has given impact */
	static private boolean isRightShoulder(LaneImpact[] li, LaneImpact v) {
		return (li.length > 1) && (li[li.length - 1] == v);
	}

	/** Check if either shoulder has given impact */
	static private boolean isEitherShoulder(LaneImpact[] li, LaneImpact v) {
		return isLeftShoulder(li, v) || isRightShoulder(li, v);
	}

	/** Get the incident severity */
	static public IncSeverity severity(Incident inc, LaneType lane_type) {
		switch (lane_type) {
		case MAINLINE:
			return severityMainline(inc);
		case EXIT:
		case CD_LANE:
			return severityExitCD(inc);
		case MERGE:
			return severityMerge(inc);
		default:
			return null;
		}
	}

	/** Get the severity of a mainline incident */
	static private IncSeverity severityMainline(Incident inc) {
		if (isLaneBlocked(inc)) {
			int n_impacted = getImpactedLanes(inc);
			int n_open = getOpenLanes(inc);
			return (n_impacted > n_open)
			      ? IncSeverity.major
			      : IncSeverity.normal;
		} else if (isShoulderBlocked(inc))
			return IncSeverity.normal;
		else if (isAnyAffected(inc))
			return IncSeverity.minor;
		else
			return null;
	}

	/** Get the severity of an exit or CD road incident */
	static private IncSeverity severityExitCD(Incident inc) {
		if (isLaneBlocked(inc)) {
			int n_impacted = getImpactedLanes(inc);
			int n_open = getOpenLanes(inc);
			return (n_impacted > n_open)
			      ? IncSeverity.normal
			      : IncSeverity.minor;
		} else if (isShoulderBlocked(inc))
			return IncSeverity.minor;
		else
			return null;
	}

	/** Get the severity of a merge incident */
	static private IncSeverity severityMerge(Incident inc) {
		if (isLaneBlocked(inc)) {
			int n_impacted = getImpactedLanes(inc);
			int n_open = getOpenLanes(inc);
			return (n_impacted > n_open) ? IncSeverity.minor : null;
		} else
			return null;
	}

	/** Check if any lanes are blocked */
	static private boolean isLaneBlocked(Incident inc) {
		LaneImpact[] li = LaneImpact.fromString(inc.getImpact());
		return isAnyLane(li, LaneImpact.BLOCKED);
	}

	/** Check if either shoulder is blocked */
	static private boolean isShoulderBlocked(Incident inc) {
		LaneImpact[] li = LaneImpact.fromString(inc.getImpact());
		return isEitherShoulder(li, LaneImpact.BLOCKED);
	}

	/** Check if any lane or shoulder is affected */
	static private boolean isAnyAffected(Incident inc) {
		LaneImpact[] li = LaneImpact.fromString(inc.getImpact());
		return isAny(li, LaneImpact.AFFECTED);
	}

	/** Get count of impacted lanes.
	 *
	 * @return If any lanes are blocked, count of blocked lanes.  Otherwise
	 *         the count of affected lanes. */
	static public int getImpactedLanes(Incident inc) {
		LaneImpact[] li = LaneImpact.fromString(inc.getImpact());
		return (isLaneBlocked(inc))
		      ? getLaneCount(li, LaneImpact.BLOCKED)
		      : getLaneCount(li, LaneImpact.AFFECTED);
	}

	/** Get count of open lanes.
	 *
	 * @return If any lanes are blocked, count of non-blocked lanes.
	 *         Otherwise the count of non-affected lanes. */
	static public int getOpenLanes(Incident inc) {
		LaneImpact[] li = LaneImpact.fromString(inc.getImpact());
		if (isLaneBlocked(inc)) {
			return getLaneCount(li, LaneImpact.AFFECTED) +
			       getLaneCount(li, LaneImpact.FREE_FLOWING);
		} else
			return getLaneCount(li, LaneImpact.FREE_FLOWING);
	}

	/** Count lanes (non-shoulder) with given impact */
	static private int getLaneCount(LaneImpact[] li, LaneImpact v) {
		int count = 0;
		for (int i = 1; i < li.length - 1; i++) {
			if (li[i] == v)
				count++;
		}
		return count;
	}
}
