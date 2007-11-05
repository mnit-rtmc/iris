/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2003-2007  Minnesota Department of Transportation
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
import java.rmi.RemoteException;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.LinkedList;

import javax.swing.AbstractCellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import us.mn.state.dot.tms.MeteringHoliday;
import us.mn.state.dot.tms.MeteringHolidayList;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.utils.ExceptionDialog;
import us.mn.state.dot.tms.utils.RemoteListAdapter;

/**
 * Table model for metering holidays
 *
 * @author Douglas Lau
 */
public class MeteringHolidayModel extends AbstractTableModel {

	/** Count of columns in metering holiday table model */
	static protected final int COLUMN_COUNT = 7;

	/** Name column number */
	static protected final int COL_NAME = 0;

	/** Month column number */
	static protected final int COL_MONTH = 1;

	/** Day column number */
	static protected final int COL_DAY = 2;

	/** Week column number */
	static protected final int COL_WEEK = 3;

	/** Weekday column number */
	static protected final int COL_WEEKDAY = 4;

	/** Shift column number */
	static protected final int COL_SHIFT = 5;

	/** Period column number */
	static protected final int COL_PERIOD = 6;

	/** Date format symbols */
	static protected final DateFormatSymbols SYMBOLS =
		new DateFormatSymbols();

	/** Displayed string for a selection of "any" */
	static protected final String ANY = "";

	/** List of all possible month selections */
	static protected final LinkedList<String> MONTHS =
		new LinkedList<String>();
	static {
		String[] months = SYMBOLS.getMonths();
		MONTHS.add(ANY);
		for(int m = Calendar.JANUARY; m <= Calendar.DECEMBER; m++)
			MONTHS.add(months[m]);
	}

	/** List of all possible day selections */
	static protected final LinkedList<String> DAYS =
		new LinkedList<String>();
	static {
		DAYS.add(ANY);
		for(int d = 1; d <= 31; d++)
			DAYS.add(String.valueOf(d));
	}

	/** List of all possible week selections */
	static protected final LinkedList<String> WEEKS =
		new LinkedList<String>();
	static {
		WEEKS.add("Last");
		WEEKS.add(ANY);
		WEEKS.add("First");
		WEEKS.add("Second");
		WEEKS.add("Third");
		WEEKS.add("Fourth");
	}

	/** List of all possible weekday selections */
	static protected final LinkedList<String> WEEKDAYS =
		new LinkedList<String>();
	static {
		String[] weekdays = SYMBOLS.getWeekdays();
		WEEKDAYS.add(ANY);
		for(int d = Calendar.SUNDAY; d <= Calendar.SATURDAY; d++)
			WEEKDAYS.add(weekdays[d]);
	}

	/** List of all possible shift selections */
	static protected final LinkedList<String> SHIFTS =
		new LinkedList<String>();
	static {
		SHIFTS.add("-2");
		SHIFTS.add("-1");
		SHIFTS.add(ANY);
		SHIFTS.add("+1");
		SHIFTS.add("+2");
	}

	/** List of all possible period selections */
	static protected final LinkedList<String> PERIODS =
		new LinkedList<String>();
	static {
		PERIODS.add(ANY);
		PERIODS.add("AM");
		PERIODS.add("PM");
	}

	/** Metering holiday remote list */
	protected final RemoteListAdapter holidayList;

	/** Metering holiday list */
	protected final MeteringHolidayList holidays;

	/** Administrator flag */
	protected final boolean admin;

	/** List of all rows (one for each metering holiday) */
	protected final LinkedList<String []> rows =
		new LinkedList<String []>();

