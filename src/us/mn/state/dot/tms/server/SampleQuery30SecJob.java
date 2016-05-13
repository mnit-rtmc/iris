/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2016  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.server.comm.SamplePoller;

/**
 * Job to query 30-second sample data
 *
 * @author Douglas Lau
 */
public class SampleQuery30SecJob extends Job {

	/** Seconds to offset each poll from start of interval */
	static public final int OFFSET_SECS = 8;

	/** Create a new 30-second timer job */
	public SampleQuery30SecJob() {
		super(Calendar.SECOND, 30, Calendar.SECOND, OFFSET_SECS);
	}

	/** Perform the 30-second timer job */
	@Override
	public void perform() {
		querySample30Sec();
	}

	/** Poll all sampling controllers 30-second interval */
	private void querySample30Sec() {
		Iterator<Controller> it = ControllerHelper.iterator();
		while (it.hasNext()) {
			Controller c = it.next();
			if (c instanceof ControllerImpl)
				querySample30Sec((ControllerImpl) c);
		}
	}

	/** Query 30-second sample data from one controller */
	private void querySample30Sec(ControllerImpl c) {
		if (c.hasActiveDetector()) {
			DevicePoller dp = c.getPoller();
			if (dp instanceof SamplePoller) {
				SamplePoller sp = (SamplePoller) dp;
				sp.querySamples(c, 30);
			}
		}
	}
}
