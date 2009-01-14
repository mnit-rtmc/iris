/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2002-2009  Minnesota Department of Transportation
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
 * PowerTableModel is a table model for power supply status.
 *
 * @author Douglas Lau
 */
public class PowerTableModel extends AbstractTableModel {

	/** Count of columns in table model */
	static protected final int COLUMN_COUNT = 4;

	/** Power supply column number */
	static protected final int COL_SUPPLY = 0;

	/** Power fail column number */
	static protected final int COL_FAIL = 1;

	/** Voltage out-of-spec column number */
	static protected final int COL_VOLTAGE = 2;

	/** Current out-of-spec column number */
	static protected final int COL_CURRENT = 3;

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

	/** Fail bitmap */
	protected final byte[] fail;

	/** Voltage bitmap */
	protected final byte[] voltage;

	/** Current bitmap */
	protected final byte[] current;

	/** Create a new power table model */
	public PowerTableModel(String[] bmaps) throws IOException {
		assert bmaps.length == 3;
		fail = Base64.decode(bmap[DMS.FAIL_BITMAP]);
		voltage = Base64.decode(bmap[DMS.VOLTAGE_BITMAP]);
		current = Base64.decode(bmap[DMS.CURRENT_BITMAP]);
	}

	/** Get the column count */
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	/** Get the column class */
	public Class getColumnClass(int column) {
		if(column == COL_SUPPLY)
			return Integer.class;
		else
			return Boolean.class;
	}

	/** Get the row count */
	public int getRowCount() {
		return errors.length;
	}

	/** Get the value at a specific cell */
	public Object getValueAt(int row, int column) {
		switch(column) {
		case COL_SUPPLY:
			return row + 1;
		case COL_FAIL:
			return lookupBit(fail, row);
		case COL_VOLTAGE:
			return lookupBit(voltage, row);
		case COL_CURRENT:
			return lookupBit(current, row);
		default:
			return null;
	}

	/** Create the table column model */
	public TableColumnModel createColumnModel() {
		TableColumnModel m = new DefaultTableColumnModel();
		m.addColumn(createColumn(COL_SUPPLY, 64, "Supply"));
		m.addColumn(createColumn(COL_FAIL, 64, "Fail"));
		m.addColumn(createColumn(COL_VOLTAGE, 64, "Voltage"));
		m.addColumn(createColumn(COL_CURRENT, 64, "Current"));
		return m;
	}
}
