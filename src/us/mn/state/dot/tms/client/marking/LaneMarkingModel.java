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
package us.mn.state.dot.tms.client.marking;

import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumnModel;
import us.mn.state.dot.sonar.Name;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.LaneMarking;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for lane markings.
 *
 * @author Douglas Lau
 */
public class LaneMarkingModel extends ProxyTableModel<LaneMarking> {

	/** Count of columns in table model */
	static protected final int COLUMN_COUNT = 2;

	/** Name column number */
	static protected final int COL_NAME = 0;

	/** Location column number */
	static protected final int COL_LOCATION = 1;

	/** Create the table column model */
	static public TableColumnModel createColumnModel() {
		TableColumnModel m = new DefaultTableColumnModel();
		m.addColumn(createColumn(COL_NAME, 120, "Lane Marking"));
		m.addColumn(createColumn(COL_LOCATION, 300, "Location"));
		return m;
	}

	/** SONAR namespace */
	protected final Namespace namespace;

	/** SONAR user */
	protected final User user;

	/** Create a new lane marking table model */
	public LaneMarkingModel(TypeCache<LaneMarking> c, Namespace ns,
		User u)
	{
		super(c);
		namespace = ns;
		user = u;
		initialize();
	}

	/** Get the count of columns in the table */
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	/** Get the value at the specified cell */
	public Object getValueAt(int row, int column) {
		LaneMarking m = getProxy(row);
		if(m == null)
			return null;
		switch(column) {
		case COL_NAME:
			return m.getName();
		case COL_LOCATION:
			return GeoLocHelper.getDescription(m.getGeoLoc());
		}
		return null;
	}

	/** Check if the specified cell is editable */
	public boolean isCellEditable(int row, int column) {
		LaneMarking m = getProxy(row);
		return m == null && column == COL_NAME && canAdd();
	}

	/** Set the value at the specified cell */
	public void setValueAt(Object value, int row, int column) {
		if(column == COL_NAME) {
			String v = value.toString().trim();
			if(v.length() > 0)
				cache.createObject(v);
		}
	}

	/** Check if the user can add a lane marking */
	public boolean canAdd() {
		return namespace.canAdd(user, new Name(LaneMarking.SONAR_TYPE,
			"name"));
	}

	/** Check if the user can remove a proxy */
	public boolean canRemove(LaneMarking lm) {
		return lm != null && namespace.canRemove(user, new Name(lm));
	}
}
