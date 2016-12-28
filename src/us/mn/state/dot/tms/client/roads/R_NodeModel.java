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
package us.mn.state.dot.tms.client.roads;

import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.R_NodeTransition;
import us.mn.state.dot.tms.R_NodeType;

/**
 * Model for determining the lanes and shift of an r_node.
 *
 * @author Douglas Lau
 */
public class R_NodeModel {

	/** Proxy to roadway node */
	public final R_Node r_node;

	/** Upstream node model on corridor */
	private final R_NodeModel upstream;

	/** Create a new roadway node model */
	public R_NodeModel(R_Node n, R_NodeModel u) {
		r_node = n;
		upstream = u;
	}

	/** Create a new roadway node model */
	public R_NodeModel(R_Node n) {
		this(n, null);
	}

	/** Get the upstream lane on the given side of the road.
	 * @param side True for left side, false for right side.
	 * @return Shift for the given side upstream of the node. */
	public int getUpstreamLane(boolean side) {
		R_NodeModel up = upstream;
		while (up != null) {
			if (up.isSideDefined(side))
				return up.getFogLane(side);
			else
				up = up.upstream;
		}
		return getFogLane(side);
	}

	/** Get the downstream lane on the given side of the road.
	 * @param side True for left side, false for right side.
	 * @return Shift for the given side at the node. */
	public int getDownstreamLane(boolean side) {
		if (isSideDefined(side))
			return getFogLane(side);
		else
			return getUpstreamLane(side);
	}

	/** Test if the given side is defined by the r_node.
	 * @param side True for left side, false for right side.
	 * @return True is the node defines the given side. */
	private boolean isSideDefined(boolean side) {
		R_NodeType nt = R_NodeType.fromOrdinal(r_node.getNodeType());
		return (nt == R_NodeType.STATION) ||
		       (nt == R_NodeType.INTERSECTION) ||
		       (side == r_node.getAttachSide()) ||
		       (!hasMainline());
	}

	/** Check if this r_node has mainline lanes (vs ramp only) */
	public boolean hasMainline() {
		R_NodeTransition nt = R_NodeTransition.fromOrdinal(
			r_node.getTransition());
		return nt != R_NodeTransition.COMMON;
	}

	/** Get the fog lane for the given side of the road.
	 * @param side True for left side, false for right side.
	 * @return Shift for fog line. */
	private int getFogLane(boolean side) {
		int line = r_node.getShift();
		if (side != r_node.getAttachSide()) {
			if (side)
				return line - r_node.getLanes();
			else
				return line + r_node.getLanes();
		} else
			return line;
	}

	/** Get the shift from an upstream node to this model node */
	public int getShift(R_Node up) {
		R_NodeModel other = getUpstreamModel(up);
		if (other != null) {
			return getDownstreamLane(false) -
			       other.getDownstreamLane(false);
		} else
			return 0;
	}

	/** Get a model for an upstream r_node */
	private R_NodeModel getUpstreamModel(R_Node up) {
		R_NodeModel other = upstream;
		while (other != null) {
			if (other.r_node == up)
				return other;
			other = other.upstream;
		}
		return null;
	}

	/** Get the lane offset for an upstream shift.
	 * @param sh Absolute shift.
	 * @return Offset from r_node */
	public int getUpstreamOffset(int sh) {
		return clampUpstream(sh) - getUpstreamLane(true);
	}

	/** Clamp an upstream lane */
	private int clampUpstream(int sh) {
		return Math.min(getUpstreamLane(false),
		       Math.max(getUpstreamLane(true), sh));
	}

	/** Get the lane offset for a downstream shift.
	 * @param sh Absolute shift.
	 * @return Offset from r_node */
	public int getDownstreamOffset(int sh) {
		return clampDownstream(sh) - getDownstreamLane(true);
	}

	/** Clamp a downstream lane */
	private int clampDownstream(int sh) {
		return Math.min(getDownstreamLane(false),
		       Math.max(getDownstreamLane(true), sh));
	}
}
