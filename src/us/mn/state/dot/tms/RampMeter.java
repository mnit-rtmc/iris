/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2026  Minnesota Department of Transportation
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
 * A ramp meter is a traffic signal which meters the flow of traffic on a
 * freeway entrance ramp.
 *
 * @author Douglas Lau
 */
public interface RampMeter extends Device {

	/** SONAR type name */
	String SONAR_TYPE = "ramp_meter";

	/** Get the SONAR type name */
	@Override
	default String getTypeName() {
		return SONAR_TYPE;
	}

	/** Get the device location */
	GeoLoc getGeoLoc();

	/** Set ramp meter type */
	void setMeterType(int t);

	/** Get the ramp meter type */
	int getMeterType();

	/** Set the queue storage length (in feet) */
	void setStorage(int storage);

	/** Get the queue storage length (in feet) */
	int getStorage();

	/** Set the maximum allowed meter wait time (in seconds) */
	void setMaxWait(int w);

	/** Get the maximum allowed meter wait time (in seconds) */
	int getMaxWait();

	/** Set the metering algorithm */
	void setAlgorithm(int a);

	/** Get the metering algorithm */
	int getAlgorithm();

	/** Set the AM target rate */
	void setAmTarget(int t);

	/** Get the AM target rate */
	int getAmTarget();

	/** Set the PM target rate */
	void setPmTarget(int t);

	/** Get the PM target rate */
	int getPmTarget();

	/** Set verification camera preset */
	void setPreset(CameraPreset cp);

	/** Get verification camera preset */
	CameraPreset getPreset();

	/** Set the lock as JSON.
	 * @see MeterLock */
	void setLock(String lk);

	/** Get the lock as JSON.
	 * @see MeterLock */
	String getLock();

	/** Get the current status as JSON */
	String getStatus();

	/** Status JSON attributes: RATE, QUEUE, FAULT */

	/** Release rate (vehicles per hour; Integer) */
	String RATE = "rate";

	/** Queue status */
	String QUEUE = "queue";

	/** QUEUE: empty */
	String QUEUE_EMPTY = "empty";

	/** QUEUE: exists */
	String QUEUE_EXISTS = "exists";

	/** QUEUE: full */
	String QUEUE_FULL = "full";

	/** Fault conditions */
	String FAULT = "fault";

	/** FAULT: Police panel flash */
	String FAULT_POLICE_PANEL = "police panel";

	/** FAULT: Manual mode flash */
	String FAULT_MANUAL_MODE = "manual mode";

	/** FAULT: No entrance node */
	String FAULT_NO_ENTRANCE_NODE = "no entrance node";

	/** FAULT: Missing state */
	String FAULT_MISSING_STATE = "missing state";
}
