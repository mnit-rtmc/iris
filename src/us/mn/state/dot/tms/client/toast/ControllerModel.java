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
import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeSet;
import javax.swing.AbstractCellEditor;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.CommLink;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for controllers
 *
 * @author Douglas Lau
 */
public class ControllerModel extends ProxyTableModel<Controller> {

	/** Color to display inactive controllers */
	static protected final Color COLOR_INACTIVE = new Color(0, 0, 0, 32);

	/** Color to display available devices */
	static protected final Color COLOR_AVAILABLE = new Color(96, 96, 255);

	/** Count of columns in table model */
	static protected final int COLUMN_COUNT = 7;

	/** Name column number */
	static protected final int COL_NAME = 0;

	/** Location column number */
	static protected final int COL_LOCATION = 1;

	/** Drop address column number */
	static protected final int COL_DROP = 2;

	/** Active column number */
	static protected final int COL_ACTIVE = 3;

	/** Communication status */
	static protected final int COL_STATUS = 4;

	/** Error detail */
	static protected final int COL_ERROR = 5;

	/** Firmware version */
	static protected final int COL_VERSION = 6;

	/** Comm link to match controllers */
	protected final CommLink comm_link;

	/** Create an empty set of proxies */
	protected TreeSet<Controller> createProxySet() {
		return new TreeSet<Controller>(
			new Comparator<Controller>() {
				public int compare(Controller a, Controller b) {
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

	/** Create a new controller table model */
	public ControllerModel(TypeCache<Controller> c, CommLink cl) {
		super(c, true);
		comm_link = cl;
		initialize();
	}

	/** Add a new proxy to the table model */
	protected int doProxyAdded(Controller proxy) {
		if(proxy.getCommLink() == comm_link)
			return super.doProxyAdded(proxy);
		else
			return -1;
	}

	/** Get the count of columns in the table */
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	/** Get the class of the specified column */
	public Class getColumnClass(int column) {
		if(column == COL_ACTIVE)
			return Boolean.class;
		else
			return String.class;
	}

	/** Get the value at the specified cell */
	public Object getValueAt(int row, int column) {
		Controller c = getProxy(row);
		if(c == null)
			return null;
		switch(column) {
			case COL_NAME:
				return c.getName();
			case COL_LOCATION:
				return GeoLocHelper.getDescription(
					c.getCabinet().getGeoLoc());
			case COL_DROP:
				return c.getDrop();
			case COL_ACTIVE:
				return c.getActive();
			case COL_STATUS:
				return c.getStatus();
			case COL_ERROR:
				return c.getError();
			case COL_VERSION:
				return c.getVersion();
			default:
				return null;
		}
	}

	/** Check if the specified cell is editable */
	public boolean isCellEditable(int row, int column) {
		if(comm_link == null)
			return false;
		if(isLastRow(row))
			return column == COL_NAME;
		else
			return column == COL_DROP || column == COL_ACTIVE;
	}

	/** Set the value at the specified cell */
	public void setValueAt(Object value, int row, int column) {
		Controller c = getProxy(row);
		switch(column) {
			case COL_NAME:
				String v = value.toString().trim();
				if(v.length() > 0)
					createController(v);
				break;
			case COL_DROP:
				c.setDrop(((Number)value).shortValue());
				break;
			case COL_ACTIVE:
				c.setActive((Boolean)value);
				break;
		}
	}

	/** Create a new controller */
	protected void createController(String name) {
		DropNumberModel m = new DropNumberModel(comm_link, cache, 1);
		HashMap<String, Object> attrs = new HashMap<String, Object>();
		attrs.put("comm_link", comm_link);
		attrs.put("drop_id", m.getNextAvailable());
		attrs.put("notes", "");
		cache.createObject(name, attrs);
	}

	/** Create the drop column */
	protected TableColumn createDropColumn() {
		TableColumn c = new TableColumn(COL_DROP, 60);
		c.setHeaderValue("Drop");
		c.setCellEditor(new DropCellEditor());
		return c;
	}

	/** Editor for drop addresses in a table cell */
	protected class DropCellEditor extends AbstractCellEditor
		implements TableCellEditor
	{
		protected final DropNumberModel model =
			new DropNumberModel(comm_link, cache, 1);
		protected final JSpinner spinner = new JSpinner(model);

		public Component getTableCellEditorComponent(JTable table,
			Object value, boolean isSelected, int row, int column)
		{
			spinner.setValue(value);
			return spinner;
		}
		public Object getCellEditorValue() {
			return spinner.getValue();
		}
	}

	/** Create the status column */
	protected TableColumn createStatusColumn() {
		TableColumn c = new TableColumn(COL_STATUS, 44);
		c.setHeaderValue("Status");
		c.setCellRenderer(new StatusCellRenderer());
		return c;
	}

	/** Renderer for link status in a table cell */
	protected class StatusCellRenderer extends DefaultTableCellRenderer {
		protected final Icon ok = new ControllerIcon(
			COLOR_AVAILABLE);
		protected final Icon fail = new ControllerIcon(Color.GRAY);
		protected final Icon inactive = new ControllerIcon(
			COLOR_INACTIVE);
		public Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean hasFocus,
			int row, int column)
		{
			JLabel label =
				(JLabel)super.getTableCellRendererComponent(
				table, "", isSelected, hasFocus, row,
				column);
			if(value == null)
				label.setIcon(null);
			else if("".equals(value))
				label.setIcon(ok);
			else {
				Controller c = getProxy(row);
				if(c != null && c.getActive())
					label.setIcon(fail);
				else
					label.setIcon(inactive);
			}
			return label;
		}
	}

	/** Create the table column model */
	public TableColumnModel createColumnModel() {
		TableColumnModel m = new DefaultTableColumnModel();
		m.addColumn(createColumn(COL_NAME, 90, "Controller"));
		m.addColumn(createColumn(COL_LOCATION, 200, "Location"));
		m.addColumn(createDropColumn());
		m.addColumn(createColumn(COL_ACTIVE, 50, "Active"));
		m.addColumn(createStatusColumn());
		m.addColumn(createColumn(COL_ERROR, 240, "Error Detail"));
		m.addColumn(createColumn(COL_VERSION, 120, "Version"));
		return m;
	}
}
