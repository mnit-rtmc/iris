/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2002-2011  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.msgfeed;

import java.io.IOException;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.OpController;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * This operation reads the message feed and sets new DMS messages.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class OpReadMsgFeed extends OpController {

	/** Feed name */
	protected final String feed;

	/** Create a new operation to read msg feed */
	protected OpReadMsgFeed(ControllerImpl c, String fid) {
		super(PriorityLevel.DATA_30_SEC, c);
		feed = fid;
		MsgFeedPoller.log("Polling feed " + feed);
	}

	/** Begin the operation */
	public final boolean begin() {
		phase = new PhaseReadMsgFeed();
		return true;
	}

	/** Phase to read the message feed */
	protected class PhaseReadMsgFeed extends Phase {

		/** Execute the phase */
		protected Phase poll(CommMessage m) throws IOException {
			if(m instanceof Message) {
				Message mess = (Message)m;
				mess.add(new MsgFeedProperty(feed));
				mess.queryProps();
			}
			return null;
		}
	}

	/** Handle a communication error */
	public void handleCommError(EventType et, String msg) {
		MsgFeedPoller.log("ERROR: " + msg);
		super.handleCommError(et, msg);
	}

	/** Cleanup the operation */
	public void cleanup() {
		MsgFeedPoller.log("Finished feed " + feed);
		super.cleanup();
	}
}
