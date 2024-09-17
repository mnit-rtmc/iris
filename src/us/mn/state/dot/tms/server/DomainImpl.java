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
import us.mn.state.dot.tms.Domain;
import us.mn.state.dot.tms.TMSException;

/**
 * A network domain for log-in access control.
 *
 * @author Douglas lau
 */
public class DomainImpl extends BaseObjectImpl implements Domain,
	Comparable<DomainImpl>
{
	/** Lookup all the domains */
	static protected void loadAll() throws TMSException {
		namespace.registerType(DomainImpl.class);
		store.query("SELECT name, block, enabled FROM iris." +
			SONAR_TYPE + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new DomainImpl(row));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", getName());
		map.put("block", getBlock());
		map.put("enabled", getEnabled());
		return map;
	}

	/** Create a new domain */
	public DomainImpl(String n) {
		super(n);
	}

	/** Create an domain from database lookup */
	private DomainImpl(ResultSet row) throws SQLException {
		this(row.getString(1),  // name
		     row.getString(2),  // block
		     row.getBoolean(3)  // enabled
		);
	}

	/** Create an domain from database lookup */
	private DomainImpl(String n, String b, boolean e) {
		this(n);
		setBlock(b);
		setEnabled(e);
	}

	/** Compare to another domain */
	@Override
	public int compareTo(DomainImpl o) {
		return name.compareTo(o.name);
	}

	/** Test if the domain equals another domain */
	@Override
	public boolean equals(Object o) {
		if (o instanceof DomainImpl)
			return name.equals(((DomainImpl) o).name);
		else
			return false;
	}

	/** CIDR (Classless Inter-Domain Routing) block */
	private String block;

	/** Set the CIDR block */
	@Override
	public void setBlock(String b) {
		block = b;
	}

	/** Set the CIDR block */
	public void doSetBlock(String b) throws TMSException {
		if (!objectEquals(b, block)) {
			store.update(this, "block", b);
			setBlock(b);
		}
	}

	/** Get the CIDR block */
	@Override
	public String getBlock() {
		return block;
	}

	/** Enabled flag */
	private boolean enabled;

	/** Set the enabled flag */
	@Override
	public void setEnabled(boolean e) {
		enabled = e;
	}

	/** Set the enabled flag */
	public void doSetEnabled(boolean e) throws TMSException {
		if (e != getEnabled()) {
			store.update(this, "enabled", e);
			setEnabled(e);
		}
	}

	/** Get the enabled flag */
	@Override
	public boolean getEnabled() {
		return enabled;
	}
}
