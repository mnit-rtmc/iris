/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.incfeed;

import java.io.IOException;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.IncidentCache;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.OpController;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * This operation reads the incident feed.
 *
 * @author Douglas Lau
 */
public class OpReadIncFeed extends OpController<IncFeedProperty> {

	/** Feed name */
	private final String feed;

	/** Incident cache */
	private final IncidentCache cache;

	/** Log a message */
	private void logMsg(String msg) {
		IncFeedPoller.log(feed + ": " + msg);
	}

	/** Create a new operation to read incident feed */
	protected OpReadIncFeed(ControllerImpl c, String fid, IncidentCache ic){
		super(PriorityLevel.DATA_30_SEC, c);
		feed = fid;
		cache = ic;
		logMsg("Polling feed");
	}

	/** Create the first phase of the operation */
	@Override
	protected Phase<IncFeedProperty> phaseOne() {
		return new PhaseReadIncFeed();
	}

	/** Phase to read the incident feed */
	protected class PhaseReadIncFeed extends Phase<IncFeedProperty> {

		/** Execute the phase */
		protected Phase<IncFeedProperty> poll(
			CommMessage<IncFeedProperty> mess) throws IOException
		{
			mess.add(new IncFeedProperty(cache));
			mess.queryProps();
			return null;
		}
	}

	/** Handle a communication error */
	@Override
	public void handleCommError(EventType et, String msg) {
		logMsg("ERROR: " + msg);
		super.handleCommError(et, msg);
	}

	/** Cleanup the operation */
	@Override
	public void cleanup() {
		logMsg("Finished feed");
		super.cleanup();
	}
}
