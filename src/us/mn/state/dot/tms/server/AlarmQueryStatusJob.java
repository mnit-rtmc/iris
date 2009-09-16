/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sched.Scheduler;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.ControllerHelper;
import us.mn.state.dot.tms.server.comm.AlarmPoller;
import us.mn.state.dot.tms.server.comm.MessagePoller;

/**
 * Job to query alarm status
 *
 * @author Douglas Lau
 */
public class AlarmQueryStatusJob extends Job {

	/** Seconds to offset each poll from start of interval */
	static protected final int OFFSET_SECS = 1;

	/** Create a new job to query alarm status */
	public AlarmQueryStatusJob() {
		super(Calendar.MINUTE, 5, Calendar.SECOND, OFFSET_SECS);
	}

	/** Perform the alarm query status timer job */
	public void perform() {
		ControllerHelper.find(new Checker<Controller>() {
			public boolean check(Controller c) {
				if(c instanceof ControllerImpl)
					queryAlarms((ControllerImpl)c);
				return false;
			}
		});
	}

	/** Query alarm status from one controller */
	protected void queryAlarms(ControllerImpl c) {
		MessagePoller p = c.getPoller();
		if(p instanceof AlarmPoller) {
			AlarmPoller ap = (AlarmPoller)p;
			ap.queryAlarms(c);
		}
	}
}
