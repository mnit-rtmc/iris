/*
 * SONAR -- Simple Object Notification And Replication
 * Copyright (C) 2006-2024  Minnesota Department of Transportation
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
package us.mn.state.dot.sonar.server;

import java.util.ArrayList;
import us.mn.state.dot.sonar.Capability;
import us.mn.state.dot.sonar.Role;
import us.mn.state.dot.tms.Domain;
import us.mn.state.dot.tms.server.DomainImpl;

/**
 * A role is a set of permissions for the SONAR namespace.
 *
 * @author Douglas Lau
 */
public class RoleImpl implements Role {

	/** Destroy a role */
	@Override
	public void destroy() {
		// Subclasses must remove role from backing store
	}

	/** Get the SONAR type name */
	@Override
	public String getTypeName() {
		return SONAR_TYPE;
	}

	/** Role name */
	protected final String name;

	/** Get the SONAR object name */
	@Override
	public String getName() {
		return name;
	}

	/** Create a new role */
	public RoleImpl(String n) {
		name = n;
	}

	/** Flag to enable the role */
	protected boolean enabled;

	/** Enable or disable the role */
	@Override
	public void setEnabled(boolean e) {
		enabled = e;
	}

	/** Get the enabled flag */
	@Override
	public boolean getEnabled() {
		return enabled;
	}

	/** Capabilities for the role */
	protected CapabilityImpl[] capabilities = new CapabilityImpl[0];

	/** Set the capabilities */
	@Override
	public void setCapabilities(Capability[] c) {
		CapabilityImpl[] _c = new CapabilityImpl[c.length];
		for (int i = 0; i < c.length; i++)
			_c[i] = (CapabilityImpl) c[i];
		capabilities = _c;
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

	/** Get the allowed login domains */
	@Override
	public Domain[] getDomains() {
		return domains;
	}
}
