/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2024  Minnesota Department of Transportation
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

import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableRowSorter;
import us.mn.state.dot.tms.AccessLevel;
import us.mn.state.dot.tms.Permission;
import us.mn.state.dot.tms.Role;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for role permissions.
 *
 * @author Douglas Lau
 */
public class PermissionModel extends ProxyTableModel<Permission> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<Permission> descriptor(Session s) {
		return new ProxyDescriptor<Permission>(
			s.getSonarState().getPermissions(),
			false,	/* has_properties */
			true,	/* has_create_delete */
			false	/* has_name */
		);
	}

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<Permission>> createColumns() {
		ArrayList<ProxyColumn<Permission>> cols =
			new ArrayList<ProxyColumn<Permission>>(3);
		cols.add(new ProxyColumn<Permission>("permission.base.resource",
			120)
		{
			public Object getValueAt(Permission p) {
				return p.getBaseResource();
			}
		});
		cols.add(new ProxyColumn<Permission>("permission.hashtag", 140) {
			public Object getValueAt(Permission p) {
				return p.getHashtag();
			}
			public boolean isEditable(Permission p) {
				return canWrite(p);
			}
			public void setValueAt(Permission p, Object value) {
				String v = value.toString().trim();
				p.setHashtag((v != null) ? v : null);
			}
		});
		cols.add(new ProxyColumn<Permission>("permission.access", 80) {
			public Object getValueAt(Permission p) {
				return AccessLevel.fromOrdinal(
					p.getAccessLevel());
			}
			public boolean isEditable(Permission p) {
				return canWrite(p);
			}
			public void setValueAt(Permission p, Object value) {
				if (value instanceof AccessLevel) {
					AccessLevel al = (AccessLevel) value;
					p.setAccessLevel(al.ordinal());
				}
			}
			protected TableCellEditor createCellEditor() {
				JComboBox<AccessLevel> cbx = new JComboBox
					<AccessLevel>(AccessLevel.VALID_VALUES);
				return new DefaultCellEditor(cbx);
			}
		});
		return cols;
	}

	/** Role associated with permissions */
	private final Role role;

	/** Create a new permission table model */
	public PermissionModel(Session s, Role r) {
		super(s, descriptor(s), 16);
		role = r;
	}

	/** Check if a proxy is included in the list */
	@Override
	protected boolean check(Permission proxy) {
		return proxy.getRole() == role;
	}

	/** Get a table row sorter */
	@Override
	public RowSorter<ProxyTableModel<Permission>> createSorter() {
		TableRowSorter<ProxyTableModel<Permission>> sorter =
			new TableRowSorter<ProxyTableModel<Permission>>(this)
		{
			@Override public boolean isSortable(int c) {
				return c == 0;
			}
		};
		sorter.setSortsOnUpdates(true);
		ArrayList<RowSorter.SortKey> keys =
			new ArrayList<RowSorter.SortKey>();
		keys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
		sorter.setSortKeys(keys);
		return sorter;
	}

	/** Create an object with the given name.
	 * @param br Base resource. */
	@Override
	public void createObject(String br) {
		String name = createUniqueName();
		if (name != null) {
			HashMap<String, Object> attrs =
				new HashMap<String, Object>();
			attrs.put("role", role);
			attrs.put("base_resource", br);
			descriptor.cache.createObject(name, attrs);
		}
	}

	/** Create a unique permission name */
	private String createUniqueName() {
		for (int uid = 1; uid <= 9999; uid++) {
			String n = "prm_" + uid;
			if (descriptor.cache.lookupObject(n) == null)
				return n;
		}
		assert false;
		return null;
	}
}
