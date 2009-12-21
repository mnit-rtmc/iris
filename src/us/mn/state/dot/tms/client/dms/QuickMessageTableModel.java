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
package us.mn.state.dot.tms.client.dms;

import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.sonar.Name;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.QuickMessage;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

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

	/** SONAR namespace */
	protected final Namespace namespace;

	/** SONAR User for permission checks */
	protected final User user;

	/** Create a new table column */
	static protected TableColumn createColumn(int column, int width,
		String header)
	{
		TableColumn c = new TableColumn(column, width);
		c.setHeaderValue(header);
		return c;
	}

	/** Create the table column model */
	public TableColumnModel createColumnModel() {
		TableColumnModel m = new DefaultTableColumnModel();
		m.addColumn(createColumn(COL_NAME, 100, "Name"));
		m.addColumn(createColumn(COL_MULTI, 680, "MULTI String"));
		return m;
	}

	/** Create a new table model.
	 *  @param session Session */
	public QuickMessageTableModel(Session session) {
		super(session.getSonarState().getDmsCache().getQuickMessages());
		namespace = session.getSonarState().getNamespace();
		user = session.getUser();
		initialize();
	}

	/** Get the count of columns in the table */
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	/** Get the value at the specified cell */
	public Object getValueAt(int row, int col) {
		QuickMessage qm = getProxy(row);
		if(qm != null) {
			switch(col) {
			case COL_NAME:
				return qm.getName();
			case COL_MULTI:
				return qm.getMulti();
			}
		}
		return null;
	}

	/** Check if the specified cell is editable */
	public boolean isCellEditable(int row, int col) {
		QuickMessage qm = getProxy(row);
		if(qm == null)
			return col == COL_NAME && canAdd("oname");
		else {
			return col == COL_MULTI &&
			       canUpdate(qm.getName(), "multi");
		}
	}

	/** Set the value at the specified cell */
	public void setValueAt(Object value, int row, int col) {
		QuickMessage qm = getProxy(row);
		if(col == COL_NAME) {
			if(qm == null) {
				String name = (value == null ? "" : 
					value.toString().replace(" ", ""));
				if(name.length() > 0 && canAdd(name))
					cache.createObject(name);
			}
		} else if(col == COL_MULTI) {
			if(qm != null) {
				qm.setMulti(new MultiString(
					value.toString()).normalize());
			}
		}
	}

	/** Check if the user can add a proxy */
	public boolean canAdd(String oname) {
		return namespace.canAdd(user, createQMName(oname));
	}

	/** Check if the user can update the named attribute,
	 *  @param aname attribute name. */
	public boolean canUpdate(String oname, String aname) {
		return namespace.canUpdate(user, createQMName(oname, aname));
	}

	/** Check if the user can remove a proxy */
	public boolean canRemove(String oname) {
		return namespace.canRemove(user, createQMName(oname));
	}
}
