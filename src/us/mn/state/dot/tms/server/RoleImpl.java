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
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.Domain;
import us.mn.state.dot.tms.Role;
import us.mn.state.dot.tms.TMSException;

/**
 * A role is a set of permissions for the SONAR namespace.
 *
 * @author Douglas lau
 */
public class RoleImpl extends BaseObjectImpl implements Role,
	Comparable<RoleImpl>
{
	/** Role/Domain table mapping */
	static private TableMapping dom_map;

	/** Load all */
	static public void loadAll() throws TMSException {
		dom_map = new TableMapping(store, "iris", SONAR_TYPE,
			Domain.SONAR_TYPE);
		store.query("SELECT name, enabled FROM iris." + SONAR_TYPE +
			";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new RoleImpl(
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

	/** Create a new role */
	public RoleImpl(String n) {
		super(n);
	}

	/** Create a role from database lookup */
	private RoleImpl(String n, boolean e) throws TMSException {
		this(n);
		enabled = e;
		setDomains(lookupDomains());
	}

	/** Lookup all the domains for a user */
	private DomainImpl[] lookupDomains() throws TMSException {
		TreeSet<DomainImpl> dset = new TreeSet<DomainImpl>();
		for (String o: dom_map.lookup(this)) {
			Object d = namespace.lookupObject(Domain.SONAR_TYPE, o);
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
