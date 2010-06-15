/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2010  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.sonar.server.ServerNamespace;
import us.mn.state.dot.sonar.server.CapabilityImpl;

/**
 * IRIS capability
 *
 * @author Douglas lau
 */
public class IrisCapabilityImpl extends CapabilityImpl
	implements Comparable<IrisCapabilityImpl>, Storable
{
	/** SQL connection to database */
	static protected SQLConnection store;

	/** Lookup all the capabilities */
	static public void lookup(SQLConnection c, final ServerNamespace ns)
		throws TMSException
	{
		store = c;
		store.query("SELECT name, enabled FROM iris.capability;",
			new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				ns.addObject(new IrisCapabilityImpl(
					row.getString(1),	// name
					row.getBoolean(2)	// enabled
				));
			}
		});
	}

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("enabled", enabled);
		return map;
	}

	/** Store an object */
	public void doStore() throws TMSException {
		store.create(this);
	}

	/** Get the database table name */
	public String getTable() {
		return "iris.capability";
	}

	/** Create a new IRIS capability */
	public IrisCapabilityImpl(String n) {
		super(n);
	}

	/** Create an IRIS capability from database lookup */
	protected IrisCapabilityImpl(String n, boolean e) {
		this(n);
		enabled = e;
	}

	/** Compare to another capability */
	public int compareTo(IrisCapabilityImpl o) {
		return name.compareTo(o.name);
	}

	/** Test if the capability equals another capability */
	public boolean equals(Object o) {
		if(o instanceof IrisCapabilityImpl)
			return name.equals(((IrisCapabilityImpl)o).name);
		else
			return false;
	}

	/** Calculate a hash code */
	public int hashCode() {
		return name.hashCode();
	}

	/** Get the primary key name */
	public String getKeyName() {
		return "name";
	}

	/** Get the primary key */
	public String getKey() {
		return name;
	}

	/** Get a string representation of the object */
	public String toString() {
		return name;
	}

	/** Destroy an IRIS capability */
	public void doDestroy() throws TMSException {
		store.destroy(this);
	}

	/** Set the enabled flag */
	public void doSetEnabled(boolean e) throws TMSException {
		if(e == enabled)
			return;
		store.update(this, "enabled", e);
		super.setEnabled(e);
	}
}
