/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2011  Minnesota Department of Transportation
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

import java.awt.Component;
import java.util.Comparator;
import java.util.Date;
import java.util.TreeSet;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.ControllerHelper;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for failed controllers
 *
 * @author Douglas Lau
 */
public class FailedControllerModel extends ProxyTableModel<Controller> {

	/** Create the columns in the model */
	protected ProxyColumn[] createColumns() {
	    // NOTE: half-indent to declare array
	    return new ProxyColumn[] {
		new ProxyColumn<Controller>("Controller", 90) {
			public Object getValueAt(Controller c) {
				return c.getName();
			}
		},
		new ProxyColumn<Controller>("Location", 200) {
			public Object getValueAt(Controller c) {
				return GeoLocHelper.getDescription(
					c.getCabinet().getGeoLoc());
			}
		},
		new ProxyColumn<Controller>("Comm Link", 120) {
			public Object getValueAt(Controller c) {
				return c.getCommLink().getName();
			}
		},
		new ProxyColumn<Controller>("Drop", 60, Short.class) {
			public Object getValueAt(Controller c) {
				return c.getDrop();
			}
		},
		new ProxyColumn<Controller>("Fail Time", 240, Long.class) {
			public Object getValueAt(Controller c) {
				return c.getFailTime();
			}
			protected TableCellRenderer createCellRenderer() {
				return new FailTimeCellRenderer();
			}
		}
	    };
	}

	/** Renderer for fail time in a table cell */
	protected class FailTimeCellRenderer extends DefaultTableCellRenderer {
		public Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean hasFocus,
			int row, int column)
		{
			JLabel label =
				(JLabel)super.getTableCellRendererComponent(
				table, "", isSelected, hasFocus, row,
				column);
			if(value instanceof Long) {
				Long ft = (Long)value;
				label.setText(new Date(ft).toString());
			} else
				label.setText("");
			return label;
		}
	}

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
	public FailedControllerModel(Session s) {
		super(s, s.getSonarState().getConCache().getControllers());
	}

	/** Add a Controller proxy if it is failed */
	protected int doProxyAdded(Controller proxy) {
		if(ControllerHelper.isFailed(proxy))
			return super.doProxyAdded(proxy);
		else
			return -1;
	}

	/** Get the count of rows in the table */
	public int getRowCount() {
		return super.getRowCount() - 1;
	}
}
