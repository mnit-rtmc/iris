/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2004  Minnesota Department of Transportation
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
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package us.mn.state.dot.tms;

import java.rmi.RemoteException;
import us.mn.state.dot.vault.ObjectVaultException;

/**
 * SimplePlanImpl
 *
 * @author Douglas Lau
 */
public class SimplePlanImpl extends MeterPlanImpl {

	/** ObjectVault table name */
	static public final String tableName = "simple_plan";

	/** Create a new simple timing plan */
	public SimplePlanImpl(int period) throws TMSException, RemoteException
	{
		super(period);
		target = RampMeter.MAX_RELEASE_RATE;
	}

	/** Create a simple timing plan */
	protected SimplePlanImpl() throws RemoteException {
		super();
	}

	/** Get the plan type */
	public String getPlanType() { return "Simple"; }

	/** Target release rate */
	protected int target;

	/** Get the target release rate for the specified ramp meter */
	public int getTarget( RampMeter m ) { return target; }

	/** Set the target release rate for the specified ramp meter */
	public synchronized void setTarget(RampMeter m, int t)
		throws TMSException
	{
		if( t < RampMeter.MIN_RELEASE_RATE ||
			t > RampMeter.MAX_RELEASE_RATE ) throw new
			ChangeVetoException( "Invalid target rate" );
		try {
			vault.update( this, "target", new Integer( t ),
				getUserName() );
		}
		catch( ObjectVaultException e ) {
			throw new TMSException( e );
		}
		target = t;
	}

	/** Check if this timing plan knows of a queue backup */
	public boolean checkQueueBackup(RampMeterImpl meter) { return false; }

	/** Check if this timing plan is in warning mode */
	public boolean checkWarning(RampMeterImpl meter) { return false; }

	/** Check if this timing plan knows of a congested mainline state */
	public boolean checkCongested(RampMeterImpl meter) { return false; }

	/** Check for the existance of a queue */
	public boolean checkQueue(RampMeterImpl meter) { return false; }

	/** Minimum release rate computed in computeDemand method */
	protected transient int minimum;

	/** Compute the demand (and the minimum release rate) for the
	    specified ramp meter */
	public int computeDemand(RampMeterImpl meter, int interval) {
		minimum = RampMeter.MIN_RELEASE_RATE;
		if(!active) return RampMeter.MIN_RELEASE_RATE;
		if(!checkWithin(interval)) return RampMeter.MIN_RELEASE_RATE;
		if(interval == (startTime * 2))
			return RampMeter.MAX_RELEASE_RATE;
		minimum = target;
		int demand = meter.getDemand();
		int diff = target - demand;
		return demand + Math.round(diff / 2.0f);
	}

	/** Get the minimum release rate */
	public int getMinimum(RampMeterImpl meter) { return minimum; }

	/** Validate the timing plan for the start time */
	protected int validateStart(RampMeterImpl meter) {
		startMetering(meter);
		return meter.getDemand();
	}

	/** Validate the timing plan for the stop time */
	protected int validateStop(RampMeterImpl meter) {
		stopMetering(meter);
		return RampMeter.MAX_RELEASE_RATE;
	}

	/** Validate the timing plan within the time frame */
	protected int validateWithin(RampMeterImpl meter, int interval) {
		return meter.getDemand();
	}
}
