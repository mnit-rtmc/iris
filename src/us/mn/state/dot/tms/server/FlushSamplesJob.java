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

import java.io.IOException;
import java.util.Calendar;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.tms.Detector;
import us.mn.state.dot.tms.DetectorHelper;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.WeatherSensor;
import us.mn.state.dot.tms.WeatherSensorHelper;
import us.mn.state.dot.tms.units.Interval;

/**
 * Job to flush sample data to disk.
 *
 * @author Douglas Lau
 */
public class FlushSamplesJob extends Job {

	/** Is archiving enabled? */
	static private boolean isArchiveEnabled() {
		return SystemAttrEnum.SAMPLE_ARCHIVE_ENABLE.getBoolean();
	}

	/** Number of milliseconds to cache periodic sample data */
	static private final long SAMPLE_CACHE_MS = new Interval(10,
		Interval.Units.MINUTES).ms();

	/** Calculate a time stamp to purge samples */
	static private long calculatePurgeStamp() {
		return TimeSteward.currentTimeMillis() - SAMPLE_CACHE_MS;
	}

	/** Periodic sample writer */
	private final PeriodicSampleWriter writer = new PeriodicSampleWriter(
		new SampleArchiveFactoryImpl());

	/** Create a new flush samples job */
	public FlushSamplesJob() {
		super(Calendar.MINUTE, 2);
	}

	/** Perform the flush samples job */
	public void perform() {
		long before = calculatePurgeStamp();
		flushDetectorSamples(before);
		flushWeatherSamples(before);
	}

	/** Flush detector sample data to disk */
	protected void flushDetectorSamples(final long before) {
		final boolean do_flush = isArchiveEnabled();
		DetectorHelper.find(new Checker<Detector>() {
			public boolean check(Detector d) {
				if(d instanceof DetectorImpl) {
					DetectorImpl det = (DetectorImpl)d;
					if(do_flush)
						flushDetectorSamples(det);
					det.purge(before);
				}
				return false;
			}
		});
	}

	/** Flush the samples for one detector */
	private void flushDetectorSamples(DetectorImpl det) {
		try {
			det.flush(writer);
		}
		catch(IOException e) {
			// FIXME: should propogate out of Job
			e.printStackTrace();
		}
	}

	/** Flush weather sample data to disk */
	protected void flushWeatherSamples(final long before) {
		final boolean do_flush = isArchiveEnabled();
		WeatherSensorHelper.find(new Checker<WeatherSensor>() {
			public boolean check(WeatherSensor w) {
				if(w instanceof WeatherSensorImpl) {
					WeatherSensorImpl ws =
						(WeatherSensorImpl)w;
					if(do_flush)
						flushWeatherSamples(ws);
					ws.purge(before);
				}
				return false;
			}
		});
	}

	/** Flush the samples for one weather sensor */
	private void flushWeatherSamples(WeatherSensorImpl ws) {
		try {
			ws.flush(writer);
		}
		catch(IOException e) {
			// FIXME: should propogate out of Job
			e.printStackTrace();
		}
	}
}
