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
package us.mn.state.dot.tms.server.comm.dinrelay;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.Lcs;
import us.mn.state.dot.tms.LcsHelper;
import us.mn.state.dot.tms.LcsState;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.LcsImpl;
import us.mn.state.dot.tms.server.comm.OpDevice;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * An LCS array operation.
 *
 * @author Douglas Lau
 */
abstract public class OpLCS extends OpDevice<DinRelayProperty> {

	/** LCS array to query */
	protected final LcsImpl lcs;

	/** Set of controllers for the LCS array */
	protected final Set<ControllerImpl> controllers;

	/** Create a new LCS operation */
	protected OpLCS(PriorityLevel p, LcsImpl l) {
		super(p, l);
		lcs = l;
		controllers = lookupControllers();
	}

	/** Lookup the set of controllers for an LCS array */
	private Set<ControllerImpl> lookupControllers() {
		HashSet<ControllerImpl> set = new HashSet<ControllerImpl>();
		LcsState[] states = LcsHelper.lookupStates(lcs);
		for (int i = 0; i < states.length; i++) {
			LcsState ls = states[i];
			Controller c = ls.getController();
			if (c instanceof ControllerImpl)
				set.add((ControllerImpl) c);
		}
		return set;
	}
}
