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

	//-----------------------------------------
	/** Database attributes */

	/** Set the GPS enable flag */
	public void setGpsEnable(boolean agps_enable);

	/** Get the GPS enable flag */
	public boolean getGpsEnable();
	
	/** Set the primary device name */
	public void setDeviceName(String adevice_name);

	/** Get the primary device name */
	public String getDeviceName();

	/** Set the primary device class */
	public void setDeviceClass(String adevice_class);

	/** Get the primary device class */
	public String getDeviceClass();

	/** Set the last cycle-polled date & time */
	public void setPollDatetime(Long xPollDatetime);

	/** Get the last cycle-polled date & time */
	public Long getPollDatetime();

	/** Set the last successful poll date & time */
	public void setSampleDatetime(Long xSampleDatetime);

	/** Get the last successful poll date & time */
	public Long getSampleDatetime();

	/** Set the most recent latitude */
	public void setSampleLat(double asample_lat);

	/** Get the most recent latitude */
	public double getSampleLat();

	/** Set the most recent longitude */
	public void setSampleLon(double asample_lon);

	/** Get the most recent longitude */
	public double getSampleLon();
	
	/** Set the comm status */
	public void setCommStatus(String acomm_status);
	
	/** Get the comm status */
	public String getCommStatus();
	
	/** Set the error status */
	public void setErrorStatus(String aerror_status);
	
	/** Get the error status */
	public String getErrorStatus();
	
	/** Set the jitter tolerance meters */
	public void setJitterToleranceMeters(int ajitter_tolerance_meters);

	/** Get the jitter tolerance meters */
	public int getJitterToleranceMeters();

}
