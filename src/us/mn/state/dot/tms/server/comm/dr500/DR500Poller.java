/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2024  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.CommLink;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import us.mn.state.dot.tms.server.comm.SamplePoller;
import us.mn.state.dot.tms.server.comm.ThreadedPoller;
import static us.mn.state.dot.tms.utils.URIUtil.TCP;

/**
 * DR500Poller is an implementation of the Houston Radar DR500 protocol.
 *
 * @author Douglas Lau
 */
public class DR500Poller extends ThreadedPoller<DR500Property>
	implements SamplePoller
{
	/** DR500 debug log */
	static private final DebugLog DR500_LOG = new DebugLog("dr500");

	/** Create a new DR500 poller */
	public DR500Poller(CommLink link) {
		super(link, TCP, DR500_LOG);
	}

	/** Send device request to a controller.
	 * @param c Controller to poll. */
	@Override
	public void sendRequest(ControllerImpl c, DeviceRequest r) {
		switch (r) {
		case RESET_DEVICE:
			addOp(new OpSendSensorSettings(c));
			break;
		case SEND_SETTINGS:
			addOp(new OpSendSensorSettings(c));
			break;
		default:
			break;
		}
	}

	/** Send settings to a controller */
	@Override
	public void sendSettings(ControllerImpl c, PriorityLevel p) {
		addOp(new OpSendSensorSettings(p, c));
	}

	/** Query sample data.
	 * @param c Controller to poll.
	 * @param per_sec Sample period in seconds. */
	@Override
	public void querySamples(ControllerImpl c, int per_sec) {
		if (c.getPollPeriodSec() == per_sec)
			addOp(new OpQuerySpeed(c, per_sec));
	}
}
