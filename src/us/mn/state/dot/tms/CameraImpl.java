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
import us.mn.state.dot.tms.comm.CameraPoller;
import us.mn.state.dot.tms.comm.MessagePoller;
import us.mn.state.dot.vault.FieldMap;

/**
 * CameraImpl
 *
 * @author Douglas Lau
 * @author <a href="mailto:timothy.a.johnson@dot.state.mn.us">Tim Johnson</a>
 */
public class CameraImpl extends TrafficDeviceImpl implements Camera, Storable {

	/** ObjectVault table name */
	static public final String tableName = "camera";

	/** Get the database table name */
	public String getTable() {
		return tableName;
	}

	/** Create a new camera with a string id */
	public CameraImpl(String i) throws ChangeVetoException, RemoteException
	{
		super(i);
		deviceList.add(i, this);
	}

	/** Create a camera from an ObjectVault field map */
	protected CameraImpl(FieldMap fields) throws RemoteException {
		super(fields);
	}

	/** Set the controller to which this camera is assigned */
	public void setController(ControllerImpl c) throws TMSException {
		super.setController(c);
		if(c == null)
			deviceList.add(id, this);
		else
			deviceList.remove(id);
	}

	/** Host (and port) of encoder for digital video stream */
	protected String encoder;

	/** Set the video encoder host name (and port) */
	public synchronized void setEncoder(String enc) throws TMSException {
		if(enc.equals(encoder))
			return;
		store.update(this, "encoder", enc);
		encoder = enc;
	}

	/** Get the video encoder host name (and port) */
	public String getEncoder() {
		return encoder;
	}

	/** Input channel for video stream on encoder */
	protected int encoder_channel;

	/** Set the input channel on the encoder */
	public synchronized void setEncoderChannel(int c) throws TMSException {
		if(c == encoder_channel)
			return;
		store.update(this, "encoder_channel", c);
		encoder_channel = c;
	}

	/** Get the input channel on the encoder */
	public int getEncoderChannel() {
		return encoder_channel;
	}

	/** Host (and port) of NVR for digital video stream */
	protected String nvr;

	/** Set the video NVR host name (and port) */
	public synchronized void setNvr(String n) throws TMSException {
		if(n.equals(nvr))
			return;
		store.update(this, "nvr", n);
		nvr = n;
	}

	/** Get the video NVR host name (and port) */
	public String getNvr() {
		return nvr;
	}

	/** Flag to allow publishing camera images */
	protected boolean publish;

	/** Set flag to allow publishing camera images */
	public synchronized void setPublish(boolean p) throws TMSException {
		if(p == publish)
			return;
		store.update(this, "publish", p);
		publish = p;
	}

	/** Get flag to allow publishing camera images */
	public boolean getPublish() {
		return publish;
	}

	/** Get the integer id of the camera */
	public int getUID() {
		try {
			return Integer.parseInt(id.substring(1));
		}
		catch(NumberFormatException e) {
			return 0;
		}
	}

	/** Get the side of the road that the camera is on */
	public short getRoadSide() {
		switch(location.free_dir) {
			case Road.EAST:
				return Road.SOUTH;
			case Road.WEST:
				return Road.NORTH;
			case Road.NORTH:
				return Road.EAST;
			case Road.SOUTH:
				return Road.WEST;
			case Road.EAST_WEST:
			case Road.NORTH_SOUTH:
				//FIXME: add a value for cameras in the median
				return Road.NONE;
		}
		return Road.NONE;
	}

	/** Get the current status code */
	public int getStatusCode() {
		if(isActive())
			return STATUS_AVAILABLE;
		else
			return STATUS_INACTIVE;
	}

	/** Notify all observers for an update */
	public void notifyUpdate() {
		super.notifyUpdate();
		cameraList.update(id);
	}

	/** Command the camera pan, tilt or zoom */
	public void move(float p, float t, float z) {
		MessagePoller mp = getPoller();
		if(mp instanceof CameraPoller) {
			CameraPoller cp = (CameraPoller)mp;
			cp.sendPTZ(this, p, t, z);
		}
	}
}
