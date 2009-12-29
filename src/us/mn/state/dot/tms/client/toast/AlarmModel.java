/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2009  Minnesota Department of Transportation
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

import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import us.mn.state.dot.tms.Alarm;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for alarms
 *
 * @author Douglas Lau
 */
public class AlarmModel extends ProxyTableModel<Alarm> {

	/** Create the columns in the model */
	protected ProxyColumn[] createColumns() {
	    // NOTE: half-indent to declare array
	    return new ProxyColumn[] {
		new ProxyColumn<Alarm>("Alarm", 80) {
			public Object getValueAt(Alarm a) {
				return a.getName();
			}
			public boolean isEditable(Alarm a) {
				return (a == null) && canAdd();
			}
			public void setValueAt(Alarm a, Object value) {
				String v = value.toString().trim();
				if(v.length() > 0)
					cache.createObject(v);
			}
		},
		new ProxyColumn<Alarm>("Description", 200) {
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
		},
		new ProxyColumn<Alarm>("State", 60) {
			public Object getValueAt(Alarm a) {
				return a.getState();
			}
			protected TableCellRenderer createCellRenderer() {
				return new StateCellRenderer();
			}
		},
		new ProxyColumn<Alarm>("Controller", 100) {
			public Object getValueAt(Alarm a) {
				return a.getController();
			}
		},
		new ProxyColumn<Alarm>("Pin", 50) {
			public Object getValueAt(Alarm a) {
				return a.getPin();
			}
		}
	    };
	}

	/** Create a new alarm table model */
	public AlarmModel(Session s) {
		super(s, s.getSonarState().getAlarms());
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
			if(!isSelected) {
				label.setBackground(null);
				label.setForeground(null);
			}
			if(value instanceof Boolean) {
				Boolean t = (Boolean)value;
				if(t) {
					if(!isSelected) {
						label.setBackground(Color.RED);
						label.setForeground(
							Color.WHITE);
					}
					label.setText("triggered");
				} else
					label.setText("clear");
			}
			return label;
		}
	}

	/** Get the SONAR type name */
	protected String getSonarType() {
		return Alarm.SONAR_TYPE;
	}
}
