/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2013  Minnesota Department of Transportation
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

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.tms.utils.I18N;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;

/**
 * A column in the proxy table model.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
abstract public class ProxyColumn<T extends SonarObject> {

	/** Column header */
	protected final String header;

	/** Column class */
	protected final Class c_class;

	/** Column width (pixels) */
	protected final int width;

	/** Create a new proxy column.
	 * @param tid Text ID for column header.
	 * @param w Width in pixels.
	 * @param c Column class. */
	public ProxyColumn(String tid, int w, Class c) {
		header = I18N.get(tid);
		width = UI.scaled(w);
		c_class = c;
	}

	/** Create a new proxy column */
	public ProxyColumn(String tid, int w) {
		this(tid, w, String.class);
	}

	/** Create a new proxy column */
	public ProxyColumn(String tid) {
		this(tid, 0, String.class);
	}

	/** Add a column to the table column model */
	public void addColumn(TableColumnModel m, int col) {
		TableColumn tc = createTableColumn(col);
		tc.setHeaderValue(header);
		tc.setCellRenderer(createCellRenderer());
		tc.setCellEditor(createCellEditor());
		m.addColumn(tc);
	}

	/** Create a table column */
	protected TableColumn createTableColumn(int col) {
		if(width > 0)
			return new TableColumn(col, width);
		else
			return new TableColumn(col);
	}

	/** Get the column class */
	public Class getColumnClass() {
		return c_class;
	}

	/** Create the table cell renderer */
	protected TableCellRenderer createCellRenderer() {
		return null;
	}

	/** Create the table cell editor */
	protected TableCellEditor createCellEditor() {
		return null;
	}

	/** Get the value of the column for the given row */
	public Object getValueAt(int row) {
		return null;
	}

	/** Get the value of the column for the given proxy */
	abstract public Object getValueAt(T proxy);

	/** Test if the column for the given proxy is editable */
	public boolean isEditable(T proxy) {
		return false;
	}

	/** Set the value of the column for the given proxy */
	public void setValueAt(T proxy, int row, Object value) {
		setValueAt(proxy, value);
	}

	/** Set the value of the column for the given proxy */
	public void setValueAt(T proxy, Object value) {
		// Subclasses must override for editable columns
	}
}
