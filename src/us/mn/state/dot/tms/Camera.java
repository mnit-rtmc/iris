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
 * CCTV Camera
 *
 * @author Douglas Lau
 */
public interface Camera extends TrafficDevice {

	/** Get the integer id of the camera */
	int getUID() throws RemoteException;

	/** Set the video encoder host name (and port) */
	void setEncoder(String enc) throws TMSException, RemoteException;

	/** Get the video encoder host name (and port) */
	String getEncoder() throws RemoteException;

	/** Set the input channel on the encoder */
	void setEncoderChannel(int c) throws TMSException, RemoteException;

	/** Get the input channel on the encoder */
	int getEncoderChannel() throws RemoteException;

	/** Set the video NVR host name (and port) */
	void setNvr(String n) throws TMSException, RemoteException;

	/** Get the video NVR host name (and port) */
	String getNvr() throws RemoteException;

	/** Set flag to allow publishing camera images */
	void setPublish(boolean p) throws TMSException, RemoteException;

	/** Get flag to allow publishing camera images */
	boolean getPublish() throws RemoteException;

	/** Get the side of the road that the camera is on */
	short getRoadSide() throws RemoteException;

	/** Command the camera to pan, tilt or zoom */
	void move(float p, float t, float z) throws RemoteException;

	/** Not published camera status code */
	public int STATUS_NOT_PUBLISHED = 11;

	/** String descriptions of status codes */
	public String[] STATUS = {
		"Inactive", "Available", "Not Published"
	};
}
