/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2010  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.system;

import java.awt.Component;
import javax.swing.AbstractCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import us.mn.state.dot.sonar.Role;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyListModel;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;
import us.mn.state.dot.tms.client.toast.WrapperComboBoxModel;

/**
 * Table model for IRIS users
 *
 * @author Douglas Lau
 */
public class UserModel extends ProxyTableModel<User> {

	/** Role list model */
	protected final ProxyListModel<Role> r_list;

	/** Role combo model */
	protected final WrapperComboBoxModel r_model;

	/** Role combo box */
	protected final JComboBox r_combo;

	/** Create the columns in the model */
	protected ProxyColumn[] createColumns() {
	    // NOTE: half-indent to declare array
	    return new ProxyColumn[] {
		new ProxyColumn<User>("User", 100) {
			public Object getValueAt(User u) {
				return u.getName();
			}
			public boolean isEditable(User u) {
				return u == null && canAdd();
			}
			public void setValueAt(User u, Object value) {
				String v = value.toString().trim();
				if(v.length() > 0)
					cache.createObject(v);
			}
		},
		new ProxyColumn<User>("Full Name", 180) {
			public Object getValueAt(User u) {
				return u.getFullName();
			}
			public boolean isEditable(User u) {
				return canUpdate(u);
			}
			public void setValueAt(User u, Object value) {
				u.setFullName(value.toString().trim());
			}
		},
		new ProxyColumn<User>("Dn", 320) {
			public Object getValueAt(User u) {
				return u.getDn();
			}
			public boolean isEditable(User u) {
				return canUpdate(u);
			}
			public void setValueAt(User u, Object value) {
				u.setDn(value.toString().trim());
			}
		},
		new ProxyColumn<User>("Role", 160) {
			public Object getValueAt(User u) {
				return u.getRole();
			}
			public boolean isEditable(User u) {
				return canUpdate(u);
			}
			public void setValueAt(User u, Object value) {
				if(value instanceof Role)
					u.setRole((Role)value);
				else
					u.setRole(null);
			}
			protected TableCellEditor createCellEditor() {
				return new RoleCellEditor();
			}
		},
		new ProxyColumn<User>("Enabled", 60, Boolean.class) {
			public Object getValueAt(User u) {
				return u.getEnabled();
			}
			public boolean isEditable(User u) {
				return canUpdate(u);
			}
			public void setValueAt(User u, Object value) {
				if(value instanceof Boolean)
					u.setEnabled((Boolean)value);
			}
		}
	    };
	}

	/** Editor for roles in a table cell */
	protected class RoleCellEditor extends AbstractCellEditor
		implements TableCellEditor
	{
		public Component getTableCellEditorComponent(JTable table,
			Object value, boolean isSelected, int row, int column)
		{
			r_combo.setSelectedItem(value);
			return r_combo;
		}
		public Object getCellEditorValue() {
			return r_combo.getSelectedItem();
		}
	}

	/** Create a new user table model */
	public UserModel(Session s) {
		super(s, s.getSonarState().getUsers());
		r_list = new ProxyListModel<Role>(s.getSonarState().getRoles());
		r_list.initialize();
		r_model = new WrapperComboBoxModel(r_list, true);
		r_combo = new JComboBox(r_model);
	}

	/** Dispose of the user model */
	public void dispose() {
		r_list.dispose();
		super.dispose();
	}

	/** Get the SONAR type name */
	protected String getSonarType() {
		return User.SONAR_TYPE;
	}
}
