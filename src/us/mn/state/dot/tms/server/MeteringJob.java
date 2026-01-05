/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2026  Minnesota Department of Transportation
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

import java.util.Iterator;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.RampMeterHelper;

/**
 * Job to calculate ramp meter rates.
 *
 * @author Douglas Lau
 */
public class MeteringJob extends Job {

	/** Create a new metering job */
	public MeteringJob() {
		super(0);
	}

	/** Perform the metering job */
	@Override
	public void perform() {
		validateMetering();
	}

	/** Validate all metering algorithms */
	private void validateMetering() {
		KAdaptiveAlgorithm.processAllStates();
		Iterator<RampMeter> it = RampMeterHelper.iterator();
		while (it.hasNext()) {
			RampMeter rm = it.next();
			if (rm instanceof RampMeterImpl) {
				RampMeterImpl meter = (RampMeterImpl) rm;
				meter.checkLockExpired();
				meter.validateAlgorithm();
			}
		}
		it = RampMeterHelper.iterator();
		while (it.hasNext()) {
			RampMeter rm = it.next();
			if (rm instanceof RampMeterImpl) {
				RampMeterImpl meter = (RampMeterImpl) rm;
				meter.updateQueueState();
				meter.updateRatePlanned();
			}
		}
	}
}
