/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2002  Minnesota Department of Transportation
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
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package us.mn.state.dot.tms;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * StatusTable is a class which encapsulates a table of information for the
 * client to display.
 *
 * @author Douglas Lau
 */
public class StatusTable implements Serializable {

	/** Column names */
	protected final String[] columns;

	/** Row data */
	protected final ArrayList rows = new ArrayList();

	/** Cell background colors */
	protected final ArrayList background = new ArrayList();

	/** Create a new status table */
	public StatusTable(String[] c) {
		columns = c;
	}

	/** Create an empty status table */
	public StatusTable() {
		columns = new String[] { "No data" };
	}

	/** Add a row to the status table */
	public void addRow(String[] r, int[] b) {
		rows.add(r);
		background.add(b);
	}

	/** Get the column count */
	public int getColumnCount() { return columns.length; }

	/** Get the row count */
	public int getRowCount() { return rows.size(); }

	/** Get a column name */
	public String getColumnName(int column) {
		return columns[column];
	}

	/** Get the value at a specific cell */
	public Object getValueAt(int row, int column) {
		String[] r = (String [])rows.get(row);
		return r[column];
	}

	/** Get the background color at a specified cell */
	public int getBackgroundAt(int row, int column) {
		int[] b = (int [])background.get(row);
		return b[column];
	}
}
