/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2003-2016  Minnesota Department of Transportation
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

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.TreeSet;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.table.TableCellEditor;
import us.mn.state.dot.tms.DayPlan;
import us.mn.state.dot.tms.Holiday;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;
import us.mn.state.dot.tms.utils.I18N;

/**
 * Table model for holidays.
 *
 * @author Douglas Lau
 */
public class HolidayModel extends ProxyTableModel<Holiday> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<Holiday> descriptor(Session s) {
		return new ProxyDescriptor<Holiday>(
			s.getSonarState().getHolidays(), false
		);
	}

	/** Date format symbols */
	static private final DateFormatSymbols SYMBOLS =
		new DateFormatSymbols();

	/** Displayed string for a selection of "any" */
	static private final String ANY = "";

	/** List of all possible month selections */
	static private final LinkedList<String> MONTHS =
		new LinkedList<String>();
	static {
		String[] months = SYMBOLS.getMonths();
		MONTHS.add(ANY);
		for (int m = Calendar.JANUARY; m <= Calendar.DECEMBER; m++)
			MONTHS.add(months[m]);
	}

	/** List of all possible day selections */
	static private final LinkedList<String> DAYS =
		new LinkedList<String>();
	static {
		DAYS.add(ANY);
		for (int d = 1; d <= 31; d++)
			DAYS.add(String.valueOf(d));
	}

	/** List of all possible week selections */
	static private final LinkedList<String> WEEKS =
		new LinkedList<String>();
	static {
		WEEKS.add(I18N.get("action.plan.week.last"));
		WEEKS.add(ANY);
		WEEKS.add(I18N.get("action.plan.week.first"));
		WEEKS.add(I18N.get("action.plan.week.second"));
		WEEKS.add(I18N.get("action.plan.week.third"));
		WEEKS.add(I18N.get("action.plan.week.fourth"));
	}

	/** List of all possible weekday selections */
	static private final LinkedList<String> WEEKDAYS =
		new LinkedList<String>();
	static {
		String[] weekdays = SYMBOLS.getWeekdays();
		WEEKDAYS.add(ANY);
		for (int d = Calendar.SUNDAY; d <= Calendar.SATURDAY; d++)
			WEEKDAYS.add(weekdays[d]);
	}

	/** List of all possible shift selections */
	static private final LinkedList<String> SHIFTS =
		new LinkedList<String>();
	static {
		SHIFTS.add("-2");
		SHIFTS.add("-1");
		SHIFTS.add(ANY);
		SHIFTS.add("+1");
		SHIFTS.add("+2");
	}

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<Holiday>> createColumns() {
		ArrayList<ProxyColumn<Holiday>> cols =
			new ArrayList<ProxyColumn<Holiday>>(7);
		cols.add(new ProxyColumn<Holiday>("action.plan.assigned", 80,
			Boolean.class)
		{
			public Object getValueAt(Holiday h) {
				return isAssigned(h);
			}
			public boolean isEditable(Holiday h) {
				return canUpdateDayPlanHolidays();
			}
			public void setValueAt(Holiday h, Object value) {
				if (value instanceof Boolean)
					setAssigned(h, (Boolean)value);
			}
		});
		cols.add(new ProxyColumn<Holiday>("action.plan.holiday", 200) {
			public Object getValueAt(Holiday h) {
				return h.getName();
			}
		});
		cols.add(new ProxyColumn<Holiday>("action.plan.month", 100) {
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
				JComboBox<String> cbx = new JComboBox<String>(
					MONTHS.toArray(new String[0]));
				return new DefaultCellEditor(cbx);
			}
		});
		cols.add(new ProxyColumn<Holiday>("action.plan.day.name", 64) {
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
				JComboBox<String> cbx = new JComboBox<String>(
					DAYS.toArray(new String[0]));
				return new DefaultCellEditor(cbx);
			}
		});
		cols.add(new ProxyColumn<Holiday>("action.plan.week", 80) {
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
				JComboBox<String> cbx = new JComboBox<String>(
					WEEKS.toArray(new String[0]));
				return new DefaultCellEditor(cbx);
			}
		});
		cols.add(new ProxyColumn<Holiday>("action.plan.weekday", 100) {
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
				JComboBox<String> cbx = new JComboBox<String>(
					WEEKDAYS.toArray(new String[0]));
				return new DefaultCellEditor(cbx);
			}
		});
		cols.add(new ProxyColumn<Holiday>("action.plan.day.shift", 64) {
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
				JComboBox<String> cbx = new JComboBox<String>(
					SHIFTS.toArray(new String[0]));
				return new DefaultCellEditor(cbx);
			}
		});
		return cols;
	}

	/** Selected day plan */
	private final DayPlan day_plan;

	/** Create a new holiday table model */
	public HolidayModel(Session s, DayPlan dp) {
		super(s, descriptor(s), 16);
		day_plan = dp;
	}

	/** Check if the day is blank for the specified holiday */
	private boolean isDayBlank(Holiday h) {
		return h.getDay() == 0;
	}

	/** Check if the week/shift is blank for the specified holiday */
	private boolean isWeekShiftBlank(Holiday h) {
		return (h.getWeek() == 0) && (h.getShift() == 0);
	}

	/** Check if the given holiday is assigned */
	private Boolean isAssigned(Holiday hol) {
		if (day_plan != null) {
			for (Holiday h: day_plan.getHolidays()) {
				if (h == hol)
					return true;
			}
			return false;
		}
		return null;
	}

	/** Assign or unassign the specified holiday */
	private void setAssigned(Holiday h, boolean a) {
		if (day_plan != null) {
			Holiday[] holidays = day_plan.getHolidays();
			if (a)
				holidays = addHoliday(holidays, h);
			else
				holidays = removeHoliday(holidays, h);
			day_plan.setHolidays(holidays);
		}
	}

	/** Add a holiday to an array */
	private Holiday[] addHoliday(Holiday[] holidays, Holiday hol) {
		TreeSet<Holiday> h_set = new TreeSet<Holiday>(comparator());
		for (Holiday h: holidays)
			h_set.add(h);
		h_set.add(hol);
		return h_set.toArray(new Holiday[0]);
	}

	/** Remove a holiday from an array */
	private Holiday[] removeHoliday(Holiday[] holidays, Holiday hol) {
		TreeSet<Holiday> h_set = new TreeSet<Holiday>(comparator());
		for (Holiday h: holidays)
			h_set.add(h);
		h_set.remove(hol);
		return h_set.toArray(new Holiday[0]);
	}

	/** Check if the user can update day plan holidays */
	private boolean canUpdateDayPlanHolidays() {
		return session.canUpdate(day_plan, "holidays");
	}
}
