/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2009  Minnesota Department of Transportation
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

import java.util.TreeSet;
import us.mn.state.dot.sonar.Name;
import us.mn.state.dot.sonar.Role;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for roles assigned to IRIS users
 *
 * @author Douglas Lau
 */
public class UserRoleModel extends ProxyTableModel<Role> {

	/** Create the columns in the model */
	protected ProxyColumn[] createColumns() {
	    // NOTE: half-indent to declare array
	    return new ProxyColumn[] {
		new ProxyColumn<Role>("Role", 120) {
			public Object getValueAt(Role r) {
				return r.getName();
			}
		},
		new ProxyColumn<Role>("Assigned", 80, Boolean.class) {
			public Object getValueAt(Role r) {
				return isAssigned(r);
			}
			public boolean isEditable(Role r) {
				return canUpdateUserRoles();
			}
			public void setValueAt(Role r, Object value) {
				if(value instanceof Boolean)
					setAssigned(r, (Boolean)value);
			}
		}
	    };
	}

	/** Check if the given role is assigned */
	protected boolean isAssigned(Role role) {
		User u = sel_user;	// Avoid NPE
		if(u != null) {
			for(Role r: u.getRoles())
				if(r == role)
					return true;
		}
		return false;
	}

	/** Assign or unassign the specified role */
	protected void setAssigned(Role r, boolean a) {
		User u = sel_user;	// Avoid NPE
		if(u != null) {
			Role[] roles = u.getRoles();
			if(a)
				roles = addRole(roles, r);
			else
				roles = removeRole(roles, r);
			u.setRoles(roles);
		}
	}

	/** Currently selected user */
	protected User sel_user;

	/** Create a new user-role table model */
	public UserRoleModel(Session s) {
		super(s, s.getSonarState().getRoles());
	}

	/** Set the roles for a new user */
	public void setSelectedUser(User u) {
		sel_user = u;
		fireTableDataChanged();
	}

	/** Update the roles for the specified user */
	public void updateUserRoles(User u) {
		if(u == sel_user)
			fireTableDataChanged();
	}

	/** Get the count of rows in the table */
	public int getRowCount() {
		return super.getRowCount() - 1;
	}

	/** Add a role to an array of roles */
	protected Role[] addRole(Role[] roles, Role role) {
		TreeSet<Role> role_set = createProxySet();
		for(Role r: roles)
			role_set.add(r);
		role_set.add(role);
		return role_set.toArray(new Role[0]);
	}

	/** Remove a role from an array of roles */
	protected Role[] removeRole(Role[] roles, Role role) {
		TreeSet<Role> role_set = createProxySet();
		for(Role r: roles)
			role_set.add(r);
		role_set.remove(role);
		return role_set.toArray(new Role[0]);
	}

	/** Check if the user can update user roles */
	protected boolean canUpdateUserRoles() {
		User u = sel_user;	// Avoid NPE
		return (u != null) && namespace.canUpdate(user, new Name(u,
			"roles"));
	}
}
