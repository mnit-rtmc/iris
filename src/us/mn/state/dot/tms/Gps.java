/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2016  SRF Consulting Group
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
 * Interface for a GPS (Global Positioning System) device
 *
 * @author John L. Stanley
 */
public interface Gps extends Device {

	/** SONAR type name */
	String SONAR_TYPE = "gps";

	/** Set the GPS enable flag */
	void setGpsEnable(boolean agps_enable);

	/** Get the GPS enable flag */
	boolean getGpsEnable();

	/** Set the primary device name */
	void setDeviceName(String adevice_name);

	/** Get the primary device name */
	String getDeviceName();

	/** Set the primary device class */
	void setDeviceClass(String adevice_class);

	/** Get the primary device class */
	String getDeviceClass();

	/** Set the last cycle-polled date & time */
	void setPollDatetime(Long xPollDatetime);

	/** Get the last cycle-polled date & time */
	Long getPollDatetime();

	/** Set the last successful poll date & time */
	void setSampleDatetime(Long xSampleDatetime);

	/** Get the last successful poll date & time */
	Long getSampleDatetime();

	/** Set the most recent latitude */
	void setSampleLat(double asample_lat);

	/** Get the most recent latitude */
	double getSampleLat();

	/** Set the most recent longitude */
	void setSampleLon(double asample_lon);

	/** Get the most recent longitude */
	double getSampleLon();

	/** Set the comm status */
	void setCommStatus(String acomm_status);

	/** Get the comm status */
	String getCommStatus();

	/** Set the error status */
	void setErrorStatus(String aerror_status);

	/** Get the error status */
	String getErrorStatus();

	/** Set the jitter tolerance meters */
	void setJitterToleranceMeters(int ajitter_tolerance_meters);

	/** Get the jitter tolerance meters */
	int getJitterToleranceMeters();
}
