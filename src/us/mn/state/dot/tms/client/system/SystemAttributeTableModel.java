/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.system;

import java.awt.Color;
import java.awt.Font;
import java.awt.Component;
import java.util.HashMap;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.SystemAttribute;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for system attributes.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class SystemAttributeTableModel extends ProxyTableModel<SystemAttribute>{

	/** Create the columns in the model */
	protected ProxyColumn[] createColumns() {
	    // NOTE: half-indent to declare array
	    return new ProxyColumn[] {
		new ProxyColumn<SystemAttribute>("Name", 200) {
			public Object getValueAt(SystemAttribute sa) {
				return sa.getName();
			}
			public boolean isEditable(SystemAttribute sa) {
				return (sa == null) && canAdd();
			}
			public void setValueAt(SystemAttribute sa,Object value){
				addRow(value);
			}
			protected TableCellRenderer createCellRenderer() {
				return new NameCellRenderer();
			}
		},
		new ProxyColumn<SystemAttribute>("Value", 340) {
			public Object getValueAt(SystemAttribute sa) {
				return sa.getValue();
			}
			public boolean isEditable(SystemAttribute sa) {
				return canUpdate(sa);
			}
			public void setValueAt(SystemAttribute sa,Object value){
				String v = value.toString().trim();
				SystemAttrEnum sae = SystemAttrEnum.lookup(
					sa.getName());
				if(sae != null)
					v = sae.parseValue(v).toString();
				sa.setValue(v);
			}
			protected TableCellRenderer createCellRenderer() {
				return new ValueCellRenderer();
			}
		}
	    };
	}

	/** Create a new table model */
	public SystemAttributeTableModel(Session s) {
		super(s, s.getSonarState().getSystemAttributes());
	}

	/** Add a row to the table */
	protected void addRow(Object value) {
		String aname = value.toString().replace(" ","");
		if(aname.isEmpty())
			return;
		// use default value if SA exists
		SystemAttrEnum sa = SystemAttrEnum.lookup(aname); 
		final String def = (sa == null ? "" : sa.getDefault());
		HashMap<String, Object> attrs =	new HashMap<String, Object>();
		attrs.put("value", def);
		cache.createObject(aname, attrs);
	}

	/** Renderer for system attribute names in a table cell */
	static protected class NameCellRenderer extends DefaultTableCellRenderer
	{
		public Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean hasFocus,
			int row, int column)
		{
			JLabel label = (JLabel)
				super.getTableCellRendererComponent(table,
				value, isSelected, hasFocus, row, column);
			if(value instanceof String) {
				String v = (String)value;
				if(SystemAttrEnum.lookup(v) == null)
					label.setForeground(Color.RED);
				else
					label.setForeground(null);
			}
			return label;
		}
	}

	/** Renderer for system attribute value in a table cell */
	protected class ValueCellRenderer extends DefaultTableCellRenderer {
		public Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean hasFocus,
			int row, int column)
		{
			JLabel label = (JLabel)
				super.getTableCellRendererComponent(table,
				value, isSelected, hasFocus, row, column);
			SystemAttribute sa = getProxy(row);
			if(!isValueDefault(sa)) {
				Font f = label.getFont();
				label.setFont(f.deriveFont(
					f.getStyle() ^ Font.BOLD));
			}
			return label;
		}
	}

	/** Check if a system attribute value is default */
	static protected boolean isValueDefault(SystemAttribute sa) {
		if(sa == null)
			return true;
		SystemAttrEnum sae = SystemAttrEnum.lookup(sa.getName());
		return sae == null || sae.equalsDefault();
	}

	/** Get the SONAR type name */
	protected String getSonarType() {
		return SystemAttribute.SONAR_TYPE;
	}
}
