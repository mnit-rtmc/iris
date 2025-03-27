/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2025  Minnesota Department of Transportation
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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.tms.CameraPreset;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.CtrlCondition;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.Lcs;
import us.mn.state.dot.tms.LcsHelper;
import us.mn.state.dot.tms.LcsIndication;
import us.mn.state.dot.tms.LcsLock;
import us.mn.state.dot.tms.LcsState;
import us.mn.state.dot.tms.LcsStateHelper;
import us.mn.state.dot.tms.LcsType;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.server.comm.DevicePoller;
import us.mn.state.dot.tms.server.comm.LCSPoller;
import us.mn.state.dot.tms.server.event.LcsEvent;

/**
 * A Lane-Use Control Signal Array is a series of LCS devices across all lanes
 * of a roadway corridor.
 *
 * @author Douglas Lau
 */
public class LcsImpl extends DeviceImpl implements Lcs {

	/** Test if all indications are DARK */
	static private boolean areAllDark(int[] ind) {
		for (int i = 0; i < ind.length; i++) {
			if (ind[i] != LcsIndication.DARK.ordinal())
				return false;
		}
		return true;
	}

	/** Load all the LCS arrays */
	static protected void loadAll() throws TMSException {
		store.query("SELECT name, geo_loc, controller, pin, notes, " +
			"preset, lcs_type, shift, lock, status FROM iris." +
			SONAR_TYPE  + ";",
			new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new LcsImpl(
					row.getString(1), // name
					row.getString(2), // geo_loc
					row.getString(3), // controller
					row.getInt(4),    // pin
					row.getString(5), // notes
					row.getString(6), // preset
					row.getInt(7),    // lcs_type
					row.getInt(8),    // shift
					row.getString(9), // lock
					row.getString(10) // status
				));
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
		map.put("lcs_type", lcs_type.ordinal());
		map.put("shift", shift);
		map.put("lock", lock);
		map.put("status", status);
		return map;
	}

	/** Create a new LCS array */
	public LcsImpl(String n) throws TMSException, SonarException {
		super(n);
		GeoLocImpl g = new GeoLocImpl(name, SONAR_TYPE);
		g.notifyCreate();
		geo_loc = g;
		shift = 1;
		lock = null;
		status = null;
	}

	/** Create an LCS array */
	private LcsImpl(String n, String loc, String c, int p, String nt,
		String cp, int lt, int sh, String lk, String st)
	{
		super(n, lookupController(c), p, nt);
		geo_loc = lookupGeoLoc(loc);
		setPreset(lookupPreset(cp));
		lcs_type = LcsType.fromOrdinal(lt);
		shift = sh;
		lock = lk;
		status = st;
		initTransients();
	}

	/** Destroy an object */
	@Override
	public void doDestroy() throws TMSException {
		super.doDestroy();
		setPreset(null);
		geo_loc.notifyRemove();
	}

	/** Device location */
	private final GeoLocImpl geo_loc;

