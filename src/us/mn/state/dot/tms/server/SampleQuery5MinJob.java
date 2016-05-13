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
 * Job to query 5-minute sample data
 *
 * @author Douglas Lau
 */
public class SampleQuery5MinJob extends Job {

	/** Seconds to offset each poll from start of interval */
	static private final int OFFSET_SECS = 12;

	/** Create a new 5-minute timer job */
	public SampleQuery5MinJob() {
		super(Calendar.MINUTE, 5, Calendar.SECOND, OFFSET_SECS);
	}

	/** Perform the 5-minute timer job */
	@Override
	public void perform() {
		querySample5Min();
	}

	/** Poll all controllers 5 minute interval */
	private void querySample5Min() {
		Iterator<Controller> it = ControllerHelper.iterator();
		while (it.hasNext()) {
			Controller c = it.next();
			if (c instanceof ControllerImpl)
				querySample5Min((ControllerImpl) c);
		}
	}

	/** Query 5-minute sample data from one controller */
	private void querySample5Min(ControllerImpl c) {
		// Must check hasActiveMeter for green counts (mndot protocol)
		if (c.hasActiveDetector() || c.hasActiveMeter()) {
			DevicePoller dp = c.getPoller();
			if (dp instanceof SamplePoller) {
				SamplePoller sp = (SamplePoller) dp;
				sp.querySamples(c, 300);
			}
		}
	}
}
