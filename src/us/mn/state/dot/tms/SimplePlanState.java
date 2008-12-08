/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms;

/**
 * Simple timing plan state
 *
 * @author Douglas Lau
 */
public class SimplePlanState extends MeterPlanState {

	/** Create a simple timing plan state */
	public SimplePlanState(TimingPlanImpl p) {
		super(p);
	}

	/** Demand rate (vehicles per hour) */
	protected Integer demand = null;

	/** Get the release rate for the specified ramp meter */
	public Integer getRate(RampMeter meter) {
		return demand;
	}

	/** Start operating the timing plan */
	protected void start() {
		super.start();
		demand = SystemAttributeHelper.getMeterMaxRelease();
	}

	/** Stop operating the timing plan */
	protected void stop() {
		super.stop();
		demand = null;
	}

	/** Validate the timing plan */
	public void validate() {
		if(demand != null) {
			int diff = plan.getTarget() - demand;
			demand += Math.round(diff / 2.0f);
		}
		super.validate();
	}
}
