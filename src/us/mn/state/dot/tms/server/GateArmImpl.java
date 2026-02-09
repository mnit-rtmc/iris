/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013-2026  Minnesota Department of Transportation
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.tms.CameraPreset;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.GateArm;
import us.mn.state.dot.tms.GateArmHelper;
import us.mn.state.dot.tms.GateArmInterlock;
import us.mn.state.dot.tms.GateArmState;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.Hashtags;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.Road;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.TMSException;
import static us.mn.state.dot.tms.server.GateArmSystem.sendEmailAlert;
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
		store.query("SELECT name, geo_loc, controller, pin, " +
			"preset, notes, opposing, downstream_hashtag, " +
			"arm_state, interlock, fault FROM iris." +
			SONAR_TYPE + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new GateArmImpl(row));
			}
		});
		initAllTransients();
	}

	/** Initialize transients for all gate arms.  This needs to happen after
	 * all gate arms are loaded (for resolving dependencies). */
	static private void initAllTransients() throws TMSException {
		Iterator<GateArm> it = GateArmHelper.iterator();
		while (it.hasNext()) {
			GateArm ga = it.next();
			if (ga instanceof GateArmImpl)
				((GateArmImpl) ga).initTransients();
		}
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("geo_loc", geo_loc);
		map.put("controller", controller);
		map.put("pin", pin);
		map.put("preset", preset);
		map.put("notes", notes);
		map.put("opposing", opposing);
		map.put("downstream_hashtag", downstream_hashtag);
		map.put("arm_state", getArmState());
		map.put("interlock", getInterlock());
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
		     row.getString(2),  // geo_loc
		     row.getString(3),  // controller
		     row.getInt(4),     // pin
		     row.getString(5),  // preset
		     row.getString(6),  // notes
		     row.getBoolean(7), // opposing
		     row.getString(8),  // downstream_hashtag
		     row.getInt(9),     // arm_state
		     row.getInt(10),    // interlock
		     row.getString(11)  // fault
		);
	}

	/** Create a gate arm */
	private GateArmImpl(String n, String loc, String c, int p, String cp,
		String nt, boolean o, String ds, int as, int lk, String flt)
	{
		super(n, lookupController(c), p, nt);
		geo_loc = lookupGeoLoc(loc);
		setPreset(lookupPreset(cp));
		notes = nt;
		opposing = o;
		downstream_hashtag = ds;
		arm_state = GateArmState.fromOrdinal(as);
		interlock = GateArmInterlock.fromOrdinal(lk);
		fault = flt;
	}

	/** Initialize the gate arm */
	@Override
	public void initTransients() {
		// calling updateControllerPin would disable GateArmSystem
		ControllerImpl c = controller;
		if (c != null)
			c.setIO(pin, this);
		if (GateArmInterlock.SYSTEM_DISABLE != interlock) {
			if (isActive())
				initDependencyArrays();
		}
	}

	/** Destroy an object */
	@Override
	public void doDestroy() throws TMSException {
		super.doDestroy();
		geo_loc.notifyRemove();
		GateArmSystem.disable(name, "destroy gate arm");
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

	/** Verification camera preset */
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
	private String downstream_hashtag;

	/** Set the downstream hashtag */
	@Override
	public void setDownstreamHashtag(String ds) {
		GateArmSystem.disable(name, "set downstream_hashtag");
		downstream_hashtag = ds;
	}

	/** Set the downstream hashtag */
	public void doSetDownstreamHashtag(String ds) throws TMSException {
		if (!objectEquals(ds, downstream_hashtag)) {
			store.update(this, "downstream_hashtag", ds);
			setDownstreamHashtag(ds);
		}
	}

	/** Get downstream hashtag */
	@Override
	public String getDownstreamHashtag() {
		return downstream_hashtag;
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
		if (gas == arm_state) {
			updateStyles();
			return;
		}
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

	/** Test if gate arm is closed */
	private boolean isClosed() {
		return isOnline() && arm_state == GateArmState.CLOSED;
	}

	/** Test if gate arm is possibly open */
	private boolean isPossiblyOpen() {
		return isActive() && (
		       isOffline() || arm_state != GateArmState.CLOSED
		);
	}

	/** Test if gate arm is open */
	private boolean isOpen() {
		return isOnline() && isPossiblyOpen();
	}

	/** Test if gate arm is possibly closed */
	private boolean isPossiblyClosed() {
		return isActive() && (
		       isOffline() || arm_state != GateArmState.OPEN
		);
	}

	/** Test if gate arm is changing */
	private boolean isChanging() {
		return isOnline() && (
		       arm_state == GateArmState.OPENING ||
		       arm_state == GateArmState.CLOSING
		);
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

	/** Send gate arm interlock settings.
	 *
	 * Do not test checkEnabled since this is used to shut off interlocks
	 * when disabling gate arm system.*/
	private void sendInterlocks() {
		GateArmPoller p = getGateArmPoller();
		if (p != null)
			p.sendRequest(this, DeviceRequest.SEND_SETTINGS);
	}

	/** Opposing gate arm dependencies */
	private transient ArrayList<GateArmImpl> opposing_arms =
		new ArrayList<GateArmImpl>();

	/** Downstream gate arm dependencies */
	private transient ArrayList<GateArmImpl> downstream_arms =
		new ArrayList<GateArmImpl>();

	/** Upstream gate arm dependencies */
	private transient ArrayList<GateArmImpl> upstream_arms =
		new ArrayList<GateArmImpl>();

	/** Check if gate arm is denied from opening */
	public boolean isOpenDenied() {
		return getInterlockEnum().isOpenDenied();
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

	/** Test if gate arm has faults */
	@Override
	protected boolean hasFaults() {
		return (fault != null) || (arm_state == GateArmState.FAULT);
	}

	/** Get the gate arm poller */
	private GateArmPoller getGateArmPoller() {
		DevicePoller dp = getPoller();
		return (dp instanceof GateArmPoller) ? (GateArmPoller) dp :null;
	}

	/** Update the gate arm styles.  This is called by the controller
	 * when active or fail state changes. */
	@Override
	public void updateStyles() {
		checkDependencies();
		super.updateStyles();
	}

	/** Check dependencies */
	private void checkDependencies() {
		GateArmInterlock before = interlock;
		updateInterlock(GateArmSystem.checkEnabled());
		setOpenConflict(interlock.isOpenDenied() && isPossiblyOpen());
		setCloseConflict(interlock.isCloseDenied() && isClosed());
		GateArmInterlock after = interlock;
		if (before != after) {
			if (GateArmInterlock.SYSTEM_DISABLE == after) {
				opposing_arms.clear();
				downstream_arms.clear();
				upstream_arms.clear();
			}
			if (GateArmInterlock.SYSTEM_DISABLE == before) {
				if (isActive())
					initDependencyArrays();
			}
		}
	}

	/** Update the interlock by checking dependencies */
	private void updateInterlock(boolean e) {
		GateArmLockState ls = new GateArmLockState(e && isActive());
		for (GateArmImpl ga: opposing_arms) {
			if (ga.isPossiblyOpen())
				ls.setOpposingOpen();
		}
		for (GateArmImpl ga: downstream_arms) {
			if (ga.isPossiblyClosed())
				ls.setDownstreamClosed();
		}
		for (GateArmImpl ga: upstream_arms) {
			if (ga.isPossiblyOpen())
				ls.setUpstreamOpen();
		}
		setInterlockNotify(ls.getInterlock());
	}

	/** Set the interlock enum */
	private void setInterlockNotify(GateArmInterlock lk) {
		if (lk != interlock) {
			try {
				store.update(this, "interlock", lk.ordinal());
			}
			catch (TMSException e) {
				GateArmSystem.disable(name, "DB interlock");
				logError("setInterlockNotify: " +
					e.getMessage());
				return;
			}
			interlock = lk;
			sendInterlocks();
			notifyAttribute("interlock");
		}
	}

	/** Open conflict detected flag.  This is initially set to true because
	 * devices start in failed state after a server restart. */
	private transient boolean open_conflict = true;

	/** Set open conflict state */
	private void setOpenConflict(boolean c) {
		if (c != open_conflict) {
			open_conflict = c;
			if (c)
				sendEmailAlert("OPEN CONFLICT: " + name);
		}
	}

	/** Close conflict detected flag. */
	private transient boolean close_conflict = false;

	/** Set close conflict state */
	private void setCloseConflict(boolean c) {
		if (c != close_conflict) {
			close_conflict = c;
			if (c)
				sendEmailAlert("CLOSE CONFLICT: " + name);
		}
	}

	/** Initialize opposing / downstream / upstream arrays */
	private void initDependencyArrays() {
		final Road road = getRoad();
		final int dir = (opposing) ? getRoadDir() : 0;
		Hashtags my_tags = new Hashtags(getNotes());
		Iterator<GateArm> it = GateArmHelper.iterator();
		while (it.hasNext()) {
			GateArm ga = it.next();
			if (!(ga instanceof GateArmImpl))
				continue;
			GateArmImpl gai = (GateArmImpl) ga;
			if (gai.isActive()) {
				if (my_tags.contains(gai.getDownstreamHashtag()))
					upstream_arms.add(gai);
				Hashtags tags = new Hashtags(gai.getNotes());
				if (tags.contains(downstream_hashtag))
					downstream_arms.add(gai);
				int rd = gai.getRoadDir();
				if (objectEquals(road, gai.getRoad()) &&
				    (dir != rd) &&
				    (dir != 0) &&
				    (rd != 0))
				{
					opposing_arms.add(gai);
				}
			}
		}
	}

	/** Get gate arm road */
	private Road getRoad() {
		GeoLoc gl = getGeoLoc();
		return (gl != null) ? gl.getRoadway() : null;
	}

	/** Get gate arm road direction.
	 * @return Index of road direction, or 0 for unknown */
	private int getRoadDir() {
		GeoLoc gl = getGeoLoc();
		return (gl != null) ? gl.getRoadDir() : 0;
	}

	/** Calculate item styles */
	@Override
	protected long calculateStyles() {
		long s = super.calculateStyles();
		if (isClosed())
			s |= ItemStyle.CLOSED.bit();
		if (isOpen())
			s |= ItemStyle.OPEN.bit();
		if (isChanging())
			s |= ItemStyle.CHANGE.bit();
		return s;
	}

	/** Choose the planned action */
	@Override
	public PlannedAction choosePlannedAction() {
		PlannedAction pa = super.choosePlannedAction();
		GateArmInterlock gai = interlock;
		if (pa != null && gai.isOpenAllowed())
			requestArmOpen();
		if (pa == null && gai.isCloseAllowed())
			requestArmClose();
		return pa;
	}

	/** Request to open the gate arm */
	private void requestArmOpen() {
		switch (arm_state) {
			case OPENING:
			case OPEN:
				break;
			default:
				requestArmState(GateArmState.OPENING);
		}
	}

	/** Request to close the gate arm */
	private void requestArmClose() {
		switch (arm_state) {
			case CLOSING:
			case CLOSED:
				break;
			default:
				requestArmState(GateArmState.CLOSING);
		}
	}

	/** Request a change to the gate arm state.
	 * @param gas Requested gate arm state. */
	private void requestArmState(GateArmState gas) {
		if (GateArmSystem.checkEnabled()) {
			GateArmPoller p = getGateArmPoller();
			if (p != null) {
				if (gas == GateArmState.OPENING)
					p.openGate(this);
				if (gas == GateArmState.CLOSING)
					p.closeGate(this);
			}
		}
	}

	/** Set the lock (JSON) */
	@Override
	public void setLock(String lk) {
		// NOTE: this attribute is only used for permission checks
		//       on action plans, which control gate arm states
	}
}
