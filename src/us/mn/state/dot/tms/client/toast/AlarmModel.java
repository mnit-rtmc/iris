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

import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Alarm;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for alarms
 *
 * @author Douglas Lau
 */
public class AlarmModel extends ProxyTableModel<Alarm> {

	/** Count of columns in table model */
	static protected final int COLUMN_COUNT = 5;

	/** Name column number */
	static protected final int COL_NAME = 0;

	/** Description column number */
	static protected final int COL_DESCRIPTION = 1;

	/** Triggered state column number */
	static protected final int COL_STATE = 2;

	/** Controller column number */
	static protected final int COL_CONTROLLER = 3;

	/** Pin column number */
	static protected final int COL_PIN = 4;

	/** Create a new alarm table model */
	public AlarmModel(TypeCache<Alarm> c) {
		super(c, true);
		initialize();
	}

	/** Get the count of columns in the table */
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	/** Get the value at the specified cell */
	public Object getValueAt(int row, int column) {
		Alarm a = getProxy(row);
		if(a == null)
			return null;
		switch(column) {
			case COL_NAME:
				return a.getName();
			case COL_DESCRIPTION:
				return a.getDescription();
			case COL_STATE:
				return a.getState();
			case COL_CONTROLLER:
				return a.getController();
			case COL_PIN:
				return a.getPin();
			default:
				return null;
		}
	}

	/** Check if the specified cell is editable */
	public boolean isCellEditable(int row, int column) {
		if(isLastRow(row))
			return column == COL_NAME;
		else
			return column == COL_DESCRIPTION;
	}

	/** Set the value at the specified cell */
	public void setValueAt(Object value, int row, int column) {
		String v = value.toString().trim();
		Alarm a = getProxy(row);
		switch(column) {
			case COL_NAME:
				if(v.length() > 0)
					cache.createObject(v);
				break;
			case COL_DESCRIPTION:
				a.setDescription(v);
				break;
		}
	}

	/** Create the table column model */
	public TableColumnModel createColumnModel() {
		TableColumnModel m = new DefaultTableColumnModel();
		m.addColumn(createColumn(COL_NAME, 80, "Alarm"));
		m.addColumn(createColumn(COL_DESCRIPTION, 200, "Description"));
		m.addColumn(createStateColumn());
		m.addColumn(createColumn(COL_CONTROLLER, 100, "Controller"));
		m.addColumn(createColumn(COL_PIN, 50, "Pin"));
		return m;
	}

	/** Create the state column */
	protected TableColumn createStateColumn() {
		TableColumn c = new TableColumn(COL_STATE, 60);
		c.setHeaderValue("State");
		c.setCellRenderer(new StateCellRenderer());
		return c;
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
}
