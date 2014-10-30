/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2014  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ss105;

import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.OpController;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import static us.mn.state.dot.tms.server.comm.ss105.SS105Poller.SS105_LOG;

/**
 * Operation for SS105 device
 *
 * @author Douglas Lau
 */
abstract public class OpSS105 extends OpController<SS105Property> {

	/** Log an error msg */
	protected void logError(String msg) {
		if (SS105_LOG.isOpen())
			SS105_LOG.log(controller.getName() + "! " + msg);
	}

	/** Create a new SS105 operation */
	protected OpSS105(PriorityLevel p, ControllerImpl c) {
		super(p, c);
	}
}
