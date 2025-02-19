/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2025  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.adectdc;

import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.OpController;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation for ADEC TDC device
 *
 * @author Douglas Lau
 */
abstract public class OpTdc extends OpController<TdcProperty> {

	/** Create a new TDC operation */
	protected OpTdc(PriorityLevel p, ControllerImpl c) {
		super(p, c);
	}

	/** Log a property query */
	protected void logQuery(TdcProperty prop) {
		if (TdcPoller.TDC_LOG.isOpen()) {
			TdcPoller.TDC_LOG.log(controller.getName() + ": " +
				prop);
		}
	}

	/** Log a property store */
	protected void logStore(TdcProperty prop) {
		if (TdcPoller.TDC_LOG.isOpen()) {
			TdcPoller.TDC_LOG.log(controller.getName() + ":= " +
				prop);
		}
	}
}
