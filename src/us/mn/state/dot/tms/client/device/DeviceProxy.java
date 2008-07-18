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
package us.mn.state.dot.tms.client.device;

import java.awt.geom.AffineTransform;
import java.rmi.RemoteException;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.Device;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.client.proxy.GeoTransform;
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

	/** Device location name */
	protected String geo_loc;

	/** Device location */
	protected GeoLoc loc;

	/** Device transform */
	protected GeoTransform trans;

	/** The cached active value */
	private boolean active;

	/** The cached failed value */
	private boolean failed;

	/** Create a new device proxy */
	public DeviceProxy(Device device) {
		super( device );
		this.device = device;
	}

	/** Refresh the status information */
	public void updateStatusInfo() throws RemoteException {
		failed = device.isFailed();
		active = device.isActive();
	}

	/** Refresh the update information */
	public void updateUpdateInfo() throws RemoteException {
		geo_loc = device.getGeoLoc();
		loc = SonarState.singleton.lookupGeoLoc(geo_loc);
		trans = new GeoTransform(loc);
	}

	/** Get the controller to which this device is attached */
	public Controller getController() throws RemoteException {
		return device.getController();
	}

	/** Set the device controller */
	public void setController(Controller c) throws TMSException,
		RemoteException
	{
		device.setController(c);
	}

	/** Get the I/O pin */
	public int getPin() throws RemoteException {
		return device.getPin();
	}

	/** Set the I/O pin */
	public void setPin(int p) throws TMSException, RemoteException {
		device.setPin(p);
	}

	/** Get the device location */
	public String getGeoLoc() {
		return geo_loc;
	}

	/** Set the device location */
	public void setGeoLoc(String l) {
		// FIXME
	}

	/** Get the geo location description */
	public String getDescription() {
		return GeoLocHelper.getDescription(loc);
	}

	/** Get the cross street description */
	public String getCrossDescription() {
		return GeoLocHelper.getCrossDescription(loc);
	}

	/** Get the freeway direction */
	public short getFreeDir() {
		return loc.getFreeDir();
	}

	/** Check if the location is valid */
	public boolean hasLocation() {
		return !GeoLocHelper.isNull(loc);
	}

	/** Get the transform to render as a map object */
	public AffineTransform getTransform() {
		return trans.getTransform();
	}

	/** Get the inverse transform */
	public AffineTransform getInverseTransform() {
		return trans.getInverseTransform();
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
