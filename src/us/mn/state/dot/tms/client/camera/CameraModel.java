/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.camera;

import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumnModel;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for cameras
 *
 * @author Douglas Lau
 */
public class CameraModel extends ProxyTableModel<Camera> {

	/** Count of columns in table model */
	static protected final int COLUMN_COUNT = 3;

	/** Name column number */
	static protected final int COL_NAME = 0;

	/** Location column number */
	static protected final int COL_LOCATION = 1;

	/** Publish column number */
	static protected final int COL_PUBLISH = 2;

	/** Create a new camera table model */
	public CameraModel(TypeCache<Camera> c) {
		super(c, true);
		initialize();
	}

	/** Get the count of columns in the table */
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	/** Get the value at the specified cell */
	public Object getValueAt(int row, int column) {
		Camera c = getProxy(row);
		if(c == null)
			return null;
		switch(column) {
			case COL_NAME:
				return c.getName();
			case COL_LOCATION:
				return GeoLocHelper.getDescription(
					c.getGeoLoc());
			case COL_PUBLISH:
				return c.getPublish();
		}
		return null;
	}

	/** Get the class of the specified column */
	public Class getColumnClass(int column) {
		if(column == COL_PUBLISH)
			return Boolean.class;
		else
			return String.class;
	}

	/** Check if the specified cell is editable */
	public boolean isCellEditable(int row, int column) {
		if(isLastRow(row))
			return column == COL_NAME;
		return column == COL_PUBLISH;
	}

	/** Set the value at the specified cell */
	public void setValueAt(Object value, int row, int column) {
		Camera c = getProxy(row);
		switch(column) {
			case COL_NAME:
				String v = value.toString().trim();
				if(v.length() > 0)
					cache.createObject(v);
				break;
			case COL_PUBLISH:
				c.setPublish((Boolean)value);
				break;
		}
	}

	/** Create the table column model */
	public TableColumnModel createColumnModel() {
		TableColumnModel m = new DefaultTableColumnModel();
		m.addColumn(createColumn(COL_NAME, 200, "Camera"));
		m.addColumn(createColumn(COL_LOCATION, 300, "Location"));
		m.addColumn(createColumn(COL_PUBLISH, 120, "Publish"));
		return m;
	}
}
