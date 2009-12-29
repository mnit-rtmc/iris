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
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import us.mn.state.dot.sonar.Name;
import us.mn.state.dot.tms.Holiday;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for holidays
 *
 * @author Douglas Lau
 */
public class HolidayModel extends ProxyTableModel<Holiday> {

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

	/** Create the columns in the model */
	protected ProxyColumn[] createColumns() {
	    // NOTE: half-indent to declare array
	    return new ProxyColumn[] {
		new ProxyColumn<Holiday>("Holiday Name", 200) {
			public Object getValueAt(Holiday h) {
				return h.getName();
			}
			public boolean isEditable(Holiday h) {
				return h == null && canAdd();
			}
			public void setValueAt(Holiday h, Object value) {
				String v = value.toString().trim();
				if(v.length() > 0)
					cache.createObject(v);
			}
		},
		new ProxyColumn<Holiday>("Month", 100) {
			public Object getValueAt(Holiday h) {
				return MONTHS.get(h.getMonth() + 1);
			}
			public boolean isEditable(Holiday h) {
				return canUpdate(h);
			}
			public void setValueAt(Holiday h, Object value) {
				h.setMonth(MONTHS.indexOf(value) - 1);
			}
			protected TableCellEditor createCellEditor() {
				JComboBox combo = new JComboBox(
					MONTHS.toArray());
				return new DefaultCellEditor(combo);
			}
		},
		new ProxyColumn<Holiday>("Day", 64) {
			public Object getValueAt(Holiday h) {
				return DAYS.get(h.getDay());
			}
			public boolean isEditable(Holiday h) {
				return canUpdate(h) && isWeekShiftBlank(h);
			}
			public void setValueAt(Holiday h, Object value) {
				h.setDay(DAYS.indexOf(value));
			}
			protected TableCellEditor createCellEditor() {
				JComboBox combo = new JComboBox(DAYS.toArray());
				return new DefaultCellEditor(combo);
			}
		},
		new ProxyColumn<Holiday>("Week", 80) {
			public Object getValueAt(Holiday h) {
				return WEEKS.get(h.getWeek() + 1);
			}
			public boolean isEditable(Holiday h) {
				return canUpdate(h) && isDayBlank(h);
			}
			public void setValueAt(Holiday h, Object value) {
				h.setWeek(WEEKS.indexOf(value) - 1);
			}
			protected TableCellEditor createCellEditor() {
				JComboBox combo = new JComboBox(
					WEEKS.toArray());
				return new DefaultCellEditor(combo);
			}
		},
		new ProxyColumn<Holiday>("Weekday", 100) {
			public Object getValueAt(Holiday h) {
				return WEEKDAYS.get(h.getWeekday());
			}
			public boolean isEditable(Holiday h) {
				return canUpdate(h);
			}
			public void setValueAt(Holiday h, Object value) {
				h.setWeekday(WEEKDAYS.indexOf(value));
			}
			protected TableCellEditor createCellEditor() {
				JComboBox combo = new JComboBox(
					WEEKDAYS.toArray());
				return new DefaultCellEditor(combo);
			}
		},
		new ProxyColumn<Holiday>("Shift", 64) {
			public Object getValueAt(Holiday h) {
				return SHIFTS.get(h.getShift() + 2);
			}
			public boolean isEditable(Holiday h) {
				return canUpdate(h) && isDayBlank(h);
			}
			public void setValueAt(Holiday h, Object value) {
				h.setShift(SHIFTS.indexOf(value) - 2);
			}
			protected TableCellEditor createCellEditor() {
				JComboBox combo = new JComboBox(
					SHIFTS.toArray());
				return new DefaultCellEditor(combo);
			}
		},
		new ProxyColumn<Holiday>("Period", 64) {
			public Object getValueAt(Holiday h) {
				return PERIODS.get(h.getPeriod() + 1);
			}
			public boolean isEditable(Holiday h) {
				return canUpdate(h);
			}
			public void setValueAt(Holiday h, Object value) {
				h.setPeriod(PERIODS.indexOf(value) - 1);
			}
			protected TableCellEditor createCellEditor() {
				JComboBox combo = new JComboBox(
					PERIODS.toArray());
				return new DefaultCellEditor(combo);
			}
		},
	    };
	}

	/** Create a new holiday table model */
	public HolidayModel(Session s) {
		super(s, s.getSonarState().getHolidays());
	}

	/** Check if the day is blank for the specified holiday */
	protected boolean isDayBlank(Holiday h) {
		return h.getDay() == 0;
	}

	/** Check if the week/shift is blank for the specified holiday */
	protected boolean isWeekShiftBlank(Holiday h) {
		return (h.getWeek() == 0) && (h.getShift() == 0);
	}

	/** Check if the user can add a holiday */
	public boolean canAdd() {
		return namespace.canAdd(user, new Name(Holiday.SONAR_TYPE,
		       "oname"));
	}
}
