/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.warning;

import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumnModel;
import us.mn.state.dot.tms.WarningSign;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;
import us.mn.state.dot.tms.client.toast.SonarObjectForm;

/**
 * Table model for warning signs.
 *
 * @author Douglas Lau
 */
public class WarningSignModel extends ProxyTableModel<WarningSign> {

	/** Count of columns in table model */
	static protected final int COLUMN_COUNT = 2;

	/** Name column number */
	static protected final int COL_NAME = 0;

	/** Location column number */
	static protected final int COL_LOCATION = 1;

	/** Create a new warning sign table model */
	public WarningSignModel(Session s) {
		super(s, s.getSonarState().getWarningSigns());
	}

	/** Get the count of columns in the table */
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	/** Get the value at the specified cell */
	public Object getValueAt(int row, int column) {
		WarningSign s = getProxy(row);
		if(s == null)
			return null;
		switch(column) {
		case COL_NAME:
			return s.getName();
		case COL_LOCATION:
			return GeoLocHelper.getDescription(s.getGeoLoc());
		}
		return null;
	}

	/** Check if the specified cell is editable */
	public boolean isCellEditable(int row, int column) {
		return isLastRow(row) && column == COL_NAME;
	}

	/** Set the value at the specified cell */
	public void setValueAt(Object value, int row, int column) {
		WarningSign s = getProxy(row);
		if(column == COL_NAME) {
			String v = value.toString().trim();
			if(v.length() > 0)
				cache.createObject(v);
		}
	}

	/** Create the table column model */
	public TableColumnModel createColumnModel() {
		TableColumnModel m = new DefaultTableColumnModel();
		m.addColumn(createColumn(COL_NAME, 200, "Warning Sign"));
		m.addColumn(createColumn(COL_LOCATION, 300, "Location"));
		return m;
	}

	/** Determine if a properties form is available */
	public boolean hasProperties() {
		return true;
	}

	/** Create a properties form for one proxy */
	protected SonarObjectForm<WarningSign> createPropertiesForm(
		WarningSign proxy)
	{
		return new WarningSignProperties(session, proxy);
	}
}
