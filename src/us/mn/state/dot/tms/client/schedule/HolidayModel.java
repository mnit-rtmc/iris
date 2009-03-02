/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2003-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.schedule;

import java.awt.Component;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.LinkedList;
import javax.swing.AbstractCellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Holiday;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for holidays
 *
 * @author Douglas Lau
 */
public class HolidayModel extends ProxyTableModel<Holiday> {

	/** Count of columns in holiday table model */
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

	/** Create a new holiday table model */
	public HolidayModel(TypeCache<Holiday> c) {
		super(c, true);
		initialize();
	}

	/** Get the count of columns in the table */
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	/** Get the value at the specified cell */
	public Object getValueAt(int row, int column) {
		Holiday h = getProxy(row);
		if(h == null)
			return null;
		switch(column) {
			case COL_NAME:
				return h.getName();
			case COL_MONTH:
				return MONTHS.get(h.getMonth() + 1);
			case COL_DAY:
				return DAYS.get(h.getDay());
			case COL_WEEK:
				return WEEKS.get(h.getWeek() + 1);
			case COL_WEEKDAY:
				return WEEKDAYS.get(h.getWeekday());
			case COL_SHIFT:
				return SHIFTS.get(h.getShift() + 2);
			case COL_PERIOD:
				return PERIODS.get(h.getPeriod() + 1);
			default:
				return null;
		}
	}

	/** Check if the day is blank for the specified holiday */
	protected boolean isDayBlank(Holiday h) {
		return h.getDay() == 0;
	}

	/** Check if the week/shift is blank for the specified holiday */
	protected boolean isWeekShiftBlank(Holiday h) {
		return (h.getWeek() == 0) && (h.getShift() == 0);
	}

	/** Check if the specified cell is editable */
	public boolean isCellEditable(int row, int column) {
		if(isLastRow(row))
			return column == COL_NAME;
		if(column == COL_NAME)
			return false;
		Holiday h = getProxy(row);
		if(column == COL_DAY)
			return isWeekShiftBlank(h);
		if(column == COL_WEEK)
			return isDayBlank(h);
		if(column == COL_SHIFT)
			return isDayBlank(h);
		return true;
	}

	/** Set the value at the specified cell */
	public void setValueAt(Object value, int row, int column) {
		Holiday h = getProxy(row);
		switch(column) {
			case COL_NAME:
				String v = value.toString().trim();
				if(v.length() > 0)
					cache.createObject(v);
				break;
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
	}

	/** Create the holiday name column */
	protected TableColumn createNameColumn() {
		TableColumn c = new TableColumn(COL_NAME, 200);
		c.setHeaderValue("Holiday Name");
		c.setCellEditor(new NameCellEditor());
		return c;
	}

	/** Create the month column */
	protected TableColumn createMonthColumn() {
		TableColumn c = new TableColumn(COL_MONTH, 100);
		c.setHeaderValue("Month");
		JComboBox combo = new JComboBox(MONTHS.toArray());
		c.setCellEditor(new DefaultCellEditor(combo));
		return c;
	}

	/** Create the day column */
	protected TableColumn createDayColumn() {
		TableColumn c = new TableColumn(COL_DAY, 64);
		c.setHeaderValue("Day");
		JComboBox combo = new JComboBox(DAYS.toArray());
		c.setCellEditor(new DefaultCellEditor(combo));
		return c;
	}

	/** Create the week column */
	protected TableColumn createWeekColumn() {
		TableColumn c = new TableColumn(COL_WEEK, 80);
		c.setHeaderValue("Week");
		JComboBox combo = new JComboBox(WEEKS.toArray());
		c.setCellEditor(new DefaultCellEditor(combo));
		return c;
	}

	/** Create the weekday column */
	protected TableColumn createWeekdayColumn() {
		TableColumn c = new TableColumn(COL_WEEKDAY, 100);
		c.setHeaderValue("Weekday");
		JComboBox combo = new JComboBox(WEEKDAYS.toArray());
		c.setCellEditor(new DefaultCellEditor(combo));
		return c;
	}

	/** Create the shift column */
	protected TableColumn createShiftColumn() {
		TableColumn c = new TableColumn(COL_SHIFT, 64);
		c.setHeaderValue("Shift");
		JComboBox combo = new JComboBox(SHIFTS.toArray());
		c.setCellEditor(new DefaultCellEditor(combo));
		return c;
	}

	/** Create the period column */
	protected TableColumn createPeriodColumn() {
		TableColumn c = new TableColumn(COL_PERIOD, 64);
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

	/** Inner class for editing cells in the name column */
	static protected class NameCellEditor extends AbstractCellEditor
		implements TableCellEditor
	{
		protected final JTextField text = new JTextField();
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
