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
import us.mn.state.dot.sonar.Role;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for IRIS roles
 *
 * @author Douglas Lau
 */
public class RoleModel extends ProxyTableModel<Role> {

	/** Count of columns in table model */
	static protected final int COLUMN_COUNT = 6;

	/** Name column number */
	static protected final int COL_NAME = 0;

	/** Pattern column number */
	static protected final int COL_PATTERN = 1;

	/** Read privilege column number */
	static protected final int COL_PRIV_R = 2;

	/** Write privilege column number */
	static protected final int COL_PRIV_W = 3;

	/** Create privilege column number */
	static protected final int COL_PRIV_C = 4;

	/** Delete privilege column number */
	static protected final int COL_PRIV_D = 5;

	/** Create a new role table model */
	public RoleModel(TypeCache<Role> c, boolean a) {
		super(c, a);
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
		Role r = getProxy(row);
		String v = value.toString();
		switch(column) {
			case COL_NAME:
				cache.createObject(v);
				break;
			case COL_PATTERN:
				r.setPattern(v);
				break;
			case COL_PRIV_R:
				r.setPrivR((Boolean)value);
				break;
			case COL_PRIV_W:
				r.setPrivW((Boolean)value);
				break;
			case COL_PRIV_C:
				r.setPrivC((Boolean)value);
				break;
			case COL_PRIV_D:
				r.setPrivD((Boolean)value);
				break;
		}
	}

	/** Create the table column model */
	public TableColumnModel createColumnModel() {
		TableColumnModel m = new DefaultTableColumnModel();
		m.addColumn(createColumn(COL_NAME, 200, "Role"));
		m.addColumn(createColumn(COL_PATTERN, 420, "Pattern"));
		m.addColumn(createColumn(COL_PRIV_R, 80, "Read"));
		m.addColumn(createColumn(COL_PRIV_W, 80, "Write"));
		m.addColumn(createColumn(COL_PRIV_C, 80, "Create"));
		m.addColumn(createColumn(COL_PRIV_D, 80, "Delete"));
		return m;
	}
}
