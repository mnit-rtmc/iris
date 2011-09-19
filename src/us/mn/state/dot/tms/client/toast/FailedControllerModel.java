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
import java.util.Date;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel2;

/**
 * Table model for failed controllers
 *
 * @author Douglas Lau
 */
public class FailedControllerModel extends ProxyTableModel2<Controller> {

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

	/** Create a new failed controller table model */
	public FailedControllerModel(Session s) {
		super(s, s.getSonarState().getConCache().getControllers());
	}

	/** Change a proxy in the table model */
	protected void proxyChangedSlow(Controller proxy, String attrib) {
		if(!"status".equals(attrib))
			super.proxyChangedSlow(proxy, attrib);
	}
}
