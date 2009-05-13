/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.comm.LCSPoller;
import us.mn.state.dot.tms.comm.MessagePoller;
import us.mn.state.dot.tms.comm.Operation;

/**
 * A Lane-Use Control Signal Array is a series of LCS devices across all lanes
 * of a freeway corridor.
 *
 * @author Douglas Lau
 */
public class LCSArrayImpl extends BaseObjectImpl implements LCSArray {

	/** Ordinal value for lock "OFF" */
	static protected final Integer OFF_ORDINAL =
		new Integer(LCSArrayLock.OFF.ordinal());

	/** Load all the LCS arrays */
	static protected void loadAll() throws TMSException {
		System.err.println("Loading LCS arrays...");
		namespace.registerType(SONAR_TYPE, LCSArrayImpl.class);
		store.query("SELECT name, lcs_lock FROM iris." + SONAR_TYPE  +
			";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new LCSArrayImpl(
					row.getString(1),	// name
					row.getInt(2)		// lcs_lock
				));
			}
		});
	}

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		if(lcs_lock != null)
			map.put("lcs_lock", lcs_lock.ordinal());
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
	public LCSArrayImpl(String n) {
		super(n);
	}

	/** Create an LCS array */
	public LCSArrayImpl(String n, Integer lk) {
		super(n);
		lcs_lock = LCSArrayLock.fromOrdinal(lk);
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
	}

	/** Set the lock status */
	public void doSetLcsLock(Integer l) throws TMSException {
		if(OFF_ORDINAL.equals(l))
			throw new ChangeVetoException("Invalid lock value");
		setLcsLock(LCSArrayLock.fromOrdinal(l));
	}

	/** Get the lock status */
	public Integer getLcsLock() {
		if(lcs_lock != null)
			return lcs_lock.ordinal();
		else
			return null;
	}

	/** Next indications owner */
	protected transient User ownerNext;

	/** Set the next indications owner */
	public synchronized void setOwnerNext(User o) {
		if(ownerNext != null && o != null) {
			System.err.println("LCSArrayImpl.setOwnerNext: " +
				getName() + ", " + ownerNext.getName() +
				" vs. " + o.getName());
			ownerNext = null;
		} else
			ownerNext = o;
	}

	/** Next indications to be displayed */
	protected transient int[] indicationsNext;

	/** Set the next indications */
	public void setIndicationsNext(int[] ind) {
		indicationsNext = ind;
	}

	/** Set the next indications */
	public void doSetIndicationsNext(int[] ind) throws TMSException {
		try {
			doSetIndicationsNext(ind, ownerNext);
		}
		finally {
			// Clear the owner even if there was an exception
			ownerNext = null;
		}
	}

	/** Set the next indications */
	protected synchronized void doSetIndicationsNext(int[] ind, User o)
		throws TMSException
	{
		if(ind.length != lanes.length)
			throw new ChangeVetoException("Wrong lane count");
		final LCSPoller p = getLCSPoller();
		if(p == null)
			throw new ChangeVetoException("No active poller");
		for(int i: ind) {
			if(LaneUseIndication.fromOrdinal(i) == null)
				throw new ChangeVetoException(
					"Invalid indication: " + i);
		}
		// FIXME: check the priority of each sign
		p.sendIndications(this, ind, o);
		setIndicationsNext(ind);
	}

	/** Owner of current indications */
	protected transient User ownerCurrent;

	/** Get the owner of the current indications.
	 * @return User who deployed the current indications. */
	public User getOwnerCurrent() {
		return ownerCurrent;
	}

	/** Current indications (Shall not be null) */
	protected transient int[] indicationsCurrent = createDarkIndications();

	/** Create an array of DARK indications */
	protected int[] createDarkIndications() {
		int[] ind = new int[lanes.length];
		for(int i = 0; i < ind.length; i++)
			ind[i] = LaneUseIndication.DARK.ordinal();
		return ind;
	}

	/** Set the current indications */
	public synchronized void setIndicationsCurrent(int[] ind, User o) {
		if(ind.length != lanes.length)
			return;
		if(Arrays.equals(ind, indicationsCurrent))
			return;
		indicationsCurrent = ind;
		notifyAttribute("indicationsCurrent");
		ownerCurrent = o;
		notifyAttribute("ownerCurrent");
		setIndicationsNext(null);
	}

	/** Get the current lane-use indications.
	 * @return Currently active indications (cannot be null) */
	public int[] getIndicationsCurrent() {
		return indicationsCurrent;
	}

	/** Array of all LCS lanes (right-to-left) */
	protected transient LCSImpl[] lanes = new LCSImpl[0];

	/** Get the LCS for all lanes (right-to-left) */
	public synchronized LCSImpl[] getLanes() {
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
		for(int i: indicationsCurrent) {
			if(i != LaneUseIndication.DARK.ordinal())
				throw new ChangeVetoException("LCS in use");
		}
		if(lane < 1 || lane > 16)
			throw new ChangeVetoException("Invalid lane number");
		int n_lanes = Math.max(lanes.length, lane);
		LCSImpl[] lns = Arrays.copyOf(lanes, n_lanes);
		if(lcs != null && lns[lane - 1] != null)
			throw new ChangeVetoException("Lane already assigned");
		lns[lane - 1] = (LCSImpl)lcs;
		lanes = Arrays.copyOf(lns, getMaxLane(lns));
		indicationsCurrent = createDarkIndications();
		notifyAttribute("indicationsCurrent");
	}

	/** Get the highest lane number */
	static protected int getMaxLane(LCS[] lns) {
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
		for(DMSImpl dms: signs) {
			MessagePoller mp = dms.getPoller();
			if(mp instanceof LCSPoller)
				return (LCSPoller)mp;
		}
		return null;
	}

	/** Get an array of the DMS */
	protected DMSImpl[] getDMSArray() {
		LCS[] lcss = getLanes();
		DMSImpl[] signs = new DMSImpl[lcss.length];
		for(int i = 0; i < lcss.length; i++) {
			LCS lcs = lcss[i];
			if(lcs != null) {
				DMSImpl dms = (DMSImpl)namespace.lookupObject(
					DMS.SONAR_TYPE, lcs.getName());
				if(dms.isActive())
					signs[i] = dms;
			}
		}
		return signs;
	}

	/** Find the indications for this LCS array */
	public void findIndications(final Checker<LCSIndication> checker) {
		final LCSArrayImpl lcs_array = this;
		namespace.findObject(LCSIndication.SONAR_TYPE,
			new Checker<LCSIndication>()
		{
			public boolean check(LCSIndication li) {
				LCS lcs = li.getLcs();
				if(lcs.getArray() == lcs_array)
					return checker.check(li);
				else
					return false;
			}
		});
	}

	/** Operation which owns the LCS array */
	protected transient Operation owner;

	/** Acquire ownership of the LCS array */
	public Operation acquire(Operation o) {
		try {
			// Name used for unique device acquire/release lock
			synchronized(name) {
				if(owner == null)
					owner = o;
				return owner;
			}
		}
		finally {
			notifyAttribute("operation");
		}
	}

	/** Release ownership of the LCS array */
	public Operation release(Operation o) {
		try {
			// Name used for unique device acquire/release lock
			synchronized(name) {
				Operation _owner = owner;
				if(owner == o)
					owner = null;
				return _owner;
			}
		}
		finally {
			notifyAttribute("operation");
		}
	}

	/** Get a description of the current operation */
	public String getOperation() {
		Operation o = owner;
		if(o == null)
			return "None";
		else
			return o.getOperationDescription();
	}
}
