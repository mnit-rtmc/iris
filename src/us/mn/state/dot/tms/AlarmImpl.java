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

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.tms.event.AlarmEvent;
import us.mn.state.dot.tms.event.EventType;

/**
 * AlarmImpl is a class for reading alarm inputs on controllers.
 *
 * @author Douglas Lau
 */
public class AlarmImpl extends BaseObjectImpl implements Alarm, ControllerIO {

	/** Get the event type for the new state */
	static protected EventType getEventType(boolean s) {
		if(s)
			return EventType.ALARM_TRIGGERED;
		else
			return EventType.ALARM_CLEARED;
	}

	/** Load all the comm links */
	static protected void loadAll() throws TMSException {
		System.err.println("Loading alarms...");
		namespace.registerType(SONAR_TYPE, AlarmImpl.class);
		store.query("SELECT name, description, controller, pin, state" +
			" FROM iris." + SONAR_TYPE + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.add(new AlarmImpl(namespace,
					row.getString(1),	// name
					row.getString(2),	// description
					row.getString(3),	// controller
					row.getInt(4),		// pin
					row.getBoolean(5)	// state
				));
			}
		});
	}

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("description", description);
		if(controller != null)
			map.put("controller", controller.getName());
		map.put("pin", pin);
		map.put("state", state);
		return map;
	}

	/** Get the database table name */
	public String getTable() {
		return "iris." + SONAR_TYPE;
	}

	/** Get the SONAR type name */
	public String getTypeName() {
		return SONAR_TYPE;
	}

	/** Create a new alarm */
	public AlarmImpl(String n) {
		super(n);
		state = false;
	}

	/** Create a new alarm */
	public AlarmImpl(String n, String d, ControllerImpl c, int p,
		boolean s)
	{
		this(n);
		description = d;
		controller = c;
		pin = p;
		state = s;
		initTransients();
	}

	/** Create a new alarm */
	public AlarmImpl(Namespace ns, String n, String d, String c, int p,
		boolean s)
	{
		this(n, d, (ControllerImpl)ns.lookupObject(
			Controller.SONAR_TYPE, c), p, s);
	}

	/** Initialize the controller for this alarm */
	public void initTransients() {
		try {
			ControllerImpl c = controller;
			if(c != null)
				c.setIO(pin, this);
		}
		catch(TMSException e) {
			System.err.println("Alarm " + getName() +
				" initialization error");
			e.printStackTrace();
		}
	}

	/** Description of the alarm */
	protected String description = "";

	/** Set the description */
	public void setDescription(String d) {
		description = d;
	}

	/** Set the description */
	public void doSetDescription(String d) throws TMSException {
		if(d.equals(description))
			return;
		store.update(this, "description", d);
		setDescription(d);
	}

	/** Get the description */
	public String getDescription() {
		return description;
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
	public void setController(Controller c) {
		controller = (ControllerImpl)c;
	}

	/** Set the controller of the alarm */
	public void doSetController(Controller c) throws TMSException {
		if(c == controller)
			return;
		updateController((ControllerImpl)c, pin);
		if(c == null)
			store.update(this, "controller", null);
		else
			store.update(this, "controller", c.getName());
		setController(c);
	}

	/** Get the controller to which this alarm is assigned */
	public Controller getController() {
		return controller;
	}

	/** Controller I/O pin number */
	protected int pin;

	/** Set the controller I/O pin number */
	public void setPin(int p) {
		pin = p;
	}

	/** Set the controller I/O pin number */
	public void doSetPin(int p) throws TMSException {
		if(p == pin)
			return;
		updateController(controller, p);
		store.update(this, "pin", p);
		setPin(p);
	}

	/** Get the controller I/O pin number */
	public int getPin() {
		return pin;
	}

	/** Current state of the alarm */
	protected boolean state;

	/** Update the state of the alarm. This is not meant to be writable
	 * by SONAR clients since it is updated by reading from controller. */
	public void setStateNotify(boolean s) {
		if(s == state)
			return;
		AlarmEvent ev = new AlarmEvent(getEventType(s), getName());
		try {
			store.update(this, "state", s);
			ev.doStore();
			state = s;
			notifyState();
		}
		catch(TMSException e) {
			e.printStackTrace();
		}
	}

	/** Notify SONAR clients of changes to the alarm state */
	protected void notifyState() {
		if(MainServer.server != null) {
			String[] s = new String[] { String.valueOf(state) };
			MainServer.server.setAttribute(this, "state", s);
		}
	}

	/** Get the state of the alarm */
	public boolean getState() {
		return state;
	}
}
