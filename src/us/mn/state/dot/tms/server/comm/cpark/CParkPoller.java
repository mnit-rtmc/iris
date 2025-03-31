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
package us.mn.state.dot.tms.server.comm.cpark;

import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.tms.CommLink;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.SamplePoller;
import us.mn.state.dot.tms.server.comm.ThreadedPoller;
import static us.mn.state.dot.tms.utils.URIUtil.HTTPS;

/**
 * CParkPoller is a reader for Drivewyze Central Park JSON.
 *
 * @author Douglas Lau
 */
public class CParkPoller extends ThreadedPoller<CParkProp>
	implements SamplePoller
{
	/** Central park debug log */
	static private final DebugLog CPARK_LOG = new DebugLog("cpark");

	/** Log a message to the debug log */
	static public void slog(String msg) {
		CPARK_LOG.log(msg);
	}

	/** Create a new CPark poller */
	public CParkPoller(CommLink link) {
		super(link, HTTPS, CPARK_LOG);
	}

	/** Create a comm thread */
	@Override
	protected CParkThread createCommThread(String uri, int timeout,
		int nrd)
	{
		return new CParkThread(this, queue, scheme, uri, timeout, nrd,
			CPARK_LOG);
	}

	/** Send device request to a controller.
	 * @param c Controller to poll. */
	@Override
	public void sendRequest(ControllerImpl c, DeviceRequest r) {
		// nothing to do?
	}

	/** Query sample data.
	 * @param c Controller to poll.
	 * @param per_sec Sample period in seconds. */
	@Override
	public void querySamples(ControllerImpl c, int per_sec) {
		if (c.getPollPeriodSec() == per_sec)
			addOp(new OpQuerySpots(c, per_sec));
	}
}
