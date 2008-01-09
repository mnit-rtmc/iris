/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005-2008  Minnesota Department of Transportation
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

import java.awt.Component;
import java.rmi.RemoteException;
import java.util.LinkedList;
import javax.swing.SwingConstants;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.AbstractCellEditor;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DmsMessage;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.utils.ExceptionDialog;

/**
 * Table model for DMS messages
 *
 * @author Douglas Lau
 */
public class DmsMessageModel extends AbstractTableModel {

	/** Count of columns in table model */
	static protected final int COLUMN_COUNT = 5;

	/** Message line column number */
	static protected final int COL_LINE = 0;

	/** Global flag column number */
	static protected final int COL_GLOBAL = 1;

	/** Message text column number */
	static protected final int COL_MESSAGE = 2;

	/** Message abbreviation column number */
	static protected final int COL_ABBREV = 3;

	/** Priority column number */
	static protected final int COL_PRIORITY = 4;

	/** Cell renderer for this table */
	static protected final DefaultTableCellRenderer RENDERER =
		new DefaultTableCellRenderer();
	static {
		RENDERER.setHorizontalAlignment(SwingConstants.CENTER);
	}

	/** Create a new table column */
	static protected TableColumn createColumn(int col, String name,
		boolean center, int width)
	{
		TableColumn c = new TableColumn(col, width);
		c.setHeaderValue(name);
		if(center)
			c.setCellRenderer(RENDERER);
		if(col == COL_PRIORITY)
			c.setCellEditor(new SpinnerCellEditor());
		return c;
	}

	/** Inner class for editing cells in the priority column */
	static protected class SpinnerCellEditor extends AbstractCellEditor
		implements TableCellEditor
	{
		protected final SpinnerNumberModel model =
			new SpinnerNumberModel(50, 1, 99, 1);
		protected final JSpinner spinner = new JSpinner(model);

		public Component getTableCellEditorComponent(JTable table,
			Object value, boolean isSelected, int row, int column)
		{
			model.setValue(value);
			return spinner;
		}
		public Object getCellEditorValue() {
			return model.getValue();
		}
	}

	/** Create the table column model */
	static public TableColumnModel createColumnModel() {
		TableColumnModel m = new DefaultTableColumnModel();
		m.addColumn(createColumn(COL_LINE, "Line", true, 40));
		m.addColumn(createColumn(COL_GLOBAL, "Global", false, 40));
		m.addColumn(createColumn(COL_MESSAGE, "Message", true, 200));
		m.addColumn(createColumn(COL_ABBREV, "Abbrev", true, 80));
		m.addColumn(createColumn(COL_PRIORITY, "Priority", false, 40));
		return m;
	}

	/** DMS message list */
	protected final DmsMessage[] messages;

	/** DMS whose form contains the timing plan model */
	protected final DMS dms;

	/** Administrator flag */
	protected final boolean admin;

	/** TIGER administrator flag */
	protected final boolean tiger;

	/** List of all rows (one for each timing plan) */
	protected final LinkedList<Object []> rows =
		new LinkedList<Object []>();

	/** Create a new DMS message table model */
	public DmsMessageModel(DMS dms, boolean a, boolean t)
		throws RemoteException
	{
		this.dms = dms;
		messages = dms.getMessages();
		for(int i = 0; i < messages.length; i++)
			rows.add(fillRow(createRow(), messages[i]));
		admin = a;
		tiger = t;
	}

	/** Create a new table row */
	protected Object[] createRow() {
		return new Object[COLUMN_COUNT];
	}

	/** Fill a row with a timing plan's values */
	protected Object[] fillRow(Object[] row, DmsMessage m) {
		row[COL_LINE] = new Short(m.line);
		row[COL_GLOBAL] = Boolean.valueOf(m.dms == null);
		row[COL_MESSAGE] = m.message;
		row[COL_ABBREV] = m.abbrev;
		row[COL_PRIORITY] = new Short(m.priority);
		return row;
	}

	/** Get the count of columns in the table */
	public int getColumnCount() { return COLUMN_COUNT; }

	/** Get the count of rows in the table */
	public int getRowCount() { return rows.size(); }

	/** Get the column class */
	public Class getColumnClass(int column) {
		if(column == COL_LINE)
			return Short.class;
		if(column == COL_GLOBAL)
			return Boolean.class;
		if(column == COL_PRIORITY)
			return Short.class;
		else
			return String.class;
	}

	/** Get the message at the specified row */
	public DmsMessage getRowMessage(int row) {
		return messages[row];
	}

	/** Get the value at the specified cell */
	public Object getValueAt(int row, int column) {
		Object[] r = rows.get(row);
		return r[column];
	}

	/** Check if the specified cell is editable */
	public boolean isCellEditable(int row, int column) {
		Object[] r = rows.get(row);
		boolean global = ((Boolean)r[COL_GLOBAL]).booleanValue();
		return column >= COL_MESSAGE && (admin || (tiger && !global));
	}

	/** Set the value at the specified cell */
	public void setValueAt(Object value, int row, int column) {
		Object[] r = rows.get(row);
		try {
			DmsMessage m = messages[row];
			switch(column) {
				case COL_MESSAGE:
					setMessage(m, value);
					break;
				case COL_ABBREV:
					setAbbrev(m, value);
					break;
				case COL_PRIORITY:
					setPriority(m, value);
					break;
				default:
					return;
			}
			r[column] = value;
		}
		catch(TMSException e) {
			new ExceptionDialog(e).setVisible(true);
		}
		catch(RemoteException e) {
			new ExceptionDialog(e).setVisible(true);
		}
	}

	/** Set the DMS message text */
	protected void setMessage(DmsMessage m, Object value)
		throws TMSException, RemoteException
	{
		String t = ((String)value).toUpperCase();
		m.message = t;
		dms.updateMessage(m);
	}

	/** Set the abbreviated message text */
	protected void setAbbrev(DmsMessage m, Object value)
		throws TMSException, RemoteException
	{
		String t = ((String)value).toUpperCase();
		m.abbrev = t;
		dms.updateMessage(m);
	}

	/** Set the message priority */
	protected void setPriority(DmsMessage m, Object value)
		throws TMSException, RemoteException
	{
		Number p = (Number)value;
		m.priority = p.shortValue();
		dms.updateMessage(m);
	}
}
