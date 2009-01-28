/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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

	/** Demand rate (vehicles per hour) */
	protected Integer demand = null;

	/** Validate a timing plan */
	public void validate(TimingPlanImpl plan) {
		if(plan.isOperating()) {
			if(demand != null) {
				int diff = plan.getTarget() - demand;
				demand += Math.round(diff / 2.0f);
			} else
				demand = getMaxRelease();
		} else
			demand = null;
	}

	/** Get the release rate for the specified ramp meter */
	public Integer getRate(RampMeter meter) {
		return demand;
	}
}
