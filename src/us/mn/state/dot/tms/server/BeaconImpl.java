/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2025  Minnesota Department of Transportation
 * Copyright (C) 2022       SRF Consulting Group
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

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.sql.ResultSet;
import java.sql.SQLException;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.tms.Beacon;
import us.mn.state.dot.tms.BeaconState;
import us.mn.state.dot.tms.CameraPreset;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.server.comm.BeaconPoller;
import us.mn.state.dot.tms.server.comm.DevicePoller;
import us.mn.state.dot.tms.server.event.BeaconEvent;

/**
 * A Beacon is a light which flashes toward oncoming traffic.
 *
 * @author Douglas Lau
 * @author John L. Stanley - SRF Consulting
 */
public class BeaconImpl extends DeviceImpl implements Beacon {

	/** Load all the beacons */
	static protected void loadAll() throws TMSException {
		store.query("SELECT name, geo_loc, controller, pin, notes, " +
			"preset, message, verify_pin, ext_mode, state " +
			"FROM iris." + SONAR_TYPE + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new BeaconImpl(row));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("geo_loc", geo_loc);
		map.put("controller", controller);
		map.put("pin", pin);
		map.put("notes", notes);
		map.put("preset", preset);
		map.put("message", message);
		map.put("verify_pin", verify_pin);
		map.put("ext_mode", ext_mode);
		map.put("state", state);
		return map;
	}

	/** Create a new beacon with a string name */
	public BeaconImpl(String n) throws TMSException, SonarException {
		super(n);
		GeoLocImpl g = new GeoLocImpl(name, SONAR_TYPE);
		g.notifyCreate();
		geo_loc = g;
	}

	/** Create a beacon */
	private BeaconImpl(ResultSet row) throws SQLException {
		this(row.getString(1),           // name
		     row.getString(2),           // geo_loc
		     row.getString(3),           // controller
		     row.getInt(4),              // pin
		     row.getString(5),           // notes
		     row.getString(6),           // preset
		     row.getString(7),           // message
		     (Integer) row.getObject(8), // verify_pin
		     row.getBoolean(9),          // ext_mode
		     row.getInt(10)              // state
		);
	}

	/** Create a beacon */
	private BeaconImpl(String n, String l, String c, int p, String nt,
		String cp, String m, Integer vp, boolean em, int bs)
	{
		super(n, lookupController(c), p, nt);
		geo_loc = lookupGeoLoc(l);
		setPreset(lookupPreset(cp));
		message = m;
		verify_pin = vp;
		ext_mode = em;
		state = bs;
		initTransients();
	}

	/** Destroy an object */
	@Override
	public void doDestroy() throws TMSException {
		super.doDestroy();
		setPreset(null);
		geo_loc.notifyRemove();
	}

	/** Update the item styles */
	@Override
	public void updateStyles() {
		// NOTE: called by ControllerImpl.setOffline
		if (isOffline())
			setStateNotify(BeaconState.UNKNOWN);
		super.updateStyles();
	}

	/** Calculate the item styles */
	@Override
	protected long calculateStyles() {
		long s = super.calculateStyles();
		if (isStyleDeployed())
			s |= ItemStyle.DEPLOYED.bit();
		if (isStyleExternal())
			s |= ItemStyle.EXTERNAL.bit();
		return s;
	}

	/** Test if beacon is available */
	@Override
	protected boolean isAvailable() {
		return super.isAvailable() &&
			state == BeaconState.DARK.ordinal();
	}

	/** Test if style is deployed */
	private boolean isStyleDeployed() {
		if (!isOnline())
			return false;
		switch (BeaconState.fromOrdinal(state)) {
		case FLASHING:
		case FLASHING_EXTERNAL:
		case FAULT_STUCK_ON:
			return true;
		default:
			return false;
		}
	}

	/** Test if style is external */
	private boolean isStyleExternal() {
		return isOnline()
		    && state == BeaconState.FLASHING_EXTERNAL.ordinal();
	}

	/** Test if beacon has faults */
	@Override
	protected boolean hasFaults() {
		int bs = state;
		return bs == BeaconState.FAULT_NO_VERIFY.ordinal()
		    || bs == BeaconState.FAULT_STUCK_ON.ordinal();
	}

	/** Device location */
	private final GeoLocImpl geo_loc;

	/** Get the device location */
	@Override
	public GeoLoc getGeoLoc() {
		return geo_loc;
	}

	/** Camera preset from which the beacon can be seen */
	private CameraPreset preset;

	/** Set the verification camera preset */
	@Override
	public void setPreset(CameraPreset cp) {
		final CameraPreset ocp = preset;
		if (cp instanceof CameraPresetImpl) {
			CameraPresetImpl cpi = (CameraPresetImpl)cp;
			cpi.setAssignedNotify(true);
		}
		preset = cp;
		if (ocp instanceof CameraPresetImpl) {
			CameraPresetImpl ocpi = (CameraPresetImpl)ocp;
			ocpi.setAssignedNotify(false);
		}
	}

	/** Set the verification camera preset */
	public void doSetPreset(CameraPreset cp) throws TMSException {
		if (cp == preset)
			return;
		store.update(this, "preset", cp);
		setPreset(cp);
	}

