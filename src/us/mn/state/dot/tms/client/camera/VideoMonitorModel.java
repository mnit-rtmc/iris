/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2009  Minnesota Department of Transportation
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
import us.mn.state.dot.sonar.Name;
import us.mn.state.dot.tms.VideoMonitor;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for video monitors
 *
 * @author Douglas Lau
 */
public class VideoMonitorModel extends ProxyTableModel<VideoMonitor> {

	/** Count of columns in table model */
	static protected final int COLUMN_COUNT = 3;

	/** Name column number */
	static protected final int COL_NAME = 0;

	/** Description column number */
	static protected final int COL_DESCRIPTION = 1;

	/** Restricted column number */
	static protected final int COL_RESTRICTED = 2;

	/** Create a new video monitor table model */
	public VideoMonitorModel(Session s) {
		super(s, s.getSonarState().getCamCache().getVideoMonitors());
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
		case COL_RESTRICTED:
			return m.getRestricted();
		}
		return null;
	}

	/** Get the class of the specified column */
	public Class getColumnClass(int column) {
		if(column == COL_RESTRICTED)
			return Boolean.class;
		else
			return String.class;
	}

	/** Check if the specified cell is editable */
	public boolean isCellEditable(int row, int col) {
		VideoMonitor vm = getProxy(row);
		if(vm != null)
			return col != COL_NAME && canUpdate(vm);
		else
			return col == COL_NAME && canAdd();
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
		case COL_RESTRICTED:
			m.setRestricted((Boolean)value);
			break;
		}
	}

	/** Create the table column model */
	public TableColumnModel createColumnModel() {
		TableColumnModel m = new DefaultTableColumnModel();
		m.addColumn(createColumn(COL_NAME, 160, "Video Monitor"));
		m.addColumn(createColumn(COL_DESCRIPTION, 300, "Description"));
		m.addColumn(createColumn(COL_RESTRICTED, 120, "Restricted"));
		return m;
	}

	/** Check if the user can add a proxy */
	public boolean canAdd() {
		return namespace.canAdd(user,
			new Name(VideoMonitor.SONAR_TYPE));
	}
}
