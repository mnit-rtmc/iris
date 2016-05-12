/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2016  Minnesota Department of Transportation
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.LaneUseIndication;
import static us.mn.state.dot.tms.LaneUseIndication.DARK;
import us.mn.state.dot.tms.LCS;
import us.mn.state.dot.tms.LCSArray;
import us.mn.state.dot.tms.LCSArrayLock;
import us.mn.state.dot.tms.LCSIndication;
import static us.mn.state.dot.tms.R_Node.MAX_LANES;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.server.comm.DevicePoller;
import us.mn.state.dot.tms.server.comm.LCSPoller;
import us.mn.state.dot.tms.server.event.SignStatusEvent;

/**
 * A Lane-Use Control Signal Array is a series of LCS devices across all lanes
 * of a roadway corridor.
 *
 * @author Douglas Lau
 */
public class LCSArrayImpl extends DeviceImpl implements LCSArray {

	/** Ordinal value for lock "OFF" */
	static protected final Integer OFF_ORDINAL =
		new Integer(LCSArrayLock.OFF.ordinal());

	/** Load all the LCS arrays */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, LCSArrayImpl.class);
		store.query("SELECT name, controller, pin, notes, shift, " +
			"lcs_lock FROM iris." + SONAR_TYPE  + ";",
			new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new LCSArrayImpl(namespace,
					row.getString(1),	// name
					row.getString(2),	// controller
					row.getInt(3),		// pin
					row.getString(4),	// notes
					row.getInt(5),		// shift
					row.getInt(6)		// lcs_lock
				));
			}
		});
	}

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("controller", controller);
		map.put("pin", pin);
		map.put("notes", notes);
		map.put("shift", shift);
		LCSArrayLock lock = lcs_lock;
		if(lock != null)
			map.put("lcs_lock", lock.ordinal());
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

	/** Create a new LCS array */
	public LCSArrayImpl(String n) throws TMSException, SonarException {
		super(n);
	}

	/** Create an LCS array */
	public LCSArrayImpl(Namespace ns, String n, String c, int p, String nt,
		int s, Integer lk)
	{
		this(n, (ControllerImpl)ns.lookupObject(Controller.SONAR_TYPE,
		     c), p, nt, s, lk);
	}

	/** Create an LCS array */
	public LCSArrayImpl(String n, ControllerImpl c, int p, String nt, int s,
		Integer lk)
	{
		super(n, c, p, nt);
		shift = s;
		lcs_lock = LCSArrayLock.fromOrdinal(lk);
	}

	/** Set the controller of the device */
	public void doSetController(Controller c) throws TMSException {
		throw new ChangeVetoException("Cannot assign controller");
	}

	/** Get the controller for an LCS array */
	public synchronized Controller getController() {
		// Get the controller for the DMS in lane 1
		if(lanes.length > 0) {
			LCS lcs = lanes[0];
			if(lcs != null) {
				DMS dms = DMSHelper.lookup(lcs.getName());
				if(dms != null)
					return dms.getController();
			}
		}
		return null;
	}

	/** Set the controller I/O pin number */
	public void doSetPin(int p) throws TMSException {
		throw new ChangeVetoException("Cannot assign pin");
	}

	/** Lane shift of left lane */
	protected int shift;

	/** Set the lane shift of left lane */
	public void setShift(int s) {
		shift = s;
	}

	/** Set the lane shift of left lane */
	public void doSetShift(int s) throws TMSException {
		if(s == shift)
			return;
		if(s < 0)
			throw new ChangeVetoException("Negative shift");
		store.update(this, "shift", s);
		setShift(s);
	}

	/** Get the lane shift of left lane */
	public int getShift() {
		return shift;
	}

	/** Lock status */
	protected LCSArrayLock lcs_lock = null;

	/** Set the lock status */
	public void setLcsLock(Integer l) {
		lcs_lock = LCSArrayLock.fromOrdinal(l);
	}

	/** Set the lock (update) */
	protected void setLcsLock(LCSArrayLock l) throws TMSException {
		if(l == lcs_lock)
			return;
		if(l != null)
			store.update(this, "lcs_lock", l.ordinal());
		else
			store.update(this, "lcs_lock", null);
		lcs_lock = l;
		updateStyles();
	}

	/** Set the lock status */
	public void doSetLcsLock(Integer l) throws TMSException {
		if(OFF_ORDINAL.equals(l))
			throw new ChangeVetoException("Invalid lock value");
		setLcsLock(LCSArrayLock.fromOrdinal(l));
	}

	/** Get the lock status */
	public Integer getLcsLock() {
		LCSArrayLock lock = lcs_lock;
		return lock != null ? lock.ordinal() : null;
	}

	/** Next indications owner */
	protected transient User ownerNext;

	/** Set the next indications owner */
	public synchronized void setOwnerNext(User o) {
		if (ownerNext != null && o != null) {
			logError("OWNER CONFLICT: " + ownerNext.getName() +
			         " vs. " + o.getName());
			ownerNext = null;
		} else
			ownerNext = o;
	}

	/** Next indications to be displayed */
	protected transient Integer[] indicationsNext;

	/** Set the next indications */
	public void setIndicationsNext(Integer[] ind) {
		indicationsNext = ind;
	}

	/** Set the next indications */
	public void doSetIndicationsNext(Integer[] ind) throws TMSException {
		try {
			doSetIndicationsNext(ind, ownerNext);
		}
		finally {
			// Clear the owner even if there was an exception
			ownerNext = null;
		}
	}

	/** Set the next indications */
	protected synchronized void doSetIndicationsNext(Integer[] ind, User o)
		throws TMSException
	{
		if(ind.length != lanes.length)
			throw new ChangeVetoException("Wrong lane count");
		final LCSPoller p = getLCSPoller();
		if(p == null)
			throw new ChangeVetoException("No active poller");
		for(Integer i: ind) {
			if(LaneUseIndication.fromOrdinal(i) == null)
				throw new ChangeVetoException(
					"Invalid indication: " + i);
		}
		// FIXME: check that all indications are either dark or lit
		// FIXME: check the priority of each sign
		p.sendIndications(this, ind, o);
		// wait 15 seconds before allowing indications to be queried
		allow_query_time = TimeSteward.currentTimeMillis() + 15 * 1000;
		setIndicationsNext(ind);
	}

	/** Time after which indications are allowed to be queried */
	protected long allow_query_time = TimeSteward.currentTimeMillis();

	/** Is indication query allowed? */
	public boolean isQueryAllowed() {
		return TimeSteward.currentTimeMillis() >= allow_query_time;
	}

	/** Owner of current indications */
	protected transient User ownerCurrent;

	/** Get the owner of the current indications.
	 * @return User who deployed the current indications. */
	public User getOwnerCurrent() {
		return ownerCurrent;
	}

	/** Current indications (Shall not be null) */
	protected transient Integer[] indicationsCurrent =
		createDarkIndications(0);

	/** Create an array of DARK indications */
	protected Integer[] createDarkIndications(int n_lanes) {
		Integer[] ind = new Integer[n_lanes];
		for(int i = 0; i < n_lanes; i++)
			ind[i] = DARK.ordinal();
		return ind;
	}

	/** Set the current indications */
	public synchronized void setIndicationsCurrent(Integer[] ind, User o) {
		if(ind.length != lanes.length)
			return;
		if(!Arrays.equals(ind, indicationsCurrent)) {
			logIndications(ind, o);
			indicationsCurrent = ind;
			notifyAttribute("indicationsCurrent");
			ownerCurrent = o;
			notifyAttribute("ownerCurrent");
			setIndicationsNext(null);
		}
		// Update styles even if indications don't change.  This is
		// needed because DMS status might have changed.
		updateStyles();
	}

	/** Log indications in event db */
	protected void logIndications(Integer[] ind, User o) {
		EventType et = EventType.LCS_DEPLOYED;
		String text = createLogText(ind);
		if (areAllDark(ind)) {
			et = EventType.LCS_CLEARED;
			text = null;
		}
		String owner = (o != null) ? o.getName() : null;
		logEvent(new SignStatusEvent(et, name, text, owner));
	}

	/** Create a message to log LCS sign status event */
	static protected String createLogText(Integer[] ind) {
		StringBuilder sb = new StringBuilder();
		for(int i = ind.length - 1; i >= 0; i--) {
			Integer li = ind[i];
			if(li != null)
				sb.append(LaneUseIndication.fromOrdinal(li));
			else
				sb.append("UNKNOWN");
			sb.append(' ');
		}
		return sb.toString().trim();
	}

	/** Test if all indications are DARK */
	static protected boolean areAllDark(Integer[] ind) {
		for(Integer i: ind) {
			if(i == null || i != DARK.ordinal())
				return false;
		}
		return true;
	}

	/** Get the current lane-use indications.
	 * @return Array of currently active indications, one for each lane.
	 *         These are ordinal values of the LaneUseIndication enum.
	 *         A null indicates the indication for that lane is unknown. */
	public Integer[] getIndicationsCurrent() {
		return indicationsCurrent;
	}

	/** Array of all LCS lanes (right-to-left) */
	protected transient LCSImpl[] lanes = new LCSImpl[0];

	/** Get the LCS for all lanes (right-to-left) */
	private synchronized LCSImpl[] getLanes() {
		return Arrays.copyOf(lanes, lanes.length);
	}

	/** Get the count of lanes in the LCS array */
	public synchronized int getLaneCount() {
		return lanes.length;
	}

	/** Set the LCS for the given lane.
	 * @param lane Lane number (right-to-left, starting from 1)
	 * @param lcs Lane-Use Control Signal */
	public synchronized void setLane(int lane, LCS lcs)
		throws TMSException
	{
		lanes = createLanes(lane, lcs);
		Integer[] ind = Arrays.copyOf(indicationsCurrent, lanes.length);
		if(lane <= ind.length)
			ind[lane - 1] = null;	// Unknown indication
		indicationsCurrent = ind;
		notifyAttribute("indicationsCurrent");
		updateStyles();
	}

	/** Create an array of LCS for all lanes, by altering one lane.
	 * @param lane Lane number (right-to-left, starting from 1)
	 * @param lcs Lane-Use Control Signal */
	private LCSImpl[] createLanes(int lane, LCS lcs)
		throws ChangeVetoException
	{
		if(lane < 1 || lane > MAX_LANES)
			throw new ChangeVetoException("Invalid lane number");
		int n_lanes = Math.max(lanes.length, lane);
		LCSImpl[] lns = Arrays.copyOf(lanes, n_lanes);
		if(lcs != null && lns[lane - 1] != null)
			throw new ChangeVetoException("Lane already assigned");
		lns[lane - 1] = (LCSImpl)lcs;
		return Arrays.copyOf(lns, getMaxLane(lns));
	}

	/** Get the highest lane number */
	static private int getMaxLane(LCS[] lns) {
		int lane = 0;
		for(int i = 0; i < lns.length; i++) {
			if(lns[i] != null)
				lane = i + 1;
		}
		return lane;
	}

	/** Get an LCS poller for the array */
	public LCSPoller getLCSPoller() {
		DMSImpl[] signs = getDMSArray();
		// Make sure all signs have LCS pollers
		for(DMSImpl dms: signs) {
			if(dms == null)
				return null;
			if(!(dms.getPoller() instanceof LCSPoller))
				return null;
		}
		// Just grab the first poller we find ...
		for (DMSImpl dms: signs) {
			DevicePoller dp = dms.getPoller();
			if (dp instanceof LCSPoller)
				return (LCSPoller)dp;
		}
		return null;
	}

	/** Send a device request operation */
	@Override
	protected void sendDeviceRequest(DeviceRequest dr) {
		LCSPoller p = getLCSPoller();
		if (p != null)
			p.sendRequest(this, dr);
	}

	/** Get an array of the DMS */
	protected DMSImpl[] getDMSArray() {
		LCSImpl[] lns = getLanes();
		DMSImpl[] signs = new DMSImpl[lns.length];
		for(int i = 0; i < lns.length; i++) {
			LCS lcs = lns[i];
			if(lcs != null) {
				DMSImpl dms = (DMSImpl)namespace.lookupObject(
					DMS.SONAR_TYPE, lcs.getName());
				if(dms.isActive())
					signs[i] = dms;
			}
		}
		return signs;
	}

	/** Test if LCS array is locked */
	private boolean isLocked() {
		return lcs_lock != null;
	}

	/** Interface to check the DMS in an LCS array */
	private interface DMSChecker {
		boolean check(DMSImpl dms, LaneUseIndication u);
	}

	/** Check each DMS in an LCS array */
	private DMSImpl forEachDMS(DMSChecker chk) {
		LCSImpl[] lns;
		Integer[] ind;
		synchronized(this) {
			lns = getLanes();
			ind = getIndicationsCurrent();
		}
		int n_lns = Math.min(lns.length, ind.length);
		for(int i = 0; i < n_lns; i++) {
			LCSImpl lcs = lns[i];
			if(lcs != null) {
				DMS dms = DMSHelper.lookup(lcs.getName());
				if(dms instanceof DMSImpl) {
					DMSImpl d = (DMSImpl)dms;
					if(chk.check(d, LaneUseIndication.
					   fromOrdinal(ind[i])))
					{
						return d;
					}
				}
			}
		}
		return null;
	}

	/** Check if LCS array is active */
	@Override
	public boolean isActive() {
		return forEachDMS(new DMSChecker() {
			public boolean check(DMSImpl dms, LaneUseIndication u) {
				return dms.isActive();
			}
		}) != null;
	}

	/** Check if LCS array is failed */
	@Override
	public boolean isFailed() {
		return forEachDMS(new DMSChecker() {
			public boolean check(DMSImpl dms, LaneUseIndication u) {
				return dms.isFailed();
			}
		}) != null;
	}

	/** Test if LCS array is deployed */
	private boolean isDeployed() {
		return forEachDMS(new DMSChecker() {
			public boolean check(DMSImpl dms, LaneUseIndication u) {
				return (u != null && u != DARK) ||
				        dms.isMsgDeployed();
			}
		}) != null;
	}

	/** Check if LCS array is user deployed */
	private boolean isUserDeployed() {
		return forEachDMS(new DMSChecker() {
			public boolean check(DMSImpl dms, LaneUseIndication u) {
				return isUserDeployed(dms, u);
			}
		}) != null;
	}

	/** Check if one LCS is user deployed */
	private boolean isUserDeployed(DMSImpl dms, LaneUseIndication u) {
		return dms.isUserDeployed() ||
		     ((u != null && u != DARK) &&
		       dms.isOnline() &&
		      !dms.isScheduleDeployed());
	}

	/** Check if LCS array is schedule deployed */
	private boolean isScheduleDeployed() {
		return forEachDMS(new DMSChecker() {
			public boolean check(DMSImpl dms, LaneUseIndication u) {
				return dms.isScheduleDeployed();
			}
		}) != null;
	}

	/** Test if LCS array needs maintenance */
	private boolean needsMaintenance() {
		LCSArrayLock lock = lcs_lock;
		if(lock == LCSArrayLock.MAINTENANCE)
			return true;
		return forEachDMS(new DMSChecker() {
			public boolean check(DMSImpl dms, LaneUseIndication u) {
				return dms.needsMaintenance();
			}
		}) != null;
	}

	/** Test if LCS array is available */
	private boolean isAvailable() {
		return !isLocked() &&
		        isOnline() &&
		       !isDeployed() &&
		       !needsMaintenance();
	}

	/** Item style bits */
	private transient long styles = 0;

	/** Update the LCS array styles */
	@Override
	public void updateStyles() {
		long s = ItemStyle.ALL.bit();
		if(isAvailable())
			s |= ItemStyle.AVAILABLE.bit();
		if(isUserDeployed())
			s |= ItemStyle.DEPLOYED.bit();
		if(isScheduleDeployed())
			s |= ItemStyle.SCHEDULED.bit();
		if(needsMaintenance())
			s |= ItemStyle.MAINTENANCE.bit();
		if(isActive() && isFailed())
			s |= ItemStyle.FAILED.bit();
		setStyles(s);
	}

	/** Set the item style bits (and notify clients) */
	private void setStyles(long s) {
		if(s != styles) {
			styles = s;
			notifyAttribute("styles");
		}
	}

	/** Get item style bits */
	public long getStyles() {
		return styles;
	}
}
