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
package us.mn.state.dot.tms.client.meter;

import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumnModel;
import us.mn.state.dot.sonar.Name;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;
import us.mn.state.dot.tms.client.toast.SonarObjectForm;

/**
 * Table model for ramp meters.
 *
 * @author Douglas Lau
 */
public class RampMeterModel extends ProxyTableModel<RampMeter> {

	/** Count of columns in table model */
	static protected final int COLUMN_COUNT = 2;

	/** Name column number */
	static protected final int COL_NAME = 0;

	/** Location column number */
	static protected final int COL_LOCATION = 1;

	/** Create a new ramp meter table model */
	public RampMeterModel(Session s) {
		super(s, s.getSonarState().getRampMeters());
	}

	/** Get the count of columns in the table */
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	/** Get the value at the specified cell */
	public Object getValueAt(int row, int column) {
		RampMeter m = getProxy(row);
		if(m == null)
			return null;
		switch(column) {
			case COL_NAME:
				return m.getName();
			case COL_LOCATION:
				return GeoLocHelper.getDescription(
					m.getGeoLoc());
		}
		return null;
	}

	/** Get the class of the specified column */
	public Class getColumnClass(int column) {
		return String.class;
	}

	/** Check if the specified cell is editable */
	public boolean isCellEditable(int row, int col) {
		RampMeter mtr = getProxy(row);
		return (mtr == null) && (col == COL_NAME) && canAdd();
	}

	/** Set the value at the specified cell */
	public void setValueAt(Object value, int row, int column) {
		if(column == COL_NAME) {
			String v = value.toString().trim();
			if(v.length() > 0)
				cache.createObject(v);
		}
	}

	/** Create the table column model */
	public TableColumnModel createColumnModel() {
		TableColumnModel m = new DefaultTableColumnModel();
		m.addColumn(createColumn(COL_NAME, 200, "Meter"));
		m.addColumn(createColumn(COL_LOCATION, 300, "Location"));
		return m;
	}

	/** Determine if a properties form is available */
	public boolean hasProperties() {
		return true;
	}

	/** Create a properties form for one proxy */
	protected SonarObjectForm<RampMeter> createPropertiesForm(
		RampMeter proxy)
	{
		return new RampMeterProperties(session, proxy);
	}

	/** Check if the user can add a proxy */
	public boolean canAdd() {
		return namespace.canAdd(user, new Name(RampMeter.SONAR_TYPE));
	}
}
