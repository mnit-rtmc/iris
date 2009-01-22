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
 * A ramp meter is a traffic signal which meters the flow of traffic on a
 * freeway entrance ramp.
 *
 * @author Douglas Lau
 */
public interface RampMeter extends Device2 {

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

	/** Set verification camera */
	void setCamera(Camera c);

	/** Get verification camera */
	Camera getCamera();

	/** Set the meter lock status code */
	void setLock(int c);

	/** Get the meter lock status code */
	int getLock();

	/* Transient attributes (not stored in database) */

	/** Set the release rate (vehicles per hour) */
	void setRate(Integer r);

	/** Get the release rate (vehciels per hour) */
	Integer getRate();

	/** Get the queue status */
	int getQueue();
}
