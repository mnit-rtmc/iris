/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2002-2022  Minnesota Department of Transportation
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
 * PowerTableModel is a table model for power supply status.
 *
 * @author Douglas Lau
 */
public class PowerTableModel extends AbstractTableModel {

	/** Count of columns in table model */
	static private final int COLUMN_COUNT = 5;

	/** Power supply description column number */
	static private final int COL_DESC = 0;

	/** Power supply type column number */
	static private final int COL_TYPE = 1;

	/** Power supply status column number */
	static private final int COL_STATUS = 2;

	/** Power supply detail column number */
	static private final int COL_DETAIL = 3;

	/** Power supply voltage column number */
	static private final int COL_VOLTAGE = 4;

	/** Create a new table column */
	static private TableColumn createColumn(int column, int width,
		String header)
	{
		TableColumn c = new TableColumn(column, UI.scaled(width));
		c.setHeaderValue(header);
		return c;
	}

	/** Get one power supply value */
	static private String getColumn(JSONObject ps, int column) {
		switch (column) {
			case COL_DESC: return ps.optString("description");
			case COL_TYPE: return ps.optString("supply_type");
			case COL_STATUS: return ps.optString("power_status");
			case COL_DETAIL: return ps.optString("detail");
			case COL_VOLTAGE:
				Number v = ps.optNumber("voltage");
				return (v != null) ? v.toString() : null;
			default: return null;
		}
	}

	/** Power supply array status */
	private final JSONArray status;

	/** Create a new power table model */
	public PowerTableModel(JSONArray st) {
		status = st;
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
			I18N.get("dms.power.description")));
		m.addColumn(createColumn(COL_TYPE, 80,
			I18N.get("dms.power.type")));
		m.addColumn(createColumn(COL_STATUS, 80,
			I18N.get("dms.power.status")));
		m.addColumn(createColumn(COL_DETAIL, 100,
			I18N.get("dms.power.detail")));
		m.addColumn(createColumn(COL_VOLTAGE, 80,
			I18N.get("dms.power.voltage")));
		return m;
	}
}
