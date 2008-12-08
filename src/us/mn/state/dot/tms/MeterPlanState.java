/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2008  Minnesota Department of Transportation
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
 * Timing plan state for ramp meter operation.
 *
 * @author Douglas Lau
 */
abstract public class MeterPlanState extends TimingPlanState {

	/** Get the absolute minimum release rate */
	static protected int getMinRelease() {
		return SystemAttributeHelper.getMeterMinRelease();
	}

	/** Get the absolute maximum release rate */
	static protected int getMaxRelease() {
		return SystemAttributeHelper.getMeterMaxRelease();
	}

	/** Create a new ramp meter timing plan */
	public MeterPlanState(TimingPlanImpl p) {
		super(p);
	}

	/** Check for the existance of a queue */
	public RampMeterQueue getQueue(RampMeter meter) {
		return RampMeterQueue.UNKNOWN;
	}

	/** Get release rate for the specified ramp meter.
	 * @param meter Requested ramp meter
	 * @return Release rate (vehicles per hour), or null for no metering */
	abstract public Integer getRate(RampMeter meter);
}
