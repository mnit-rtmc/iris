/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2007  Minnesota Department of Transportation
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

import java.rmi.RemoteException;

/**
 * Timing plan for ramp meter operation
 *
 * @author Douglas Lau
 */
abstract public class MeterPlanImpl extends TimingPlanImpl
	implements MeterPlan
{
	/** ObjectVault table name */
	static public final String tableName = "meter_plan";

	/** Create a new ramp meter timing plan */
	public MeterPlanImpl(int period) throws TMSException,
		RemoteException
	{
		super(period);
		within = false;
	}

	/** Create a ramp meter timing plan */
	protected MeterPlanImpl() throws RemoteException {
		super();
		within = false;
	}

	/** Check if this timing plan is for the given peak period */
	public boolean checkPeriod(int period) {
		int NOON = MINUTES_PER_DAY / 2;
		if(period == AM && startTime < NOON)
			return true;
		if(period == PM && stopTime > NOON)
			return true;
		return false;
	}

	/** Get the target release rate for the specified ramp meter */
	abstract public int getTarget(RampMeter m);

	/** Set the target release rate for the specified ramp meter */
	abstract public void setTarget(RampMeter m, int t)
		throws TMSException;

	/** Check if this timing plan knows of a queue backup */
	abstract public boolean checkQueueBackup(RampMeterImpl meter);

	/** Check if this timing plan is in warning mode */
	abstract public boolean checkWarning(RampMeterImpl meter);

	/** Check if this timing plan knows of a congested mainline state */
	abstract public boolean checkCongested(RampMeterImpl meter);

	/** Check for the existance of a queue */
	abstract public boolean checkQueue(RampMeterImpl meter);

	/** Compute ramp meter demand */
	abstract public int computeDemand(RampMeterImpl meter, int interval);

	/** Get minimum release rate for the specified ramp meter */
	abstract public int getMinimum(RampMeterImpl meter);

	/** Flag to determine if within time frame */
	protected transient boolean within;

	/** Validate the timing plan
	    @param meter Ramp meter to validate
	    @param interval 30-second interval in day (0-2879)
	    @return Release rate called for by the plan */
	public synchronized int validate(RampMeterImpl meter, int interval) {
		if(!active)
			return RampMeter.MAX_RELEASE_RATE;
		if(checkWithin(interval)) {
			if(!within) {
				within = true;
				return validateStart(meter);
			}
			return validateWithin(meter, interval);
		}
		if(within) {
			within = false;
			return validateStop(meter);
		}
		return RampMeter.MAX_RELEASE_RATE;
	}

	/** Validate the timing plan for the start time */
	abstract protected int validateStart(RampMeterImpl meter);

	/** Validate the timing plan for the stop time */
	abstract protected int validateStop(RampMeterImpl meter);

	/** Validate the timing plan within the time frame */
	abstract protected int validateWithin(RampMeterImpl meter,
		int interval);

	/** Start metering the specified meter */
	protected void startMetering(RampMeterImpl meter) {
		if(!(meter.isLocked() || meter.isUnavailable())) {
			try {
				meter.startMetering();
			}
			catch(TMSException e) {
				System.err.println(e.getMessage());
			}
		}
	}

	/** Stop metering the specified meter */
	protected void stopMetering(RampMeterImpl meter) {
		if(!meter.isLocked())
			meter.stopMetering();
	}
}
