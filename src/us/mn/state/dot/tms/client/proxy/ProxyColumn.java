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
package us.mn.state.dot.tms.client.proxy;

import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import us.mn.state.dot.sonar.SonarObject;

/**
 * A column in the proxy table model.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
abstract public class ProxyColumn<T extends SonarObject> {

	/** Create a table column */
	static protected TableColumn createColumn(int column, int width,
		String header)
	{
		TableColumn c = new TableColumn(column, width);
		c.setHeaderValue(header);
		return c;
	}

	/** Column header */
	protected final String label;

	/** Column class */
	protected final Class c_class;

	/** Column width (pixels) */
	protected final int width;

	/** Create a new proxy column */
	public ProxyColumn(String l, Class c, int w) {
		label = l;
		c_class = c;
		width = w;
	}

	/** Add a column to the table column model */
	public void addColumn(TableColumnModel m, int index) {
		m.addColumn(createColumn(index, width, label));
	}

	/** Get the column class */
	public Class getColumnClass() {
		return c_class;
	}

	/** Get the value of the column for the given proxy */
	abstract public Object getValueAt(T proxy);

	/** Test if the column for the given proxy is editable */
	public boolean isEditable(T proxy) {
		return false;
	}

	/** Set the value of the column for the given proxy */
	public void setValueAt(T proxy, Object value) {
		// Subclasses must override for editable columns
	}
}
