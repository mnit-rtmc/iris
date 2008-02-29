/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.roads;

import java.rmi.RemoteException;
import java.util.ArrayList;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import us.mn.state.dot.tms.Detector;
import us.mn.state.dot.tms.IndexedList;
import us.mn.state.dot.tms.utils.AbstractJob;

/**
 * Table model for roadway node detectors
 *
 * @author Douglas Lau
 */
public class R_NodeDetectorModel extends AbstractTableModel {

	/** Count of columns in table model */
	static protected final int COLUMN_COUNT = 2;

	/** Detector ID column number */
	static protected final int COL_DET_ID = 0;

	/** Detector label column number */
	static protected final int COL_DET_LABEL = 1;

	/** Table cell renderer */
	static protected final DefaultTableCellRenderer RENDERER =
		new DefaultTableCellRenderer();
	static {
		RENDERER.setHorizontalAlignment(SwingConstants.CENTER);
	}

	/** Create a new table column */
	static protected TableColumn createColumn(int col, String name,
		int width)
	{
		TableColumn c = new TableColumn(col, width);
		c.setHeaderValue(name);
		c.setCellRenderer(RENDERER);
		return c;
	}

	/** Create the table column model */
	static public TableColumnModel createColumnModel() {
		TableColumnModel m = new DefaultTableColumnModel();
		m.addColumn(createColumn(COL_DET_ID, "ID", 60));
		m.addColumn(createColumn(COL_DET_LABEL, "Label", 200));
		return m;
	}

	/** Detector list */
	protected final IndexedList det_list;

	/** Administrator flag */
	protected final boolean admin;

	/** List of all detectors */
	protected final ArrayList<Detector> dets;

	/** List of all rows (one for each detector) */
	protected final ArrayList<Object []> rows;

	/** Create a new roadway node detector model */
	public R_NodeDetectorModel(IndexedList dlist, Detector[] da,
		boolean a) throws RemoteException
	{
		det_list = dlist;
		admin = a;
		dets = new ArrayList<Detector>(da.length);
		rows = new ArrayList<Object []>(da.length);
		for(Detector d: da) {
			dets.add(d);
			rows.add(createRow(d));
		}
	}

	/** Fill a row with a detector */
	protected void fillRow(Object[] row, Object oid, String label) {
		row[COL_DET_ID] = oid;
		row[COL_DET_LABEL] = label;
	}

	/** Create a new table row */
	protected Object[] createRow() {
		return new Object[COLUMN_COUNT];
	}

	/** Create a new detector row */
	protected Object[] createRow(Detector d) throws RemoteException {
		Integer oid = d.getIndex();
		String label = d.getLabel(false);
		Object[] row = createRow();
		fillRow(row, oid, label);
		return row;
	}

	/** Get the count of columns in the table */
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	/** Get the count of rows in the table */
	public int getRowCount() {
		return rows.size() + 1;
	}

	/** Get the column class */
	public Class getColumnClass(int column) {
		if(column == COL_DET_ID)
			return Integer.class;
		else
			return String.class;
	}

	/** Get the value at the specified cell */
	protected Object _getValueAt(int row, int column) {
		Object[] r = (Object [])rows.get(row);
		return r[column];
	}

	/** Get the value at the specified cell */
	public synchronized Object getValueAt(int row, int column) {
		if(row < rows.size())
			return _getValueAt(row, column);
		else
			return null;
	}

	/** Check if the specified cell is editable */
	public boolean isCellEditable(int row, int column) {
		return admin && column == COL_DET_ID;
	}

	/** Lookup a detector by ID */
	protected Detector lookupDetector(Integer did) throws RemoteException {
		try {
			if(did != null)
				return (Detector)det_list.getElement(did);
		}
		catch(IndexOutOfBoundsException e) {
			// Fall through here
		}
		return null;
	}

	/** Add a new row to the table */
	protected synchronized void addRow(Detector d) throws RemoteException {
		int row = rows.size();
		dets.add(d);
		rows.add(createRow(d));
		fireTableRowsInserted(row, row);
	}

	/** Add a new row to the table */
	protected void addRow(Integer did) throws RemoteException {
		Detector d = lookupDetector(did);
		if(d != null)
			addRow(d);
	}

	/** Remove a row from the table */
	public synchronized void removeRow(int row) {
		dets.remove(row);
		rows.remove(row);
		fireTableRowsDeleted(row, row);
	}

	/** Update the specified row with the given detector */
	protected synchronized void updateRow(int row, Detector d)
		throws RemoteException
	{
		dets.set(row, d);
		rows.set(row, createRow(d));
		fireTableRowsUpdated(row, row);
	}

	/** Update the specified row with the given detector ID */
	protected void updateRow(int row, Integer did) throws RemoteException {
		Detector d = lookupDetector(did);
		if(d != null)
			updateRow(row, d);
	}

	/** Set the detector ID for the specified row */
	public void setDetectorID(int row, Integer did) throws RemoteException {
		if(row >= rows.size())
			addRow(did);
		else if(did == null)
			removeRow(row);
		else
			updateRow(row, did);
	}

	/** Set the value at the specified cell */
	public void setValueAt(Object value, final int row, int column) {
		if(column == COL_DET_ID) {
			final Integer did = (Integer)value;
			new AbstractJob() {
				public void perform() throws RemoteException {
					setDetectorID(row, did);
				}
			}.addToScheduler();
		}
	}

	/** Get the detector at the specified row */
	public Integer getDetectorID(int row) {
		Object value = getValueAt(row, COL_DET_ID);
		if(value instanceof Integer)
			return (Integer)value;
		else
			return null;
	}

	/** Get the currently selected detectors */
	public Detector[] getDetectors() {
		return (Detector [])dets.toArray(new Detector[0]);
	}
}
