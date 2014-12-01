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

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
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
import us.mn.state.dot.tms.ControllerHelper;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel2;

/**
 * Table model for controllers.
 *
 * @author Douglas Lau
 */
public class ControllerModel extends ProxyTableModel2<Controller> {

	/** Color to display inactive controllers */
	static private final Color COLOR_INACTIVE = new Color(0, 0, 0, 32);

	/** Color to display available devices */
	static private final Color COLOR_AVAILABLE = new Color(96, 96, 255);

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<Controller>> createColumns() {
		ArrayList<ProxyColumn<Controller>> cols =
			new ArrayList<ProxyColumn<Controller>>(7);
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
		cols.add(new ProxyColumn<Controller>("controller.drop", 60) {
			public Object getValueAt(Controller c) {
				return c.getDrop();
			}
			public boolean isEditable(Controller c) {
				return canUpdate(c);
			}
			public void setValueAt(Controller c, Object value) {
				if (value instanceof Number)
					c.setDrop(((Number)value).shortValue());
			}
			protected TableCellEditor createCellEditor() {
				return new DropCellEditor();
			}
		});
		cols.add(new ProxyColumn<Controller>("controller.active", 50,
			Boolean.class)
		{
			public Object getValueAt(Controller c) {
				return c.getActive();
			}
			public boolean isEditable(Controller c) {
				return canUpdate(c, "active");
			}
			public void setValueAt(Controller c, Object value) {
				if (value instanceof Boolean)
					c.setActive((Boolean)value);
			}
		});
		cols.add(new ProxyColumn<Controller>("comm", 44) {
			public Object getValueAt(Controller c) {
				return c;
			}
			protected TableCellRenderer createCellRenderer() {
				return new CommCellRenderer();
			}
		});
		cols.add(new ProxyColumn<Controller>("controller.status", 240) {
			public Object getValueAt(Controller c) {
				return c.getStatus();
			}
		});
		cols.add(new ProxyColumn<Controller>("controller.version", 120){
			public Object getValueAt(Controller c) {
				return c.getVersion();
			}
		});
		return cols;
	}

	/** Comm link to match controllers */
	private final CommLink comm_link;

	/** Get a proxy comparator */
	@Override
	protected Comparator<Controller> comparator() {
		return new Comparator<Controller>() {
			public int compare(Controller a, Controller b) {
				Short aa = Short.valueOf(a.getDrop());
				Short bb = Short.valueOf(b.getDrop());
				int c = aa.compareTo(bb);
				if (c == 0) {
					String an = a.getName();
					String bn = b.getName();
					return an.compareTo(bn);
				} else
					return c;
			}
			public boolean equals(Object o) {
				return o == this;
			}
			public int hashCode() {
				return super.hashCode();
			}
		};
	}

	/** Create a new controller table model */
	public ControllerModel(Session s, CommLink cl) {
		super(s, s.getSonarState().getConCache().getControllers(),
		      true,	/* has_properties */
		      true,	/* has_create_delete */
		      true);	/* has_name */
		comm_link = cl;
	}

	/** Get the SONAR type name */
	@Override
	protected String getSonarType() {
		return Controller.SONAR_TYPE;
	}

	/** Create a properties form for one proxy */
	@Override
	protected ControllerForm createPropertiesForm(Controller proxy) {
		return new ControllerForm(session, proxy);
	}

	/** Get the visible row count */
	@Override
	public int getVisibleRowCount() {
		return 10;
	}

	/** Get the row height */
	@Override
	public int getRowHeight() {
		return 24;
	}

	/** Check if a proxy is included in the list */
	@Override
	protected boolean check(Controller proxy) {
		return proxy.getCommLink() == comm_link;
	}

	/** Create a new controller */
	@Override
	public void createObject(String name) {
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

	/** Renderer for comm status in a table cell */
	protected class CommCellRenderer extends DefaultTableCellRenderer {
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
			if (value instanceof Controller) {
				Controller c = (Controller)value;
				if (ControllerHelper.isFailed(c))
					label.setIcon(fail);
				else if (ControllerHelper.isActive(c))
					label.setIcon(ok);
				else
					label.setIcon(inactive);
			} else
				label.setIcon(null);
			return label;
		}
	}
}
