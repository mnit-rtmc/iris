/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2016  Minnesota Department of Transportation
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

import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.MessagePoller;
import us.mn.state.dot.tms.server.comm.Messenger;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import us.mn.state.dot.tms.server.comm.SamplePoller;

/**
 * DR500Poller is an implementation of the Houston Radar DR500 protocol.
 *
 * @author Douglas Lau
 */
public class DR500Poller extends MessagePoller<DR500Property>
	implements SamplePoller
{
	/** DR500 debug log */
	static private final DebugLog DR500_LOG = new DebugLog("dr500");

	/** Create a new DR500 poller */
	public DR500Poller(String n, Messenger m) {
		super(n, m, DR500_LOG);
	}

	/** Perform a controller download */
	@Override
	protected void download(ControllerImpl c, PriorityLevel p) {
		if (c.isActive())
			addOp(new OpSendSensorSettings(p, c));
	}

	/** Perform a controller reset */
	@Override
	public void resetController(ControllerImpl c) {
		addOp(new OpSendSensorSettings(c));
	}

	/** Send sample settings to a controller */
	@Override
	public void sendSettings(ControllerImpl c) {
		addOp(new OpSendSensorSettings(c));
	}

	/** Query sample data.
 	 * @param c Controller to poll.
 	 * @param p Sample period in seconds. */
	@Override
	public void querySamples(ControllerImpl c, int p) {
		if (p == 30)
			addOp(new OpQuerySpeed(c, p));
	}
}
