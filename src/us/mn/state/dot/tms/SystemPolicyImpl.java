/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2008  Minnesota Department of Transportation
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
import java.util.HashMap;
import java.util.Map;

/**
 * A system policy is a parameter name mapped to an integer value.
 *
 * @author Douglas Lau
 */
public class SystemPolicyImpl extends BaseObjectImpl implements SystemPolicy {

	/** Load all the system policies */
	static protected void loadAll() throws TMSException {
		System.err.println("Loading system policy...");
		namespace.registerType(SONAR_TYPE, SystemPolicyImpl.class);
		store.query("SELECT name, value FROM system_policy;",
			new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.add(new SystemPolicyImpl(
					row.getString(1),	// name
					row.getInt(2)		// value
				));
			}
		});
	}

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("value", value);
		return map;
	}

	/** Get the database table name */
	public String getTable() {
		return SONAR_TYPE;
	}

	/** Get the SONAR type name */
	public String getTypeName() {
		return SONAR_TYPE;
	}

	/** Create a new system policy */
	public SystemPolicyImpl(String n) {
		super(n);
	}

	/** Create a new system policy object */
	protected SystemPolicyImpl(String n, int v) {
		super(n);
		value = v;
	}

	/** Policy value */
	protected int value;

	/** Set the system policy value */
	public void setValue(int v) {
		value = v;
	}

	/** Set the system policy value */
	public void doSetValue(int v) throws TMSException {
		if(v == value)
			return;
		store.update(this, "value", v);
		setValue(v);
	}

	/** Get the system policy value */
	public int getValue() {
		return value;
	}
}
