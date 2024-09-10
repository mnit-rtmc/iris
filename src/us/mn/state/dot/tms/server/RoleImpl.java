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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import us.mn.state.dot.sonar.Capability;
import us.mn.state.dot.sonar.server.CapabilityImpl;
import us.mn.state.dot.sonar.server.ServerNamespace;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.Domain;
import us.mn.state.dot.tms.Role;
import us.mn.state.dot.tms.TMSException;

/**
 * A role is a set of permissions for the SONAR namespace.
 *
 * @author Douglas lau
 */
public class RoleImpl implements Role, Comparable<RoleImpl>,
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
				ns.addObject(new RoleImpl(ns,
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

	/** Get the SONAR type name */
	@Override
	public String getTypeName() {
		return SONAR_TYPE;
	}

	/** Get the database table name */
	@Override
	public String getTable() {
		return "iris." + SONAR_TYPE;
	}

	/** Create a new role */
	public RoleImpl(String n) {
		name = n;
	}

	/** Create an role from database lookup */
	private RoleImpl(ServerNamespace ns, String n, boolean e)
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
	public int compareTo(RoleImpl o) {
		return name.compareTo(o.name);
	}

	/** Test if the role equals another role */
	@Override
	public boolean equals(Object o) {
		if (o instanceof RoleImpl)
			return name.equals(((RoleImpl) o).name);
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

	/** Destroy a role */
	@Override
	public void destroy() {
		// Subclasses must remove role from backing store
	}

	/** Destroy a role */
	public void doDestroy() throws TMSException {
		store.destroy(this);
	}

	/** Role name */
	private final String name;

	/** Get the SONAR object name */
	@Override
	public String getName() {
		return name;
	}

	/** Flag to enable the role */
	private boolean enabled;

	/** Enable or disable the role */
	@Override
	public void setEnabled(boolean e) {
		enabled = e;
	}

	/** Set the enabled flag */
	public void doSetEnabled(boolean e) throws TMSException {
		if (e != enabled) {
			store.update(this, "enabled", e);
			setEnabled(e);
		}
	}

	/** Get the enabled flag */
	@Override
	public boolean getEnabled() {
		return enabled;
	}

	/** Capabilities for the role */
	private CapabilityImpl[] capabilities = new CapabilityImpl[0];

	/** Set the capabilities */
	@Override
	public void setCapabilities(Capability[] c) {
		CapabilityImpl[] _c = new CapabilityImpl[c.length];
		for (int i = 0; i < c.length; i++)
			_c[i] = (CapabilityImpl) c[i];
		capabilities = _c;
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

	/** Get the capabilities */
	@Override
	public Capability[] getCapabilities() {
		return capabilities;
	}

	/** Allowed login domains */
	private DomainImpl[] domains = new DomainImpl[0];

	/** Set the allowed login domains */
	@Override
	public void setDomains(Domain[] ds) {
		ArrayList<DomainImpl> list = new ArrayList<DomainImpl>();
		for (Domain d : ds) {
			if (d instanceof DomainImpl)
				list.add((DomainImpl) d);
		}
		domains = list.toArray(new DomainImpl[0]);
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

	/** Get the allowed login domains */
	@Override
	public Domain[] getDomains() {
		return domains;
	}
}
