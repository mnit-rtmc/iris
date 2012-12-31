/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2012  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.WeatherSensor;
import us.mn.state.dot.tms.WeatherSensorHelper;
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
		Iterator<WeatherSensor> it = WeatherSensorHelper.iterator();
		while(it.hasNext()) {
			WeatherSensor ws = it.next();
			if(ws instanceof WeatherSensorImpl)
				queryWeather((WeatherSensorImpl)ws);
		}
	}

	/** Query weather sample data from one sensor */
	protected void queryWeather(WeatherSensorImpl ws) {
		WeatherPoller wp = ws.getWeatherPoller();
		if(wp != null)
			wp.queryConditions(ws);
	}
}
