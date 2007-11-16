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
import us.mn.state.dot.tms.comm.MessagePoller;
import us.mn.state.dot.vault.FieldMap;
import us.mn.state.dot.vault.ObjectVaultException;

/**
 * DeviceImpl is the base class for all field devices, including detectors,
 * cameras, ramp meters, dynamic message signs, etc.
 *
 * @author Douglas Lau
 */
abstract class DeviceImpl extends TMSObjectImpl implements Device, ControllerIO,
	Storable
{
	/** ObjectVault table name */
	static public final String tableName = "device";

	/** Get the database table name */
	public String getTable() {
		return tableName;
	}

	/** Create a new device */
	public DeviceImpl() throws RemoteException {
		location = new LocationImpl();
		notes = "";
	}

	/** Constructor needed for ObjectVault */
	protected DeviceImpl(FieldMap fields) throws RemoteException {
		location = (LocationImpl)fields.get("location");
	}

	/** Initialize the controller for this device */
	public void initTransients() throws ObjectVaultException,
		TMSException, RemoteException
	{
		try {
			ControllerImpl c = controller;
			if(c != null)
				c.setIO(pin, this);
		}
		catch(TMSException e) {
			System.err.println("Device " + getId() +
				" initialization error");
			e.printStackTrace();
		}
	}

	/** Get the "unique" string id for this object */
	abstract String getId();

	/** Get the active status */
	public boolean isActive() {
		ControllerImpl c = controller;	// Avoid race
		if(c == null)
			return false;
		else
			return c.isActive();
	}

	/** Is the device available for a controller? */
	public boolean isAvailable() {
		return controller == null;
	}

	/** Is this object deletable? */
	public boolean isDeletable() throws TMSException {
		if(isActive())
			return false;
		else
			return super.isDeletable();
	}

	/** Controller associated with this traffic device */
	protected ControllerImpl controller;

	/** Set the controller of the device */
	public synchronized void setController(ControllerImpl c)
		throws TMSException
	{
		if(c == controller)
			return;
		if(c != null && controller != null)
			throw new ChangeVetoException("Device has controller");
		store.update(this, "controller", c.getOID());
		controller = c;
	}

	/** Get the controller to which this device is assigned */
	public Controller getController() {
		return controller;
	}

	/** Get the message poller */
	public MessagePoller getPoller() {
		ControllerImpl c = controller;	// Avoid race
		if(c != null)
			return c.getPoller();
		else
			return null;
	}

	/** Controller I/O pin number */
	protected int pin;

	/** Set the controller I/O pin number */
	public synchronized void setPin(int p) throws TMSException {
		if(p == pin)
			return;
		store.update(this, "pin", p);
		pin = p;
	}

	/** Get the controller I/O pin number */
	public int getPin() {
		return pin;
	}

	/** Get the failure status */
	public boolean isFailed() {
		ControllerImpl c = controller;	// Avoid race
		if(c == null)
			return true;
		else
			return c.isFailed();
	}

	/** Device location */
	protected final LocationImpl location;

	/** Get the device location */
	public Location getLocation() {
		return location;
	}

	/** Administrator notes for this device */
	protected String notes;

	/** Get the administrator notes */
	public String getNotes() {
		return notes;
	}

	/** Set the administrator notes */
	public synchronized void setNotes(String n) throws TMSException {
		if(n.equals(notes))
			return;
		validateText(n);
		store.update(this, "notes", n);
		notes = n;
	}
}
