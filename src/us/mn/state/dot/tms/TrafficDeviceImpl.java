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
package us.mn.state.dot.tms;

import java.rmi.RemoteException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import us.mn.state.dot.tms.comm.DeviceOperation;
import us.mn.state.dot.vault.FieldMap;

/**
 * TrafficDeviceImpl is the base class for all traffic control devices,
 * such as ramp meters, dynamic message signs, etc.
 *
 * @author Douglas Lau
 */
abstract public class TrafficDeviceImpl extends DeviceImpl
	implements TrafficDevice
{
	/** ObjectVault table name */
	static public final String tableName = "traffic_device";

	/** Table mapping for traffic_device_timing_plan relation */
	static public TableMapping plan_mapping;

	/** Traffic device ID regex pattern */
	static protected final Pattern ID_PATTERN =
		Pattern.compile("[A-Z][A-Z0-9]{1,9}");

	/** Create a new traffic device */
	public TrafficDeviceImpl(String i) throws ChangeVetoException,
		RemoteException
	{
		super();
		Matcher m = ID_PATTERN.matcher(i);
		if(!m.matches())
			throw new ChangeVetoException("Invalid ID: " + i);
		id = i;
		status = null;
	}

	/** Constructor needed for ObjectVault */
	protected TrafficDeviceImpl(FieldMap fields) throws RemoteException {
		super(fields);
		id = (String)fields.get( "id" );
		status = null;
	}

	/** Get the primary key name */
	public String getKeyName() {
		return "id";
	}

	/** Device ID */
	protected final String id;

	/** Get the device ID */
	public String getId() { return id; }

	/** Get the object key */
	public String getKey() {
		return id;
	}

	/** Get a string representation of the traffic device */
	public String toString() { return id; }

	/** Device status string */
	protected transient String status;

	/** Set the device status */
	public void setStatus(String s) { status = s; }

	/** Get the device status */
	public String getStatus() { return status; }

	/** Operation which owns the device */
	protected transient DeviceOperation owner;

	/** Acquire ownership of the device */
	public DeviceOperation acquire(DeviceOperation o) {
		// ID used for unique device acquire/release lock
		synchronized(id) {
			if(owner == null)
				owner = o;
			return owner;
		}
	}

	/** Release ownership of the device */
	public DeviceOperation release(DeviceOperation o) {
		// ID used for unique device acquire/release lock
		synchronized(id) {
			DeviceOperation _owner = owner;
			if(owner == o)
				owner = null;
			return _owner;
		}
	}

	/** Get a description of the current device operation */
	public String getOperation() {
		DeviceOperation o = owner;
		if(o == null) {
			String s = status;
			if(s == null)
				return "None";
			else
				return s;
		} else {
			String name = o.getClass().getName();
			int i = name.lastIndexOf('.');
			if(i >= 0)
				return name.substring(i + 1);
			else
				return name;
		}
	}

	/** Get the current status code */
	abstract public int getStatusCode();
}
