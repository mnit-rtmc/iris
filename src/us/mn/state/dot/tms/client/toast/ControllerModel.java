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
import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeSet;
import javax.swing.AbstractCellEditor;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import us.mn.state.dot.tms.CommLink;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
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

	/** Create the columns in the model */
	protected ProxyColumn[] createColumns() {
	    // NOTE: half-indent to declare array
	    return new ProxyColumn[] {
		new ProxyColumn<Controller>("Controller", 90) {
			public Object getValueAt(Controller c) {
				return c.getName();
			}
			public boolean isEditable(Controller c) {
				return (c == null) && canAdd();
			}
			public void setValueAt(Controller c, Object value) {
				String v = value.toString().trim();
				if(v.length() > 0)
					createController(v);
			}
		},
		new ProxyColumn<Controller>("Location", 200) {
			public Object getValueAt(Controller c) {
				return GeoLocHelper.getDescription(
					c.getCabinet().getGeoLoc());
			}
		},
		new ProxyColumn<Controller>("Drop", 60) {
			public Object getValueAt(Controller c) {
				return c.getDrop();
			}
			public boolean isEditable(Controller c) {
				return canUpdate(c);
			}
			public void setValueAt(Controller c, Object value) {
				if(value instanceof Number)
					c.setDrop(((Number)value).shortValue());
			}
			protected TableCellEditor createCellEditor() {
				return new DropCellEditor();
			}
		},
		new ProxyColumn<Controller>("Active", 50, Boolean.class) {
			public Object getValueAt(Controller c) {
				return c.getActive();
			}
			public boolean isEditable(Controller c) {
				return canUpdate(c);
			}
			public void setValueAt(Controller c, Object value) {
				if(value instanceof Boolean)
					c.setActive((Boolean)value);
			}
		},
		new ProxyColumn<Controller>("Status", 44) {
			public Object getValueAt(Controller c) {
				return c.getStatus();
			}
			protected TableCellRenderer createCellRenderer() {
				return new StatusCellRenderer();
			}
		},
		new ProxyColumn<Controller>("Error Detail", 240) {
			public Object getValueAt(Controller c) {
				return c.getError();
			}
		},
		new ProxyColumn<Controller>("Version", 120) {
			public Object getValueAt(Controller c) {
				return c.getVersion();
			}
		}
	    };
	}

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
	public ControllerModel(Session s, CommLink cl) {
		super(s, s.getSonarState().getConCache().getControllers());
		comm_link = cl;
	}

	/** Add a new proxy to the table model */
	protected int doProxyAdded(Controller proxy) {
		if(proxy.getCommLink() == comm_link)
			return super.doProxyAdded(proxy);
		else
			return -1;
	}

	/** Check if the specified cell is editable */
	public boolean isCellEditable(int row, int col) {
		return comm_link != null && super.isCellEditable(row, col);
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

	/** Get the SONAR type name */
	protected String getSonarType() {
		return Controller.SONAR_TYPE;
	}
}
