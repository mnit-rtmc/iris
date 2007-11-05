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

import java.awt.geom.AffineTransform;
import java.rmi.RemoteException;

import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.Device;
import us.mn.state.dot.tms.Location;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.client.proxy.LocationProxy;
import us.mn.state.dot.tms.client.proxy.TmsMapProxy;

/**
 * Creates a proxy for a Device object.  Commonly called method results are
 * cached otherwise method calls are delegated to the internal device.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
abstract public class DeviceProxy extends TmsMapProxy implements Device {

	/** The device being proxied */
	private final Device device;

	/** Device location */
	protected LocationProxy loc;

	/** The cached active value */
	private boolean active;

	/** The cached failed value */
	private boolean failed;

	/** Create a new device proxy */
	public DeviceProxy(Device device) {
		super( device );
		this.device = device;
		loc = null;
	}

	/** Refresh the status information */
	public void updateStatusInfo() throws RemoteException {
		failed = device.isFailed();
		active = device.isActive();
	}

	/** Refresh the update information */
	public void updateUpdateInfo() throws RemoteException {
		if(loc == null)
			loc = new LocationProxy(device.getLocation());
		loc.updateUpdateInfo();
	}

	/** Get the controller to which this device is attached */
	public Controller getController() throws RemoteException {
		return device.getController();
	}

	/** Get the device location */
	public Location getLocation() {
		return loc;
	}

	/** Check if the location is valid */
	public boolean hasLocation() {
		return !loc.isZero();
	}

	/** Get the transform to render as a map object */
	public AffineTransform getTransform() {
		return loc.getTransform();
	}

	/** Get the inverse transform */
	public AffineTransform getInverseTransform() {
		return loc.getInverseTransform();
	}

	/** Get the active status */
	public boolean isActive() {
		return active;
	}

	/** Get the failure status */
	public final boolean isFailed() {
		return failed;
	}

	/** Get the administrator notes */
	public final String getNotes() throws RemoteException {
		return device.getNotes();
	}

	/** Set the administrator notes */
	public final void setNotes(String n) throws TMSException,
		RemoteException
	{
		device.setNotes(n);
	}
}
