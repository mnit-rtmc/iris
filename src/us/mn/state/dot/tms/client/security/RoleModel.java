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

import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumnModel;
import us.mn.state.dot.sonar.Name;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.sonar.Role;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for IRIS roles
 *
 * @author Douglas Lau
 */
public class RoleModel extends ProxyTableModel<Role> {

	/** Count of columns in table model */
	static protected final int COLUMN_COUNT = 2;

	/** Name column number */
	static protected final int COL_NAME = 0;

	/** Enabled column number */
	static protected final int COL_ENABLED = 1;

	/** Create the table column model */
	static public TableColumnModel createColumnModel() {
		TableColumnModel m = new DefaultTableColumnModel();
		m.addColumn(createColumn(COL_NAME, 160, "Name"));
		m.addColumn(createColumn(COL_ENABLED, 60, "Enabled"));
		return m;
	}

	/** SONAR namespace */
	protected final Namespace namespace;

	/** SONAR user */
	protected final User user;

	/** Create a new role table model */
	public RoleModel(TypeCache<Role> c, Namespace ns, User u) {
		super(c);
		namespace = ns;
		user = u;
		initialize();
	}

	/** Get the count of columns in the table */
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	/** Get the value at the specified cell */
	public Object getValueAt(int row, int column) {
		Role r = getProxy(row);
		if(r == null)
			return null;
		switch(column) {
			case COL_NAME:
				return r.getName();
			case COL_ENABLED:
				return r.getEnabled();
		}
		return null;
	}

	/** Get the class of the specified column */
	public Class getColumnClass(int column) {
		switch(column) {
			case COL_ENABLED:
				return Boolean.class;
			default:
				return String.class;
		}
	}

	/** Check if the specified cell is editable */
	public boolean isCellEditable(int row, int column) {
		Role r = getProxy(row);
		if(r != null)
			return column != COL_NAME && canUpdate(r);
		else
			return column == COL_NAME && canAdd();
	}

	/** Set the value at the specified cell */
	public void setValueAt(Object value, int row, int column) {
		Role r = getProxy(row);
		String v = value.toString();
		switch(column) {
			case COL_NAME:
				cache.createObject(v);
				break;
			case COL_ENABLED:
				r.setEnabled((Boolean)value);
				break;
		}
	}

	/** Check if the user can add a role */
	public boolean canAdd() {
		return namespace.canAdd(user, new Name(Role.SONAR_TYPE,"name"));
	}

	/** Check if the user can update */
	public boolean canUpdate(Role r) {
		return namespace.canUpdate(user, new Name(Role.SONAR_TYPE,
			r.getName()));
	}
}
