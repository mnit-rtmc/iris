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
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumnModel;
import us.mn.state.dot.sonar.Name;
import us.mn.state.dot.sonar.Role;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for roles assigned to IRIS users
 *
 * @author Douglas Lau
 */
public class UserRoleModel extends ProxyTableModel<Role> {

	/** Count of columns in table model */
	static protected final int COLUMN_COUNT = 2;

	/** Name column number */
	static protected final int COL_NAME = 0;

	/** Assigned column number */
	static protected final int COL_ASSIGNED = 1;

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

	/** Get the count of columns in the table */
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	/** Get the count of rows in the table */
	public int getRowCount() {
		synchronized(proxies) {
			return proxies.size();
		}
	}

	/** Get the value at the specified cell */
	public Object getValueAt(int row, int column) {
		Role role = getProxy(row);
		if(role == null)
			return "";
		if(column == COL_NAME)
			return role.getName();
		User u = sel_user;	// Avoid NPE
		if(u != null) {
			for(Role r: u.getRoles())
				if(r == role)
					return true;
		}
		return false;
	}

	/** Get the class of the specified column */
	public Class getColumnClass(int column) {
		if(column == COL_ASSIGNED)
			return Boolean.class;
		else
			return String.class;
	}

	/** Check if the specified cell is editable */
	public boolean isCellEditable(int row, int column) {
		return (column == COL_ASSIGNED) && (sel_user != null) &&
			canUpdate();
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

	/** Set the value at the specified cell */
	public void setValueAt(Object value, int row, int column) {
		User u = sel_user;	// Avoid NPE
		if((column != COL_ASSIGNED) || (u == null))
			return;
		Boolean v = (Boolean)value;
		Role role = getProxy(row);
		if(role != null) {
			Role[] roles = u.getRoles();
			if(v)
				roles = addRole(roles, role);
			else
				roles = removeRole(roles, role);
			u.setRoles(roles);
		}
	}

	/** Create the table column model */
	public TableColumnModel createColumnModel() {
		TableColumnModel m = new DefaultTableColumnModel();
		m.addColumn(createColumn(COL_NAME, 120, "Role"));
		m.addColumn(createColumn(COL_ASSIGNED, 80, "Assigned"));
		return m;
	}

	/** Check if the user can set user roles */
	public boolean canUpdate() {
		return namespace.canUpdate(user, new Name(User.SONAR_TYPE,
			"roles"));
	}
}