	/** Create a new metering holiday table model */
	public MeteringHolidayModel(MeteringHolidayList holidays, boolean a)
		throws RemoteException
	{
		this.holidays = holidays;
		final MeteringHolidayModel m = this;
		holidayList = new RemoteListAdapter(holidays) {
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
		holidayList.initialize();
		admin = a;
		if(admin)
			rows.add(createRow());
	}

	/** Dispose of the metering holiday model */
	public void dispose() {
		holidayList.dispose();
	}

	/** Add a new holiday to the table model */
	protected void doAdd(int index, Object element) throws RemoteException {
		MeteringHoliday h = getHoliday((String)element);
		rows.add(index, fillRow(createRow(), h));
		fireTableRowsInserted(index, index);
	}

	/** Remove a holiday from the table model */
	protected Object doRemove(int index) {
		Object row = rows.remove(index);
		fireTableRowsDeleted(index, index);
		return row;
	}

	/** Set (update) a holiday in the table model */
	protected void doSet(int index, Object element) throws RemoteException {
		MeteringHoliday h = getHoliday(getName(index));
		fillRow(rows.get(index), h);
		fireTableRowsUpdated(index, index);
	}

	/** Create a new table row */
	protected String[] createRow() {
		String[] row = new String[COLUMN_COUNT];
		row[COL_NAME] = ANY;
		row[COL_MONTH] = ANY;
		row[COL_DAY] = ANY;
		row[COL_WEEK] = ANY;
		row[COL_WEEKDAY] = ANY;
		row[COL_SHIFT] = ANY;
		row[COL_PERIOD] = ANY;
		return row;
	}

	/** Fill a row with a metering holidays values */
	protected String[] fillRow(String[] row, MeteringHoliday h)
		throws RemoteException
	{
		row[COL_NAME] = h.getName();
		row[COL_MONTH] = MONTHS.get(h.getMonth() + 1);
		row[COL_DAY] = DAYS.get(h.getDay());
		row[COL_WEEK] = WEEKS.get(h.getWeek() + 1);
		row[COL_WEEKDAY] = WEEKDAYS.get(h.getWeekday());
		row[COL_SHIFT] = SHIFTS.get(h.getShift() + 2);
		row[COL_PERIOD] = PERIODS.get(h.getPeriod() + 1);
		return row;
	}

	/** Get the count of columns in the table */
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	/** Get the count of rows in the table */
	public int getRowCount() {
		return rows.size();
	}

	/** Get the value at the specified cell */
	public Object getValueAt(int row, int column) {
		String[] r = rows.get(row);
		return r[column];
	}

	/** Check if the specified cell is editable */
	public boolean isCellEditable(int row, int column) {
		if(!admin)
			return false;
		if(row == rows.size() - 1)
			return column == COL_NAME;
		if(column == COL_NAME)
			return false;
		if(column == COL_DAY)
			return isWeekShiftBlank(row);
		if(column == COL_WEEK)
			return isDayBlank(row);
		if(column == COL_SHIFT)
			return isDayBlank(row);
		return true;
	}

	/** Check if the day is blank for the specified row */
	protected boolean isDayBlank(int row) {
		String[] r = rows.get(row);
		return ANY.equals(r[COL_DAY]);
	}

	/** Check if the week/shift is blank for the specified row */
	protected boolean isWeekShiftBlank(int row) {
		String[] r = rows.get(row);
		return ANY.equals(r[COL_WEEK]) && ANY.equals(r[COL_SHIFT]);
	}

	/** Set the value at the specified cell */
	public void setValueAt(Object value, int row, int column) {
		try {
			String name = getName(row);
			MeteringHoliday h = getHoliday(name);
			switch(column) {
				case COL_NAME:
					createHoliday(value.toString());
					return;
				case COL_MONTH:
					h.setMonth(MONTHS.indexOf(value) - 1);
					break;
				case COL_DAY:
					h.setDay(DAYS.indexOf(value));
					break;
				case COL_WEEK:
					h.setWeek(WEEKS.indexOf(value) - 1);
					break;
				case COL_WEEKDAY:
					h.setWeekday(WEEKDAYS.indexOf(value));
					break;
				case COL_SHIFT:
					h.setShift(SHIFTS.indexOf(value) - 2);
					break;
				case COL_PERIOD:
					h.setPeriod(PERIODS.indexOf(value) - 1);
					break;
			}
			holidays.update(name);
		}
		catch(TMSException e) {
			new ExceptionDialog(e).setVisible(true);
		}
		catch(RemoteException e) {
			new ExceptionDialog(e).setVisible(true);
		}
	}

	/** Create a new holiday */
	protected void createHoliday(String name) throws TMSException,
		RemoteException
	{
		if(!name.equals(""))
			holidays.add(name);
	}

	/** Get the holiday name for the specified row */
	public String getName(int row) {
		String[] r = rows.get(row);
		return r[COL_NAME];
	}

	/** Get the metering holiday for the specified name */
	protected MeteringHoliday getHoliday(String name)
		throws RemoteException
	{
		return (MeteringHoliday)holidays.getElement(name);
	}

	/** Create the holiday name column */
	protected TableColumn createNameColumn() {
		TableColumn c = new TableColumn(COL_NAME, 340);
		c.setHeaderValue("Holiday Name");
		if(admin) {
			c.setCellRenderer(new NameCellRenderer());
			c.setCellEditor(new NameCellEditor());
		}
		return c;
	}

	/** Create the month column */
	protected TableColumn createMonthColumn() {
		TableColumn c = new TableColumn(COL_MONTH, 200);
		c.setHeaderValue("Month");
		JComboBox combo = new JComboBox(MONTHS.toArray());
		c.setCellEditor(new DefaultCellEditor(combo));
		return c;
	}

	/** Create the day column */
	protected TableColumn createDayColumn() {
		TableColumn c = new TableColumn(COL_DAY, 86);
		c.setHeaderValue("Day");
		JComboBox combo = new JComboBox(DAYS.toArray());
		c.setCellEditor(new DefaultCellEditor(combo));
		return c;
	}

	/** Create the week column */
	protected TableColumn createWeekColumn() {
		TableColumn c = new TableColumn(COL_WEEK, 120);
		c.setHeaderValue("Week");
		JComboBox combo = new JComboBox(WEEKS.toArray());
		c.setCellEditor(new DefaultCellEditor(combo));
		return c;
	}

	/** Create the weekday column */
	protected TableColumn createWeekdayColumn() {
		TableColumn c = new TableColumn(COL_WEEKDAY, 160);
		c.setHeaderValue("Weekday");
		JComboBox combo = new JComboBox(WEEKDAYS.toArray());
		c.setCellEditor(new DefaultCellEditor(combo));
		return c;
	}

	/** Create the shift column */
	protected TableColumn createShiftColumn() {
		TableColumn c = new TableColumn(COL_SHIFT, 80);
		c.setHeaderValue("Shift");
		JComboBox combo = new JComboBox(SHIFTS.toArray());
		c.setCellEditor(new DefaultCellEditor(combo));
		return c;
	}

	/** Create the period column */
	protected TableColumn createPeriodColumn() {
		TableColumn c = new TableColumn(COL_PERIOD, 100);
		c.setHeaderValue("Period");
		JComboBox combo = new JComboBox(PERIODS.toArray());
		c.setCellEditor(new DefaultCellEditor(combo));
		return c;
	}

	/** Create the table column model */
	public TableColumnModel createColumnModel() {
		TableColumnModel m = new DefaultTableColumnModel();
		m.addColumn(createNameColumn());
		m.addColumn(createMonthColumn());
		m.addColumn(createDayColumn());
		m.addColumn(createWeekColumn());
		m.addColumn(createWeekdayColumn());
		m.addColumn(createShiftColumn());
		m.addColumn(createPeriodColumn());
		return m;
	}

	/** Inner class for rendering cells in the name column */
	protected class NameCellRenderer extends DefaultTableCellRenderer {
		protected final JButton button = new JButton("Add Holiday");
		public Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean hasFocus,
			int row, int column)
		{
			if(row == rows.size() - 1)
				return button;
			return super.getTableCellRendererComponent(table, value,
				isSelected, hasFocus, row, column);
		}
	}

	/** Inner class for editing cells in the name column */
	protected class NameCellEditor extends AbstractCellEditor
		implements TableCellEditor
	{
		protected JTextField text = new JTextField();
		public Component getTableCellEditorComponent(JTable table,
			Object value, boolean isSelected, int row, int column)
		{
			text.setText("");
			return text;
		}
		public Object getCellEditorValue() {
			return text.getText();
		}
	}
}
