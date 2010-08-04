/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2010  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.Detector;
import us.mn.state.dot.tms.DetectorHelper;
import us.mn.state.dot.tms.WeatherSensor;
import us.mn.state.dot.tms.WeatherSensorHelper;

/**
 * Job to flush sample data to disk.
 *
 * @author Douglas Lau
 */
public class FlushSamplesJob extends Job {

	/** Flush debug log */
	static protected final IDebugLog FLUSH_LOG = new IDebugLog("flush");

	/** Create a new flush samples job */
	public FlushSamplesJob() {
		super(Calendar.MINUTE, 2);
	}

	/** Perform the flush samples job */
	public void perform() {
		FLUSH_LOG.log("Starting FLUSH");
		flushDetectorSamples();
		flushWeatherSamples();
		FLUSH_LOG.log("Finished FLUSH");
	}

	/** Flush detector sample data to disk */
	protected void flushDetectorSamples() {
		DetectorHelper.find(new Checker<Detector>() {
			public boolean check(Detector det) {
				if(det instanceof DetectorImpl)
					((DetectorImpl)det).flush();
				return false;
			}
		});
	}

	/** Flush weather sample data to disk */
	protected void flushWeatherSamples() {
		WeatherSensorHelper.find(new Checker<WeatherSensor>() {
			public boolean check(WeatherSensor ws) {
				if(ws instanceof WeatherSensorImpl)
					((WeatherSensorImpl)ws).flush();
				return false;
			}
		});
	}
}
