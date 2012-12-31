/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2012  Minnesota Department of Transportation
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
	static public boolean isStation(R_Node r_node) {
		return r_node.getNodeType() == R_NodeType.STATION.ordinal();
	}

	/** Check if an r_node is an entrance */
	static public boolean isEntrance(R_Node r_node) {
		return r_node.getNodeType() == R_NodeType.ENTRANCE.ordinal();
	}

	/** Check if an r_node is an exit */
	static public boolean isExit(R_Node r_node) {
		return r_node.getNodeType() == R_NodeType.EXIT.ordinal();
	}

	/** Check if an r_node links to a CD road */
	static public boolean isCD(R_Node r_node) {
		return r_node.getTransition() == R_NodeTransition.CD.ordinal();
	}

	/** Check if an r_node an an intersection */
	static public boolean isIntersection(R_Node r_node) {
		return r_node.getNodeType() ==
		       R_NodeType.INTERSECTION.ordinal();
	}

	/** Test if an r_node (acc) is linked by another r_node (ot) */
	static public boolean isAccessLink(R_Node acc, R_Node ot) {
		return isEntrance(ot) && GeoLocHelper.accessMatches(
		       acc.getGeoLoc(), ot.getGeoLoc());
	}

	/** Check if an r_node is an access node */
	static public boolean isAccess(R_Node r_node) {
		return r_node.getNodeType() == R_NodeType.ACCESS.ordinal();
	}

	/** Test if an r_node (en) links with another r_node (ex) */
	static public boolean isExitLink(R_Node ex, R_Node en) {
		return isMatchingEntrance(ex, en) || isMatchingAccess(ex, en);
	}

	/** Test if an r_node (en) is a matching entrance for (ex) */
	static protected boolean isMatchingEntrance(R_Node ex, R_Node en) {
		return isEntrance(en) &&
		       GeoLocHelper.rampMatches(ex.getGeoLoc(), en.getGeoLoc());
	}

	/** Test if an r_node (ac) is a matching access for (ex) */
	static protected boolean isMatchingAccess(R_Node ex, R_Node ac) {
		return isAccess(ac) && GeoLocHelper.accessMatches(
		       ex.getGeoLoc(), ac.getGeoLoc());
	}

	/** Check if an R_Node has detection (not abandoned) */
	static public boolean hasDetection(R_Node r_node) {
		Iterator<Detector> it = DetectorHelper.iterator();
		while(it.hasNext()) {
			Detector d = it.next();
			if(d.getR_Node() == r_node &&
			  (!d.getAbandoned()) &&
			   DetectorHelper.isActive(d))
				return true;
		}
		return false;
	}

	/** Check if a node is at a station break */
	static public boolean isStationBreak(R_Node r_node) {
		return r_node.getActive() &&
		       isStation(r_node) &&
		       hasDetection(r_node);
	}

	/** Check if a node should be joined with a segment */
	static public boolean isJoined(R_Node r_node) {
		return r_node.getTransition() !=
		       R_NodeTransition.COMMON.ordinal() ||
		       r_node.getNodeType() != R_NodeType.ENTRANCE.ordinal();
	}

	/** Get the roadway corridor name */
	static public String getCorridorName(R_Node r_node) {
		GeoLoc loc = r_node.getGeoLoc();
		return GeoLocHelper.getCorridorName(loc);
	}
}
