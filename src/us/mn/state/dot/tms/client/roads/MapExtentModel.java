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
package us.mn.state.dot.tms.client.roads;

import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumnModel;
import us.mn.state.dot.sonar.Name;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.MapExtent;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for map extents.
 *
 * @author Douglas Lau
 */
public class MapExtentModel extends ProxyTableModel<MapExtent> {

	/** Count of columns in table model */
	static protected final int COLUMN_COUNT = 5;

	/** Name column number */
	static protected final int COL_NAME = 0;

	/** Easting column number */
	static protected final int COL_EASTING = 1;

	/** East-span column number */
	static protected final int COL_EAST_SPAN = 2;

	/** Northing column number */
	static protected final int COL_NORTHING = 3;

	/** North-span column number */
	static protected final int COL_NORTH_SPAN = 4;

	/** Create the table column model */
	static public TableColumnModel createColumnModel() {
		TableColumnModel m = new DefaultTableColumnModel();
		m.addColumn(createColumn(COL_NAME, 160, "Map Extent"));
		m.addColumn(createColumn(COL_EASTING, 80, "Easting"));
		m.addColumn(createColumn(COL_EAST_SPAN, 80, "East Span"));
		m.addColumn(createColumn(COL_NORTHING, 80, "Northing"));
		m.addColumn(createColumn(COL_NORTH_SPAN, 80, "North Span"));
		return m;
	}

	/** SONAR namespace */
	protected final Namespace namespace;

	/** SONAR user */
	protected final User user;

	/** Create a new map extent table model */
	public MapExtentModel(TypeCache<MapExtent> c, Namespace ns, User u) {
		super(c);
		namespace = ns;
		user = u;
	}

	/** Get the count of columns in the table */
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	/** Get the column class */
	public Class getColumnClass(int column) {
		if(column == COL_NAME)
			return String.class;
		else
			return Integer.class;
	}

	/** Get the value at the specified cell */
	public Object getValueAt(int row, int column) {
		MapExtent me = getProxy(row);
		if(me == null)
			return null;
		switch(column) {
		case COL_NAME:
			return me.getName();
		case COL_EASTING:
			return me.getEasting();
		case COL_EAST_SPAN:
			return me.getEastSpan();
		case COL_NORTHING:
			return me.getNorthing();
		case COL_NORTH_SPAN:
			return me.getNorthSpan();
		}
		return null;
	}

	/** Check if the specified cell is editable */
	public boolean isCellEditable(int row, int column) {
		MapExtent me = getProxy(row);
		if(me != null)
			return canUpdate(me);
		else
			return column == COL_NAME && canAdd();
	}

	/** Set the value at the specified cell */
	public void setValueAt(Object value, int row, int column) {
		MapExtent me = getProxy(row);
		switch(column) {
		case COL_NAME:
			String v = value.toString().trim();
			if(v.length() > 0)
				cache.createObject(v);
			break;
		case COL_EASTING:
			if(value instanceof Integer)
				me.setEasting((Integer)value);
			break;
		case COL_EAST_SPAN:
			if(value instanceof Integer)
				me.setEastSpan((Integer)value);
			break;
		case COL_NORTHING:
			if(value instanceof Integer)
				me.setNorthing((Integer)value);
			break;
		case COL_NORTH_SPAN:
			if(value instanceof Integer)
				me.setNorthSpan((Integer)value);
			break;
		}
	}

	/** Check if the user can add a map extent */
	public boolean canAdd() {
		return namespace.canAdd(user, new Name(MapExtent.SONAR_TYPE,
			"name"));
	}

	/** Check if the user can update */
	public boolean canUpdate(MapExtent me) {
		return namespace.canUpdate(user, new Name(MapExtent.SONAR_TYPE,
			me.getName()));
	}

	/** Check if the user can remove a map extent */
	public boolean canRemove(MapExtent me) {
		if(me == null)
			return false;
		return namespace.canRemove(user, new Name(MapExtent.SONAR_TYPE,
			me.getName()));
	}
}
