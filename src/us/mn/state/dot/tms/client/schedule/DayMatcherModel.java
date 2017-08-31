/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2003-2017  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.DayMatcher;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;
import us.mn.state.dot.tms.utils.I18N;

/**
 * Table model for day matchers.
 *
 * @author Douglas Lau
 */
public class DayMatcherModel extends ProxyTableModel<DayMatcher> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<DayMatcher> descriptor(Session s) {
		return new ProxyDescriptor<DayMatcher>(
			s.getSonarState().getDayMatchers(), false
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
		WEEKS.add(I18N.get("day.matcher.week.last"));
		WEEKS.add(ANY);
		WEEKS.add(I18N.get("day.matcher.week.first"));
		WEEKS.add(I18N.get("day.matcher.week.second"));
		WEEKS.add(I18N.get("day.matcher.week.third"));
		WEEKS.add(I18N.get("day.matcher.week.fourth"));
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
	protected ArrayList<ProxyColumn<DayMatcher>> createColumns() {
		ArrayList<ProxyColumn<DayMatcher>> cols =
			new ArrayList<ProxyColumn<DayMatcher>>(8);
		cols.add(new ProxyColumn<DayMatcher>("day.matcher.assigned", 80,
			Boolean.class)
		{
			public Object getValueAt(DayMatcher dm) {
				return isAssigned(dm);
			}
			public boolean isEditable(DayMatcher dm) {
				return canWriteDayPlanDayMatchers();
			}
			public void setValueAt(DayMatcher dm, Object value) {
				if (value instanceof Boolean)
					setAssigned(dm, (Boolean) value);
			}
		});
		cols.add(new ProxyColumn<DayMatcher>("day.matcher.name", 200) {
			public Object getValueAt(DayMatcher dm) {
				return dm.getName();
			}
		});
		cols.add(new ProxyColumn<DayMatcher>("day.matcher.holiday", 80,
			Boolean.class)
		{
			public Object getValueAt(DayMatcher dm) {
				return dm.getHoliday();
			}
			public boolean isEditable(DayMatcher dm) {
				return canWrite(dm);
			}
			public void setValueAt(DayMatcher dm, Object value) {
				if (value instanceof Boolean)
					dm.setHoliday((Boolean) value);
			}
		});
		cols.add(new ProxyColumn<DayMatcher>("day.matcher.month", 100) {
			public Object getValueAt(DayMatcher dm) {
				return MONTHS.get(dm.getMonth() + 1);
			}
			public boolean isEditable(DayMatcher dm) {
				return canWrite(dm);
			}
			public void setValueAt(DayMatcher dm, Object value) {
				dm.setMonth(MONTHS.indexOf(value) - 1);
			}
			protected TableCellEditor createCellEditor() {
				JComboBox<String> cbx = new JComboBox<String>(
					MONTHS.toArray(new String[0]));
				return new DefaultCellEditor(cbx);
			}
		});
		cols.add(new ProxyColumn<DayMatcher>("day.matcher.day", 64) {
			public Object getValueAt(DayMatcher dm) {
				return DAYS.get(dm.getDay());
			}
			public boolean isEditable(DayMatcher dm) {
				return canWrite(dm) && isWeekShiftBlank(dm);
			}
			public void setValueAt(DayMatcher dm, Object value) {
				dm.setDay(DAYS.indexOf(value));
			}
			protected TableCellEditor createCellEditor() {
				JComboBox<String> cbx = new JComboBox<String>(
					DAYS.toArray(new String[0]));
				return new DefaultCellEditor(cbx);
			}
		});
		cols.add(new ProxyColumn<DayMatcher>("day.matcher.week", 80) {
			public Object getValueAt(DayMatcher dm) {
				return WEEKS.get(dm.getWeek() + 1);
			}
			public boolean isEditable(DayMatcher dm) {
				return canWrite(dm) && isDayBlank(dm);
			}
			public void setValueAt(DayMatcher dm, Object value) {
				dm.setWeek(WEEKS.indexOf(value) - 1);
			}
			protected TableCellEditor createCellEditor() {
				JComboBox<String> cbx = new JComboBox<String>(
					WEEKS.toArray(new String[0]));
				return new DefaultCellEditor(cbx);
			}
		});
		cols.add(new ProxyColumn<DayMatcher>("day.matcher.weekday", 100)
		{
			public Object getValueAt(DayMatcher dm) {
				return WEEKDAYS.get(dm.getWeekday());
			}
			public boolean isEditable(DayMatcher dm) {
				return canWrite(dm);
			}
			public void setValueAt(DayMatcher dm, Object value) {
				dm.setWeekday(WEEKDAYS.indexOf(value));
			}
			protected TableCellEditor createCellEditor() {
				JComboBox<String> cbx = new JComboBox<String>(
					WEEKDAYS.toArray(new String[0]));
				return new DefaultCellEditor(cbx);
			}
		});
		cols.add(new ProxyColumn<DayMatcher>("day.matcher.shift", 64) {
			public Object getValueAt(DayMatcher dm) {
				return SHIFTS.get(dm.getShift() + 2);
			}
			public boolean isEditable(DayMatcher dm) {
				return canWrite(dm) && isDayBlank(dm);
			}
			public void setValueAt(DayMatcher dm, Object value) {
				dm.setShift(SHIFTS.indexOf(value) - 2);
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

	/** Create a new day matcher table model */
	public DayMatcherModel(Session s, DayPlan dp) {
		super(s, descriptor(s), 16);
		day_plan = dp;
	}

	/** Check if the day is blank for the specified day matcher */
	private boolean isDayBlank(DayMatcher dm) {
		return dm.getDay() == 0;
	}

	/** Check if the week/shift is blank for the specified day matcher */
	private boolean isWeekShiftBlank(DayMatcher dm) {
		return (dm.getWeek() == 0) && (dm.getShift() == 0);
	}

	/** Check if the given day matcher is assigned to the day plan */
	private Boolean isAssigned(DayMatcher matcher) {
		if (day_plan != null) {
			for (DayMatcher dm: day_plan.getDayMatchers()) {
				if (dm == matcher)
					return true;
			}
			return false;
		}
		return null;
	}

	/** Assign or unassign the specified day matcher */
	private void setAssigned(DayMatcher dm, boolean a) {
		if (day_plan != null) {
			DayMatcher[] matchers = day_plan.getDayMatchers();
			if (a)
				matchers = addDayMatcher(matchers, dm);
			else
				matchers = removeDayMatcher(matchers, dm);
			day_plan.setDayMatchers(matchers);
		}
	}

	/** Add a day matcher to an array */
	private DayMatcher[] addDayMatcher(DayMatcher[] matchers,
		DayMatcher matcher)
	{
		TreeSet<DayMatcher> dm_set = new TreeSet<DayMatcher>(
			comparator());
		for (DayMatcher dm: matchers)
			dm_set.add(dm);
		dm_set.add(matcher);
		return dm_set.toArray(new DayMatcher[0]);
	}

	/** Remove a day matcher from an array */
	private DayMatcher[] removeDayMatcher(DayMatcher[] matchers,
		DayMatcher matcher)
	{
		TreeSet<DayMatcher> dm_set = new TreeSet<DayMatcher>(
			comparator());
		for (DayMatcher dm: matchers)
			dm_set.add(dm);
		dm_set.remove(matcher);
		return dm_set.toArray(new DayMatcher[0]);
	}

	/** Check if the user can write day matchers on the day plan */
	private boolean canWriteDayPlanDayMatchers() {
		return session.canWrite(day_plan, "dayMatchers");
	}
}
