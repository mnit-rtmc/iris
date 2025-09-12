/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013-2025  Minnesota Department of Transportation
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
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.tms.CameraPreset;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.GateArm;
import us.mn.state.dot.tms.GateArmArrayHelper;
import us.mn.state.dot.tms.GateArmInterlock;
import us.mn.state.dot.tms.GateArmState;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.User;
import us.mn.state.dot.tms.server.comm.DevicePoller;
import us.mn.state.dot.tms.server.comm.GateArmPoller;
import us.mn.state.dot.tms.server.event.GateArmEvent;

/**
 * A Gate Arm is a device for restricting access to a ramp on a road.
 *
 * @author Douglas Lau
 */
public class GateArmImpl extends DeviceImpl implements GateArm {

	/** Timeout (ms) for a comm failure to result in UNKNOWN status */
	static private final long failTimeoutMS() {
		return 1000*SystemAttrEnum.GATE_ARM_ALERT_TIMEOUT_SECS.getInt();
	}

	/** Load all the gate arms */
	static protected void loadAll() throws TMSException {
		store.query("SELECT name, ga_array, idx, geo_loc, " +
			"controller, pin, preset, notes, opposing, " +
			"downstream, arm_state, interlock, fault " +
			"FROM iris." + SONAR_TYPE + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new GateArmImpl(row));
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
		map.put("geo_loc", geo_loc);
		map.put("controller", controller);
		map.put("pin", pin);
		map.put("preset", preset);
		map.put("notes", notes);
		map.put("opposing", opposing);
		map.put("downstream", downstream);
		map.put("arm_state", getArmState());
		map.put("interlock", interlock);
		map.put("fault", fault);
		return map;
	}

	/** Create a new gate arm with a string name */
	public GateArmImpl(String n) throws TMSException, SonarException {
		super(n);
		GeoLocImpl g = new GeoLocImpl(name, SONAR_TYPE);
		g.notifyCreate();
		geo_loc = g;
		opposing = true;
		arm_state = GateArmState.UNKNOWN;
		interlock = GateArmInterlock.NONE;
		GateArmSystem.disable(n, "create gate arm");
	}

	/** Create a gate arm */
	private GateArmImpl(ResultSet row) throws SQLException {
		this(row.getString(1),  // name
		     row.getString(2),  // ga_array
		     row.getInt(3),     // idx
		     row.getString(4),  // geo_loc
		     row.getString(5),  // controller
		     row.getInt(6),     // pin
		     row.getString(7),  // preset
		     row.getString(8),  // notes
		     row.getBoolean(9), // opposing
		     row.getString(10), // downstream
		     row.getInt(11),    // arm_state
		     row.getInt(12),    // interlock
		     row.getString(13)  // fault
		);
	}

	/** Create a gate arm */
	private GateArmImpl(String n, String a, int i, String loc,
		String c, int p, String cp, String nt, boolean o, String ds,
		int as, int lk, String flt)
	{
		super(n, lookupController(c), p, nt);
		ga_array = (GateArmArrayImpl) GateArmArrayHelper.lookup(a);
		idx = i;
		geo_loc = lookupGeoLoc(loc);
		setPreset(lookupPreset(cp));
		notes = nt;
		opposing = o;
		downstream = ds;
		arm_state = GateArmState.fromOrdinal(as);
		interlock = GateArmInterlock.fromOrdinal(lk);
		fault = flt;
		initTransients();
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
		// calling updateControllerPin would disable GateArmSystem
		ControllerImpl c = controller;
		if (c != null)
			c.setIO(pin, this);
		updateStyles();
	}

