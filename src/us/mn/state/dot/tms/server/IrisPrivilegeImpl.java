/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2017  Minnesota Department of Transportation
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
	static private SQLConnection store;

	/** Lookup all the privileges */
	static public void lookup(SQLConnection c, final ServerNamespace ns)
		throws TMSException
	{
		store = c;
		store.query("SELECT name, capability, type_n, obj_n, " +
			"attr_n, write FROM iris." + SONAR_TYPE + ";",
			new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				ns.addObject(new IrisPrivilegeImpl(row, ns));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", getName());
		map.put("capability", getCapability());
		map.put("type_n", getTypeN());
		map.put("obj_n", getObjN());
		map.put("attr_n", getAttrN());
		map.put("write", getWrite());
		return map;
	}

	/** Store an object */
	public void doStore() throws TMSException {
		store.create(this);
	}

	/** Get the database table name */
	@Override
	public String getTable() {
		return "iris.privilege";
	}

	/** Create a new IRIS privilege */
	public IrisPrivilegeImpl(String n) {
		super(n);
	}

	/** Create an IRIS privilege */
	private IrisPrivilegeImpl(ResultSet row, Namespace ns)
		throws SQLException
	{
		this(ns,
		     row.getString(1),	// name
		     row.getString(2),	// capability
		     row.getString(3),	// typeN
		     row.getString(4),	// objN
		     row.getString(5),	// attrN
		     row.getBoolean(6)	// write
		);
	}

	/** Create an IRIS privilege from database lookup */
	private IrisPrivilegeImpl(Namespace ns, String n, String c, String tn,
		String on, String an, boolean w)
	{
		this(n, (Capability) ns.lookupObject(Capability.SONAR_TYPE, c),
		     tn, on, an, w);
	}

	/** Create an IRIS privilege from database lookup */
	private IrisPrivilegeImpl(String n, Capability c, String tn,
		String on, String an, boolean w)
	{
		super(n, c);
		setTypeN(tn);
		setObjN(on);
		setAttrN(an);
		setWrite(w);
	}

	/** Compare to another privilege */
	@Override
	public int compareTo(IrisPrivilegeImpl o) {
		return getName().compareTo(o.getName());
	}

	/** Test if the privilege equals another privilege */
	@Override
	public boolean equals(Object o) {
		if (o instanceof IrisPrivilegeImpl) {
			IrisPrivilegeImpl other = (IrisPrivilegeImpl) o;
			return getName().equals(other.getName());
		} else
			return false;
	}

	/** Calculate a hash code */
	@Override
	public int hashCode() {
		return getName().hashCode();
	}

	/** Get the primary key name */
	@Override
	public String getKeyName() {
		return "name";
	}

	/** Get the primary key */
	@Override
	public String getKey() {
		return getName();
	}

	/** Get a string representation of the object */
	@Override
	public String toString() {
		return getName();
	}

	/** Destroy an IRIS privilege */
	public void doDestroy() throws TMSException {
		store.destroy(this);
	}

	/** Set the type name */
	@Override
	public void doSetTypeN(String n) throws TMSException, NamespaceError {
		if (!n.equals(getTypeN())) {
			checkPattern(n);
			store.update(this, "type_n", n);
			setTypeN(n);
		}
	}

	/** Set the object name */
	@Override
	public void doSetObjN(String n) throws TMSException, NamespaceError {
		if (!n.equals(getObjN())) {
			checkPattern(OBJ_PATTERN, n);
			store.update(this, "obj_n", n);
			setObjN(n);
		}
	}

	/** Set the attribute name */
	@Override
	public void doSetAttrN(String n) throws TMSException, NamespaceError {
		if (!n.equals(getAttrN())) {
			checkPattern(n);
			store.update(this, "attr_n", n);
			setAttrN(n);
		}
	}

	/** Set the write privilege */
	public void doSetWrite(boolean w) throws TMSException {
		if (w != getWrite()) {
			store.update(this, "write", w);
			setWrite(w);
		}
	}
}
