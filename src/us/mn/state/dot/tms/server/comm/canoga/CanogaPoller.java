/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2024  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.canoga;

import java.util.HashMap;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.tms.CommLink;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import us.mn.state.dot.tms.server.comm.SamplePoller;
import us.mn.state.dot.tms.server.comm.ThreadedPoller;
import static us.mn.state.dot.tms.utils.URIUtil.TCP;

/**
 * CanogaPoller is a java implementation of the Canoga (tm) Traffic Sensing
 * System serial communication protocol
 *
 * @author Douglas Lau
 */
public class CanogaPoller extends ThreadedPoller<CanogaProperty>
	implements SamplePoller
{
	/** Canoga debug log */
	static protected final DebugLog CANOGA_LOG = new DebugLog("canoga");

	/** Create a new Canoga poller */
	public CanogaPoller(CommLink link) {
		super(link, TCP, CANOGA_LOG);
	}

	/** Mapping of all event data collectors on line */
	private final HashMap<ControllerImpl, OpQueryEventSamples> collectors =
		new HashMap<ControllerImpl, OpQueryEventSamples>();

	/** Send device request to a controller.
	 * @param c Controller to poll. */
	@Override
	public void sendRequest(ControllerImpl c, DeviceRequest r) {
		switch (r) {
		case RESET_DEVICE:
			addOp(new OpQueryConfig(c));
			break;
		case SEND_SETTINGS:
			addOp(new OpQueryConfig(c));
			break;
		default:
			break;
		}
	}

	/** Send settings to a controller */
	@Override
	public void sendSettings(ControllerImpl c, PriorityLevel p) {
		addOp(new OpQueryConfig(p, c));
	}

	/** Query sample data.
	 * @param c Controller to poll.
	 * @param per_sec Sample period in seconds. */
	@Override
	public void querySamples(ControllerImpl c, int per_sec) {
		if (c.getPollPeriodSec() == per_sec) {
			OpQueryEventSamples qes = getEventOp(c);
			if (qes != null)
				qes.updateCounters(per_sec);
		}
	}

	/** Get event operation for a controller */
	private synchronized OpQueryEventSamples getEventOp(ControllerImpl c) {
		final OpQueryEventSamples qes = collectors.get(c);
		if (qes == null || qes.isDone()) {
			OpQueryEventSamples op = new OpQueryEventSamples(c);
			collectors.put(c, op);
			addOp(op);
		}
		return qes;
	}
}
