/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2024  Minnesota Department of Transportation
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
import java.util.TreeSet;
import us.mn.state.dot.sonar.Capability;
import us.mn.state.dot.sonar.server.ServerNamespace;
import us.mn.state.dot.sonar.server.RoleImpl;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.Domain;
import us.mn.state.dot.tms.TMSException;

/**
 * IRIS role
 *
 * @author Douglas lau
 */
public class IrisRoleImpl extends RoleImpl implements Comparable<IrisRoleImpl>,
	Storable
{
	/** SQL connection to database */
	static private SQLConnection store;

	/** Role/Capability table mapping */
	static private TableMapping cap_map;

	/** Role/Domain table mapping */
	static private TableMapping dom_map;

	/** Lookup all the roles */
	static public void lookup(SQLConnection c, final ServerNamespace ns)
		throws TMSException
	{
		store = c;
		cap_map = new TableMapping(store, "iris", SONAR_TYPE,
			Capability.SONAR_TYPE);
		dom_map = new TableMapping(store, "iris", SONAR_TYPE,
			Domain.SONAR_TYPE);
		store.query("SELECT name, enabled FROM iris." + SONAR_TYPE +
			";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				ns.addObject(new IrisRoleImpl(ns,
					row.getString(1),	// name
					row.getBoolean(2)	// enabled
				));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
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
	@Override
	public String getTable() {
		return "iris." + SONAR_TYPE;
	}

	/** Create a new IRIS role */
	public IrisRoleImpl(String n) {
		super(n);
	}

	/** Create an IRIS role from database lookup */
	private IrisRoleImpl(ServerNamespace ns, String n, boolean e)
		throws TMSException
	{
		this(n);
		enabled = e;
		setCapabilities(lookupCapabilities(ns));
		setDomains(lookupDomains(ns));
	}

	/** Lookup all the capabilities for a role */
	private IrisCapabilityImpl[] lookupCapabilities(ServerNamespace ns)
		throws TMSException
	{
		TreeSet<IrisCapabilityImpl> cset =
			new TreeSet<IrisCapabilityImpl>();
		for (String o: cap_map.lookup(this)) {
			Object c = ns.lookupObject("capability", o);
			if (c instanceof IrisCapabilityImpl)
				cset.add((IrisCapabilityImpl) c);
		}
		return cset.toArray(new IrisCapabilityImpl[0]);
	}

	/** Lookup all the domains for a user */
	private DomainImpl[] lookupDomains(ServerNamespace ns)
		throws TMSException
	{
		TreeSet<DomainImpl> dset = new TreeSet<DomainImpl>();
		for (String o: dom_map.lookup(this)) {
			Object d = ns.lookupObject(Domain.SONAR_TYPE, o);
			if (d instanceof DomainImpl)
				dset.add((DomainImpl) d);
		}
		return dset.toArray(new DomainImpl[0]);
	}

	/** Compare to another role */
	@Override
	public int compareTo(IrisRoleImpl o) {
		return name.compareTo(o.name);
	}

	/** Test if the role equals another role */
	@Override
	public boolean equals(Object o) {
		if (o instanceof IrisRoleImpl)
			return name.equals(((IrisRoleImpl) o).name);
		else
			return false;
	}

	/** Calculate a hash code */
	@Override
	public int hashCode() {
		return name.hashCode();
	}

	/** Get the primary key name */
	@Override
	public String getPKeyName() {
		return "name";
	}

	/** Get the primary key */
	@Override
	public String getPKey() {
		return name;
	}

	/** Get a string representation of the object */
	@Override
	public String toString() {
		return name;
	}

	/** Destroy an IRIS role */
	public void doDestroy() throws TMSException {
		store.destroy(this);
	}

	/** Set the enabled flag */
	public void doSetEnabled(boolean e) throws TMSException {
		if (e != enabled) {
			store.update(this, "enabled", e);
			setEnabled(e);
		}
	}

	/** Set the capabilities assigned to the role */
	public void doSetCapabilities(Capability[] caps) throws TMSException {
		TreeSet<Storable> cset = new TreeSet<Storable>();
		for (Capability c: caps) {
			if (c instanceof IrisCapabilityImpl)
				cset.add((IrisCapabilityImpl) c);
			else
				throw new ChangeVetoException("Bad capability");
		}
		cap_map.update(this, cset);
		setCapabilities(caps);
	}

	/** Set the domains assigned to the user */
	public void doSetDomains(Domain[] doms) throws TMSException {
		TreeSet<Storable> dset = new TreeSet<Storable>();
		for (Domain d: doms) {
			if (d instanceof DomainImpl)
				dset.add((DomainImpl) d);
			else
				throw new ChangeVetoException("Bad domain");
		}
		dom_map.update(this, dset);
		setDomains(doms);
	}
}
