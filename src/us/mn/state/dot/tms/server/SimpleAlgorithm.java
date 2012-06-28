/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2012  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.Device;
import us.mn.state.dot.tms.RampMeterQueue;
import us.mn.state.dot.tms.SystemAttributeHelper;

/**
 * Simple metering algorithm state
 *
 * @author Douglas Lau
 */
public class SimpleAlgorithm implements MeterAlgorithmState {

	/** Get the absolute maximum release rate */
	static protected int getMaxRelease() {
		return SystemAttributeHelper.getMeterMaxRelease();
	}

	/** Demand rate (vehicles per hour) */
	protected Integer demand = null;

	/** Validate a ramp meter */
	public void validate(RampMeterImpl meter) {
		if(demand != null) {
			int diff = meter.getTarget() - demand;
			demand += Math.round(diff / 2.0f);
		} else
			demand = getMaxRelease();
		meter.setRatePlanned(demand);
	}

	/** Get the ramp meter queue state */
	public RampMeterQueue getQueueState(RampMeterImpl meter) {
		return RampMeterQueue.UNKNOWN;
	}
}
