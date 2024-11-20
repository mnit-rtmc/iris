/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2003-2024  Minnesota Department of Transportation
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
import java.util.HashMap;
import java.util.List;
import javax.swing.JComboBox;
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
			s.getSonarState().getDayMatchers(),
			false,  /* has_properties */
			true,   /* has_create_delete */
			false   /* has_name */
		);
	}

	/** Find a string in a list */
	static private Integer findString(List<String> list, String val) {
		int i = list.indexOf(val);
		return (i > 0) ? i : null;
	}

	/** Date format symbols */
	static private final DateFormatSymbols SYMBOLS =
		new DateFormatSymbols();

	/** Displayed string for a selection of "any" */
	static private final String ANY = "";

	/** List of all possible month selections */
	static private final ArrayList<String> MONTHS =
		new ArrayList<String>();
	static {
		String[] months = SYMBOLS.getMonths();
		MONTHS.add(ANY);
		for (int m = Calendar.JANUARY; m <= Calendar.DECEMBER; m++)
			MONTHS.add(months[m]);
	}

	/** Create a month selector combo box */
	static JComboBox<String> monthSelector() {
		return new JComboBox<String>(MONTHS.toArray(new String[0]));
	}

	/** List of all possible day selections */
	static private final ArrayList<String> DAYS =
		new ArrayList<String>();
	static {
		DAYS.add(ANY);
		for (int d = 1; d <= 31; d++)
			DAYS.add(String.valueOf(d));
	}

	/** Create a day selector combo box */
	static JComboBox<String> daySelector() {
		return new JComboBox<String>(DAYS.toArray(new String[0]));
	}

	/** List of all possible weekday selections */
	static private final ArrayList<String> WEEKDAYS =
		new ArrayList<String>();
	static {
		String[] weekdays = SYMBOLS.getWeekdays();
		WEEKDAYS.add(ANY);
		for (int d = Calendar.SUNDAY; d <= Calendar.SATURDAY; d++)
			WEEKDAYS.add(weekdays[d]);
	}

	/** Create a weekday selector combo box */
	static JComboBox<String> weekdaySelector() {
		return new JComboBox<String>(WEEKDAYS.toArray(new String[0]));
	}

	/** List of all possible week selections */
	static private final ArrayList<String> WEEKS =
		new ArrayList<String>();
	static {
		WEEKS.add(ANY);
		WEEKS.add(I18N.get("day.matcher.week.first"));
		WEEKS.add(I18N.get("day.matcher.week.second"));
		WEEKS.add(I18N.get("day.matcher.week.third"));
		WEEKS.add(I18N.get("day.matcher.week.fourth"));
		WEEKS.add(I18N.get("day.matcher.week.last"));
	}

	/** Create a week selector combo box */
	static JComboBox<String> weekSelector() {
		return new JComboBox<String>(WEEKS.toArray(new String[0]));
	}

	/** List of all possible shift selections */
	static private final ArrayList<String> SHIFTS =
		new ArrayList<String>();
	static {
		SHIFTS.add("-2");
		SHIFTS.add("-1");
		SHIFTS.add(ANY);
		SHIFTS.add("+1");
		SHIFTS.add("+2");
	}

	/** Find a shift string */
	static private Integer findShift(String val) {
		int i = SHIFTS.indexOf(val);
		if (i >= 0) {
			i -= 2;
			if (i != 0)
				return i;
		}
		return null;
	}

	/** Create a shift selector combo box */
	static JComboBox<String> shiftSelector() {
		return new JComboBox<String>(SHIFTS.toArray(new String[0]));
	}

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<DayMatcher>> createColumns() {
		ArrayList<ProxyColumn<DayMatcher>> cols =
			new ArrayList<ProxyColumn<DayMatcher>>(5);
		cols.add(new ProxyColumn<DayMatcher>("day.matcher.month", 100) {
			public Object getValueAt(DayMatcher dm) {
				Integer m = dm.getMonth();
				return MONTHS.get((m != null) ? m : 0);
			}
		});
		cols.add(new ProxyColumn<DayMatcher>("day.matcher.day", 64) {
			public Object getValueAt(DayMatcher dm) {
				Integer d = dm.getDay();
				return DAYS.get((d != null) ? d : 0);
			}
		});
		cols.add(new ProxyColumn<DayMatcher>("day.matcher.weekday", 100)
		{
			public Object getValueAt(DayMatcher dm) {
				Integer wd = dm.getWeekday();
				return WEEKDAYS.get((wd != null) ? wd : 0);
			}
		});
		cols.add(new ProxyColumn<DayMatcher>("day.matcher.week", 80) {
			public Object getValueAt(DayMatcher dm) {
				Integer w = dm.getWeek();
				if (w == null)
					w = 0;
				if (w < 0)
					w = 5;
				return WEEKS.get(w);
			}
		});
		cols.add(new ProxyColumn<DayMatcher>("day.matcher.shift", 64) {
			public Object getValueAt(DayMatcher dm) {
				Integer s = dm.getShift();
				if (s == null)
					s = 0;
				return SHIFTS.get(s + 2);
			}
		});
		return cols;
	}

	/** Selected day plan */
	private final DayPlan day_plan;

	/** Create a new day matcher table model */
	public DayMatcherModel(Session s, DayPlan dp) {
		super(s, descriptor(s), 12);
		day_plan = dp;
	}

	/** Check if a proxy is included in the list */
	@Override
	protected boolean check(DayMatcher proxy) {
		return proxy.getDayPlan() == day_plan;
	}

	/** Create a new day matcher */
	public void createObject(String m, String d, String wd, String w,
		String s)
	{
		Integer month = findString(MONTHS, m);
		Integer day = findString(DAYS, d);
		Integer weekday = findString(WEEKDAYS, wd);
		Integer week = findString(WEEKS, w);
		if (week != null && week == 5)
			week = -1;
		Integer shift = findShift(s);
		if (month != null || day != null || weekday != null ||
		    week != null)
		{
			String name = createUniqueName();
			if (name != null)
				create(name, month, day, weekday, week, shift);
		}
	}

	/** Create a unique day matcher name */
	private String createUniqueName() {
		for (int uid = 1; uid <= 999; uid++) {
			String n = "dm_" + uid;
			if (descriptor.cache.lookupObject(n) == null)
				return n;
		}
		return null;
	}

	/** Create a new day matcher */
	private void create(String name, Integer m, Integer d, Integer wd,
		Integer w, Integer s)
	{
		HashMap<String, Object> attrs = new HashMap<String, Object>();
		attrs.put("day_plan", day_plan);
		if (m != null)
			attrs.put("month", m);
		if (d != null)
			attrs.put("day", d);
		if (wd != null)
			attrs.put("weekday", wd);
		if (w != null)
			attrs.put("week", w);
		if (s != null)
			attrs.put("shift", s);
		descriptor.cache.createObject(name, attrs);
	}
}
