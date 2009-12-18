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
package us.mn.state.dot.tms.client.toast;

import java.awt.Component;
import javax.swing.AbstractCellEditor;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.CabinetStyle;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for cabinet styles
 *
 * @author Douglas Lau
 */
public class CabinetStyleModel extends ProxyTableModel<CabinetStyle> {

	/** Count of columns in table model */
	static protected final int COLUMN_COUNT = 2;

	/** Name column number */
	static protected final int COL_NAME = 0;

	/** Dip switch column number */
	static protected final int COL_DIP = 1;

	/** Create a new cabinet style table model */
	public CabinetStyleModel(TypeCache<CabinetStyle> c) {
		super(c);
	}

	/** Get the count of columns in the table */
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	/** Get the value at the specified cell */
	public Object getValueAt(int row, int column) {
		CabinetStyle c = getProxy(row);
		if(c == null)
			return null;
		switch(column) {
		case COL_NAME:
			return c.getName();
		case COL_DIP:
			return c.getDip();
		default:
			return null;
		}
	}

	/** Check if the specified cell is editable */
	public boolean isCellEditable(int row, int column) {
		if(isLastRow(row))
			return column == COL_NAME;
		else
			return column != COL_NAME;
	}

	/** Set the value at the specified cell */
	public void setValueAt(Object value, int row, int column) {
		String v = value.toString().trim();
		CabinetStyle c = getProxy(row);
		switch(column) {
		case COL_NAME:
			if(v.length() > 0)
				cache.createObject(v);
			break;
		case COL_DIP:
			c.setDip((Integer)value);
			break;
		}
	}

	/** Create the dip column */
	protected TableColumn createDipColumn() {
		TableColumn c = new TableColumn(COL_DIP, 60);
		c.setHeaderValue("Dip");
		c.setCellEditor(new DipEditor());
		return c;
	}

	/** Editor for dip values in a table cell */
	public class DipEditor extends AbstractCellEditor
		implements TableCellEditor
	{
		protected final SpinnerNumberModel model =
			new SpinnerNumberModel(0, 0, 256, 1);
		protected final JSpinner spinner = new JSpinner(model);

		public Component getTableCellEditorComponent(JTable table,
			Object value, boolean isSelected, int row, int column)
		{
			if(value != null)
				spinner.setValue(value);
			else
				spinner.setValue(0);
			return spinner;
		}
		public Object getCellEditorValue() {
			return spinner.getValue();
		}
	}

	/** Create the table column model */
	public TableColumnModel createColumnModel() {
		TableColumnModel m = new DefaultTableColumnModel();
		m.addColumn(createColumn(COL_NAME, 90, "Style"));
		m.addColumn(createDipColumn());
		return m;
	}
}