	/** Destroy an object */
	@Override
	public void doDestroy() throws TMSException {
		super.doDestroy();
		geo_loc.notifyRemove();
		GateArmSystem.disable(name, "destroy gate arm");
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

	/** Device location */
	private final GeoLocImpl geo_loc;

	/** Get the device location */
	@Override
	public GeoLoc getGeoLoc() {
		return geo_loc;
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
		GateArmSystem.disable(name, "update controller/pin");
		super.updateControllerPin(oc, op, nc, np);
	}

	/** Camera preset from which this can be seen */
	private CameraPreset preset;

	/** Set the verification camera preset */
	@Override
	public void setPreset(CameraPreset cp) {
		final CameraPreset ocp = preset;
		if (cp instanceof CameraPresetImpl) {
			CameraPresetImpl cpi = (CameraPresetImpl) cp;
			cpi.setAssignedNotify(true);
		}
		preset = cp;
		if (ocp instanceof CameraPresetImpl) {
			CameraPresetImpl ocpi = (CameraPresetImpl) ocp;
			ocpi.setAssignedNotify(false);
		}
	}

	/** Set the verification camera preset */
	public void doSetPreset(CameraPreset cp) throws TMSException {
		if (!objectEquals(cp, preset)) {
			GateArmSystem.disable(name, "set preset");
			store.update(this, "preset", cp);
			setPreset(cp);
		}
	}

	/** Get verification camera preset */
	@Override
	public CameraPreset getPreset() {
		return preset;
	}

	/** Opposing traffic flag */
	private boolean opposing;

	/** Set the opposing traffic flag */
	@Override
	public void setOpposing(boolean ot) {
		GateArmSystem.disable(name, "set opposing");
		opposing = ot;
	}

	/** Set the opposing traffic flag */
	public void doSetOpposing(boolean ot) throws TMSException {
		if (ot != opposing) {
			store.update(this, "opposing", ot);
			setOpposing(ot);
		}
	}

	/** Get the opposing traffic flag */
	@Override
	public boolean getOpposing() {
		return opposing;
	}

	/** Downstream hashtag */
	private String downstream;

	/** Set the downstream hashtag */
	@Override
	public void setDownstream(String ds) {
		GateArmSystem.disable(name, "set downstream");
		downstream = ds;
	}

	/** Set the downstream hashtag */
	public void doSetDownstream(String ds) throws TMSException {
		if (!objectEquals(ds, downstream)) {
			store.update(this, "downstream", ds);
			setDownstream(ds);
		}
	}

	/** Get downstream hashtag */
	@Override
	public String getDownstream() {
		return downstream;
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

	/** Check for comm timeout to UNKNOWN status */
	public void checkTimeout() {
		if (getFailMillis() > failTimeoutMS())
			setArmStateNotify(GateArmState.UNKNOWN);
	}

	/** Gate arm state */
	private GateArmState arm_state;

	/** Set the gate arm state and notify clients */
	public void setArmStateNotify(GateArmState gas) {
		if (gas == arm_state)
			return;
		logEvent(new GateArmEvent(gas, name, fault));
		try {
			store.update(this, "arm_state", gas.ordinal());
		}
		catch (TMSException e) {
			GateArmSystem.disable(name, "DB arm_state");
			logError("setArmStateNotify: " + e.getMessage());
			return;
		}
		arm_state = gas;
		notifyAttribute("armState");
		if (gas == GateArmState.UNKNOWN)
			sendEmailAlert("COMMUNICATION FAILED: " + name);
		if (gas == GateArmState.FAULT)
			sendEmailAlert("FAULT: " + name);
		updateStyles();
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


	/** Test if gate arm is deployable */
	public boolean isDeployable(boolean open) {
		if (open) {
			return isOnline() &&
			      interlock.isOpenAllowed() &&
			      !hasFaults();
		} else {
			return isOnline() &&
			      interlock.isCloseAllowed() &&
			      !hasFaults();
		}
	}

	/** Gate arm interlock */
	private GateArmInterlock interlock;

	/** Get the interlock ordinal */
	@Override
	public int getInterlock() {
		return interlock.ordinal();
	}

	/** Get the interlock enum */
	public GateArmInterlock getInterlockEnum() {
		return interlock;
	}

	/** Set the interlock flag */
	private void setInterlockNotify() {
		GateArmInterlock lk = lock_state.getInterlock();
		if (lk != interlock) {
			try {
				store.update(this, "interlock", lk.ordinal());
			}
			catch (TMSException e) {
				GateArmSystem.disable(name, "DB interlock");
			}
			interlock = lk;
			notifyAttribute("interlock");
			sendInterlocks();
		}
	}

	/** Lock state for calculating interlock */
	private transient GateArmLockState lock_state = new GateArmLockState();

	/** Begin dependency transaction */
	public void beginDependencies() {
		lock_state.beginDependencies();
	}

	/** Check gate arm array dependencies */
	public void checkDependencies() {
		// FIXME: adapt from gate arm arrays
		// FIXME: also, check all gate arms with downstream hashtag
	}

	/** Commit dependcy transaction */
	public void commitDependencies() {
		lock_state.commitDependencies();
		setInterlockNotify();
	}

	/** Check if gate arm open is locked */
	public boolean isOpenLocked() {
		return ga_array.getInterlockEnum().isOpenLocked();
	}

	/** Fault description */
	private String fault;

	/** Get fault description (or null) */
	@Override
	public String getFault() {
		return fault;
	}

	/** Set fault description */
	public void setFaultNotify(String flt) {
		flt = trimTruncate(flt, 32);
		if (!objectEquals(flt, fault)) {
			try {
				store.update(this, "fault", flt);
			}
			catch (TMSException e) {
				logError("setFaultNotify: " + e.getMessage());
			}
			fault = flt;
			notifyAttribute("fault");
		}
	}

	/** Get the gate arm poller */
	private GateArmPoller getGateArmPoller() {
		DevicePoller dp = getPoller();
		return (dp instanceof GateArmPoller) ? (GateArmPoller) dp :null;
	}

	/** Update the gate arm styles.  This is called by the controller
	 * when active or fail state changes. */
	public void updateStyles() {
		ga_array.updateStyles();
	}

	/** Choose the planned action */
	@Override
	public PlannedAction choosePlannedAction() {
		PlannedAction pa = super.choosePlannedAction();
		GateArmInterlock gai = interlock;
		if (pa != null && gai.isOpenAllowed())
			requestArmState(GateArmState.OPENING);
		if (pa == null && gai.isCloseAllowed())
			requestArmState(GateArmState.CLOSING);
		return pa;
	}

	/** Request a change to the gate arm state.
	 * @param gas Requested gate arm state. */
	private void requestArmState(GateArmState gas) {
		if (GateArmSystem.checkEnabled()) {
			if (gas != arm_state) {
				GateArmPoller p = getGateArmPoller();
				if (p != null)
					requestArmState(p, gas);
			}
		}
	}

	/** Request a change to the gate arm state */
	private void requestArmState(GateArmPoller p, GateArmState gas) {
		if (gas == GateArmState.OPENING)
			p.openGate(this);
		if (gas == GateArmState.CLOSING)
			p.closeGate(this);
	}

	/** Set the lock (JSON) */
	@Override
	public void setLock(String lk) {
		// FIXME: adapt from gate arm array?
	}
}
