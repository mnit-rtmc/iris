/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2024  Minnesota Department of Transportation
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
import org.json.JSONArray;
import org.json.JSONObject;
import us.mn.state.dot.tms.utils.I18N;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;

/**
 * PhotocellTableModel is a table model for photocell status.
 *
 * @author Douglas Lau
 */
public class PhotocellTableModel extends AbstractTableModel {

	/** Count of columns in table model */
	static private final int COLUMN_COUNT = 2;

	/** Photocell description column number */
	static private final int COL_DESC = 0;

	/** Photocell reading detail column number */
	static private final int COL_READING = 1;

	/** Create a new table column */
	static private TableColumn createColumn(int column, int width,
		String header)
	{
		TableColumn c = new TableColumn(column, UI.scaled(width));
		c.setHeaderValue(header);
		return c;
	}

	/** Get one photocell value */
	static private String getColumn(JSONObject pc, int column) {
		if (pc == null)
			return null;
		switch (column) {
			case COL_DESC: return pc.optString("description");
			case COL_READING: return pc.optString("reading");
			default: return null;
		}
	}

	/** Photocell status array */
	private final JSONArray status;

	/** Create a new photocell table model */
	public PhotocellTableModel(JSONArray s) {
		status = s;
	}

	/** Get the column count */
	@Override
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	/** Get the column class */
	@Override
	public Class getColumnClass(int column) {
		return String.class;
	}

	/** Get the row count */
	@Override
	public int getRowCount() {
		return status.length();
	}

	/** Get the value at a specific cell */
	@Override
	public Object getValueAt(int row, int column) {
		return (row >= 0 && row < status.length())
		      ? getColumn(status.optJSONObject(row), column)
		      : null;
	}

	/** Create the table column model */
	public TableColumnModel createColumnModel() {
		TableColumnModel m = new DefaultTableColumnModel();
		m.addColumn(createColumn(COL_DESC, 120,
			I18N.get("dms.photocell.description")));
		m.addColumn(createColumn(COL_READING, 80,
			I18N.get("dms.photocell.reading")));
		return m;
	}
}
