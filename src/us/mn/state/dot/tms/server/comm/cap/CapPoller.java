/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group, Inc.
 * Copyright (C) 2021  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.cap;

import us.mn.state.dot.tms.CommLink;
import static us.mn.state.dot.tms.server.CapAlert.LOG;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.CommThread;
import us.mn.state.dot.tms.server.comm.FeedPoller;
import us.mn.state.dot.tms.server.comm.ThreadedPoller;
import static us.mn.state.dot.tms.utils.URIUtil.HTTPS;

/**
 * Common Alerting Protocol (CAP) poller, which periodically retrieves emergency
 * alerts from a feed, such as IPAWS-OPEN, a system managed by FEMA to help
 * disseminate alerts to the public.
 *
 * For IPAWS, administrators must specify a URL that contains an identification
 * number obtained from FEMA that is unique to each organization.
 *
 * @author Gordon Parikh
 * @author Douglas Lau
 */
public class CapPoller extends ThreadedPoller<CapProperty> implements FeedPoller
{
	/** Log a message to the debug log */
	static public void slog(String msg) {
		LOG.log(msg);
	}

	/** Create a new poller */
	public CapPoller(CommLink link) {
		super(link, HTTPS, LOG);
	}

	/** Create a comm thread */
	@Override
	protected CapThread createCommThread(String uri, int timeout, int nrd) {
		return new CapThread(this, queue, scheme, uri, timeout, nrd,
			LOG);
	}

	/** Query feed for alert messages */
	@Override
	public void queryFeed(ControllerImpl c) {
		slog("creating OpReadCap: " + c);
		addOp(new OpReadCap(c, name));
	}

	/** Run a test of the alert processing system.
	 *
	 *  Note that this does not test communication with the feed itself,
	 *  which is assumed to be working and relatively easy to verify/debug,
	 *  but instead tests the machinery in IRIS to read, parse, process, and
	 *  deploy these alerts to DMS. */
	public void startTesting(ControllerImpl c) {
		slog("creating OpTestCap: " + c);
		addOp(new OpTestCap(c, name));
	}
}
