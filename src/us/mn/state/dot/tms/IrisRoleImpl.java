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

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("pattern", pattern);
		map.put("priv_r", priv_r);
		map.put("priv_w", priv_w);
		map.put("priv_c", priv_c);
		map.put("priv_d", priv_d);
		return map;
	}

	/** Get the database table name */
	public String getTable() {
		return "role";
	}

	/** Create a new IRIS role */
	public IrisRoleImpl(String n) {
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

	/** Test if the role equals another role */
	public boolean equals(Object o) {
		if(o instanceof IrisRoleImpl)
			return name.equals(((IrisRoleImpl)o).name);
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
