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
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.sonar.NamespaceError;
import us.mn.state.dot.sonar.Capability;
import us.mn.state.dot.sonar.server.ServerNamespace;
import us.mn.state.dot.sonar.server.PrivilegeImpl;

/**
 * IRIS privilege
 *
 * @author Douglas lau
 */
public class IrisPrivilegeImpl extends PrivilegeImpl
	implements Comparable<IrisPrivilegeImpl>, Storable
{
	/** SQL connection to database */
	static protected SQLConnection store;

	/** Lookup all the privileges */
	static public void lookup(SQLConnection c, final ServerNamespace ns)
		throws TMSException
	{
		store = c;
		store.query("SELECT name, capability, pattern, priv_r, " +
			"priv_w, priv_c, priv_d FROM iris.privilege;",
			new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				ns.addObject(new IrisPrivilegeImpl(ns,
					row.getString(1),  // name
					row.getString(2),  // capability
					row.getString(3),  // pattern
					row.getBoolean(4), // priv_r
					row.getBoolean(5), // priv_w
					row.getBoolean(6), // priv_c
					row.getBoolean(7)  // priv_d
				));
			}
		});
	}

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("capability", capability);
		map.put("pattern", pattern);
		map.put("priv_r", priv_r);
		map.put("priv_w", priv_w);
		map.put("priv_c", priv_c);
		map.put("priv_d", priv_d);
		return map;
	}

	/** Store an object */
	public void doStore() throws TMSException {
		store.create(this);
	}

	/** Get the database table name */
	public String getTable() {
		return "iris.privilege";
	}

	/** Create a new IRIS privilege */
	public IrisPrivilegeImpl(String n) {
		super(n);
		pattern = "";
	}

	/** Create an IRIS privilege from database lookup */
	protected IrisPrivilegeImpl(Namespace ns, String n, String cap,
		String p, boolean r, boolean w, boolean c, boolean d)
	{
		this(n, (Capability)ns.lookupObject(Capability.SONAR_TYPE, cap),
		     p, r, w, c, d);
	}

	/** Create an IRIS privilege from database lookup */
	protected IrisPrivilegeImpl(String n, Capability cap, String p,
		boolean r, boolean w, boolean c, boolean d)
	{
		this(n);
		capability = cap;
		pattern = p;
		priv_r = r;
		priv_w = w;
		priv_c = c;
		priv_d = d;
	}

	/** Compare to another privilege */
	public int compareTo(IrisPrivilegeImpl o) {
		return name.compareTo(o.name);
	}

	/** Test if the privilege equals another privilege */
	public boolean equals(Object o) {
		if(o instanceof IrisPrivilegeImpl)
			return name.equals(((IrisPrivilegeImpl)o).name);
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

	/** Destroy an IRIS privilege */
	public void doDestroy() throws TMSException {
		store.destroy(this);
	}

	/** Set the namespace pattern */
	public void doSetPattern(String p) throws TMSException, NamespaceError {
		if(p.equals(pattern))
			return;
		checkPattern(p);
		store.update(this, "pattern", p);
		super.setPattern(p);
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
