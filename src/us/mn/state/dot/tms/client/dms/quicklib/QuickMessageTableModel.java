/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.dms.quicklib;

import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.sonar.Name;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.QuickMessage;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;
import us.mn.state.dot.tms.utils.SField;

/**
 * Table model for quick messages, which is for editing and creating
 * quick messages.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public class QuickMessageTableModel extends ProxyTableModel<QuickMessage> {

	/** Create a quick message object name */
	static protected Name createQMName(String oname) {
		return new Name(QuickMessage.SONAR_TYPE, oname);
	}

	/** Create a quick message object attribute name */
	static protected Name createQMName(String oname, String aname) {
		return new Name(QuickMessage.SONAR_TYPE, oname, aname);
	}

	/** Count of columns in table model */
	static protected final int COLUMN_COUNT = 2;

	/** name column number */
	static protected final int COL_NAME = 0;

	/** multi column number */
	static protected final int COL_MULTI = 1;
	static protected final String COL_MULTI_NAME = "multi";

	/** SONAR namespace */
	protected final Namespace namespace;

	/** SONAR User for permission checks */
	protected final User m_user;

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
		m.addColumn(createColumn(COL_NAME, 80, "Name"));
		m.addColumn(createColumn(COL_MULTI, 600, "MULTI String"));
		return m;
	}

	/** Create a new table model.
	 *  @param tc TypeCache for the table items being displayed/edited. */
	public QuickMessageTableModel(TypeCache<QuickMessage> arg_tc, 
		Namespace ns, User user)
	{
		super(arg_tc);
		namespace = ns;
		m_user = user;
		initialize();
	}

	/** Get the count of columns in the table */
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	/** Get the value of a column */
	protected Object getValue(QuickMessage t, int col) {
		if(t == null)
			return null;
		if(col == COL_NAME)
			return SField.tail(t.getName());
		else if(col == COL_MULTI)
			return t.getMulti();
		return null;
	}

	/** Get the value at the specified cell */
	public Object getValueAt(int row, int col) {
		QuickMessage t = getProxy(row);
		return getValue(t, col);
	}

	/** Check if the specified cell is editable */
	public boolean isCellEditable(int row, int col) {
		if(col == COL_NAME)
			return isLastRow(row) && canAdd("whatever");
		else if(col == COL_MULTI) {
			QuickMessage p = getProxy(row);
			if(p != null)
				return canUpdate(p.getName(), COL_MULTI_NAME);
		}
		return false;
	}

	/** Set the value at the specified cell */
	public void setValueAt(Object value, int row, int col) {
		QuickMessage t = getProxy(row);
		if(col == COL_NAME) {
			if(t == null) {
				// trim
				String name = (value == null ? "" : 
					value.toString().replace(" ", ""));
				addRow(name);
			}
		} else if(col == COL_MULTI) {
			if(t != null)
				t.setMulti(new MultiString(
					value.toString()).normalize());
		}
	}

	/** Add a row to the table.
	 *  @param arg_name. */
	protected void addRow(Object arg_name) {
		String name = (arg_name == null ? "" : arg_name.toString());
		if(canAdd(name))
			cache.createObject(name);
	}

	/** Add a new proxy to the table model, if
	 *  the user has read permission. */
	protected int doProxyAdded(QuickMessage p) {
		if(p != null && canRead(p.getName()))
			return super.doProxyAdded(p);
		return -1;
	}

	/** Check if the user can read a proxy */
	public boolean canRead(String oname) {
		return oname != null && 
			namespace.canRead(m_user, createQMName(oname));
	}

	/** Check if the user can remove a proxy */
	public boolean canRemove(String oname) {
		return oname != null && 
			namespace.canRemove(m_user, createQMName(oname));
	}

	/** Check if the user can add a proxy */
	public boolean canAdd(String oname) {
		if(oname == null || oname.isEmpty())
			return false;
		return namespace.canAdd(m_user, createQMName(oname));
	}

	/** Check if the user can update the named attribute,
	 *  @param aname attribute name. */
	public boolean canUpdate(String oname, String aname) {
		return namespace.canUpdate(m_user, createQMName(oname, aname));
	}
}
