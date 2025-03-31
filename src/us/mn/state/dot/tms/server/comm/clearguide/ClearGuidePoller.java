/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2021  Iteris Inc.
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
package us.mn.state.dot.tms.server.comm.clearguide;

import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.tms.CommLink;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.FeedPoller;
import us.mn.state.dot.tms.server.comm.ThreadedPoller;
import static us.mn.state.dot.tms.utils.URIUtil.HTTP;

/**
 * Poller for communicating with an Iteris ClearGuide instance.
 *
 * @author Michael Darter
 */
public class ClearGuidePoller extends ThreadedPoller<ClearGuideProperty>
	implements FeedPoller
{
	/** Debug log */
	static protected final DebugLog CG_LOG = new DebugLog("clearguide");

	/** Container for DMS and associated data */
	static public DmsContainer cg_dms = new DmsContainer();

	/** Write a message to the protocol log */
	static protected void slog(String msg) {
		if (CG_LOG.isOpen())
			CG_LOG.log(msg);
	}

	/** ClearGuide authentication tokens */
	protected Tokens cg_tokens = new Tokens();

	/** Write a message to the protocol log */
	@Override
	public void log(String msg) {
		slog("ClearGuidePoller: " + msg);
	}

	/** Create new poller */
	public ClearGuidePoller(CommLink cl) {
		super(cl, HTTP, CG_LOG);
		log("ClearGuidePoller: created");
	}

	/** Create a comm thread */
	@Override
	protected ClearGuideThread createCommThread(String uri,
		int timeout, int nrd)
	{
		return new ClearGuideThread(this, queue, scheme,
			uri, timeout, nrd, CG_LOG);
	}

	/** Query messages */
	@Override
	public void queryFeed(ControllerImpl ci) {
		addOp(new OpRead(cg_tokens, ci, name));
	}
}
