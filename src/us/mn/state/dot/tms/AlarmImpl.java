/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005-2008  Minnesota Department of Transportation
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
import java.util.Map;
import us.mn.state.dot.vault.ObjectVaultException;

/**
 * AlarmImpl is a class for reading alarm inputs on controllers.
 *
 * @author Douglas Lau
 */
public class AlarmImpl extends TMSObjectImpl implements Alarm, ControllerIO,
	Storable
{
	/** ObjectVault table name */
	static public final String tableName = "alarm";

	/** Alarm debug log */
	static protected final DebugLog ALARM_LOG = new DebugLog("alarm");

	/** Get the database table name */
	public String getTable() {
		return tableName;
	}

	/** Create a new alarm */
	public AlarmImpl() throws RemoteException {
		notes = "";
	}

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		// FIXME: implement this for SONAR
		return null;
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

	/** Update the controller and/or pin */
	protected void updateController(ControllerImpl c, int p)
		throws TMSException
	{
		if(controller != null)
			controller.setIO(pin, null);
		try {
			if(c != null)
				c.setIO(p, this);
		}
		catch(TMSException e) {
			if(controller != null)
				controller.setIO(pin, this);
			throw e;
		}
	}

	/** Set the controller of the alarm */
	public synchronized void setController(ControllerImpl c)
		throws TMSException
	{
		if(c == controller)
			return;
		updateController(c, pin);
		if(c == null)
			store.update(this, "controller", "0");
		else
			store.update(this, "controller", c.getOID());
		controller = c;
	}

	/** Set the controller of the alarm */
	public void setController(Controller c) throws TMSException {
		ControllerImpl ctr = lineList.findController(c);
		setController(ctr);
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
		updateController(controller, p);
		store.update(this, "pin", p);
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
		store.update(this, "notes", n);
		notes = n;
	}

	/** Current state of the alarm */
	protected transient boolean state;

	/** Update the state of the alarm */
	public void setState(boolean s) {
		if(s != state) {
			String m = s ? "TRIGGERED" : "CLEARED";
			ALARM_LOG.log("ALARM " + m + " on " + controller +
				" pin " + pin + ": " + notes);
		}
		state = s;
	}
}
