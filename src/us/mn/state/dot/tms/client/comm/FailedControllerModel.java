/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2014  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.comm;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.ControllerHelper;
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
	@Override
	protected ArrayList<ProxyColumn<Controller>> createColumns() {
		ArrayList<ProxyColumn<Controller>> cols =
			new ArrayList<ProxyColumn<Controller>>(5);
		cols.add(new ProxyColumn<Controller>("controller", 90) {
			public Object getValueAt(Controller c) {
				return c.getName();
			}
		});
		cols.add(new ProxyColumn<Controller>("location", 200) {
			public Object getValueAt(Controller c) {
				return GeoLocHelper.getDescription(
					c.getCabinet().getGeoLoc());
			}
		});
		cols.add(new ProxyColumn<Controller>("comm.link", 120) {
			public Object getValueAt(Controller c) {
				return c.getCommLink().getName();
			}
		});
		cols.add(new ProxyColumn<Controller>("controller.drop", 60,
			Short.class)
		{
			public Object getValueAt(Controller c) {
				return c.getDrop();
			}
		});
		cols.add(new ProxyColumn<Controller>("controller.fail", 240,
			Long.class)
		{
			public Object getValueAt(Controller c) {
				return c.getFailTime();
			}
			protected TableCellRenderer createCellRenderer() {
				return new FailTimeCellRenderer();
			}
		});
		return cols;
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
		super(s, s.getSonarState().getConCache().getControllers(),
		      false,	/* has_properties */
		      false,	/* has_create */
		      false);	/* has_delete */
	}

	/** Check if a proxy is included in the list */
	@Override
	protected boolean check(Controller proxy) {
		return ControllerHelper.isFailed(proxy);
	}

	/** Check if an attribute change is interesting */
	@Override
	protected boolean checkAttributeChange(String attr) {
		return !("status".equals(attr));
	}
}
