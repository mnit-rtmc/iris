/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018-2024  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.sonar.Domain;
import us.mn.state.dot.sonar.server.ServerNamespace;
import us.mn.state.dot.sonar.server.DomainImpl;

/**
 * IRIS domain
 *
 * @author Douglas lau
 */
public class IrisDomainImpl extends DomainImpl implements Storable,
	Comparable<IrisDomainImpl>
{
	/** SQL connection to database */
	static private SQLConnection store;

	/** Lookup all the domains */
	static public void lookup(SQLConnection c, final ServerNamespace ns)
		throws TMSException
	{
		store = c;
		store.query("SELECT name, cidr, enabled FROM iris." +
			SONAR_TYPE + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				ns.addObject(new IrisDomainImpl(row));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", getName());
		map.put("cidr", getCIDR());
		map.put("enabled", getEnabled());
		return map;
	}

	/** Store an object */
	public void doStore() throws TMSException {
		store.create(this);
	}

	/** Get the database table name */
	@Override
	public String getTable() {
		return "iris." + SONAR_TYPE;
	}

	/** Create a new IRIS domain */
	public IrisDomainImpl(String n) {
		super(n);
	}

	/** Create an IRIS domain from database lookup */
	private IrisDomainImpl(ResultSet row) throws SQLException {
		this(row.getString(1),  // name
		     row.getString(2),  // CIDR
		     row.getBoolean(3)  // enabled
		);
	}

	/** Create an IRIS domain from database lookup */
	private IrisDomainImpl(String n, String c, boolean e) {
		this(n);
		setCIDR(c);
		setEnabled(e);
	}

	/** Compare to another domain */
	@Override
	public int compareTo(IrisDomainImpl o) {
		return getName().compareTo(o.getName());
	}

	/** Test if the domain equals another domain */
	@Override
	public boolean equals(Object o) {
		if (o instanceof IrisDomainImpl)
			return getName().equals(((IrisDomainImpl) o).getName());
		else
			return false;
	}

	/** Calculate a hash code */
	@Override
	public int hashCode() {
		return getName().hashCode();
	}

	/** Get the primary key name */
	@Override
	public String getPKeyName() {
		return "name";
	}

	/** Get the primary key */
	@Override
	public String getPKey() {
		return getName();
	}

	/** Get a string representation of the object */
	@Override
	public String toString() {
		return getName();
	}

	/** Destroy an IRIS domain */
	public void doDestroy() throws TMSException {
		store.destroy(this);
	}

	/** Set the CIDR */
	public void doSetCIDR(String c) throws TMSException {
		if (!c.equals(getCIDR())) {
			store.update(this, "cidr", c);
			setCIDR(c);
		}
	}

	/** Set the enabled flag */
	public void doSetEnabled(boolean e) throws TMSException {
		if (e != getEnabled()) {
			store.update(this, "enabled", e);
			setEnabled(e);
		}
	}
}
