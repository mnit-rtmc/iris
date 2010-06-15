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
import us.mn.state.dot.sonar.Role;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.sonar.server.ServerNamespace;
import us.mn.state.dot.sonar.server.UserImpl;

/**
 * IRIS user
 *
 * @author Douglas lau
 */
public class IrisUserImpl extends UserImpl implements Storable {

	/** SQL connection to database */
	static protected SQLConnection store;

	/** Lookup all the users */
	static public void lookup(SQLConnection c, final ServerNamespace ns)
		throws TMSException
	{
		store = c;
		store.query("SELECT name, full_name, dn, role, enabled FROM " +
			"iris.i_user;", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				ns.addObject(new IrisUserImpl(ns,
					row.getString(1),	// name
					row.getString(2),	// full_name
					row.getString(3),	// dn
					row.getString(4),	// role
					row.getBoolean(5)	// enabled
				));
			}
		});
	}

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("full_name", fullName);
		map.put("dn", dn);
		map.put("rolw", role);
		map.put("enabled", enabled);
		return map;
	}

	/** Store an object */
	public void doStore() throws TMSException {
		store.create(this);
	}

	/** Get the database table name */
	public String getTable() {
		return "iris.i_user";
	}

	/** Create a new IRIS user */
	public IrisUserImpl(String n) {
		super(n);
		// FIXME: validate for SQL injections
		fullName = "";
		dn = "";
		role = null;
		enabled = false;
	}

	/** Create an IRIS user from database lookup */
	protected IrisUserImpl(ServerNamespace ns, String n, String fn,
		String d, String r, boolean e) throws TMSException
	{
		this(n, fn, d, (IrisRoleImpl)ns.lookupObject(Role.SONAR_TYPE,r),
		     e);
	}

	/** Create an IRIS user from database lookup */
	protected IrisUserImpl(String n, String fn, String d, IrisRoleImpl r,
		boolean e)
	{
		super(n);
		fullName = fn;
		dn = d;
		role = r;
		enabled = e;
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

	/** Destroy an IRIS user */
	public void doDestroy() throws TMSException {
		store.destroy(this);
	}

	/** Set the full (display) name */
	public void doSetFullName(String n) throws TMSException {
		if(n.equals(fullName))
			return;
		store.update(this, "full_name", n);
		super.setFullName(n);
	}

	/** Set the LDAP distinguished name */
	public void doSetDn(String d) throws TMSException {
		if(d.equals(dn))
			return;
		store.update(this, "dn", d);
		super.setDn(d);
	}

	/** Set the role assigned to the user */
	public void doSetRole(Role r) throws TMSException {
		if(r == role)
			return;
		store.update(this, "role", r);
		super.setRole(r);
	}

	/** Set the enabled flag */
	public void doSetEnabled(boolean e) throws TMSException {
		if(e == enabled)
			return;
		store.update(this, "enabled", e);
		super.setEnabled(e);
	}
}
