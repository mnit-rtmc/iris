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

import java.util.HashMap;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.tms.CommLink;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.SamplePoller;
import us.mn.state.dot.tms.server.comm.ThreadedPoller;
import static us.mn.state.dot.tms.utils.URIUtil.TCP;

/**
 * TdcPoller is a java implementation of the ADEC TDC detector protocol.
 *
 * @author Douglas Lau
 */
public class TdcPoller extends ThreadedPoller<TdcProperty>
	implements SamplePoller
{
	/** Debug log */
	static public final DebugLog TDC_LOG = new DebugLog("tdc");

	/** Mapping of all per vehicle data collectors on line */
	private final HashMap<ControllerImpl, OpPerVehicle> collectors =
		new HashMap<ControllerImpl, OpPerVehicle>();

	/** Create a new TDC poller */
	public TdcPoller(CommLink link) {
		super(link, TCP, TDC_LOG);
	}

	/** Send device request to a controller.
	 * @param c Controller to poll. */
	@Override
	public void sendRequest(ControllerImpl c, DeviceRequest r) {
		// FIXME
	}

	/** Query sample data.
	 * @param c Controller to poll.
	 * @param per_sec Sample period in seconds. */
	@Override
	public void querySamples(ControllerImpl c, int per_sec) {
		if (c.getPollPeriodSec() == per_sec) {
			OpPerVehicle opv = getVehicleOp(c);
			if (opv != null)
				opv.updateCounters(per_sec);
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
