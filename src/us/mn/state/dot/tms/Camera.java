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

import us.mn.state.dot.sonar.SonarObject;

/**
 * CCTV Camera
 *
 * @author Douglas Lau
 */
public interface Camera extends Device2 {

	/** SONAR type name */
	String SONAR_TYPE = "camera";

	/** Set the video encoder host name (and port) */
	void setEncoder(String enc);

	/** Get the video encoder host name (and port) */
	String getEncoder();

	/** Set the input channel on the encoder */
	void setEncoderChannel(int c);

	/** Get the input channel on the encoder */
	int getEncoderChannel();

	/** Set the video NVR host name (and port) */
	void setNvr(String n);

	/** Get the video NVR host name (and port) */
	String getNvr();

	/** Set flag to allow publishing camera images */
	void setPublish(boolean p);

	/** Get flag to allow publishing camera images */
	boolean getPublish();

	/** Command the camera to pan, tilt or zoom */
	void setPtz(float[] ptz);

	/** Command the camera to set the preset */
	void setPreset(int preset);

	/** Command the camera to goto the preset */
	void goToPreset(int preset);
}
