/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2008  Minnesota Department of Transportation
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

import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumnModel;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for IRIS users
 *
 * @author Douglas Lau
 */
public class UserModel extends ProxyTableModel<User> {

	/** Count of columns in table model */
	static protected final int COLUMN_COUNT = 3;

	/** Name column number */
	static protected final int COL_NAME = 0;

	/** Full name column number */
	static protected final int COL_FULL_NAME = 1;

	/** Distinguished name column number */
	static protected final int COL_DN = 2;

	/** User role model */
	protected final UserRoleModel rmodel;

	/** Create a new user table model */
	public UserModel(TypeCache<User> c, boolean a, UserRoleModel r) {
		super(c, a);
		rmodel = r;
		initialize();
	}

	/** Change a user in the table model */
	protected void proxyChangedSlow(User proxy, String attrib) {
		super.proxyChangedSlow(proxy, attrib);
		if(attrib.equals("roles"))
			rmodel.updateUserRoles(proxy);
	}

	/** Get the count of columns in the table */
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	/** Get the value at the specified cell */
	public Object getValueAt(int row, int column) {
		User u = getProxy(row);
		if(u == null)
			return null;
		switch(column) {
			case COL_NAME:
				return u.getName();
			case COL_FULL_NAME:
				return u.getFullName();
			case COL_DN:
				return u.getDn();
		}
		return null;
	}

	/** Check if the specified cell is editable */
	public boolean isCellEditable(int row, int column) {
		if(!admin)
			return false;
		synchronized(proxies) {
			if(row == proxies.size())
				return column == COL_NAME;
		}
		if(column == COL_NAME)
			return false;
		return true;
	}

	/** Set the value at the specified cell */
	public void setValueAt(Object value, int row, int column) {
		User u = getProxy(row);
		String v = value.toString();
		switch(column) {
			case COL_NAME:
				cache.createObject(v);
				break;
			case COL_FULL_NAME:
				u.setFullName(v);
				break;
			case COL_DN:
				u.setDn(v);
				break;
		}
	}

	/** Create the table column model */
	public TableColumnModel createColumnModel() {
		TableColumnModel m = new DefaultTableColumnModel();
		m.addColumn(createColumn(COL_NAME, 100, "User"));
		m.addColumn(createColumn(COL_FULL_NAME, 180, "Full Name"));
		m.addColumn(createColumn(COL_DN, 420, "Dn"));
		return m;
	}
}
