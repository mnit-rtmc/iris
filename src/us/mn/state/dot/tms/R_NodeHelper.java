/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2018  Minnesota Department of Transportation
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

import java.util.Iterator;

/**
 * R_NodeHelper has static methods for dealing with r_nodes.
 *
 * @author Douglas Lau
 */
public class R_NodeHelper extends BaseHelper {

	/** Don't create any instances */
	private R_NodeHelper() {
		assert false;
	}

	/** Get an r_node iterator */
	static public Iterator<R_Node> iterator() {
		return new IteratorWrapper<R_Node>(namespace.iterator(
			R_Node.SONAR_TYPE));
	}

	/** Check if an r_node is a station */
	static public boolean isStation(R_Node n) {
		return n.getNodeType() == R_NodeType.STATION.ordinal();
	}

	/** Check if an r_node is an entrance */
	static public boolean isEntrance(R_Node n) {
		return n.getNodeType() == R_NodeType.ENTRANCE.ordinal();
	}

	/** Check if an r_node is an exit */
	static public boolean isExit(R_Node n) {
		return n.getNodeType() == R_NodeType.EXIT.ordinal();
	}

	/** Check if an r_node an an intersection */
	static public boolean isIntersection(R_Node n) {
		return n.getNodeType() == R_NodeType.INTERSECTION.ordinal();
	}

	/** Check if an r_node is an access node */
	static public boolean isAccess(R_Node n) {
		return n.getNodeType() == R_NodeType.ACCESS.ordinal();
	}

	/** Test if an access node links with an entrance node.
	 * @param ac Access node.
	 * @param n Node to check.
	 * @return true If nodes should link. */
	static public boolean isAccessLink(R_Node ac, R_Node n) {
		return isEntrance(n) &&
		       GeoLocHelper.accessMatches(ac.getGeoLoc(),n.getGeoLoc());
	}

	/** Test if an exit node links with another node.
	 * @param ex Exit node.
	 * @param n Node to check.
	 * @return true If nodes should link. */
	static public boolean isExitLink(R_Node ex, R_Node n) {
		return isMatchingEntrance(ex, n)
		    || isMatchingAccess(ex, n);
	}

	/** Test if an exit node links with a matching entrance node.
	 * @param ex Exit node.
	 * @param n Possible entrance node.
	 * @return true If nodes should link. */
	static private boolean isMatchingEntrance(R_Node ex, R_Node n) {
		return isEntrance(n) &&
		       GeoLocHelper.rampMatches(ex.getGeoLoc(), n.getGeoLoc());
	}

	/** Test if an exit node links with a matching access node.
	 * @param ex Exit node.
	 * @param n Possible access node.
	 * @return true If nodes should link. */
	static private boolean isMatchingAccess(R_Node ex, R_Node n) {
		return isAccess(n) &&
		       GeoLocHelper.accessMatches(ex.getGeoLoc(),n.getGeoLoc());
	}

	/** Check if an r_node links to a CD road */
	static public boolean isCD(R_Node n) {
		return n.getTransition() == R_NodeTransition.CD.ordinal();
	}

	/** Check if an r_node has a COMMON transition */
	static public boolean isCommon(R_Node n) {
		return n.getTransition() == R_NodeTransition.COMMON.ordinal();
	}

	/** Check if a node is at a station break */
	static public boolean isStationBreak(R_Node n) {
		return n.getActive() &&
		       isStation(n) &&
		       hasDetection(n);
	}

	/** Check if an R_Node has detection (not abandoned) */
	static private boolean hasDetection(R_Node n) {
		Iterator<Detector> it = DetectorHelper.iterator();
		while (it.hasNext()) {
			Detector d = it.next();
			if (d.getR_Node() == n && DetectorHelper.isActive(d))
				return true;
		}
		return false;
	}

	/** Check if a given node is a continuity break */
	static public boolean isContinuityBreak(R_Node n) {
		return isAccess(n) || isCommon(n);
	}

	/** Check if a node should be joined with a segment */
	static public boolean isJoined(R_Node r_node) {
		return (!isCommon(r_node)) ||
		       (!isEntrance(r_node));
	}

	/** Get the roadway corridor name */
	static public String getCorridorName(R_Node r_node) {
		GeoLoc loc = r_node.getGeoLoc();
		return GeoLocHelper.getCorridorName(loc);
	}

	/** Check for parking detection */
	static public boolean isParking(R_Node n) {
		Iterator<Detector> it = DetectorHelper.iterator();
		while (it.hasNext()) {
			Detector d = it.next();
			if (d.getR_Node() == n &&
			    d.getLaneType() == LaneType.PARKING.ordinal())
				return true;
		}
		return false;
	}
}