	/** Get the device location */
	@Override
	public GeoLoc getGeoLoc() {
		return geo_loc;
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
			store.update(this, "preset", cp);
			setPreset(cp);
		}
	}

	/** Get verification camera preset */
	@Override
	public CameraPreset getPreset() {
		return preset;
	}

	/** LCS type */
	private LcsType lcs_type = LcsType.OVER_LANE_DEDICATED;

	/** Set LCS type */
	@Override
	public void setLcsType(int t) {
		lcs_type = LcsType.fromOrdinal(t);
	}

	/** Set the LCS type */
	public void doSetLcsType(int t) throws TMSException {
		if (t != lcs_type.ordinal()) {
			store.update(this, "lcs_type", t);
			setLcsType(t);
		}
	}

	/** Get the LCS type */
	@Override
	public int getLcsType() {
		return lcs_type.ordinal();
	}

	/** Lane shift */
	private int shift;

	/** Set the lane shift */
	@Override
	public void setShift(int sh) {
		shift = sh;
	}

	/** Set the lane shift */
	public void doSetShift(int sh) throws TMSException {
		if (sh != shift) {
			store.update(this, "shift", sh);
			setShift(sh);
		}
	}

	/** Get the lane shift */
	@Override
	public int getShift() {
		return shift;
	}

	/** LCS lock (JSON) */
	private String lock;

	/** Set the lock as JSON */
	@Override
	public void setLock(String lk) {
		lock = lk;
	}

	/** Set the lock as JSON */
	public void doSetLock(String lk) throws TMSException {
		if (!objectEquals(lk, lock)) {
			if (lk != null)
				checkLock(new LcsLock(lk));
			setLockChecked(lk);
		}
	}

	/** Check a lock */
	private void checkLock(LcsLock lk) throws TMSException {
		if (lk.optReason() == null)
			throw new ChangeVetoException("No reason!");
		if (!getProcUser().equals(lk.optUser()))
			throw new ChangeVetoException("Bad user!");
		String exp = lk.optExpires();
		if (exp != null && TimeSteward.parse8601(exp) == null)
			throw new ChangeVetoException("Bad expiration!");
		int[] ind = lk.optIndications();
		if (ind != null) {
			int n_lanes = LcsHelper.countLanes(this);
			if (ind.length != n_lanes)
				throw new ChangeVetoException("Wrong lanes!");
			for (int ln = 0; ln < ind.length; ln++) {
				LcsIndication li =
					LcsIndication.fromOrdinal(ind[ln]);
				if (li == LcsIndication.UNKNOWN ||
				    li == LcsIndication.DARK)
					throw new ChangeVetoException(
						"Bad indication: " + li);
			}
		}
	}

	/** Set the lock as JSON */
	private void setLockChecked(String lk) throws TMSException {
		store.update(this, "lock", lk);
		lock = lk;
		EventType et = (lk != null)
			? EventType.LCS_LOCKED
			: EventType.LCS_UNLOCKED;
		logEvent(new LcsEvent(et, name, lk, status));
		updateStyles();
		sendIndications(lk);
	}

	/** Check if lock has expired */
	public void checkLockExpired() {
		LcsLock lk = new LcsLock(lock);
		String exp = lk.optExpires();
		if (exp != null) {
			Long e = TimeSteward.parse8601(exp);
			if (e != null && e < TimeSteward.currentTimeMillis()) {
				try {
					setLockChecked(null);
					notifyAttribute("lock");
				}
				catch (TMSException ex) {
					logError("checkLockExpired: " +
						ex.getMessage());
				}
			}
		}
	}

	/** Get the lock as JSON */
	@Override
	public String getLock() {
		return lock;
	}

	/** Current status (JSON) */
	private String status;

	/** Set the status (JSON) */
	private String setStatusNotify(String st) {
		if (!objectEquals(st, status)) {
			try {
				store.update(this, "status", st);
				status = st;
				notifyAttribute("status");
				updateStyles();
				return st;
			}
			catch (TMSException e) {
				logError("status: " + e.getMessage());
			}
		}
		return null;
	}

	/** Set a status value and notify clients of the change */
	private String setStatusNotify(String key, Object value) {
		String st = LcsHelper.putJson(status, key, value);
		return setStatusNotify(st);
	}

	/** Set the indications and notify clients */
	public void setIndicationsNotify(int[] ind) {
		String st = setStatusNotify(
			Lcs.INDICATIONS,
			LcsHelper.makeIndications(ind)
		);
		if (st != null) {
			EventType et = areAllDark(ind)
				? EventType.LCS_CLEARED
				: EventType.LCS_DEPLOYED;
			logEvent(new LcsEvent(et, name, lock, st));
		}
	}

	/** Set a fault value and notify clients of the change */
	public void setFaultsNotify(String value) {
		setStatusNotify(Lcs.FAULTS, value);
	}

	/** Get the current status (JSON) */
	@Override
	public String getStatus() {
		return status;
	}

	/** Send LCS indications */
	private void sendIndications(String lk) throws TMSException {
		final LCSPoller p = getLCSPoller();
		if (p == null)
			throw new ChangeVetoException("No active poller");
		p.sendIndications(this, lk);
		// wait 15 seconds before allowing indications to be queried
		allow_query_time = TimeSteward.currentTimeMillis() + 15 * 1000;
	}

	/** Time after which indications are allowed to be queried */
	private long allow_query_time = TimeSteward.currentTimeMillis();

	/** Is indication query allowed? */
	public boolean isQueryAllowed() {
		return TimeSteward.currentTimeMillis() >= allow_query_time;
	}

	/** Get an LCS poller for the array */
	public LCSPoller getLCSPoller() {
		DevicePoller dp = getPoller();
		return (dp instanceof LCSPoller) ? (LCSPoller) dp : null;
	}

	/** Send a device request operation */
	@Override
	protected void sendDeviceRequest(DeviceRequest dr) {
		LCSPoller p = getLCSPoller();
		if (p != null)
			p.sendRequest(this, dr);
	}

	/** Perform a periodic poll */
	@Override
	public void periodicPoll(boolean is_long) {
		if (!is_long)
			sendDeviceRequest(DeviceRequest.QUERY_MESSAGE);
	}

	/** Check if LCS array is active */
	@Override
	public boolean isActive() {
		int ACTIVE = CtrlCondition.ACTIVE.ordinal();
		boolean active = false;
		for (Controller c: lookupControllers()) {
			active = true;
			if (c.getCondition() != ACTIVE)
				return false;
		}
		return active;
	}

	/** Check if LCS array is offline */
	@Override
	public boolean isOffline() {
		boolean offline = true;
		for (Controller c: lookupControllers()) {
			offline = false;
			if (c.getFailTime() != null)
				return true;
		}
		return offline;
	}

	/** Test if LCS array is deployed */
	private boolean isDeployed() {
		int[] ind = LcsHelper.getIndications(this);
		for (int ln = 0; ln < ind.length; ln++) {
			if (ind[ln] > LcsIndication.DARK.ordinal())
				return true;
		}
		return false;
	}

	/** Test if LCS array has faults */
	@Override
	protected boolean hasFaults() {
		return LcsHelper.hasFaults(this);
	}

	/** Test if LCS array is available */
	@Override
	protected boolean isAvailable() {
		return (lock == null) &&
		        isOnline() &&
		       !isDeployed() &&
		       !hasFaults();
	}

	/** Calculate the item styles */
	@Override
	protected long calculateStyles() {
		long s = super.calculateStyles();
		if (isDeployed())
			s |= ItemStyle.DEPLOYED.bit();
		return s;
	}

	/** Lookup the set of controllers for an LCS array */
	public Set<ControllerImpl> lookupControllers() {
		TreeSet<ControllerImpl> set = new TreeSet<ControllerImpl>();
		if (controller != null)
			set.add(controller);
		Iterator<LcsState> it = LcsStateHelper.iterator();
		while (it.hasNext()) {
			LcsState ls = it.next();
			if (ls.getLcs() == this) {
				Controller c = ls.getController();
				if (c instanceof ControllerImpl)
					set.add((ControllerImpl) c);
			}
		}
		return set;
	}
}
