/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013-2016  Minnesota Department of Transportation
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
import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.ControllerHelper;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.GateArm;
import us.mn.state.dot.tms.GateArmArrayHelper;
import us.mn.state.dot.tms.GateArmState;
import static us.mn.state.dot.tms.GateArmState.TIMEOUT;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.server.comm.DevicePoller;
import us.mn.state.dot.tms.server.comm.GateArmPoller;
import us.mn.state.dot.tms.server.event.GateArmEvent;

/**
 * A Gate Arm is a device for restricting access to a ramp on a road.
 *
 * @author Douglas Lau
 */
public class GateArmImpl extends DeviceImpl implements GateArm {

	/** Timeout (ms) for a comm failure to result in TIMEOUT status */
	static private final long failTimeoutMS() {
		return 1000*SystemAttrEnum.GATE_ARM_ALERT_TIMEOUT_SECS.getInt();
	}

	/** Load all the gate arms */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, GateArmImpl.class);
		store.query("SELECT name, ga_array, idx, controller, pin, " +
			"notes FROM iris." + SONAR_TYPE  + ";",
			new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new GateArmImpl(
					row.getString(1),	// name
					row.getString(2),	// ga_array
					row.getInt(3),		// idx
					row.getString(4),	// controller
					row.getInt(5),		// pin
					row.getString(6)	// notes
				));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("ga_array", ga_array);
		map.put("idx", idx);
		map.put("controller", controller);
		map.put("pin", pin);
		map.put("notes", notes);
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

	/** Create a new gate arm with a string name */
	public GateArmImpl(String n) throws TMSException, SonarException {
		super(n);
		GateArmSystem.disable(n, "create");
	}

	/** Create a gate arm */
	private GateArmImpl(String n, GateArmArrayImpl a, int i,
		ControllerImpl c, int p, String nt)
	{
		super(n, c, p, nt);
		ga_array = a;
		idx = i;
		initTransients();
	}

	/** Create a gate arm */
	private GateArmImpl(String n, String a, int i, String c, int p,
		String nt)
	{
		this(n, (GateArmArrayImpl)GateArmArrayHelper.lookup(a), i,
		    (ControllerImpl)ControllerHelper.lookup(c), p, nt);
	}

	/** Set gate arm array index */
	private void setArrayIndex(GateArmImpl ga) {
		try {
			GateArmArrayImpl a = ga_array;
			if (a != null)
				a.setIndex(idx, ga);
		}
		catch (TMSException e) {
			logError("setArrayIndex: " + e.getMessage());
		}
	}

	/** Initialize the gate arm */
	@Override
	public void initTransients() {
		setArrayIndex(this);
		super.initTransients();
	}

	/** Destroy an object */
	@Override
	public void doDestroy() throws TMSException {
		GateArmSystem.disable(name, "destroy");
		super.doDestroy();
		setArrayIndex(null);
	}

	/** Gate arm array */
	private GateArmArrayImpl ga_array;

	/** Get the gate arm array */
	@Override
	public GateArmArrayImpl getGaArray() {
		return ga_array;
	}

	/** Index in array (1 to MAX_ARMS) */
	private int idx;

	/** Get the index in array (1 to MAX_ARMS) */
	@Override
	public int getIdx() {
		return idx;
	}

	/** Update the controller and/or pin.
	 * @param oc Old controller.
	 * @param op Old pin.
	 * @param nc New controller.
	 * @param np New pin. */
	@Override
	protected void updateControllerPin(ControllerImpl oc, int op,
		ControllerImpl nc, int np)
	{
		GateArmSystem.disable(name, "controller/pin");
		super.updateControllerPin(oc, op, nc, np);
	}

	/** Software version */
	private transient String version;

	/** Set the version */
	public void setVersion(String v) {
		if (!v.equals(version)) {
			version = v;
			notifyAttribute("version");
			ControllerImpl c = (ControllerImpl)getController();
			if (c != null)
				c.setVersion(version);
		}
	}

	/** Get the version */
	@Override
	public String getVersion() {
		return version;
	}

	/** Send a device request operation */
	@Override
	protected void sendDeviceRequest(DeviceRequest dr) {
		DeviceRequest req = GateArmSystem.checkRequest(dr);
		if (req == DeviceRequest.DISABLE_SYSTEM)
			GateArmSystem.disable(name, "system disable");
		else if (req != null) {
			checkTimeout();
			GateArmPoller p = getGateArmPoller();
			if (p != null)
				p.sendRequest(this, req);
		}
	}

	/** Check for comm timeout */
	public void checkTimeout() {
		ControllerImpl c = (ControllerImpl)getController();
		if (c != null) {
			if (c.getFailMillis() > failTimeoutMS())
				setArmStateNotify(TIMEOUT, null);
		}
	}

	/** Send gate arm interlock settings.  Do not test checkEnabled since
	 * this is used to shut off interlocks when disabling gate arm system.*/
	public void sendInterlocks() {
		GateArmPoller p = getGateArmPoller();
		if (p != null)
			p.sendRequest(this, DeviceRequest.SEND_SETTINGS);
	}

	/** Gate arm state */
	private transient GateArmState arm_state = GateArmState.UNKNOWN;

	/** Request a change to the gate arm state.
	 * @param gas Requested gate arm state.
	 * @param o User requesting new state. */
	public void requestArmState(GateArmState gas, User o) {
		if (GateArmSystem.checkEnabled()) {
			GateArmPoller p = getGateArmPoller();
			if (p != null) {
				if (gas == GateArmState.OPENING)
					p.openGate(this, o);
				if (gas == GateArmState.CLOSING)
					p.closeGate(this, o);
			}
		}
	}

	/** Set the gate arm state.
	 * @param gas Gate arm state.
	 * @param o User who requested new state, or null. */
	public void setArmStateNotify(GateArmState gas, User o) {
		if (gas != arm_state) {
			String owner = (o != null) ? o.getName() : null;
			logEvent(new GateArmEvent(gas, name, owner));
			arm_state = gas;
			notifyAttribute("armState");
		}
		ga_array.updateArmState();
	}

	/** Get the arm state */
	public GateArmState getArmStateEnum() {
		return arm_state;
	}

	/** Get the arm state */
	@Override
	public int getArmState() {
		return getArmStateEnum().ordinal();
	}

	/** Check is arm open interlock in effect */
	public boolean isOpenInterlock() {
		return ga_array.isOpenInterlock();
	}

	/** Get the gate arm poller */
	private GateArmPoller getGateArmPoller() {
		DevicePoller dp = getPoller();
		return (dp instanceof GateArmPoller) ? (GateArmPoller)dp : null;
	}

	/** Update the gate arm styles.  This is called by the controller
	 * when active or fail state changes. */
	public void updateStyles() {
		ga_array.updateStyles();
	}
}
