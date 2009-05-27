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
package us.mn.state.dot.tms.client.dms;

import java.util.TreeSet;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.SignText;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for sign text.
 *
 * @author Douglas Lau
 */
public class SignTextTableModel extends ProxyTableModel<SignText> {

	/** Count of columns in table model */
	static protected final int COLUMN_COUNT = 3;

	/** Message line column number */
	static protected final int COL_LINE = 0;

	/** Message text column number */
	static protected final int COL_MESSAGE = 1;

	/** Priority column number */
	static protected final int COL_PRIORITY = 2;

	/** Cell renderer for this table */
	static protected final DefaultTableCellRenderer RENDERER =
		new DefaultTableCellRenderer();
	static {
		RENDERER.setHorizontalAlignment(SwingConstants.CENTER);
	}

	/** Create a new table column */
	static protected TableColumn createColumn(int column, int width,
		String header)
	{
		TableColumn c = new TableColumn(column, width);
		c.setHeaderValue(header);
		if(column == COL_MESSAGE)
			c.setCellRenderer(RENDERER);
		if(column == COL_PRIORITY)
			c.setCellEditor(new PriorityCellEditor());
		return c;
	}

	/** Format message text */
	static protected String formatMessage(Object value) {
		return value.toString().trim().toUpperCase();
	}

	/** Return the value as a short */
	static protected short asShort(Object value) {
		if(value instanceof Number) {
			Number n = (Number)value;
			return n.shortValue();
		} else
			return 0;
	}

	/** Get the attribute name for the given column */
	static protected String getAttributeName(int column) {
		switch(column) {
		case COL_LINE: return "line";
		case COL_MESSAGE: return "message";
		case COL_PRIORITY: return "priority";
		default: return null;
		}
	}

	/** Create an empty set of proxies */
	protected TreeSet<SignText> createProxySet() {
		return new TreeSet<SignText>(new SignTextComparator());
	}

	/** Add a new proxy to the table model */
	protected int doProxyAdded(SignText proxy) {
		if(proxy.getSignGroup() == group)
			return super.doProxyAdded(proxy);
		else
			return -1;
	}

	/** Sign group */
	protected final SignGroup group;

	/** Sign text creator */
	protected final SignTextCreator creator;

	/** Line for adding a new row */
	protected Short m_line;

	/** Priority for adding a new row */
	protected Short m_priority;

	/** Create a new sign text table model */
	public SignTextTableModel(SignGroup g, TypeCache<SignText> c, User u) {
		super(c, true);
		group = g;
		creator = new SignTextCreator(c, u);
		initialize();
	}

	/** Get the count of columns in the table */
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	/** Get the value at the specified cell */
	public Object getValueAt(int row, int column) {
		SignText t = getProxy(row);
		if(t != null)
			return getValue(t, column);
		switch(column) {
		case COL_LINE:
			return m_line;
		case COL_PRIORITY:
			return m_priority;
		}
		return null;
	}

	/** Get the value of a sign text column */
	protected Object getValue(SignText t, int column) {
		switch(column) {
		case COL_LINE:
			return t.getLine();
		case COL_MESSAGE:
			return t.getMessage();
		case COL_PRIORITY:
			return t.getPriority();
		}
		return null;
	}

	/** Get the column class */
	public Class getColumnClass(int column) {
		if(column == COL_MESSAGE)
			return String.class;
		else
			return Short.class;
	}

	/** Check if the specified cell is editable */
	public boolean isCellEditable(int row, int column) {
		SignText st = getProxy(row);
		if(st != null) {
			return creator.canUpdateSignText(st.getName() + "/" +
				getAttributeName(column));
		} else {
			String oname = group.getName() + "_XX";
			return creator.canAddSignText(oname);
		}
	}

	/** Set the value at the specified cell */
	public void setValueAt(Object value, int row, int column) {
		SignText t = getProxy(row);
		if(t != null)
			setValue(t, column, value);
		else
			addRow(value, column);
	}

	/** Set the value of the specified sign text column */
	protected void setValue(SignText t, int column, Object value) {
		switch(column) {
		case COL_LINE:
			t.setLine(asShort(value));
			break;
		case COL_MESSAGE:
			t.setMessage(formatMessage(value));
			break;
		case COL_PRIORITY:
			t.setPriority(asShort(value));
			break;
		}
	}

	/** Add a row to the sign text table */
	protected void addRow(Object value, int column) {
		switch(column) {
		case COL_LINE:
			m_line = asShort(value);
			break;
		case COL_MESSAGE:
			String v = formatMessage(value);
			if(v.length() > 0)
				createSignText(v);
			break;
		case COL_PRIORITY:
			m_priority = asShort(value);
			break;
		}
	}

	/** Create a new sign text message using the current line and priority
	 * values */
	protected void createSignText(String message) {
		if(m_line == null)
			m_line = 1;
		if(m_priority == null)
			m_priority = 50;
		creator.create(group, m_line, message, m_priority);
		m_line = null;
		m_priority = null;
	}

	/** Create the table column model */
	static public TableColumnModel createColumnModel() {
		TableColumnModel m = new DefaultTableColumnModel();
		m.addColumn(createColumn(COL_LINE, 36, "Line"));
		m.addColumn(createColumn(COL_MESSAGE, 200, "Message"));
		m.addColumn(createColumn(COL_PRIORITY, 48, "Priority"));
		return m;
	}
}
