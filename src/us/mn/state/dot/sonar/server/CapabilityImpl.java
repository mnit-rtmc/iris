/*
 * SONAR -- Simple Object Notification And Replication
 * Copyright (C) 2006-2017  Minnesota Department of Transportation
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

import us.mn.state.dot.sonar.Capability;

/**
 * A capability is a set of privileges for the SONAR namespace.
 *
 * @author Douglas Lau
 */
public class CapabilityImpl implements Capability {

	/** Destroy a capability */
	@Override
	public void destroy() {
		// Subclasses must remove capability from backing store
	}

	/** Get the SONAR type name */
	@Override
	public String getTypeName() {
		return SONAR_TYPE;
	}

	/** Capability name */
	protected final String name;

	/** Get the SONAR object name */
	@Override
	public String getName() {
		return name;
	}

	/** Create a new capability */
	public CapabilityImpl(String n) {
		name = n;
	}

	/** Flag to enable the capability */
	protected boolean enabled;

	/** Enable or disable the capability */
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
