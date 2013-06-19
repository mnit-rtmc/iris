/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2013  Minnesota Department of Transportation
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
import java.util.Iterator;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sched.TimeSteward;
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
	private final PeriodicSampleWriter writer;

	/** Create a new flush samples job */
	public FlushSamplesJob(SampleArchiveFactory saf) {
		super(Calendar.MINUTE, 2);
		writer = new PeriodicSampleWriter(saf);
	}

	/** Perform the flush samples job */
	public void perform() throws IOException {
		long before = calculatePurgeStamp();
		flushDetectorSamples(before);
		flushWeatherSamples(before);
	}

	/** Flush detector sample data to disk */
	private void flushDetectorSamples(long before) throws IOException {
		boolean do_flush = isArchiveEnabled();
		Iterator<Detector> it = DetectorHelper.iterator();
		while(it.hasNext()) {
			Detector d = it.next();
			if(d instanceof DetectorImpl) {
				DetectorImpl det = (DetectorImpl)d;
				if(do_flush)
					det.flush(writer);
				det.purge(before);
			}
		}
	}

	/** Flush weather sample data to disk */
	private void flushWeatherSamples(long before) throws IOException {
		boolean do_flush = isArchiveEnabled();
		Iterator<WeatherSensor> it = WeatherSensorHelper.iterator();
		while(it.hasNext()) {
			WeatherSensor w = it.next();
			if(w instanceof WeatherSensorImpl) {
				WeatherSensorImpl ws = (WeatherSensorImpl)w;
				if(do_flush)
					ws.flush(writer);
				ws.purge(before);
			}
		}
	}
}
