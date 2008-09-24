/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2008  Minnesota Department of Transportation
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

import java.security.Principal;
import java.util.HashSet;

/**
 * The IrisUser represents a IRIS user.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public class IrisUser implements Principal {

	/** Network name of the user */
	protected final String name;

	/** The full name of the user */
	protected final String fullName;

	/** User permissions */
	protected final HashSet<IrisPermission> permissions =
		new HashSet<IrisPermission>();

	/** Create a new Iris user */
	public IrisUser(String n_name, String f_name) {
		name = n_name;
		fullName = f_name;
	}

	/** Get the network name of the user */
	public String getName() {
		return name;
	}

	/** Get the full name of the user */
	public String getFullName() {
		return fullName;
	}

	/** Add a permission to the user */
	protected void addPermission(IrisPermission p) {
		permissions.add(p);
	}

	/** Check if a user has the specified permission */
	public boolean hasPermission(IrisPermission p) {
		return permissions.contains(p);
	}

	/** Check if a user has no permissions */
	public boolean hasNoPermissions() {
		return permissions.isEmpty();
	}

	/** Get a string representation of the user */
	public String toString() {
		return getName() + " (" + getFullName() + ")";
	}

	/** Get the hash code */
	public int hashCode() {
		return name.hashCode();
	}

	/** Check two users for equality */
	public boolean equals(Object obj) {
		return (obj instanceof IrisUser) &&
			(obj.hashCode() == hashCode());
	}

	/** Add permission for the specified role */
	public void addRolePermission(String role) {
		if(role.equals("view"))
			addPermission(IrisPermission.VIEW);
		else if(role.equals("admin"))
			addPermission(IrisPermission.ADMINISTRATOR);
		else if(role.equals("incidents"))
			addPermission(IrisPermission.MAIN_TAB);
		else if(role.equals("dms"))
			addPermission(IrisPermission.DMS_TAB);
		else if(role.equals("meter"))
			addPermission(IrisPermission.METER_TAB);
		else if(role.equals("rwis"))
			addPermission(IrisPermission.RWIS_TAB);
		else if(role.equals("alert"))
			addPermission(IrisPermission.ALERT);
		else if(role.equals("activate"))
			addPermission(IrisPermission.ACTIVATE);
		else if(role.equals("lcs"))
			addPermission(IrisPermission.LCS_TAB);
		else if(role.equals("tiger"))
			addPermission(IrisPermission.TIGER);
	}
}
