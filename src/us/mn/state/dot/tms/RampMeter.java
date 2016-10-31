/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2016  Minnesota Department of Transportation
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

	/** Set advance warning beacon */
	void setBeacon(Beacon b);

	/** Get advance warning beacon */
	Beacon getBeacon();

	/** Set verification camera preset */
	void setPreset(CameraPreset cp);

	/** Get verification camera preset */
	CameraPreset getPreset();

	/** Set the meter lock status code */
	void setMLock(Integer c);

	/** Get the meter lock status code */
	Integer getMLock();

	/* Transient attributes (not stored in database) */

	/** Set the release rate (vehicles per hour) */
	void setRateNext(Integer r);

	/** Get the release rate (vehciels per hour) */
	Integer getRate();

	/** Get the queue status */
	int getQueue();
}
