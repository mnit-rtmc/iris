/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2021  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ss125;

import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.tms.CommLink;
import us.mn.state.dot.tms.CommProtocol;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import us.mn.state.dot.tms.server.comm.SamplePoller;
import us.mn.state.dot.tms.server.comm.ThreadedPoller;
import static us.mn.state.dot.tms.utils.URIUtil.TCP;

/**
 * SS125Poller is an implementation of the Wavetronix SmartSensor HD serial
 * data communication protocol.
 *
 * @author Douglas Lau
 */
public class SS125Poller extends ThreadedPoller<SS125Property>
	implements SamplePoller
{
	/** SS 125 debug log */
	static private final DebugLog SS125_LOG = new DebugLog("ss125");

	/** Communication protocol */
	private final CommProtocol protocol;

	/** Create a new SS125 poller */
	public SS125Poller(CommLink link, CommProtocol cp) {
		super(link, TCP, SS125_LOG);
		protocol = cp;
	}

	/** Perform a controller reset */
	@Override
	public void resetController(ControllerImpl c) {
		addOp(new OpSendSensorSettings(c, true));
	}

	/** Send sensor settings to a controller */
	@Override
	public void sendSettings(ControllerImpl c) {
		addOp(new OpSendSensorSettings(c, false));
	}

	/** Send settings to a controller */
	@Override
	public void sendSettings(ControllerImpl c, PriorityLevel p) {
		addOp(new OpSendSensorSettings(p, c, true));
	}

	/** Query binned interval data.
 	 * @param c Controller to poll.
 	 * @param p Binning interval in seconds. */
	@Override
	public void querySamples(ControllerImpl c, int p) {
		if (c.getPollPeriodSec() == p) {
			if (protocol == CommProtocol.SS_125_VLOG) {
				c.binEventSamples(p);
				addOp(new OpQueryEvents(c, p));
			} else
				addOp(new OpQueryBinned(c, p));
		}
	}
}
