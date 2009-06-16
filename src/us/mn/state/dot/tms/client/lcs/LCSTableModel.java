/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.lcs;

import java.util.HashMap;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.LCSArray;
import us.mn.state.dot.tms.LCS;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for LCS within an array.
 *
 * @author Douglas Lau
 */
public class LCSTableModel extends ProxyTableModel<LCS> {

	/** Create a SONAR name to check for allowed updates */
	static protected String createNamespaceString(String name) {
		return LCS.SONAR_TYPE + "/" + name;
	}

	/** Count of columns in table model */
	static protected final int COLUMN_COUNT = 2;

	/** Message line column number */
	static protected final int COL_LANE = 0;

	/** Message text column number */
	static protected final int COL_NAME = 1;

	/** Create a new table column */
	static protected TableColumn createColumn(int column, int width,
		String header)
	{
		TableColumn c = new TableColumn(column, width);
		c.setHeaderValue(header);
		return c;
	}

	/** Add a new proxy to the table model */
	protected int doProxyAdded(LCS proxy) {
		if(proxy.getArray() == lcs_array)
			return super.doProxyAdded(proxy);
		else
			return -1;
	}

	/** LCS array */
	protected final LCSArray lcs_array;

	/** SONAR User for permission checks */
	protected final User user;

	/** Create a new LCS table model */
	public LCSTableModel(LCSArray la, TypeCache<LCS> c, User u) {
		super(c);
		lcs_array = la;
		user = u;
		initialize();
	}

	/** Get the count of columns in the table */
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	/** Get the value at the specified cell */
	public Object getValueAt(int row, int column) {
		LCS lcs = getProxy(row);
		if(lcs != null)
			return getValue(lcs, column);
		else
			return null;
	}

	/** Get the value of an LCS column */
	protected Object getValue(LCS lcs, int column) {
		switch(column) {
		case COL_LANE:
			return lcs.getLane();
		case COL_NAME:
			return lcs.getName();
		default:
			return null;
		}
	}

	/** Get the column class */
	public Class getColumnClass(int column) {
		if(column == COL_NAME)
			return String.class;
		else
			return Integer.class;
	}

	/** Check if the specified cell is editable */
	public boolean isCellEditable(int row, int column) {
		synchronized(proxies) {
			if(row == proxies.size() && column == COL_NAME)
				return canAddLCS("arbitrary_name");
		}
		return false;
	}

	/** Check if the user can add the named LCS */
	public boolean canAddLCS(String name) {
		return user.canAdd(createNamespaceString(name));
	}

	/** Set the value at the specified cell */
	public void setValueAt(Object value, int row, int column) {
		assert column == COL_NAME;
		addRow(value, row);
	}

	/** Add a row to the sign text table */
	protected void addRow(Object value, int row) {
		String name = value.toString();
		if(name.length() > 0)
			createLCS(name, row + 1);
	}

	/** Create a new LCS */
	protected void createLCS(String name, int lane) {
		if(canAddLCS(name)) {
			HashMap<String, Object> attrs =
				new HashMap<String, Object>();
			attrs.put("lcsArray", lcs_array);
			attrs.put("lane", new Integer(lane));
			cache.createObject(name, attrs);
		}
	}

	/** Create the table column model */
	static public TableColumnModel createColumnModel() {
		TableColumnModel m = new DefaultTableColumnModel();
		m.addColumn(createColumn(COL_LANE, 36, "Lane"));
		m.addColumn(createColumn(COL_NAME, 140, "LCS"));
		return m;
	}
}
