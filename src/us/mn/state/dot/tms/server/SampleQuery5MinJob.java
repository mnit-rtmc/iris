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
import us.mn.state.dot.sched.Completer;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sched.Scheduler;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.ControllerHelper;
import us.mn.state.dot.tms.Detector;
import us.mn.state.dot.tms.DetectorHelper;
import us.mn.state.dot.tms.WeatherSensor;
import us.mn.state.dot.tms.WeatherSensorHelper;
import us.mn.state.dot.tms.server.comm.MessagePoller;
import us.mn.state.dot.tms.server.comm.SamplePoller;

/**
 * Job to query 5-minute sample data
 *
 * @author Douglas Lau
 */
public class SampleQuery5MinJob extends Job {

	/** Seconds to offset each poll from start of interval */
	static protected final int OFFSET_SECS = 12;

	/** Job completer */
	protected final Completer comp;

	/** Job to be performed on completion */
	protected final Job flush_job = new Job(500) {
		public void perform() {
			flushSampleData();
		}
	};

	/** Create a new 5-minute timer job */
	public SampleQuery5MinJob(Scheduler flush) {
		super(Calendar.MINUTE, 5, Calendar.SECOND, OFFSET_SECS);
		comp = new Completer("5-Minute", flush, flush_job);
	}

	/** Perform the 5-minute timer job */
	public void perform() {
		comp.reset();
		try {
			querySample5Min();
		}
		finally {
			comp.makeReady();
		}
	}

	/** Poll all controllers 5 minute interval */
	protected void querySample5Min() {
		ControllerHelper.find(new Checker<Controller>() {
			public boolean check(Controller c) {
				if(c instanceof ControllerImpl)
					querySample5Min((ControllerImpl)c);
				return false;
			}
		});
	}

	/** Query 5-minute sample data from one controller */
	protected void querySample5Min(ControllerImpl c) {
		MessagePoller p = c.getPoller();
		if(p instanceof SamplePoller) {
			SamplePoller sp = (SamplePoller)p;
			sp.querySamples(c, 300, comp);
		}
	}

	/** Flush the sample data to disk */
	protected void flushSampleData() {
		System.err.println("Starting FLUSH @ " +
			TimeSteward.getDateInstance());
		DetectorHelper.find(new Checker<Detector>() {
			public boolean check(Detector det) {
				if(det instanceof DetectorImpl)
					((DetectorImpl)det).flush();
				return false;
			}
		});
		WeatherSensorHelper.find(new Checker<WeatherSensor>() {
			public boolean check(WeatherSensor ws) {
				if(ws instanceof WeatherSensorImpl)
					((WeatherSensorImpl)ws).flush();
				return false;
			}
		});
		System.err.println("Finished FLUSH @ " +
			TimeSteward.getDateInstance());
	}
}
