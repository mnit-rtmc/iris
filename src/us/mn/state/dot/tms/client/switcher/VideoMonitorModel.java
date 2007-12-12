/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.switcher;

import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumnModel;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.VideoMonitor;
import us.mn.state.dot.tms.client.security.ProxyTableModel;

/**
 * Table model for video monitors
 *
 * @author Douglas Lau
 */
public class VideoMonitorModel extends ProxyTableModel<VideoMonitor> {

	/** Count of columns in table model */
	static protected final int COLUMN_COUNT = 2;

	/** Name column number */
	static protected final int COL_NAME = 0;

	/** Description column number */
	static protected final int COL_DESCRIPTION = 1;

	/** Create a new video monitor table model */
	public VideoMonitorModel(TypeCache<VideoMonitor> c) {
		super(c, true);
	}

	/** Get the count of columns in the table */
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	/** Get the value at the specified cell */
	public Object getValueAt(int row, int column) {
		VideoMonitor m = getProxy(row);
		if(m == null)
			return null;
		switch(column) {
			case COL_NAME:
				return m.getName();
			case COL_DESCRIPTION:
				return m.getDescription();
		}
		return null;
	}

	/** Get the class of the specified column */
	public Class getColumnClass(int column) {
		return String.class;
	}

	/** Check if the specified row is the last row in the table */
	public boolean isLastRow(int row) {
		synchronized(proxies) {
			return row == proxies.size();
		}
	}

	/** Check if the specified cell is editable */
	public boolean isCellEditable(int row, int column) {
		if(isLastRow(row))
			return column == COL_NAME;
		if(column == COL_NAME)
			return false;
		return true;
	}

	/** Set the value at the specified cell */
	public void setValueAt(Object value, int row, int column) {
		VideoMonitor m = getProxy(row);
		switch(column) {
			case COL_NAME:
				String v = value.toString().trim();
				if(v.length() > 0)
					cache.createObject(v);
				break;
			case COL_DESCRIPTION:
				m.setDescription(value.toString());
				break;
		}
	}

	/** Create the table column model */
	public TableColumnModel createColumnModel() {
		TableColumnModel m = new DefaultTableColumnModel();
		m.addColumn(createColumn(COL_NAME, 200, "Video Monitor"));
		m.addColumn(createColumn(COL_DESCRIPTION, 300, "Description"));
		return m;
	}
}
