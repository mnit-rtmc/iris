/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2012  Minnesota Department of Transportation
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
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.tms.Detector;
import us.mn.state.dot.tms.DetectorHelper;
import us.mn.state.dot.tms.WeatherSensor;
import us.mn.state.dot.tms.WeatherSensorHelper;
import static us.mn.state.dot.tms.Interval.MINUTE;

/**
 * Job to flush sample data to disk.
 *
 * @author Douglas Lau
 */
public class FlushSamplesJob extends Job {

	/** Flush debug log */
	static protected final IDebugLog FLUSH_LOG = new IDebugLog("flush");

	/** Number of seconds to cache periodic sample data */
	static private final long SAMPLE_CACHE_SEC = 10 * MINUTE;

	/** Number of milliseconds to cache periodic sample data */
	static private final long SAMPLE_CACHE_MS = SAMPLE_CACHE_SEC * 1000;

	/** Calculate a time stamp to purge samples */
	static private long calculatePurgeStamp() {
		return TimeSteward.currentTimeMillis() - SAMPLE_CACHE_MS;
	}

	/** Periodic sample writer */
	private final PeriodicSampleWriter writer = new PeriodicSampleWriter();

	/** Create a new flush samples job */
	public FlushSamplesJob() {
		super(Calendar.MINUTE, 2);
	}

	/** Perform the flush samples job */
	public void perform() {
		long before = calculatePurgeStamp();
		FLUSH_LOG.log("Starting FLUSH");
		flushDetectorSamples(before);
		flushWeatherSamples(before);
		FLUSH_LOG.log("Finished FLUSH");
	}

	/** Flush detector sample data to disk */
	protected void flushDetectorSamples(final long before) {
		DetectorHelper.find(new Checker<Detector>() {
			public boolean check(Detector d) {
				if(d instanceof DetectorImpl) {
					DetectorImpl det = (DetectorImpl)d;
					det.flush(writer);
					det.purge(before);
				}
				return false;
			}
		});
	}

	/** Flush weather sample data to disk */
	protected void flushWeatherSamples(final long before) {
		WeatherSensorHelper.find(new Checker<WeatherSensor>() {
			public boolean check(WeatherSensor w) {
				if(w instanceof WeatherSensorImpl) {
					WeatherSensorImpl ws =
						(WeatherSensorImpl)w;
					ws.flush(writer);
					ws.purge(before);
				}
				return false;
			}
		});
	}
}
