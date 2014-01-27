/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2014  Minnesota Department of Transportation
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
import us.mn.state.dot.sched.Scheduler;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.RampMeterHelper;

/**
 * Job to calculate station data and ramp metering.
 *
 * @author Douglas Lau
 */
public class MeteringJob extends Job {

	/** Seconds to offset each poll from start of interval */
	static private final int OFFSET_SECS = 29;

	/** Station manager */
	private final StationManager station_manager;

	/** Create a new metering job */
	public MeteringJob(Scheduler flush) {
		super(Calendar.SECOND, 30, Calendar.SECOND, OFFSET_SECS);
		station_manager = new StationManager(flush);
	}

	/** Perform the metering job */
	public void perform() {
		try {
			station_manager.calculateData();
			BaseObjectImpl.corridors.findBottlenecks();
		}
		finally {
			validateMetering();
		}
	}

	/** Validate all metering algorithms */
	private void validateMetering() {
		KAdaptiveAlgorithm.processAllStates();
		Iterator<RampMeter> it = RampMeterHelper.iterator();
		while(it.hasNext()) {
			RampMeter rm = it.next();
			if(rm instanceof RampMeterImpl) {
				RampMeterImpl meter = (RampMeterImpl)rm;
				meter.validateAlgorithm();
			}
		}
		StratifiedAlgorithm.processAllStates();
		it = RampMeterHelper.iterator();
		while(it.hasNext()) {
			RampMeter rm = it.next();
			if(rm instanceof RampMeterImpl) {
				RampMeterImpl meter = (RampMeterImpl)rm;
				meter.updateQueueState();
				meter.updateRatePlanned();
			}
		}
		/* Note: this is temporarily last in case an exception is
		 *       thrown -- all other metering work will be complete. */
		DensityUMNAlgorithm.processAllStates();
	}
}
