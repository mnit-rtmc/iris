/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012  Iteris Inc.
 * Copyright (C) 2012-2024  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.g4;

import java.util.HashMap;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.tms.CommLink;
import us.mn.state.dot.tms.CommProtocol;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.SamplePoller;
import us.mn.state.dot.tms.server.comm.ThreadedPoller;
import static us.mn.state.dot.tms.utils.URIUtil.TCP;

/**
 * G4Poller is a java implementation of the RTMS G4 VDS protocol.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public class G4Poller extends ThreadedPoller<G4Property>
	implements SamplePoller
{
	/** Debug log */
	static private final DebugLog G4_LOG = new DebugLog("g4");

	/** Communication protocol */
	private final CommProtocol protocol;

	/** Mapping of all per vehicle data collectors on line */
	private final HashMap<ControllerImpl, OpPerVehicle> collectors =
		new HashMap<ControllerImpl, OpPerVehicle>();

	/** Create a new G4 poller */
	public G4Poller(CommLink link, CommProtocol cp) {
		super(link, TCP, G4_LOG);
		protocol = cp;
	}

	/** Send device request to a controller.
	 * @param c Controller to poll. */
	@Override
	public void sendRequest(ControllerImpl c, DeviceRequest r) {
		switch (r) {
		case RESET_DEVICE:
			addOp(new OpSendSensorSettings(c, protocol, true));
			break;
		case SEND_SETTINGS:
			addOp(new OpSendSensorSettings(c, protocol, false));
			break;
		default:
			break;
		}
	}

	/** Query sample data.
	 * @param c Controller to poll.
	 * @param per_sec Sample period in seconds. */
	@Override
	public void querySamples(ControllerImpl c, int per_sec) {
		if (c.getPollPeriodSec() == per_sec) {
			if (protocol == CommProtocol.RTMS_G4_VLOG) {
				OpPerVehicle opv = getVehicleOp(c);
				if (opv != null)
					opv.updateCounters(per_sec);
			} else
				addOp(new OpQueryStats(c, per_sec));
		}
	}

	/** Get per vehicle operation for a controller */
	private synchronized OpPerVehicle getVehicleOp(ControllerImpl c) {
		final OpPerVehicle opv = collectors.get(c);
		if (opv == null || opv.isDone()) {
			OpPerVehicle op = new OpPerVehicle(c);
			collectors.put(c, op);
			addOp(op);
		}
		return opv;
	}
}
