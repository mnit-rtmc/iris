/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2008  Minnesota Department of Transportation
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

import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.TimingPlan;
import us.mn.state.dot.tms.TimingPlanList;
import us.mn.state.dot.tms.utils.ExceptionDialog;
import us.mn.state.dot.tms.utils.RemoteListAdapter;

/**
 * Table model for timing plans 
 *
 * @author Douglas Lau
 */
public class TimingPlanModel extends AbstractTableModel {

	/** Count of columns in timing plan table model */
	static protected final int COLUMN_COUNT = 6;

	/** Plan type column number */
	static protected final int COL_TYPE = 0;

	/** Associated column number */
	static protected final int COL_ASSOCIATED = 1;

	/** Start time column number */
	static protected final int COL_START = 2;

	/** Stop time column number */
	static protected final int COL_STOP = 3;

	/** Active column number */
	static protected final int COL_ACTIVE = 4;

	/** Testing column number */
	static protected final int COL_TESTING = 5;

	/** Time parser formats */
	static protected final DateFormat[] TIME_FORMATS = {
		new SimpleDateFormat("h:mm a"),
		new SimpleDateFormat("H:mm"),
		new SimpleDateFormat("h a"),
		new SimpleDateFormat("H")
	};

	/** Parse a time string */
	static protected Date parseTime(String t) throws ChangeVetoException {
		for(int i = 0; i < TIME_FORMATS.length; i++) {
			try { return TIME_FORMATS[i].parse(t); }
			catch(ParseException e) {}
		}
		throw new ChangeVetoException("Invalid time format");
	}

	/** Parse a time string and return the minute-of-day */
	static protected int parseMinute(String t) throws ChangeVetoException {
		Calendar c = Calendar.getInstance();
		c.setTime(parseTime(t));
		return c.get(Calendar.HOUR_OF_DAY) * 60 +
			c.get(Calendar.MINUTE);
	}

	/** Convert minute-of-day to time string */
	static protected String time_string(int minute) {
		StringBuffer min = new StringBuffer();
		min.append(minute % 60);
		while(min.length() < 2)
			min.insert(0, '0');
		return (minute / 60) + ":" + min;
	}

	/** Cell renderer for this table */
	static protected final DefaultTableCellRenderer RENDERER =
		new DefaultTableCellRenderer();
	static {
		RENDERER.setHorizontalAlignment(SwingConstants.CENTER);
	}

	/** Create a new table column */
	static protected TableColumn createColumn(int col, String name,
		boolean center)
	{
		TableColumn c = new TableColumn(col);
		c.setHeaderValue(name);
		if(center)
			c.setCellRenderer(RENDERER);
		return c;
	}

	/** Create the table column model */
	static public TableColumnModel createColumnModel() {
		TableColumnModel m = new DefaultTableColumnModel();
		m.addColumn(createColumn(COL_TYPE, "Plan Type", true));
		m.addColumn(createColumn(COL_ASSOCIATED, "Associated", false));
		m.addColumn(createColumn(COL_START, "Start Time", true));
		m.addColumn(createColumn(COL_STOP, "Stop Time", true));
		m.addColumn(createColumn(COL_ACTIVE, "Active", false));
		m.addColumn(createColumn(COL_TESTING, "Testing", false));
		return m;
	}

	/** Timing plan remote list */
	protected final RemoteListAdapter planList;

	/** Timing plan list */
	protected final TimingPlanList plans;

	/** DMS whose form contains the timing plan model */
	protected final DMS dms;

	/** Administrator flag */
	protected final boolean admin;

	/** List of all rows (one for each timing plan) */
	protected final LinkedList<Object []> rows =
		new LinkedList<Object []>();

	/** Create a new timing plan table model */
	public TimingPlanModel(TimingPlanList plans, DMS dms, boolean a)
		throws RemoteException
	{
		this.plans = plans;
		this.dms = dms;
		final TimingPlanModel m = this;
		planList = new RemoteListAdapter(plans) {
			protected void doAdd(int index, Object element)
				throws RemoteException
			{
				m.doAdd(index, element);
			}
			protected Object doRemove(int index) {
				return m.doRemove(index);
			}
			protected void doSet(int index, Object element)
				throws RemoteException
			{
				m.doSet(index, element);
			}
		};
		planList.initialize();
		admin = a;
	}

