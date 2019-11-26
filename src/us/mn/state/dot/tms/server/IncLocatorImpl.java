/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016-2019  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.IncLocator;
import us.mn.state.dot.tms.IncLocatorHelper;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.TMSException;

/**
 * An incident locator is part of a message to deploy on a DMS, matching
 * incident attributes.
 *
 * @author Douglas Lau
 */
public class IncLocatorImpl extends BaseObjectImpl implements IncLocator {

	/** Load all the incident locators */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, IncLocatorImpl.class);
		store.query("SELECT name, range, branched, picked, multi " +
			"FROM iris." + SONAR_TYPE + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new IncLocatorImpl(row));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("range", range);
		map.put("branched", branched);
		map.put("picked", picked);
		map.put("multi", multi);
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

	/** Create an incident locator */
	private IncLocatorImpl(ResultSet row) throws SQLException {
		this(row.getString(1),          // name
		     row.getInt(2),             // range
		     row.getBoolean(3),         // branched
		     row.getBoolean(4),         // picked
		     row.getString(5)           // multi
		);
	}

	/** Create an incident locator */
	private IncLocatorImpl(String n, int r, boolean b, boolean p, String m){
		super(n);
		range = r;
		branched = b;
		picked = p;
		multi = m;
	}

	/** Create a new incident locator */
	public IncLocatorImpl(String n) {
		super(n);
	}

	/** Range ordinal */
	private int range;

	/** Set the range */
	@Override
	public void setRange(int r) {
		range = r;
	}

	/** Set the range */
	public void doSetRange(int r) throws TMSException {
		if (r != range) {
			store.update(this, "range", r);
			setRange(r);
		}
	}

	/** Get the range */
	@Override
	public int getRange() {
		return range;
	}

	/** Branched flag */
	private boolean branched;

	/** Set the branched flag */
	@Override
	public void setBranched(boolean b) {
		branched = b;
	}

	/** Set the branched flag */
	public void doSetBranched(boolean b) throws TMSException {
		if (b != branched) {
			store.update(this, "branched", b);
			setBranched(b);
		}
	}

	/** Get the branched flag */
	@Override
	public boolean getBranched() {
		return branched;
	}

	/** Picked flag */
	private boolean picked;

	/** Set the picked flag */
	@Override
	public void setPicked(boolean p) {
		picked = p;
	}

	/** Set the picked flag */
	public void doSetPicked(boolean p) throws TMSException {
		if (p != picked) {
			store.update(this, "picked", p);
			setPicked(p);
		}
	}

	/** Get the picked flag */
	@Override
	public boolean getPicked() {
		return picked;
	}

	/** MULTI string */
	private String multi = "";

	/** Set the MULTI string */
	@Override
	public void setMulti(String m) {
		multi = m;
	}

	/** Set the MULTI string */
	public void doSetMulti(String m) throws TMSException {
		if (!IncLocatorHelper.isMultiValid(m))
			throw new ChangeVetoException("Invalid MULTI: " + m);
		if (!m.equals(multi)) {
			store.update(this, "multi", m);
			setMulti(m);
		}
	}

	/** Get the MULTI string */
	@Override
	public String getMulti() {
		return multi;
	}
}
