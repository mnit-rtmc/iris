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
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.SystemAttribute;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for system attributes.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class SystemAttributeTableModel extends ProxyTableModel<SystemAttribute>{

	/** Count of columns in table model */
	static protected final int COLUMN_COUNT = 2;

	/** Name column number */
	static public final int COL_NAME = 0;

	/** Value column number */
	static public final int COL_VALUE = 1;

	/** Create a new table column */
	static protected TableColumn createColumn(int column, int width,
		String header)
	{
		TableColumn c = new TableColumn(column, width);
		c.setHeaderValue(header);
		return c;
	}

	/** Create the table column model */
	static public TableColumnModel createColumnModel() {
		TableColumnModel m = new DefaultTableColumnModel();
		TableColumn ncol = createColumn(COL_NAME, 200, "Name");
		ncol.setCellRenderer(new NameCellRenderer());
		m.addColumn(ncol);
		TableColumn vcol = createColumn(COL_VALUE, 340, "Value");
		vcol.setCellRenderer(new ValueCellRenderer());
		m.addColumn(vcol);
		return m;
	}

	/** Form containing table */
	protected final SystemAttributeForm m_form;

	/**
	 * Create a new table model.
	 * @param tc TypeCache for the table items being displayed/edited.
	 */
	public SystemAttributeTableModel(TypeCache<SystemAttribute> arg_tc,
		SystemAttributeForm f)
	{
		super(arg_tc);
		m_form = f;
		initialize();
	}

	/** Get the count of columns in the table */
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	/** Get the value of a column */
	protected Object getValue(SystemAttribute t, int col) {
		if(col == COL_NAME)
			return t.getName();
		else if(col == COL_VALUE)
			return t.getValue();
		else
			return null;
	}

	/** Get the value at the specified cell */
	public Object getValueAt(int row, int col) {
		SystemAttribute t = getProxy(row);
		if(t != null)
			return getValue(t, col);
		else
			return null;
	}

	/** Check if the specified cell is editable */
	public boolean isCellEditable(int row, int col) {
		if(col == COL_NAME)
			return isLastRow(row) && m_form.canAdd("test_attr");
		else if(col == COL_VALUE) {
			SystemAttribute t = getProxy(row);
			if(t != null)
				return m_form.canUpdate(t.getName(), "value");
		}
		return false;
	}

	/** Set the value at the specified cell */
	public void setValueAt(Object value, int row, int col) {
		SystemAttribute t = getProxy(row);
		if(t == null) {
			if(col == COL_NAME)
				addRow(value);
		} else if(col == COL_VALUE) {
			SystemAttrEnum sa = SystemAttrEnum.lookup(t.getName());
			if(sa != null)
				value = sa.parseValue(value.toString());
			t.setValue(value.toString());
		}
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
	static protected class ValueCellRenderer extends 
		DefaultTableCellRenderer
	{
		public Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean hasFocus,
			int row, int column)
		{
			JLabel label = (JLabel)
				super.getTableCellRendererComponent(table,
				value, isSelected, hasFocus, row, column);
			if(!(value instanceof String))
				return label;
			// render non-default values in bold.
			String an = (String)table.getValueAt(row, COL_NAME);
			if(an == null)
				return label;
			SystemAttrEnum sa = SystemAttrEnum.lookup(an);
			if(sa == null)
				return label;
			Font f = label.getFont();
			if(!sa.equalsDefault())
				label.setFont(f.deriveFont(
					f.getStyle() ^ Font.BOLD));
			return label;
		}
	}
}