	/** Dispose of the timing plan model */
	public void dispose() {
		planList.dispose();
	}

	/** Add a new timing plan to the table model */
	protected void doAdd(int index, Object element) throws RemoteException {
		TimingPlan p = (TimingPlan)element;
		rows.add(index, fillRow(createRow(), p));
		fireTableRowsInserted(index, index);
	}

	/** Remove a timing plan from the table model */
	protected Object doRemove(int index) {
		Object row = rows.remove(index);
		fireTableRowsDeleted(index, index);
		return row;
	}

	/** Set (update) a timing plan in the table model */
	protected void doSet(int index, Object element) throws RemoteException {
		TimingPlan p = (TimingPlan)element;
		fillRow(rows.get(index), p);
		fireTableRowsUpdated(index, index);
	}

	/** Create a new table row */
	protected Object[] createRow() {
		return new Object[COLUMN_COUNT];
	}

	/** Fill a row with a timing plan's values */
	protected Object[] fillRow(Object[] row, TimingPlan p)
		throws RemoteException
	{
		row[COL_TYPE] = p.getPlanType();
		row[COL_ASSOCIATED] = Boolean.valueOf(dms.hasTimingPlan(p));
		row[COL_START] = time_string(p.getStartTime());
		row[COL_STOP] = time_string(p.getStopTime());
		row[COL_ACTIVE] = Boolean.valueOf(p.isActive());
		row[COL_TESTING] = Boolean.valueOf(p.isTesting());
		return row;
	}

	/** Get the count of columns in the table */
	public int getColumnCount() { return COLUMN_COUNT; }

	/** Get the count of rows in the table */
	public int getRowCount() { return rows.size(); }

	/** Get the column class */
	public Class getColumnClass(int column) {
		if(column == COL_ASSOCIATED ||
			column == COL_ACTIVE ||
			column == COL_TESTING) return Boolean.class;
		else return String.class;
	}

	/** Get the value at the specified cell */
	public Object getValueAt(int row, int column) {
		Object[] r = rows.get(row);
		return r[column];
	}

	/** Check if the specified cell is editable */
	public boolean isCellEditable(int row, int column) {
		if(!admin) return false;
		if(column == COL_TYPE) return false;
		return true;
	}

	/** Set the value at the specified cell */
	public void setValueAt(Object value, int row, int column) {
		try {
			TimingPlan p = plans.getElement(row);
			switch(column) {
				case COL_ASSOCIATED:
					setPlan(p, value);
					break;
				case COL_START:
					setStart(p, value);
					break;
				case COL_STOP:
					setStop(p, value);
					break;
				case COL_ACTIVE:
					setActive(p, value);
					break;
				case COL_TESTING:
					setTesting(p, value);
					break;
			}
			plans.update(p);
		}
		catch(TMSException e) {
			new ExceptionDialog(e).setVisible(true);
		}
		catch(RemoteException e) {
			new ExceptionDialog(e).setVisible(true);
		}
	}

	/** Set the timing plan association */
	protected void setPlan(TimingPlan plan, Object value)
		throws TMSException, RemoteException
	{
		boolean a = ((Boolean)value).booleanValue();
		dms.setTimingPlan(plan, a);
	}

	/** Set the start time */
	protected void setStart(TimingPlan plan, Object value)
		throws TMSException, RemoteException
	{
		int minute = parseMinute(value.toString());
		plan.setStartTime(minute);
	}

	/** Set the stop time */
	protected void setStop(TimingPlan plan, Object value)
		throws TMSException, RemoteException
	{
		int minute = parseMinute(value.toString());
		plan.setStopTime(minute);
	}

	/** Set the plan active flag */
	protected void setActive(TimingPlan plan, Object value)
		throws TMSException, RemoteException
	{
		boolean active = ((Boolean)value).booleanValue();
		plan.setActive(active);
	}

	/** Set the plan testing flag */
	protected void setTesting(TimingPlan plan, Object value)
		throws RemoteException
	{
		boolean testing = ((Boolean)value).booleanValue();
		plan.setTesting(testing);
	}
}
