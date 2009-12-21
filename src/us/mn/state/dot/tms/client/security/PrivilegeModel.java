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

import java.util.HashMap;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumnModel;
import us.mn.state.dot.sonar.Name;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.sonar.Privilege;
import us.mn.state.dot.sonar.Role;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for IRIS privileges
 *
 * @author Douglas Lau
 */
public class PrivilegeModel extends ProxyTableModel<Privilege> {

	/** Count of columns in table model */
	static protected final int COLUMN_COUNT = 5;

	/** Pattern column number */
	static protected final int COL_PATTERN = 0;

	/** Read privilege column number */
	static protected final int COL_PRIV_R = 1;

	/** Write privilege column number */
	static protected final int COL_PRIV_W = 2;

	/** Create privilege column number */
	static protected final int COL_PRIV_C = 3;

	/** Delete privilege column number */
	static protected final int COL_PRIV_D = 4;

	/** Create the table column model */
	public TableColumnModel createColumnModel() {
		TableColumnModel m = new DefaultTableColumnModel();
		m.addColumn(createColumn(COL_PATTERN, 420, "Pattern"));
		m.addColumn(createColumn(COL_PRIV_R, 80, "Read"));
		m.addColumn(createColumn(COL_PRIV_W, 80, "Write"));
		m.addColumn(createColumn(COL_PRIV_C, 80, "Create"));
		m.addColumn(createColumn(COL_PRIV_D, 80, "Delete"));
		return m;
	}

	/** Role associated with privileges */
	protected final Role role;

	/** SONAR namespace */
	protected final Namespace namespace;

	/** SONAR user */
	protected final User user;

	/** Create a new privilege table model */
	public PrivilegeModel(TypeCache<Privilege> c, Role r, Namespace ns,
		User u)
	{
		super(c);
		role = r;
		namespace = ns;
		user = u;
		initialize();
	}

	/** Add a new proxy to the table model */
	protected int doProxyAdded(Privilege proxy) {
		if(proxy.getRole() == role)
			return super.doProxyAdded(proxy);
		else
			return -1;
	}

	/** Get the count of columns in the table */
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	/** Get the value at the specified cell */
	public Object getValueAt(int row, int column) {
		Privilege r = getProxy(row);
		if(r == null)
			return null;
		switch(column) {
			case COL_PATTERN:
				return r.getPattern();
			case COL_PRIV_R:
				return r.getPrivR();
			case COL_PRIV_W:
				return r.getPrivW();
			case COL_PRIV_C:
				return r.getPrivC();
			case COL_PRIV_D:
				return r.getPrivD();
		}
		return null;
	}

	/** Get the class of the specified column */
	public Class getColumnClass(int column) {
		switch(column) {
			case COL_PRIV_R:
			case COL_PRIV_W:
			case COL_PRIV_C:
			case COL_PRIV_D:
				return Boolean.class;
			default:
				return String.class;
		}
	}

	/** Check if the specified cell is editable */
	public boolean isCellEditable(int row, int column) {
		Privilege p = getProxy(row);
		if(p != null)
			return canUpdate(p);
		else
			return column == COL_PATTERN && canAdd();
	}

	/** Set the value at the specified cell */
	public void setValueAt(Object value, int row, int column) {
		Privilege p = getProxy(row);
		String v = value.toString();
		switch(column) {
			case COL_PATTERN:
				if(p == null)
					createPrivilege(v);
				else
					p.setPattern(v);
				break;
			case COL_PRIV_R:
				p.setPrivR((Boolean)value);
				break;
			case COL_PRIV_W:
				p.setPrivW((Boolean)value);
				break;
			case COL_PRIV_C:
				p.setPrivC((Boolean)value);
				break;
			case COL_PRIV_D:
				p.setPrivD((Boolean)value);
				break;
		}
	}

	/** Create a new privilege */
	protected void createPrivilege(String p) {
		String name = createUniqueName();
		if(name != null) {
			HashMap<String, Object> attrs =
				new HashMap<String, Object>();
			attrs.put("role", role);
			attrs.put("pattern", p);
			cache.createObject(name, attrs);
		}
	}

	/** Create a unique privilege name */
	protected String createUniqueName() {
		for(int uid = 1; uid <= 9999; uid++) {
			String n = "PRV_" + uid;
			if(cache.lookupObject(n) == null)
				return n;
		}
		assert false;
		return null;
	}

	/** Check if the user can add a privilege */
	public boolean canAdd() {
		return namespace.canAdd(user, new Name(Privilege.SONAR_TYPE,
			"name"));
	}

	/** Check if the user can update */
	public boolean canUpdate(Privilege p) {
		return namespace.canUpdate(user, new Name(Privilege.SONAR_TYPE,
			p.getName()));
	}

	/** Check if the user can remove a privilege */
	public boolean canRemove(Privilege p) {
		if(p == null)
			return false;
		return namespace.canRemove(user, new Name(Privilege.SONAR_TYPE,
			p.getName()));
	}
}
