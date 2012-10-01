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
import us.mn.state.dot.sched.Completer;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sched.Scheduler;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.ControllerHelper;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.RampMeterHelper;
import us.mn.state.dot.tms.server.comm.MessagePoller;
import us.mn.state.dot.tms.server.comm.SamplePoller;

/**
 * Job to query 30-second sample data
 *
 * @author Douglas Lau
 */
public class SampleQuery30SecJob extends Job {

	/** Seconds to offset each poll from start of interval */
	static private final int OFFSET_SECS = 8;

	/** Station manager */
	private final StationManager station_manager;

	/** Job completer */
	private final Completer comp;

	/** Job to be performed on completion */
	private final Job complete_job = new Job() {
		public void perform() {
			try {
				station_manager.calculateData();
				BaseObjectImpl.corridors.findBottlenecks();
			}
			finally {
				validateMetering();
			}
		}
	};

	/** Create a new 30-second timer job */
	public SampleQuery30SecJob(Scheduler timer, Scheduler f) {
		super(Calendar.SECOND, 30, Calendar.SECOND, OFFSET_SECS);
		station_manager = new StationManager(f);
		comp = new Completer("30-Second", timer, complete_job);
	}

	/** Perform the 30-second timer job */
	public void perform() {
		comp.reset();
		try {
			querySample30Sec();
		}
		finally {
			comp.makeReady();
		}
	}

	/** Poll all sampling controllers 30-second interval */
	private void querySample30Sec() {
		ControllerHelper.find(new Checker<Controller>() {
			public boolean check(Controller c) {
				if(c instanceof ControllerImpl)
					querySample30Sec((ControllerImpl)c);
				return false;
			}
		});
	}

	/** Query 30-second sample data from one controller */
	private void querySample30Sec(ControllerImpl c) {
		if(c.hasActiveDetector()) {
			MessagePoller p = c.getPoller();
			if(p instanceof SamplePoller) {
				SamplePoller sp = (SamplePoller)p;
				sp.querySamples(c, 30, comp);
			}
		}
	}

	/** Validate all metering algorithms */
	private void validateMetering() {
		KAdaptiveAlgorithm.processAllStates();
		RampMeterHelper.find(new Checker<RampMeter>() {
			public boolean check(RampMeter rm) {
				if(rm instanceof RampMeterImpl) {
					RampMeterImpl meter = (RampMeterImpl)rm;
					meter.validateAlgorithm();
				}
				return false;
			}
		});
		StratifiedAlgorithm.processAllStates();
		RampMeterHelper.find(new Checker<RampMeter>() {
			public boolean check(RampMeter m) {
				RampMeterImpl meter = (RampMeterImpl)m;
				meter.updateQueueState();
				meter.updateRatePlanned();
				return false;
			}
		});
		/* Note: this is temporarily last in case an exception is
		 *       thrown -- all other metering work will be complete. */
		DensityUMNAlgorithm.processAllStates();
	}
}
