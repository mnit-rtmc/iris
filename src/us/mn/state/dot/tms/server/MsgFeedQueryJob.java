/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011-2014  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server;

import java.util.Calendar;
import java.util.Iterator;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.ControllerHelper;
import us.mn.state.dot.tms.server.comm.DevicePoller;
import us.mn.state.dot.tms.server.comm.msgfeed.MsgFeedPoller;

/**
 * Job to query message feeds for DMS messages.
 *
 * @author Douglas Lau
 */
public class MsgFeedQueryJob extends Job {

	/** Seconds to offset each poll from start of interval.  This should be
	 * 5 seconds before performing the ActionPlanJob. */
	static private final int OFFSET_SECS =
		(ActionPlanJob.OFFSET_SECS + 30 - 5) % 30;

	/** Create a new message feed query job */
	protected MsgFeedQueryJob() {
		super(Calendar.SECOND, 30, Calendar.SECOND, OFFSET_SECS);
	}

	/** Perform the message feed query job */
	public void perform() {
		FeedBucket.purgeExpired();
		Iterator<Controller> it = ControllerHelper.iterator();
		while(it.hasNext()) {
			Controller c = it.next();
			if(c instanceof ControllerImpl)
				queryMsgFeed((ControllerImpl)c);
		}
	}

	/** Query message feed from one controller */
	private void queryMsgFeed(ControllerImpl c) {
		if (c.isMsgFeed()) {
			DevicePoller dp = c.getPoller();
			if (dp instanceof MsgFeedPoller) {
				MsgFeedPoller mfp = (MsgFeedPoller)dp;
				mfp.queryMessages(c);
			}
		}
	}
}
