/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2024  Minnesota Department of Transportation
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

import java.util.HashMap;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.tms.CommLink;
import us.mn.state.dot.tms.CommProtocol;
import us.mn.state.dot.tms.DeviceRequest;
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

	/** Mapping of all query event data collectors on line */
	private final HashMap<ControllerImpl, OpQueryEvents> collectors =
		new HashMap<ControllerImpl, OpQueryEvents>();

	/** Create a new SS125 poller */
	public SS125Poller(CommLink link, CommProtocol cp) {
		super(link, TCP, SS125_LOG);
		protocol = cp;
	}

	/** Send device request to a controller.
	 * @param c Controller to poll. */
	@Override
	public void sendRequest(ControllerImpl c, DeviceRequest r) {
		switch (r) {
		case RESET_DEVICE:
			addOp(new OpSendSensorSettings(c, true));
			break;
		case SEND_SETTINGS:
			addOp(new OpSendSensorSettings(c, false));
			break;
		default:
			break;
		}
	}

	/** Send settings to a controller */
	@Override
	public void sendSettings(ControllerImpl c, PriorityLevel p) {
		addOp(new OpSendSensorSettings(p, c, true));
	}

	/** Query binned interval data.
	 * @param c Controller to poll.
	 * @param per_sec Binning interval in seconds. */
	@Override
	public void querySamples(ControllerImpl c, int per_sec) {
		if (c.getPollPeriodSec() == per_sec) {
			if (protocol == CommProtocol.SS_125_VLOG) {
				OpQueryEvents oqe = getVehicleOp(c);
				if (oqe != null)
					oqe.updateCounters(per_sec);
			} else
				addOp(new OpQueryBinned(c, per_sec));
		}
	}

	/** Get query vehicle event operation for a controller */
	private synchronized OpQueryEvents getVehicleOp(ControllerImpl c) {
		final OpQueryEvents oqe = collectors.get(c);
		if (oqe == null || oqe.isDone()) {
			OpQueryEvents op = new OpQueryEvents(c);
			collectors.put(c, op);
			addOp(op);
		}
		return oqe;
	}
}
