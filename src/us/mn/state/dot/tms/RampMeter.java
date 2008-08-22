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

import java.rmi.RemoteException;

/**
 * RampMeter
 *
 * @author Douglas Lau
 */
public interface RampMeter extends TrafficDevice {

	/** Absolute minimum release rate (vehicles per hour) */
	public int MIN_RELEASE_RATE = 240;

	/** Absolute maximum release rate (vehicles per hour) */
	public int MAX_RELEASE_RATE = 1714;

	/** Unavailable meter control mode should be selected to disable
	 * metering for any reason. */
	public int MODE_UNAVAILABLE = 0;

	/** Standby meter control mode should be selected to disable metering
	 * unless deemed necessary by operators. */
	public int MODE_STANDBY = 1;

	/** Central meter control mode should be selected to operate a meter
	 * normally through a central timing plan. */
	public int MODE_CENTRAL = 2;

	/** String descriptions of the valid control modes */
	public String[] MODE = {
		"Unavailable", "Standby", "Central"
	};

	/** Set the ramp meter control mode */
	public void setControlMode(int m) throws TMSException,
		RemoteException;

	/** Get the ramp meter control mode */
	public int getControlMode() throws RemoteException;

	/** Set single release (true) or dual/alternate release (false) */
	public void setSingleRelease(boolean s) throws TMSException,
		RemoteException;

	/** Is this a single or dual/alternate release ramp? */
	public boolean isSingleRelease() throws RemoteException;

	/** Add a simple timing plan to the ramp meter */
	public void addSimpleTimingPlan(int period) throws TMSException,
		RemoteException;

	/** Add a stratified timing plan to the ramp meter */
	public void addStratifiedTimingPlan(int period) throws TMSException,
		RemoteException;

	/** Remote a timing plan from the ramp meter */
	public void removeTimingPlan(MeterPlan plan) throws TMSException,
		RemoteException;

	/** Ensure stratified plans are for the right corridor */
	public void checkStratifiedPlans() throws TMSException, RemoteException;

	/** Get an array of all timing plans which affect this meter */
	public MeterPlan[] getTimingPlans() throws RemoteException;

	/** Is the ramp meter in police panel flash? */
	public boolean isPolicePanelFlash() throws RemoteException;

	/** Start metering this ramp meter */
	public void startMetering() throws TMSException, RemoteException;

	/** Stop metering this ramp meter */
	public void stopMetering() throws RemoteException;

	/** Is the ramp meter currently metering? */
	public boolean isMetering() throws RemoteException;

	/** Get the current minimum release rate (vehicles per hour) */
	public int getMinimum() throws RemoteException;

	/** Get the current estimated ramp demand (vehicles per hour) */
	public int getDemand() throws RemoteException;

	/** Get the current ramp meter release rate (vehciels per hour) */
	public int getReleaseRate() throws RemoteException;

	/** Determine whether a queue exists at the ramp meter */
	public boolean queueExists() throws RemoteException;

	/** Grow the length of the queue by decreasing the demand */
	public void growQueue() throws RemoteException;

	/** Shrink the length of the queue by increasing the demand */
	public void shrinkQueue() throws RemoteException;

	/** Is the metering rate locked? */
	public boolean isLocked() throws RemoteException;

	/** Lock or unlock the metering rate */
	public void setLocked(boolean l, String reason) throws RemoteException;

	/** Get the lock if one exists otherwise returns null */
	public RampMeterLock getLock() throws RemoteException;

	/** Metering meter status code */
	public int STATUS_METERING = 4;

	/** "Locked off" meter status code */
	public int STATUS_LOCKED_OFF = 5;

	/** "Locked on" meter status code */
	public int STATUS_LOCKED_ON = 6;

	/** Warning (missing data) status code */
	public int STATUS_WARNING = 7;

	/** Queue exists status code */
	public int STATUS_QUEUE = 8;

	/** Queue backup status code */
	public int STATUS_QUEUE_BACKUP = 9;

	/** Mainline congested status code */
	public int STATUS_CONGESTED = 10;

	/** String descriptions of status codes */
	public String[] STATUS = {
		"Inactive", "Failed", "Unavailable", "Available", "Metering",
		"Locked Off", "Locked On", "Warning", "Queue exists",
		"Queue backup", "Congested"
	};

	/** Set the queue storage length (in feet) */
	public void setStorage(int storage) throws TMSException,
		RemoteException;

	/** Get the queue storage length (in feet) */
	public int getStorage() throws RemoteException;

	/** Default maximum wait time (in seconds) */
	public int DEFAULT_MAX_WAIT = 240;

	/** Set the maximum allowed meter wait time (in seconds) */
	public void setMaxWait(int w) throws TMSException, RemoteException;

	/** Get the maximum allowed meter wait time (in seconds) */
	public int getMaxWait() throws RemoteException;

	/** Set verification camera */
	public void setCamera(String id) throws TMSException,
		RemoteException;

	/** Get verification camera */
	public String getCamera() throws RemoteException;

	/** Get the detectors associated with the ramp meter */
	public Detector[] getDetectors() throws RemoteException;
}
