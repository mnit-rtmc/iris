/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2017  Minnesota Department of Transportation
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
 * CCTV Camera
 *
 * @author Douglas Lau
 */
public interface Camera extends Device {

	/** Minimum camera number */
	int CAM_NUM_MIN = 1;

	/** Maximum camera number */
	int CAM_NUM_MAX = 9999;

	/** SONAR type name */
	String SONAR_TYPE = "camera";

	/** Get the device location */
	GeoLoc getGeoLoc();

	/** Set the camera number */
	void setCamNum(Integer cn);

	/** Get the camera number */
	Integer getCamNum();

	/** Set the encoder type */
	void setEncoderType(EncoderType et);

	/** Get the encoder type */
	EncoderType getEncoderType();

	/** Set the encoder stream URI */
	void setEncoder(String enc);

	/** Get the encoder stream URI */
	String getEncoder();

	/** Set the encoder multicast URI */
	void setEncMulticast(String em);

	/** Get the encoder multicast URI */
	String getEncMulticast();

	/** Set the encoder input channel */
	void setEncoderChannel(int c);

	/** Get the encoder input channel */
	int getEncoderChannel();

	/** Set flag to allow publishing camera images */
	void setPublish(boolean p);

	/** Get flag to allow publishing camera images */
	boolean getPublish();

	/** Command the camera to pan, tilt or zoom */
	void setPtz(Float[] ptz);

	/** Store the current position as a preset */
	void setStorePreset(int preset);

	/** Recall the specified preset */
	void setRecallPreset(int preset);
}
