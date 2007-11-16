/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2007  Minnesota Department of Transportation
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
import us.mn.state.dot.vault.ObjectVaultException;

/**
 * Timing plan for operating a traffic management device
 *
 * @author Douglas Lau
 */
public class TimingPlanImpl extends TMSObjectImpl implements TimingPlan,
	Comparable, Storable
{
	/** ObjectVault table name */
	static public final String tableName = "timing_plan";

	/** Get the database table name */
	public String getTable() {
		return tableName;
	}

	/** Compose the hour and minute to minute-of-day */
	static protected int minute_of_day(int hour, int minute) {
		return hour * 60 + minute;
	}

	/** Default start time for AM timing plans */
	static protected final int DEFAULT_AM_START = minute_of_day(6, 30);

	/** Default stop time for AM timing plans */
	static protected final int DEFAULT_AM_STOP = minute_of_day(8, 30);

	/** Default start time for PM timing plans */
	static protected final int DEFAULT_PM_START = minute_of_day(15, 30);

	/** Default stop time for PM timing plans */
	static protected final int DEFAULT_PM_STOP = minute_of_day(17, 30);

	/** Create a new timing plan */
	public TimingPlanImpl(int period) throws TMSException,
		RemoteException
	{
		super();
		if(period == AM) {
			startTime = DEFAULT_AM_START;
			stopTime = DEFAULT_AM_STOP;
		}
		else if(period == PM) {
			startTime = DEFAULT_PM_START;
			stopTime = DEFAULT_PM_STOP;
		}
		else
			throw new ChangeVetoException("Invalid period");
	}

	/** Create a timing plan */
	protected TimingPlanImpl() throws RemoteException {
		super();
	}

	/** Compare for sorting plan arrays */
	public int compareTo(Object o) {
		TimingPlanImpl other = (TimingPlanImpl)o;
		int c = startTime - other.startTime;
		if(c == 0)
			c = stopTime - other.stopTime;
		if(c == 0)
			c = getPlanType().compareTo(other.getPlanType());
		return c;
	}

	/** Get the plan type */
	public String getPlanType() {
		return "Basic";
	}

	/** Timing plan start time (minute of day) */
	protected int startTime;

	/** Get the start time (minute of day) */
	public int getStartTime() {
		return startTime;
	}

	/** Set the start time (minute of day) */
	public synchronized void setStartTime(int t) throws TMSException {
		if(t < 0 || t > stopTime)
			throw new ChangeVetoException("Invalid start time");
		try {
			vault.update(this, "startTime", new Integer(t),
				getUserName());
		}
		catch(ObjectVaultException e) {
			throw new TMSException(e);
		}
		startTime = t;
	}

	/** Timing plan stop time (minute of day) */
	protected int stopTime;

	/** Get the stop time (minute of day) */
	public int getStopTime() {
		return stopTime;
	}

	/** Set the stop time (minute of day) */
	public synchronized void setStopTime(int t) throws TMSException {
		if(t < startTime || t >= MINUTES_PER_DAY)
			throw new ChangeVetoException("Invalid stop time");
		try {
			vault.update(this, "stopTime", new Integer(t),
				getUserName());
		}
		catch(ObjectVaultException e) {
			throw new TMSException(e);
		}
		stopTime = t;
	}

	/** Active status */
	protected boolean active;

	/** Get the active status */
	public boolean isActive() {
		return active;
	}

	/** Set the active status */
	public synchronized void setActive(boolean a) throws TMSException {
		if(a == active)
			return;
		try {
			vault.update(this, "active", new Boolean(a),
				getUserName());
		}
		catch(ObjectVaultException e) {
			throw new TMSException(e);
		}
		active = a;
	}

	/** Testing status */
	protected transient boolean testing;

	/** Get the testing status */
	public boolean isTesting() {
		return testing;
	}

	/** Set the testing status */
	public void setTesting(boolean t) {
		testing = t;
	}

	/** Check if the specified 30-second interval is within the timing
	    plan's range */
	public boolean checkWithin(int interval) {
		return interval >= (startTime * 2) && interval < (stopTime * 2);
	}
}
