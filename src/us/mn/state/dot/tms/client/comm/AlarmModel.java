/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2016  Minnesota Department of Transportation
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

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import us.mn.state.dot.tms.Alarm;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;
import us.mn.state.dot.tms.utils.I18N;

/**
 * Table model for alarms
 *
 * @author Douglas Lau
 */
public class AlarmModel extends ProxyTableModel<Alarm> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<Alarm> descriptor(Session s) {
		return new ProxyDescriptor<Alarm>(
			s.getSonarState().getAlarms(), false
		);
	}

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<Alarm>> createColumns() {
		ArrayList<ProxyColumn<Alarm>> cols =
			new ArrayList<ProxyColumn<Alarm>>(6);
		cols.add(new ProxyColumn<Alarm>("alarm", 80) {
			public Object getValueAt(Alarm a) {
				return a.getName();
			}
		});
		cols.add(new ProxyColumn<Alarm>("device.description", 200) {
			public Object getValueAt(Alarm a) {
				return a.getDescription();
			}
			public boolean isEditable(Alarm a) {
				return canUpdate(a);
			}
			public void setValueAt(Alarm a, Object value) {
				String v = value.toString().trim();
				a.setDescription(v);
			}
		});
		cols.add(new ProxyColumn<Alarm>("alarm.state", 60) {
			public Object getValueAt(Alarm a) {
				return a.getState();
			}
			protected TableCellRenderer createCellRenderer() {
				return new StateCellRenderer();
			}
		});
		cols.add(new ProxyColumn<Alarm>("alarm.trigger_time", 200) {
			public Object getValueAt(Alarm a) {
				Long tt = a.getTriggerTime();
				if (tt != null)
					return new Date(tt);
				else
					return "";
			}
		});
		cols.add(new ProxyColumn<Alarm>("controller", 100) {
			public Object getValueAt(Alarm a) {
				return a.getController();
			}
		});
		cols.add(new ProxyColumn<Alarm>("controller.pin", 50) {
			public Object getValueAt(Alarm a) {
				return a.getPin();
			}
		});
		return cols;
	}

	/** Create a new alarm table model */
	public AlarmModel(Session s) {
		super(s, descriptor(s),
		      true,	/* has_create_delete */
		      true);	/* has_name */
	}

	/** Renderer for state in a table cell */
	protected class StateCellRenderer extends DefaultTableCellRenderer {
		public Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean hasFocus,
			int row, int column)
		{
			JLabel label =
				(JLabel)super.getTableCellRendererComponent(
				table, "", isSelected, hasFocus, row,
				column);
			if (!isSelected) {
				label.setBackground(null);
				label.setForeground(null);
			}
			if (value instanceof Boolean) {
				Boolean t = (Boolean)value;
				if (t) {
					if (!isSelected) {
						label.setBackground(Color.RED);
						label.setForeground(
							Color.WHITE);
					}
					label.setText(I18N.get(
						"alarm.triggered"));
				} else
					label.setText(I18N.get("alarm.clear"));
			}
			return label;
		}
	}
}
