/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2016  Minnesota Department of Transportation
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

import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import us.mn.state.dot.tms.server.comm.SamplePoller;
import us.mn.state.dot.tms.server.comm.ThreadedPoller;
import static us.mn.state.dot.tms.utils.URIUtil.TCP;

/**
 * SS105Poller is a java implementation of the Wavetronix SmartSensor 105
 * serial data communication protocol.
 *
 * @author Douglas Lau
 */
public class SS105Poller extends ThreadedPoller<SS105Property>
	implements SamplePoller
{
	/** SS 105 debug log */
	static protected final DebugLog SS105_LOG = new DebugLog("ss105");

	/** Create a new SS105 poller */
	public SS105Poller(String n) {
		super(n, TCP, SS105_LOG);
	}

	/** Perform a controller reset */
	@Override
	public void resetController(ControllerImpl c) {
		addOp(new OpSendSensorSettings(c, true));
	}

	/** Send sample settings to a controller */
	@Override
	public void sendSettings(ControllerImpl c) {
		addOp(new OpSendSensorSettings(c, false));
	}

	/** Send sample settings to a controller */
	@Override
	public void sendSettings(ControllerImpl c, PriorityLevel p) {
		addOp(new OpSendSensorSettings(p, c, true));
	}

	/** Query sample data.
 	 * @param c Controller to poll.
 	 * @param p Sample period in seconds. */
	@Override
	public void querySamples(ControllerImpl c, int p) {
		addOp(new OpQuerySamples(c, p));
	}
}
