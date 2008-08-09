/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.toast;

import java.util.Comparator;
import java.util.TreeSet;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumnModel;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for failed controllers
 *
 * @author Douglas Lau
 */
public class FailedControllerModel extends ProxyTableModel<Controller> {

	/** Count of columns in table model */
	static protected final int COLUMN_COUNT = 4;

	/** Name column number */
	static protected final int COL_NAME = 0;

	/** Comm Link column number */
	static protected final int COL_COMM_LINK = 1;

	/** Drop address column number */
	static protected final int COL_DROP = 2;

	/** Error detail */
	static protected final int COL_ERROR = 3;

	/** Create an empty set of proxies */
	protected TreeSet<Controller> createProxySet() {
		return new TreeSet<Controller>(
			new Comparator<Controller>() {
				public int compare(Controller a, Controller b) {
					String la = a.getCommLink().getName();
					String lb = b.getCommLink().getName();
					int c = la.compareTo(lb);
					if(c != 0)
						return c;
					Short aa = Short.valueOf(a.getDrop());
					Short bb = Short.valueOf(b.getDrop());
					return aa.compareTo(bb);
				}
				public boolean equals(Object o) {
					return o == this;
				}
				public int hashCode() {
					return super.hashCode();
				}
			}
		);
	}

	/** Create a new failed controller table model */
	public FailedControllerModel(TypeCache<Controller> c) {
		super(c, true);
		initialize();
	}

	/** Check if a controller is "failed" */
	static protected boolean isFailed(Controller c) {
		return c != null && c.getActive() && !c.getStatus().equals("");
	}

	/** Add a new proxy to the table model */
	public void proxyAdded(Controller proxy) {
		if(isFailed(proxy))
			super.proxyAdded(proxy);
	}

	/** Remove a proxy from the table model */
	public void proxyRemoved(Controller proxy) {
		if(isFailed(proxy))
			super.proxyRemoved(proxy);
	}

	/** Change a proxy in the table model */
	public void proxyChanged(Controller proxy, String attrib) {
		int pre_row, post_row;
		synchronized(proxies) {
			pre_row = preChangeRow(proxy);
			post_row = postChangeRow(proxy);
		}
		if(post_row < 0)
			post_row = pre_row;
		if(pre_row < 0)
			pre_row = post_row;
		if(pre_row >= 0) {
			final int r0 = Math.min(pre_row, post_row);
			final int r1 = Math.max(pre_row, post_row);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					fireTableRowsUpdated(r0, r1);
				}
			});
		}
	}

	/** Add a Controller proxy if it is failed */
	protected int postChangeRow(Controller proxy) {
		if(isFailed(proxy))
			return doProxyAdded(proxy);
		else
			return -1;
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
		Controller c = getProxy(row);
		if(c == null)
			return null;
		switch(column) {
			case COL_NAME:
				return c.getName();
			case COL_COMM_LINK:
				return c.getCommLink().getName();
			case COL_DROP:
				return c.getDrop();
			case COL_ERROR:
				return c.getError();
			default:
				return null;
		}
	}

	/** Check if the specified cell is editable */
	public boolean isCellEditable(int row, int column) {
		return false;
	}

	/** Create the table column model */
	public TableColumnModel createColumnModel() {
		TableColumnModel m = new DefaultTableColumnModel();
		m.addColumn(createColumn(COL_NAME, 90, "Controller"));
		m.addColumn(createColumn(COL_COMM_LINK, 120, "Comm Link"));
		m.addColumn(createColumn(COL_DROP, 60, "Drop"));
		m.addColumn(createColumn(COL_ERROR, 240, "Error Detail"));
		return m;
	}
}
