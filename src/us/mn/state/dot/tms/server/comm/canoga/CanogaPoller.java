/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2016  Minnesota Department of Transportation
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

import java.util.Iterator;
import java.util.LinkedList;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.CommThread;
import us.mn.state.dot.tms.server.comm.Messenger;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import us.mn.state.dot.tms.server.comm.SamplePoller;

/**
 * CanogaPoller is a java implementation of the Canoga (tm) Traffic Sensing
 * System serial communication protocol
 *
 * @author Douglas Lau
 */
public class CanogaPoller extends CommThread<CanogaProperty>
	implements SamplePoller
{
	/** Canoga debug log */
	static protected final DebugLog CANOGA_LOG = new DebugLog("canoga");

	/** Create a new Canoga poller */
	public CanogaPoller(String n, Messenger m) {
		super(n, m, CANOGA_LOG);
	}

	/** Perform a controller download */
	@Override
	public void download(ControllerImpl c, PriorityLevel p) {
		if (c.isActive())
			addOp(new OpQueryConfig(p, c));
	}

	/** List of all event data collectors on line */
	private final LinkedList<OpQueryEventSamples> collectors =
		new LinkedList<OpQueryEventSamples>();

	/** Find an existing event collector operation */
	private OpQueryEventSamples findEventCollector(final ControllerImpl c) {
		Iterator<OpQueryEventSamples> it = collectors.iterator();
		while (it.hasNext()) {
			OpQueryEventSamples qes = it.next();
			if (qes.isDone()) {
				qes.updateCounters();
				it.remove();
			} else if (qes.getController() == c)
				return qes;
		}
		return null;
	}

	/** Get an event collector operation */
	private OpQueryEventSamples getEventCollector(final ControllerImpl c) {
		OpQueryEventSamples qes = findEventCollector(c);
		if (qes != null) {
			qes.updateCounters();
			return qes;
		} else {
			qes = new OpQueryEventSamples(c);
			collectors.add(qes);
			addOp(qes);
			return qes;
		}
	}

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

	/** Query sample data.
 	 * @param c Controller to poll.
 	 * @param p Sample period in seconds. */
	@Override
	public void querySamples(ControllerImpl c, int p) {
		OpQueryEventSamples qes = getEventCollector(c);
		if (p == 30)
			qes.binSamples();
	}
}
