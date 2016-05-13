/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server;

import java.sql.ResultSet;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.tms.Alarm;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.ControllerIO;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.server.comm.AlarmPoller;
import us.mn.state.dot.tms.server.comm.DevicePoller;
import us.mn.state.dot.tms.server.event.AlarmEvent;

/**
 * AlarmImpl is a class for reading alarm inputs on controllers.
 *
 * @author Douglas Lau
 */
public class AlarmImpl extends BaseObjectImpl implements Alarm, ControllerIO {

	/** Get the event type for the new state */
	static private EventType getEventType(boolean s) {
		if (s)
			return EventType.ALARM_TRIGGERED;
		else
			return EventType.ALARM_CLEARED;
	}

	/** Load all the alarms */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, AlarmImpl.class);
		store.query("SELECT name, description, controller, pin, " +
			"state, trigger_time FROM iris." + SONAR_TYPE + ";",
			new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new AlarmImpl(
					row.getString(1),	// name
					row.getString(2),	// description
					row.getString(3),	// controller
					row.getInt(4),		// pin
					row.getBoolean(5),	// state
					row.getTimestamp(6)	// triggerTime
				));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("description", description);
		map.put("controller", controller);
		map.put("pin", pin);
		map.put("state", state);
		map.put("trigger_time", asTimestamp(triggerTime));
		return map;
	}

	/** Get the database table name */
	@Override
	public String getTable() {
		return "iris." + SONAR_TYPE;
	}

	/** Get the SONAR type name */
	@Override
	public String getTypeName() {
		return SONAR_TYPE;
	}

	/** Create a new alarm */
	public AlarmImpl(String n) {
		super(n);
		state = false;
	}

	/** Create a new alarm */
	private AlarmImpl(String n, String d, ControllerImpl c, int p,
		boolean s, Date tt)
	{
		this(n);
		description = d;
		controller = c;
		pin = p;
		state = s;
		triggerTime = stampMillis(tt);
		initTransients();
	}

	/** Create a new alarm */
	private AlarmImpl(String n, String d, String c, int p, boolean s,
		Date tt)
	{
		this(n, d, lookupController(c), p, s, tt);
	}

	/** Initialize the controller for this alarm */
	@Override
	public void initTransients() {
		updateControllerPin(null, 0, controller, pin);
	}

	/** Description of the alarm */
	private String description = "";

	/** Set the description */
	@Override
	public void setDescription(String d) {
		description = d;
	}

	/** Set the description */
	public void doSetDescription(String d) throws TMSException {
		if (d.equals(description))
			return;
		store.update(this, "description", d);
		setDescription(d);
	}

	/** Get the description */
	@Override
	public String getDescription() {
		return description;
	}

	/** Controller associated with this alarm */
	private ControllerImpl controller;

	/** Update the controller and/or pin.
	 * @param oc Old controller.
	 * @param op Old pin.
	 * @param nc New controller.
	 * @param np New pin. */
	private void updateControllerPin(ControllerImpl oc, int op,
		ControllerImpl nc, int np)
	{
		if (oc != null)
			oc.setIO(op, null);
		if (nc != null)
			nc.setIO(np, this);
	}

	/** Set the controller of the alarm */
	@Override
	public void setController(Controller c) {
		controller = (ControllerImpl)c;
	}

	/** Set the controller of the alarm */
	public void doSetController(Controller c) throws TMSException {
		if (c == controller)
			return;
		if (pin < 1 || pin > Controller.ALL_PINS)
			throw new ChangeVetoException("Invalid pin: " + pin);
		store.update(this, "controller", c);
		updateControllerPin(controller, pin, (ControllerImpl)c, pin);
		setController(c);
	}

	/** Get the controller to which this alarm is assigned */
	@Override
	public Controller getController() {
		return controller;
	}

	/** Controller I/O pin number */
	private int pin;

	/** Set the controller I/O pin number */
	@Override
	public void setPin(int p) {
		pin = p;
	}

	/** Set the controller I/O pin number */
	public void doSetPin(int p) throws TMSException {
		if (p == pin)
			return;
		if (p < 1 || p > Controller.ALL_PINS)
			throw new ChangeVetoException("Invalid pin: " + p);
		store.update(this, "pin", p);
		updateControllerPin(controller, pin, controller, p);
		setPin(p);
	}

	/** Get the controller I/O pin number */
	@Override
	public int getPin() {
		return pin;
	}

	/** Current state of the alarm */
	private boolean state;

	/** Update the state of the alarm. This is not meant to be writable
	 * by SONAR clients since it is updated by reading from controller. */
	public void setStateNotify(boolean s) {
		if (s == state)
			return;
		if (s)
			setTriggerTime();
		AlarmEvent ev = new AlarmEvent(getEventType(s), getName());
		try {
			store.update(this, "state", s);
			ev.doStore();
			state = s;
			notifyAttribute("state");
		}
		catch (TMSException e) {
			e.printStackTrace();
		}
	}

	/** Get the state of the alarm */
	@Override
	public boolean getState() {
		return state;
	}

	/** Time stamp of most recent alarm trigger */
	private Long triggerTime = TimeSteward.currentTimeMillis();

	/** Set the trigger time */
	private void setTriggerTime() {
		Long tt = TimeSteward.currentTimeMillis();
		try {
			store.update(this, "trigger_time", asTimestamp(tt));
		}
		catch (TMSException e) {
			// FIXME: what else can we do with this exception?
			e.printStackTrace();
		}
		triggerTime = tt;
		notifyAttribute("triggerTime");
	}

	/** Get the most recent alarm trigger time. This time is in
	 * milliseconds since the epoch. */
	@Override
	public Long getTriggerTime() {
		return triggerTime;
	}

	/** Destroy an alarm */
	@Override
	public void doDestroy() throws TMSException {
		// Don't allow an alarm to be destroyed if it is assigned to
		// a controller.  This is needed because the Controller io_pins
		// HashMap will still have a reference to the alarm.
		if (controller != null) {
			throw new ChangeVetoException("Alarm must be removed" +
				" from controller before being destroyed: " +
				name);
		}
		super.doDestroy();
	}

	/** Request a device operation */
	public void sendDeviceRequest(DeviceRequest req) {
		AlarmPoller p = getAlarmPoller();
		if (p != null)
			p.sendRequest(this, req);
	}

	/** Get an alarm poller */
	private AlarmPoller getAlarmPoller() {
		DevicePoller dp = getPoller();
		return (dp instanceof AlarmPoller) ? (AlarmPoller) dp : null;
	}

	/** Get the device poller */
	private DevicePoller getPoller() {
		ControllerImpl c = controller;	// Avoid race
		return (c != null) ? c.getPoller() : null;
	}

	/** Perform a periodic poll */
	public void periodicPoll() {
		sendDeviceRequest(DeviceRequest.QUERY_STATUS);
	}
}
