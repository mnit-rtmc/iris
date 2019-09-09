/*
 * SONAR -- Simple Object Notification And Replication
 * Copyright (C) 2018  Minnesota Department of Transportation
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

import us.mn.state.dot.sonar.Domain;

/**
 * A network domain for log-in access control.
 *
 * @author Douglas Lau
 */
public class DomainImpl implements Domain {

	/** Destroy a domain */
	@Override
	public void destroy() {
		// Subclasses must remove domain from backing store
	}

	/** Get the SONAR type name */
	@Override
	public String getTypeName() {
		return SONAR_TYPE;
	}

	/** Domain name */
	private final String name;

	/** Get the SONAR object name */
	@Override
	public String getName() {
		return name;
	}

	/** Create a new domain */
	public DomainImpl(String n) {
		name = n;
	}

	/** CIDR (Classless Inter-Domain Routing) address */
	private String cidr;

	/** Set the CIDR */
	@Override
	public void setCIDR(String c) {
		cidr = c;
	}

	/** Get the CIDR */
	@Override
	public String getCIDR() {
		return cidr;
	}

	/** Enabled flag */
	private boolean enabled;

	/** Set the enabled flag */
	@Override
	public void setEnabled(boolean e) {
		enabled = e;
	}

	/** Get the enabled flag */
	@Override
	public boolean getEnabled() {
		return enabled;
	}
}
