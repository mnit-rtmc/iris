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

import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * LampTableModel is a table model for lamp status.
 *
 * @author Douglas Lau
 */
public class LampTableModel extends AbstractTableModel {

	/** Count of columns in table model */
	static protected final int COLUMN_COUNT = 3;

	/** Lamp column number */
	static protected final int COL_LAMP = 0;

	/** Stuck off column number */
	static protected final int COL_STUCK_OFF = 1;

	/** Stuck on column number */
	static protected final int COL_STUCK_ON = 2;

	/** Create a new table column */
	static protected TableColumn createColumn(int column, int width,
		String header)
	{
		TableColumn c = new TableColumn(column, width);
		c.setHeaderValue(header);
		return c;
	}

	/** Lookup a bit in a bitmap */
	static protected boolean lookupBit(byte[] bmap, int bit) {
		int pos = bit / 8;
		if(pos > bmap.length)
			return false;
		else {
			int val = bmap[pos];
			int mask = 1 << (bit % 8);
			return (val & mask) == mask;
		}
	}

	/** Stuck-off bitmap */
	protected final byte[] stuck_off;

	/** Stuck-on bitmap */
	protected final byte[] stuck_on;

	/** Create a new lamp table model */
	public LampTableModel(String[] bmaps) throws IOException {
		assert bmaps.length == 2;
		stuck_off = Base64.decode(bmap[DMS.STUCK_OFF_BITMAP]);
		stuck_on = Base64.decode(bmap[DMS.STUCK_ON_BITMAP]);
	}

	/** Get the column count */
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	/** Get the column class */
	public Class getColumnClass(int column) {
		if(column == COL_LAMP)
			return Integer.class;
		else
			return Boolean.class;
	}

	/** Get the row count */
	public int getRowCount() {
		return stuck_off.length * 8;
	}

	/** Get the value at a specific cell */
	public Object getValueAt(int row, int column) {
		switch(column) {
		case COL_LAMP:
			return row + 1;
		case COL_STUCK_OFF:
			return lookupBit(stuck_off, row);
		case COL_STUCK_ON:
			return lookupBit(stuck_on, row);
		default:
			return null;
		}
	}

	/** Create the table column model */
	public TableColumnModel createColumnModel() {
		TableColumnModel m = new DefaultTableColumnModel();
		m.addColumn(createColumn(COL_LAMP, 64, "Lamp"));
		m.addColumn(createColumn(COL_STUCK_OFF, 64, "Stuck Off"));
		m.addColumn(createColumn(COL_STUCK_ON, 64, "Stuck On"));
		return m;
	}
}
