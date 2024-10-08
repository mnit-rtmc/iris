/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2024  Minnesota Department of Transportation
 * Copyright (C) 2022-2024  SRF Consulting Group
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
 * Camera for monitoring roadway conditions.
 *
 * @author Douglas Lau
 * @author John L. Stanley - SRF Consulting
 */
public interface Camera extends Device {

	/** Minimum camera number */
	int CAM_NUM_MIN = 1;

	/** Maximum camera number */
	int CAM_NUM_MAX = 9999;

	/** SONAR type name */
	String SONAR_TYPE = "camera";

	/** Get the SONAR type name */
	@Override
	default String getTypeName() {
		return SONAR_TYPE;
	}

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

	/** Set the encoder address */
	void setEncAddress(String enc);

	/** Get the encoder address */
	String getEncAddress();

	/** Set the override encoder port */
	void setEncPort(Integer p);

	/** Get the override encoder port */
	Integer getEncPort();

	/** Set the encoder multicast address */
	void setEncMcast(String em);

	/** Get the encoder multicast address */
	String getEncMcast();

	/** Set the encoder channel number */
	void setEncChannel(Integer c);

	/** Get the encoder channel number */
	Integer getEncChannel();

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

	/** Set the camera template */
	void setCameraTemplate(CameraTemplate ct);

	/** Get the camera template */
	CameraTemplate getCameraTemplate();

	/** Save the current SONAR username as the
	 *  most recent PTZ user and the current 
	 *  Epoch timestamp as the PTZ timestamp. */
	void savePtzInfo();

	/** Get name of last user to attempt a camera motion
	 *  (PTZ or camera preset-recall).
	 *  Returns empty string if no attempt has been made. */
	String getPtzUser();

	/** Get Epoch timestamp when latest camera motion
	 *  (PTZ or camera preset-recall) was attempted.
	 *  Returns zero if no attempt has been made. */
	long getPtzTimestamp();
}
