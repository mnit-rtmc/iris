/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010  Minnesota Department of Transportation
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
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.ControllerHelper;
import us.mn.state.dot.tms.server.comm.MessagePoller;
import us.mn.state.dot.tms.server.comm.WeatherPoller;

/**
 * Job to query weather sample data
 *
 * @author Douglas Lau
 */
public class WeatherQueryJob extends Job {

	/** Seconds to offset each poll from start of interval */
	static protected final int OFFSET_SECS = 15;

	/** Create a new weather sample job */
	public WeatherQueryJob() {
		super(Calendar.SECOND, 60, Calendar.SECOND, OFFSET_SECS);
	}

	/** Perform the job */
	public void perform() {
		ControllerHelper.find(new Checker<Controller>() {
			public boolean check(Controller c) {
				if(c instanceof ControllerImpl)
					queryWeather((ControllerImpl)c);
				return false;
			}
		});
	}

	/** Query weather sample data from one controller */
	protected void queryWeather(ControllerImpl c) {
		MessagePoller p = c.getPoller();
		if(p instanceof WeatherPoller) {
			WeatherPoller wp = (WeatherPoller)p;
			wp.queryConditions(c);
		}
	}
}
