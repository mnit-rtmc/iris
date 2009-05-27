/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.security;

import java.security.acl.Permission;

/**
 * Implementation of Permissions for IRIS.
 *
 * @author Erik Engstrom
 */
public class IrisPermission implements Permission {

	protected final String name;

	/** Create a new Iris permission */
	protected IrisPermission(String n) {
		name = n;
	}

	public String toString() {
		return "Iris Permission: " + name;
	}

	public boolean equals(Object obj) {
		return (obj instanceof IrisPermission) &&
			(toString().equals(obj.toString()));
	}

	public int hashCode() {
		return toString().hashCode();
	}

	static public final IrisPermission DMS_TAB =
		new IrisPermission("DMS_TAB");
	static public final IrisPermission METER_TAB =
		new IrisPermission("METER_TAB");
	static public final IrisPermission MAIN_TAB =
		new IrisPermission("MAIN_TAB");
	static public final IrisPermission RWIS_TAB =
		new IrisPermission("RWIS_TAB");
	static public final IrisPermission LCS_TAB =
		new IrisPermission("LCS_TAB");
	static public final IrisPermission ALERT =
		new IrisPermission("ALERT");
	static public final IrisPermission ADMINISTRATOR =
		new IrisPermission("ADMINISTRATOR");
	static public final IrisPermission ACTIVATE =
		new IrisPermission("ACTIVATE");
	static public final IrisPermission VIEW = new IrisPermission("VIEW");
}
