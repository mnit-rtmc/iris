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
	/* Blocked - lane or shoulder */
	all_lanes_blocked,       // 0
	left_lanes_blocked,      // 1
	right_lanes_blocked,     // 2
	center_lanes_blocked,    // 3
	lanes_blocked,           // 4
	both_shoulders_blocked,  // 5
	left_shoulder_blocked,   // 6
	right_shoulder_blocked,  // 7
	/* Affected - partially blocked */
	all_lanes_affected,      // 8
	left_lanes_affected,     // 9
	right_lanes_affected,    // 10
	center_lanes_affected,   // 11
	lanes_affected,          // 12
	both_shoulders_affected, // 13
	left_shoulder_affected,  // 14
	right_shoulder_affected, // 15
	/* All clear */
	all_free_flowing;        // 16

	/** Get an impact from an ordinal value */
	static public IncImpact fromOrdinal(int o) {
		if (o >= 0 && o < values().length)
			return values()[o];
		else
			return all_free_flowing;
	}

	/** Get the impact of an incident */
	static public IncImpact getImpact(Incident inc) {
		LaneImpact[] li = LaneImpact.fromString(inc.getImpact());
		int blocked_groups = countGroups(li, LaneImpact.BLOCKED);
		if (blocked_groups > 0) {
			if (isAllLanes(li, LaneImpact.BLOCKED))
				return IncImpact.all_lanes_blocked;
			else if (blocked_groups == 1) {
				if (isLeftLane(li, LaneImpact.BLOCKED))
					return IncImpact.left_lanes_blocked;
				else if (isRightLane(li, LaneImpact.BLOCKED))
					return IncImpact.right_lanes_blocked;
				else
					return IncImpact.center_lanes_blocked;
			} else
				return IncImpact.lanes_blocked;
		} else if (isBothShoulders(li, LaneImpact.BLOCKED))
			return IncImpact.both_shoulders_blocked;
		else if (isLeftShoulder(li, LaneImpact.BLOCKED))
			return IncImpact.left_shoulder_blocked;
		else if (isRightShoulder(li, LaneImpact.BLOCKED))
			return IncImpact.right_shoulder_blocked;
		int affected_groups = countGroups(li, LaneImpact.AFFECTED);
		if (affected_groups > 0) {
			if (isAllLanes(li, LaneImpact.AFFECTED))
				return IncImpact.all_lanes_affected;
			else if (blocked_groups == 1) {
				if (isLeftLane(li, LaneImpact.AFFECTED))
					return IncImpact.left_lanes_affected;
				else if (isRightLane(li, LaneImpact.AFFECTED))
					return IncImpact.right_lanes_affected;
				else
					return IncImpact.center_lanes_affected;
			} else
				return IncImpact.lanes_affected;
		} else if (isBothShoulders(li, LaneImpact.AFFECTED))
			return IncImpact.both_shoulders_affected;
		else if (isLeftShoulder(li, LaneImpact.AFFECTED))
			return IncImpact.left_shoulder_affected;
		else if (isRightShoulder(li, LaneImpact.AFFECTED))
			return IncImpact.right_shoulder_affected;
		else
			return IncImpact.all_free_flowing;
	}

	/** Count groups of non-shoulder lanes with given impact */
	static private int countGroups(LaneImpact[] li, LaneImpact v) {
		int groups = 0;
		boolean in_group = false;
		for (int i = 1; i < li.length - 1; i++) {
			boolean g = (li[i] == v);
			if (g && !in_group)
				groups += 1;
			in_group = g;
		}
		return groups;
	}

	/** Check if all lanes (non-shoulder) have given impact */
	static private boolean isAllLanes(LaneImpact[] li, LaneImpact v) {
		for (int i = 1; i < li.length - 1; i++) {
			if (li[i] != v)
				return false;
		}
		return li.length > 2;
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

	/** Get the incident severity */
	public IncSeverity severity(LaneType lane_type) {
		switch (lane_type) {
		case MAINLINE:
			return severityMainline();
		case EXIT:
		case CD_LANE:
			return severityExitCD();
		case MERGE:
			return severityMerge();
		default:
			return null;
		}
	}

	/** Get the severity of a mainline incident */
	private IncSeverity severityMainline() {
		switch (this) {
		case all_lanes_blocked:
			return IncSeverity.major;
		case left_lanes_blocked:
		case right_lanes_blocked:
		case center_lanes_blocked:
		case lanes_blocked:
		case both_shoulders_blocked:
		case left_shoulder_blocked:
		case right_shoulder_blocked:
			return IncSeverity.normal;
		case all_lanes_affected:
		case left_lanes_affected:
		case right_lanes_affected:
		case center_lanes_affected:
		case lanes_affected:
		case both_shoulders_affected:
		case left_shoulder_affected:
		case right_shoulder_affected:
			return IncSeverity.minor;
		default:
			return null;
		}
	}

	/** Get the severity of an exit or CD road incident */
	private IncSeverity severityExitCD() {
		switch (this) {
		case all_lanes_blocked:
			return IncSeverity.normal;
		case left_lanes_blocked:
		case right_lanes_blocked:
		case center_lanes_blocked:
		case lanes_blocked:
		case both_shoulders_blocked:
		case left_shoulder_blocked:
		case right_shoulder_blocked:
			return IncSeverity.minor;
		default:
			return null;
		}
	}

	/** Get the severity of a merge incident */
	private IncSeverity severityMerge() {
		switch (this) {
		case all_lanes_blocked:
			return IncSeverity.minor;
		default:
			return null;
		}
	}

	/** Get count of impacted lanes.
	 *
	 * @return If any lanes are blocked, count of blocked lanes.  Otherwise
	 *         the count of affected lanes. */
	static public int getImpactedLanes(Incident inc) {
		LaneImpact[] li = LaneImpact.fromString(inc.getImpact());
		if (countGroups(li, LaneImpact.BLOCKED) > 0)
			return getLaneCount(li, LaneImpact.BLOCKED);
		else
			return getLaneCount(li, LaneImpact.AFFECTED);
	}

	/** Get count of open lanes.
	 *
	 * @return If any lanes are blocked, count of non-blocked lanes.
	 *         Otherwise the count of non-affected lanes. */
	static public int getOpenLanes(Incident inc) {
		LaneImpact[] li = LaneImpact.fromString(inc.getImpact());
		if (countGroups(li, LaneImpact.BLOCKED) > 0) {
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
