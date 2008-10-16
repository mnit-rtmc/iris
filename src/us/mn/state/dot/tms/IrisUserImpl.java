/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2008  Minnesota Department of Transportation
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
import java.util.TreeSet;
import us.mn.state.dot.sonar.NamespaceError;
import us.mn.state.dot.sonar.Role;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.sonar.User;
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

	/** User/Role table mapping */
	static protected TableMapping mapping;

	/** Lookup all the users */
	static public void lookup(SQLConnection c, final ServerNamespace ns)
		throws TMSException
	{
		store = c;
		mapping = new TableMapping(store, "iris_user", "role");
		store.query("SELECT name, dn, full_name FROM iris_user;",
			new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				ns.add(new IrisUserImpl(ns,
					row.getString(1),	// name
					row.getString(2),	// dn
					row.getString(3)	// full_name
				));
			}
		});
	}

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("dn", dn);
		map.put("full_name", fullName);
		return map;
	}

	/** Store an object */
	public void doStore() throws TMSException {
		store.create(this);
	}

	/** Get the database table name */
	public String getTable() {
		return "iris_user";
	}

	/** Create a new IRIS user */
	public IrisUserImpl(String n) {
		super(n);
		// FIXME: validate for SQL injections
		dn = "";
		fullName = "";
	}

	/** Create an IRIS user from database lookup */
	protected IrisUserImpl(ServerNamespace ns, String n, String d,
		String fn) throws TMSException, NamespaceError
	{
		this(n);
		dn = d;
		fullName = fn;
		TreeSet<IrisRoleImpl> r = new TreeSet<IrisRoleImpl>();
		for(Object o: mapping.lookup("iris_user", this))
			r.add((IrisRoleImpl)ns.lookupObject("role", (String)o));
		roles = r.toArray(new IrisRoleImpl[0]);
	}

	/** Get the primary key name */
	public String getKeyName() {
		return "name";
	}

	/** Get the primary key */
	public String getKey() {
		return name;
	}

	/** Destroy an IRIS user */
	public void doDestroy() throws TMSException {
		store.destroy(this);
	}

	/** Set the roles assigned to the user */
	public void doSetRoles(Role[] r) throws TMSException {
		TreeSet<Storable> rset = new TreeSet<Storable>();
		for(Role rl: r) {
			if(rl instanceof IrisRoleImpl)
				rset.add((IrisRoleImpl)rl);
			else
				throw new ChangeVetoException("Invalid role");
		}
		mapping.update("iris_user", this, rset);
		super.setRoles(r);
	}

	/** Set the LDAP distinguished name */
	public void doSetDn(String d) throws TMSException {
		if(d.equals(dn))
			return;
		store.update(this, "dn", d);
		super.setDn(d);
	}

	/** Set the full (display) name */
	public void doSetFullName(String n) throws TMSException {
		if(n.equals(fullName))
			return;
		store.update(this, "full_name", n);
		super.setFullName(n);
	}
}
