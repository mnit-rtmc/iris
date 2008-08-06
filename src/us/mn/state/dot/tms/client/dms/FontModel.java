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
package us.mn.state.dot.tms.client.dms;

import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumnModel;
import us.mn.state.dot.sonar.Role;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Font;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for DMS fonts
 *
 * @author Douglas Lau
 */
public class FontModel extends ProxyTableModel<Font> {

	/** Count of columns in table model */
	static protected final int COLUMN_COUNT = 6;

	/** Name column number */
	static protected final int COL_NAME = 0;

	/** Height column number */
	static protected final int COL_HEIGHT = 1;

	/** Width column number */
	static protected final int COL_WIDTH = 2;

	/** Line spacing column number */
	static protected final int COL_LINE_SPACING = 3;

	/** Character spacing column number */
	static protected final int COL_CHAR_SPACING = 4;

	/** Version ID column number */
	static protected final int COL_VERSION_ID = 5;

	/** Create a new font table model */
	public FontModel(TypeCache<Font> c, boolean a) {
		super(c, a);
		initialize();
	}

	/** Get the count of columns in the table */
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	/** Get the value at the specified cell */
	public Object getValueAt(int row, int column) {
		Font f = getProxy(row);
		if(f == null)
			return null;
		switch(column) {
			case COL_NAME:
				return f.getName();
			case COL_HEIGHT:
				return f.getHeight();
			case COL_WIDTH:
				return f.getWidth();
			case COL_LINE_SPACING:
				return f.getLineSpacing();
			case COL_CHAR_SPACING:
				return f.getCharSpacing();
			case COL_VERSION_ID:
				return f.getVersionID();
		}
		return null;
	}

	/** Get the class of the specified column */
	public Class getColumnClass(int column) {
		if(column == COL_NAME)
			return String.class;
		else
			return Integer.class;
	}

	/** Check if the specified cell is editable */
	public boolean isCellEditable(int row, int column) {
		if(!admin)
			return false;
		synchronized(proxies) {
			if(row == proxies.size())
				return column == COL_NAME;
		}
		if(column == COL_NAME)
			return false;
		return true;
	}

	/** Set the value at the specified cell */
	public void setValueAt(Object value, int row, int column) {
		Font f = getProxy(row);
		switch(column) {
			case COL_NAME:
				String v = value.toString().trim();
				if(v.length() > 0)
					cache.createObject(v);
				break;
			case COL_HEIGHT:
				f.setHeight((Integer)value);
				break;
			case COL_WIDTH:
				f.setWidth((Integer)value);
				break;
			case COL_LINE_SPACING:
				f.setLineSpacing((Integer)value);
				break;
			case COL_CHAR_SPACING:
				f.setCharSpacing((Integer)value);
				break;
			case COL_VERSION_ID:
				f.setVersionID((Integer)value);
				break;
		}
	}

	/** Create the table column model */
	public TableColumnModel createColumnModel() {
		TableColumnModel m = new DefaultTableColumnModel();
		m.addColumn(createColumn(COL_NAME, 200, "Font"));
		m.addColumn(createColumn(COL_HEIGHT, 100, "Height"));
		m.addColumn(createColumn(COL_WIDTH, 100, "Width"));
		m.addColumn(createColumn(COL_LINE_SPACING, 100,
			"Line Spacing"));
		m.addColumn(createColumn(COL_CHAR_SPACING, 100,
			"Char Spacing"));
		m.addColumn(createColumn(COL_VERSION_ID, 100, "Version ID"));
		return m;
	}
}