	/** Get verification camera preset */
	@Override
	public CameraPreset getPreset() {
		return preset;
	}

	/** Message text */
	private String message = "";

	/** Set the message text */
	@Override
	public void setMessage(String m) {
		message = m;
	}

	/** Set the message text */
	public void doSetMessage(String m) throws TMSException {
		if (m.equals(message))
			return;
		store.update(this, "message", m);
		setMessage(m);
	}

	/** Get the message text */
	@Override
	public String getMessage() {
		return message;
	}

	/** Controller I/O verify pin number */
	private Integer verify_pin;

	/** Set the controller I/O verify pin number */
	@Override
	public void setVerifyPin(Integer p) {
		verify_pin = p;
	}

	/** Set the controller I/O verify pin number */
	public void doSetVerifyPin(Integer p) throws TMSException {
		if (!objectEquals(p, verify_pin)) {
			store.update(this, "verify_pin", p);
			setVerifyPin(p);
		}
	}

	/** Get the controller I/O verify pin number */
	@Override
	public Integer getVerifyPin() {
		return verify_pin;
	}

	/** External detect mode */
	private boolean ext_mode;

	/** Set the external detect mode */
	@Override
	public void setExtMode(boolean em) {
		ext_mode = em;
	}

	/** Set the external detect mode */
	public void doSetExtMode(boolean em) throws TMSException {
		if (em != ext_mode) {
			store.update(this, "ext_mode", em);
			setExtMode(em);
		}
	}

	/** Get the external detect mode */
	@Override
	public boolean getExtMode() {
		return ext_mode;
	}

	/** Beacon state */
	private int state;

	/** Set beacon state request (ordinal of BeaconState) */
	@Override
	public void setState(int bs) {
		if (bs != BeaconState.DARK_REQ.ordinal() &&
		    bs != BeaconState.FLASHING_REQ.ordinal())
		{
			logError("INVALID beacon req state: " + bs);
		}
		BeaconPoller p = getBeaconPoller();
		if (p != null) {
			BeaconState s = BeaconState.fromOrdinal(bs);
			p.setFlashing(this, BeaconState.FLASHING_REQ == s);
		} else
			setStateNotify(BeaconState.UNKNOWN);
	}

	/** Set beacon state request (ordinal of BeaconState) */
	public void doSetState(int bs) throws TMSException {
		BeaconState s = BeaconState.fromOrdinal(bs);
		switch (s) {
			case DARK_REQ:
			case FLASHING_REQ:
				logBeaconEvent(s, getProcUser());
				setState(bs);
				break;
			default:
				throw new ChangeVetoException(
					"Invalid state request");
		}
	}

	/** Set the beacon state and notify clients */
	public void setStateNotify(BeaconState bs) {
		if (bs.ordinal() != state) {
			try {
				store.update(this, "state", bs.ordinal());
				state = bs.ordinal();
				notifyAttribute("state");
			}
			catch (TMSException e) {
				e.printStackTrace();
			}
			logBeaconEvent(bs, null);
			updateStyles();
		}
	}

	/** Get the beacon state */
	public BeaconState getBeaconState(boolean relay, boolean verify) {
		Integer vp = getVerifyPin();
		if (vp != null) {
			if (relay && !verify)
				return BeaconState.FAULT_NO_VERIFY;
			if (verify && !relay) {
				return getExtMode()
				      ? BeaconState.FLASHING_EXTERNAL
				      : BeaconState.FAULT_STUCK_ON;
			}
		}
		return (relay) ? BeaconState.FLASHING : BeaconState.DARK;
	}

	/** Set the beacon flashing state and notify clients */
	public void setFlashingNotify(boolean flashing) {
		BeaconState bs = (flashing)
			? BeaconState.FLASHING
			: BeaconState.DARK;
		setStateNotify(bs);
	}

	/** Get beacon state (ordinal of BeaconState) */
	@Override
	public int getState() {
		return state;
	}

	/** Choose the planned action */
	@Override
	public PlannedAction choosePlannedAction() {
		// Same as in DeviceImpl, but doesn't remove actions if condition is false
		// Specific handling necessary to allow plan to undeploy
		PlannedAction pa = null;
		Iterator<PlannedAction> it =
			planned_actions.descendingIterator();
		while (it.hasNext()) {
			pa = it.next();
			if (checkPlannedAction(pa))
				break;
			else
				pa = null;
		}

		if (!planned_actions.isEmpty()) {
			BeaconState bs = (pa != null)
				? BeaconState.FLASHING_REQ
				: BeaconState.DARK_REQ;
			setState(bs.ordinal());
		}
		return pa;
	}

	/** Log a beacon event */
	private void logBeaconEvent(BeaconState bs, String uid) {
		logEvent(new BeaconEvent(name, bs, uid));
	}

	/** Get a beacon poller */
	private BeaconPoller getBeaconPoller() {
		DevicePoller dp = getPoller();
		return (dp instanceof BeaconPoller) ? (BeaconPoller) dp : null;
	}

	/** Send a device request operation */
	@Override
	protected void sendDeviceRequest(DeviceRequest dr) {
		BeaconPoller p = getBeaconPoller();
		if (p != null)
			p.sendRequest(this, dr);
	}
}
