/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.dxm;

import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.BasePoller;
import us.mn.state.dot.tms.server.comm.Operation;
import us.mn.state.dot.tms.server.comm.OpStep;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import us.mn.state.dot.tms.server.comm.SamplePoller;
import static us.mn.state.dot.tms.utils.URIUtil.TCP;

/**
 * DXMPoller is an implementation of the Banner DXM protocol.
 *
 * @author Douglas Lau
 */
public class DXMPoller extends BasePoller implements SamplePoller {

	/** Create a new DXM poller */
	public DXMPoller(String n) {
		super(n, TCP, true);
	}

	/** Create an operation */
	private void createOp(String n, ControllerImpl c, OpStep s) {
		Operation op = new Operation(n, c, s);
		op.setPriority(PriorityLevel.DATA_30_SEC);
		addOp(op);
	}

	/** Perform a controller reset */
	@Override
	public void resetController(ControllerImpl c) { }

	/** Send sample settings to a controller */
	@Override
	public void sendSettings(ControllerImpl c) { }

	/** Query sample data.
 	 * @param c Controller to poll.
 	 * @param p Sample period in seconds. */
	@Override
	public void querySamples(ControllerImpl c, int p) {
		createOp("detector.op.query.samples", c, new OpQuerySamples(p));
	}
}
