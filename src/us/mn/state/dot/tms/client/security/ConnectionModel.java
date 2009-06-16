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
import us.mn.state.dot.sonar.Connection;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for IRIS connections
 *
 * @author Douglas Lau
 */
public class ConnectionModel extends ProxyTableModel<Connection> {

	/** Count of columns in table model */
	static protected final int COLUMN_COUNT = 2;

	/** Name column number */
	static protected final int COL_NAME = 0;

	/** User column number */
	static protected final int COL_USER = 1;

	/** Create a new connection table model */
	public ConnectionModel(TypeCache<Connection> c) {
		super(c);
		initialize();
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
		Connection c = getProxy(row);
		if(c == null)
			return null;
		switch(column) {
			case COL_NAME:
				return c.getName();
			case COL_USER:
				User u = c.getUser();
				if(u != null)
					return u.getName();
				else
					return null;
		}
		return null;
	}

	/** Check if the specified cell is editable */
	public boolean isCellEditable(int row, int column) {
		return false;
	}

	/** Set the value at the specified cell */
	public void setValueAt(Object value, int row, int column) {
		// Not editable
	}

	/** Create the table column model */
	public TableColumnModel createColumnModel() {
		TableColumnModel m = new DefaultTableColumnModel();
		m.addColumn(createColumn(COL_NAME, 100, "Host:Port"));
		m.addColumn(createColumn(COL_USER, 180, "User"));
		return m;
	}
}
