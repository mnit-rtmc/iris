/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005-2007  Minnesota Department of Transportation
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
import us.mn.state.dot.vault.ObjectVaultException;

/**
 * AlarmImpl is a class for reading alarm inputs on controllers.
 *
 * @author Douglas Lau
 */
public class AlarmImpl extends TMSObjectImpl implements Alarm, ControllerIO {

	/** ObjectVault table name */
	static public final String tableName = "alarm";

	/** Create a new alarm */
	public AlarmImpl() throws RemoteException {
		notes = "";
	}

	/** Initialize the controller for this alarm */
	public void initTransients() throws ObjectVaultException,
		TMSException, RemoteException
	{
		super.initTransients();
		ControllerImpl c = controller;
		if(c != null)
			c.setIO(pin, this);
	}

	/** Controller associated with this alarm */
	protected ControllerImpl controller;

	/** Set the controller of the alarm */
	public synchronized void setController(ControllerImpl c)
		throws TMSException
	{
		if(c == controller)
			return;
		try {
			vault.update(this, "controller", c, getUserName());
		}
		catch(ObjectVaultException e) {
			throw new TMSException(e);
		}
		controller = c;
	}

	/** Get the controller to which this alarm is assigned */
	public Controller getController() {
		return controller;
	}

	/** Controller I/O pin number */
	protected int pin;

	/** Set the controller I/O pin number */
	public synchronized void setPin(int p) throws TMSException {
		if(p == pin)
			return;
		try {
			vault.update(this, "pin", p, getUserName());
		}
		catch(ObjectVaultException e) {
			throw new TMSException(e);
		}
		pin = p;
	}

	/** Get the controller I/O pin number */
	public int getPin() {
		return pin;
	}

	/** Administrator notes for this alarm */
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
		try {
			vault.update(this, "notes", n, getUserName());
		}
		catch(ObjectVaultException e) {
			throw new TMSException(e);
		}
		notes = n;
	}

	/** Current state of the alarm */
	protected transient boolean state;

	/** Update the state of the alarm */
	public void setState(boolean s) {
		if(s != state) {
			String m = s ? "TRIGGERED" : "CLEARED";
			System.err.println("ALARM " + m + " on " + controller +
				" pin " + pin + ": " + notes);
		}
		state = s;
	}
}
