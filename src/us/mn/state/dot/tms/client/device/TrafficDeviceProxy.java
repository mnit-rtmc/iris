/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2007  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.device;

import java.rmi.RemoteException;
import us.mn.state.dot.tms.TrafficDevice;

/**
 * The TrafficDeviceProxy class provides a proxy representation of a
 * TrafficDevice object.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
abstract public class TrafficDeviceProxy extends DeviceProxy
	implements TrafficDevice, Comparable
{
	/** The TrafficDevice that this proxy represents */
	protected final TrafficDevice device;

	/** The device id */
	protected final String id;

	/** Create a new Traffic Device Proxy */
	public TrafficDeviceProxy(TrafficDevice device) throws RemoteException {
		super( device );
		this.device = device;
		id = device.getId();
	}

	/** Get a string description of the device */
	public String toString() {
		return getShortDescription();
	}

	/** Get the ID of the device */
	public final String getId() {
		return id;
	}

	/** Update the traffic device status information */
	public void updateStatusInfo() throws RemoteException {
		super.updateStatusInfo();
		statusCode = device.getStatusCode();
		operation = device.getOperation();
		status = device.getStatus();
	}

	public final boolean equals(Object obj) {
		if(obj instanceof TrafficDeviceProxy) {
			TrafficDeviceProxy proxy  = (TrafficDeviceProxy)obj;
			if(id.equals(proxy.getId()))
				return true;
		}
		return false;
	}

	public final int hashCode() {
		return getId().hashCode();
	}

	public final int compareTo(Object obj) {
		TrafficDeviceProxy proxy  = (TrafficDeviceProxy)obj;
		return id.compareTo(proxy.getId());
	}

	protected String operation;

	/** Get a description of the current device operation */
	public String getOperation() {
		return operation;
	}

	/** Internal representation of the status */
	protected int statusCode;

	/** Get the statusCode */
	public int getStatusCode() {
		return statusCode;
	}

	/** Device status description */
	protected String status;

	/** Get the device status */
	public String getStatus() {
		return status;
	}

	/** Get a short description of the traffic device */
	public String getShortDescription() {
		String l = loc.getDescription();
		if(l.length() > 0)
			return id + " - " + l;
		else
			return id;
	}
}
