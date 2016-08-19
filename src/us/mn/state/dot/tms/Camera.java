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
 * CCTV Camera
 *
 * @author Douglas Lau
 */
public interface Camera extends Device {

	/** SONAR type name */
	String SONAR_TYPE = "camera";

	/** Get the device location */
	GeoLoc getGeoLoc();

	/** Set the encoder type */
	void setEncoderType(int et);

	/** Get the encoder type */
	int getEncoderType();

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

	/** Set the stream type */
	void setStreamType(int st);

	/** Get the stream type */
	int getStreamType();

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
