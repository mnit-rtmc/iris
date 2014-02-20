/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012-2014  Minnesota Department of Transportation
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

import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.OpController;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation for DIN relay controller.
 *
 * @author Douglas Lau
 */
abstract public class OpDinRelay extends OpController<DinRelayProperty> {

	/** DIN relay debug log */
	static final DebugLog DIN_LOG = new DebugLog("dinrelay");

	/** Log a property query */
	protected void logQuery(DinRelayProperty prop) {
		if(DIN_LOG.isOpen())
			DIN_LOG.log(controller.getName() + ": " + prop);
	}

	/** Log a property store */
	protected void logStore(DinRelayProperty prop) {
		if(DIN_LOG.isOpen())
			DIN_LOG.log(controller.getName() + ":= " + prop);
	}

	/** Create a new DIN relay operation */
	protected OpDinRelay(PriorityLevel p, ControllerImpl c) {
		super(p, c);
	}
}
