/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007  Minnesota Department of Transportation
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
import java.sql.SQLException;
import java.util.Arrays;
import java.util.TreeSet;
import us.mn.state.dot.sonar.NamespaceError;
import us.mn.state.dot.sonar.Role;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.sonar.server.Namespace;
import us.mn.state.dot.sonar.server.RoleImpl;

/**
 * IRIS role
 *
 * @author Douglas lau
 */
public class IrisRoleImpl extends RoleImpl implements Comparable<IrisRoleImpl>,
	Storable
{
	/** SQL connection to database */
	static protected SQLConnection store;

	/** Lookup all the roles */
	static public void lookup(SQLConnection c, final Namespace ns)
		throws TMSException
	{
		store = c;
		store.query("SELECT name, pattern, priv_r, priv_w, priv_c, " +
			"priv_d FROM role;", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				ns.add(new IrisRoleImpl(
					row.getString(1),  // name
					row.getString(2),  // pattern
					row.getBoolean(3), // priv_r
					row.getBoolean(4), // priv_w
					row.getBoolean(5), // priv_c
					row.getBoolean(6)  // priv_d
				));
			}
		});
	}

	/** Get the database table name */
	public String getTable() {
		return "role";
	}

	/** Create a new IRIS role */
	protected IrisRoleImpl(String n) {
		super(n);
		pattern = "";
	}

	/** Create an IRIS role from database lookup */
	protected IrisRoleImpl(String n, String p, boolean r, boolean w,
		boolean c, boolean d)
	{
		this(n);
		pattern = p;
		priv_r = r;
		priv_w = w;
		priv_c = c;
		priv_d = d;
	}

	/** Compare to another role */
	public int compareTo(IrisRoleImpl o) {
		return name.compareTo(o.name);
	}

	/** Get the primary key name */
	public String getKeyName() {
		return "name";
	}

	/** Get the primary key */
	public String getKey() {
		return name;
	}

	/** Create a new IRIS role */
	static public Role doCreate(String name) throws TMSException {
		// FIXME: validate for SQL injections
		IrisRoleImpl role = new IrisRoleImpl(name);
		store.update("INSERT INTO role (name) VALUES ('" + name +
			"');");
		return role;
	}

	/** Destroy an IRIS role */
	public void doDestroy() throws TMSException {
		store.destroy(this);
	}

	/** Set the namespace pattern */
	public void doSetPattern(String p) throws TMSException, NamespaceError {
		if(p.equals(pattern))
			return;
		checkPattern(p);
		store.update(this, "pattern", p);
		pattern = p;
	}

	/** Set the read privilege */
	public void doSetPrivR(boolean p) throws TMSException {
		if(p == priv_r)
			return;
		store.update(this, "priv_r", p);
		super.setPrivR(p);
	}

	/** Set the write privilege */
	public void doSetPrivW(boolean p) throws TMSException {
		if(p == priv_w)
			return;
		store.update(this, "priv_w", p);
		super.setPrivW(p);
	}

	/** Set the create privilege */
	public void doSetPrivC(boolean p) throws TMSException {
		if(p == priv_c)
			return;
		store.update(this, "priv_c", p);
		super.setPrivC(p);
	}

	/** Set the delete privilege */
	public void doSetPrivD(boolean p) throws TMSException {
		if(p == priv_d)
			return;
		store.update(this, "priv_d", p);
		super.setPrivD(p);
	}
}
