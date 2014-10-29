/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.server.DeviceImpl;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import us.mn.state.dot.tms.server.comm.OpDevice;
import static us.mn.state.dot.tms.server.comm.mndot.Op170.MNDOT_LOG;

/**
 * 170 Device operation
 *
 * @author Douglas Lau
 */
abstract public class Op170Device extends OpDevice {

	/** Log an error msg */
	protected void logError(String msg) {
		if (MNDOT_LOG.isOpen())
			MNDOT_LOG.log(controller.getName() + "! " + msg);
	}

	/** Log a property query */
	protected void logQuery(MndotProperty prop) {
		if (MNDOT_LOG.isOpen())
			MNDOT_LOG.log(controller.getName() + ": " + prop);
	}

	/** Log a property store */
	protected void logStore(MndotProperty prop) {
		if (MNDOT_LOG.isOpen())
			MNDOT_LOG.log(controller.getName() + ":= " + prop);
	}

	/** Create a new 170 device operation */
	protected Op170Device(PriorityLevel p, DeviceImpl d) {
		super(p, d);
	}
}
