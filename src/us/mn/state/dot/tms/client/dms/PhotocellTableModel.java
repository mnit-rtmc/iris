/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2012  Minnesota Department of Transportation
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

import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import us.mn.state.dot.tms.utils.I18N;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;

/**
 * PhotocellTableModel is a table model for photocell status.
 *
 * @author Douglas Lau
 */
public class PhotocellTableModel extends AbstractTableModel {

	/** Count of columns in table model */
	static protected final int COLUMN_COUNT = 3;

	/** Photocell description column number */
	static protected final int COL_DESCRIPTION = 0;

	/** Photocell status column number */
	static protected final int COL_STATUS = 1;

	/** Photocell reading detail column number */
	static protected final int COL_READING = 2;

	/** Create a new table column */
	static protected TableColumn createColumn(int column, int width,
		String header)
	{
		TableColumn c = new TableColumn(column, UI.scaled(width));
		c.setHeaderValue(header);
		return c;
	}

	/** Photocell status array */
	protected final String[] status;

	/** Create a new photocell table model */
	public PhotocellTableModel(String[] s) {
		status = s;
	}

	/** Get the column count */
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	/** Get the column class */
	public Class getColumnClass(int column) {
		return String.class;
	}

	/** Get the row count */
	public int getRowCount() {
		return status.length;
	}

	/** Get the value at a specific cell */
	public Object getValueAt(int row, int column) {
		if(row >= 0 && row < status.length)
			return parseValue(status[row], column);
		else
			return null;
	}

	/** Parse one status value */
	static protected String parseValue(String s, int column) {
		String[] cols = s.split(",");
		if(column >= 0 && column < cols.length)
			return cols[column];
		else
			return null;
	}

	/** Create the table column model */
	public TableColumnModel createColumnModel() {
		TableColumnModel m = new DefaultTableColumnModel();
		m.addColumn(createColumn(COL_DESCRIPTION, 120,
			I18N.get("dms.photocell.description")));
		m.addColumn(createColumn(COL_STATUS, 80,
			I18N.get("dms.photocell.status")));
		m.addColumn(createColumn(COL_READING, 80,
			I18N.get("dms.photocell.reading")));
		return m;
	}
}
