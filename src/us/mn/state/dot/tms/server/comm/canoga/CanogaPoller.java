/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2019  Minnesota Department of Transportation
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
	public CanogaPoller(String n) {
		super(n, TCP, CANOGA_LOG);
	}

	/** Mapping of all event data collectors on line */
	private final HashMap<ControllerImpl, OpQueryEventSamples> collectors =
		new HashMap<ControllerImpl, OpQueryEventSamples>();

	/** Perform a controller reset */
	@Override
	public void resetController(ControllerImpl c) {
		addOp(new OpQueryConfig(c));
	}

	/** Send sample settings to a controller */
	@Override
	public void sendSettings(ControllerImpl c) {
		addOp(new OpQueryConfig(c));
	}

	/** Send settings to a controller */
	@Override
	public void sendSettings(ControllerImpl c, PriorityLevel p) {
		addOp(new OpQueryConfig(p, c));
	}

	/** Query sample data.
 	 * @param c Controller to poll.
 	 * @param p Sample period in seconds. */
	@Override
	public void querySamples(ControllerImpl c, int p) {
		if (c.getPollPeriod() == p) {
			c.binEventSamples(p);
			updateOpCounters(c);
		}
	}

	/** Update operation counters for a controller */
	private synchronized void updateOpCounters(final ControllerImpl c) {
		OpQueryEventSamples qes = collectors.get(c);
		if (qes != null)
			qes.updateCounters();
		if (qes == null || qes.isDone()) {
			qes = new OpQueryEventSamples(c);
			collectors.put(c, qes);
			addOp(qes);
		}
	}
}
