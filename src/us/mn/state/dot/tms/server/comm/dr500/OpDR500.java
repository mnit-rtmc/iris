/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.dr500;

import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.OpController;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import static us.mn.state.dot.tms.server.comm.dr500.DR500Poller.DR500_LOG;

/**
 * Operation for DR-500 device.
 *
 * @author Douglas Lau
 */
abstract public class OpDR500 extends OpController<DR500Property> {

	/** Log an error msg */
	protected void logError(String msg) {
		if (DR500_LOG.isOpen())
			DR500_LOG.log(controller.getName() + "! " + msg);
	}

	/** Create a new DR500 operation */
	protected OpDR500(PriorityLevel p, ControllerImpl c) {
		super(p, c);
	}
}
