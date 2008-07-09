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

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.SignText;
import us.mn.state.dot.tms.client.security.ProxyTableModel;

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

	/** Create an empty set of proxies */
	protected TreeSet<SignText> createProxySet() {
		return new TreeSet<SignText>(new SignTextComparator());
	}

	/** Add a new proxy to the table model */
	public void proxyAdded(SignText proxy) {
		if(proxy.getSignGroup() == group)
			super.proxyAdded(proxy);
	}

	/** Remove a proxy from the table model */
	public void proxyRemoved(SignText proxy) {
		if(proxy.getSignGroup() == group)
			super.proxyRemoved(proxy);
	}

	/** Change a proxy in the table model */
	public void proxyChanged(SignText proxy, String attrib) {
		if(proxy.getSignGroup() == group)
			super.proxyChanged(proxy, attrib);
	}

	/** Sign group */
	protected final SignGroup group;

	/** Line for adding a new row */
	protected Short m_line;

	/** Priority for adding a new row */
	protected Short m_priority;

	/** Create a new sign text table model 
	 *  @param a True if admin else false.
	 */
	public SignTextTableModel(SignGroup g, TypeCache<SignText> c, boolean a) {
		super(c, a);
		group = g;
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
		return admin;
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

	/** Create a new sign text message using the current line and priority values */
	protected void createSignText(String message) {
		if(m_line == null)
			m_line = 1;
		if(m_priority == null)
			m_priority = 50;
		createSignText(m_line,message,m_priority);
		m_line = null;
		m_priority = null;
	}

	/** Create a new sign text message using the specified args */
	protected void createSignText(short line,String message,short priority) {
		// validate args
		if(message==null || line<=0) {
			return;
		}

		String name = createName();
		if(name == null) {
			// FIXME: display a warning?
			return;
		}
		HashMap<String, Object> attrs = new HashMap<String, Object>();
		attrs.put("sign_group", group);
		attrs.put("line", new Short(line));
		attrs.put("message", message);
		attrs.put("priority", new Short(priority));
		cache.createObject(name, attrs);
	}

	/** 
	 * Create a new sign text name. A sign text name is the associated
	 * sign group name with _n appended, where n is a unique integer.
	 * @return A unique string in the form: group name + _n
	 */
	protected String createName() {
		if (group==null)
			return null;
		HashSet<String> names = getNames();
		for(int i = 0; i < 10000; i++) {
			String n = group.getName() + "_" + i;
			if(!names.contains(n))
				return n;
		}
		String msg="Warning: something is wrong in SignTextTableModel.createName().";
		System.err.println(msg);
		assert false : msg;
		return null;
	}

	/** Get a set of sign text names */
	protected HashSet<String> getNames() {
		HashSet<String> names = new HashSet<String>();
		synchronized(proxies) {
			for(SignText t: proxies)
				names.add(t.getName());
		}
		return names;
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
