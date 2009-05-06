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

/**
 * A Lane-Use Control Signal Array is a series of LCS devices across all lanes
 * of a freeway corridor.
 *
 * @author Douglas Lau
 */
public class LCSArrayImpl extends BaseObjectImpl implements LCSArray {

	/** Load all the LCS arrays */
	static protected void loadAll() throws TMSException {
		System.err.println("Loading LCS arrays...");
		namespace.registerType(SONAR_TYPE, LCSArrayImpl.class);
		store.query("SELECT name FROM iris." + SONAR_TYPE  + ";",
			new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new LCSArrayImpl(
					row.getString(1)	// name
				));
			}
		});
	}

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
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
		final LCSPoller p = getLCSPoller();
		if(p == null)
			throw new ChangeVetoException("No active poller");
		// FIXME: check for the appropriate number of lanes
		// FIXME: check that the indications are valid
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

	/** Set the current indications */
	public void setIndicationsCurrent(int[] ind, User o) {
		if(Arrays.equals(ind, indicationsCurrent))
			return;
		setDeployTime();
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
}
