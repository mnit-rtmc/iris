/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2025  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.mndot;

import us.mn.state.dot.tms.server.LcsImpl;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * An LCS array operation.
 *
 * @author Douglas Lau
 */
abstract public class OpLCS extends Op170Device {

	/** LCS array to query */
	protected final LcsImpl lcs;

	/** Create a new LCS operation */
	protected OpLCS(PriorityLevel p, LcsImpl l) {
		super(p, l);
		lcs = l;
	}
}
