/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005-2008  Minnesota Department of Transportation
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.SystemAttribute;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for system attributes.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class SystemAttributeTableModel extends ProxyTableModel<SystemAttribute> 
{

	/** Count of columns in table model */
	static protected final int COLUMN_COUNT = 2;

	/** name column number */
	static protected final int COL_NAME = 0;

	/** value column number */
	static protected final int COL_VALUE = 1;

	/** Cell renderer for this table */
	static protected final DefaultTableCellRenderer RENDERER =
		new DefaultTableCellRenderer();
	static {
		RENDERER.setHorizontalAlignment(SwingConstants.CENTER);
	}

	/** Form containing table */
	protected final SystemAttributeForm m_form;

	/** 
	 *  Create a new table model.
	 *  @param tc TypeCache for the table items being displayed/edited.
	 */
	public SystemAttributeTableModel(TypeCache<SystemAttribute> arg_tc,
		SystemAttributeForm f)
	{
		super(arg_tc, true);
		m_form = f;
		initialize();
	}

	/** Create a new table column */
	static protected TableColumn createColumn(int column, int width,
		String header)
	{
		assert header != null;
		TableColumn c = new TableColumn(column, width);
		c.setHeaderValue(header);
		if(column == COL_NAME || column == COL_VALUE)
			c.setCellRenderer(RENDERER);
		else {
			assert false;
			System.err.println("WARNING: bogus column");
			c.setCellRenderer(RENDERER);
		}
		return c;
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

	/** Create the table column model */
	static public TableColumnModel createColumnModel() {
		TableColumnModel m = new DefaultTableColumnModel();
		m.addColumn(createColumn(COL_NAME, 140, "Name"));
		m.addColumn(createColumn(COL_VALUE, 180, "Value"));
		return m;
	}

	/** Check if the specified cell is editable */
	public boolean isCellEditable(int row, int col) {
		if(col == COL_NAME) {
			return isLastRow(row) &&
				m_form.canAddAttribute("test_attr");
		} else if(col == COL_VALUE) {
			SystemAttribute t = getProxy(row);
			if(t != null) {
				String a = t.getName() + '/' + t.getValue();
				return m_form.canUpdateAttribute(a);
			}
		}
		return false;
	}

	/** Set the value at the specified cell */
	public void setValueAt(Object value, int row, int col) {
		SystemAttribute t = getProxy(row);
		if(t == null)
			addRow(value, col);
		else if(col == COL_VALUE)
			t.setValue(value.toString());
	}
	
	/** Add a row to the table */
	protected void addRow(Object value, int col) {
		if(col == COL_NAME) {
			String aname = value.toString();
			if(aname.length() > 0)
				cache.createObject(aname);
		}
	}
}
