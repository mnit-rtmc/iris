/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2011  Minnesota Department of Transportation
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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sched.ListSelectionJob;
import us.mn.state.dot.sonar.Name;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.sonar.client.ProxyListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.DayPlan;
import us.mn.state.dot.tms.DayPlanHelper;
import us.mn.state.dot.tms.Holiday;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.toast.WrapperComboBoxModel;
import us.mn.state.dot.tms.client.widget.CalendarWidget;
import us.mn.state.dot.tms.client.widget.ZTable;

/**
 * A panel for editing day plans.
 *
 * @author Douglas Lau
 */
public class DayPlanPanel extends JPanel {

	/** Check if the user is permitted to use the form */
	static public boolean isPermitted(Session s) {
		return s.canRead(DayPlan.SONAR_TYPE) &&
		       s.canRead(Holiday.SONAR_TYPE);
	}

	/** Formatter for month labels */
	static protected final SimpleDateFormat MONTH_LBL =
		new SimpleDateFormat("MMMM");

	/** Formatter for year labels */
	static protected final SimpleDateFormat YEAR_LBL =
		new SimpleDateFormat("yyyy");

	/** Cache of day plans */
	protected final TypeCache<DayPlan> cache;

	/** Proxy listener to update day plan holiday model */
	protected final ProxyListener<DayPlan> listener;

	/** Combo box for day plans */
	protected final JComboBox day_cbox = new JComboBox();

	/** Button to delete the selected day plan */
	protected final JButton del_plan = new JButton("Delete");

	/** Month to display on calendar widget */
	protected final Calendar month = Calendar.getInstance();

	/** Button to select previous month */
	protected final JButton prev_month = new JButton("<");

	/** Month label */
	protected final JLabel month_lbl = new JLabel();

	/** Button to select next month */
	protected final JButton next_month = new JButton(">");

	/** Button to select previous year */
	protected final JButton prev_year = new JButton("<");

	/** Year label */
	protected final JLabel year_lbl = new JLabel();

	/** Button to select next year */
	protected final JButton next_year = new JButton(">");

	/** Calendar widget */
	protected final CalendarWidget cal_widget = new CalendarWidget();

	/** Table model for holidays */
	protected final HolidayModel h_model;

	/** Table to hold the holiday list */
	protected final ZTable h_table = new ZTable();

	/** Button to delete the selected holiday */
	protected final JButton del_holiday = new JButton("Delete");

	/** SONAR namespace */
	protected final Namespace namespace;

	/** Logged-in user */
	protected final User user;

	/** Create a new day plan panel */
	public DayPlanPanel(Session s) {
		super(new GridBagLayout());
		namespace = s.getSonarState().getNamespace();
		user = s.getUser();
		cache = s.getSonarState().getDayPlans();
		ListModel m = s.getSonarState().getDayModel();
		day_cbox.setPrototypeDisplayValue("0123456789");
		day_cbox.setModel(new WrapperComboBoxModel(m));
		day_cbox.setEditable(canAdd());
		GridBagConstraints bag = new GridBagConstraints();
		bag.insets.top = 2;
		bag.insets.bottom = 2;
		bag.insets.left = 2;
		bag.insets.right = 2;
		add(new JLabel("Day Plan"), bag);
		add(day_cbox, bag);
		bag.gridx = 0;
		bag.gridy = 1;
		bag.gridwidth = 2;
		add(del_plan, bag);
		bag.gridx = 2;
		bag.gridy = 0;
		bag.gridwidth = 1;
		bag.gridheight = 1;
		prev_month.setContentAreaFilled(false);
		prev_month.setRolloverEnabled(true);
		prev_month.setBorderPainted(false);
		add(prev_month, bag);
		bag.gridx = 3;
		setMonthLabel();
		add(month_lbl, bag);
		bag.gridx = 4;
		next_month.setContentAreaFilled(false);
		next_month.setRolloverEnabled(true);
		next_month.setBorderPainted(false);
		add(next_month, bag);
		bag.gridx = 5;
		prev_year.setContentAreaFilled(false);
		prev_year.setRolloverEnabled(true);
		prev_year.setBorderPainted(false);
		add(prev_year, bag);
		bag.gridx = 6;
		setYearLabel();
		add(year_lbl, bag);
		bag.gridx = 7;
		next_year.setContentAreaFilled(false);
		next_year.setRolloverEnabled(true);
		next_year.setBorderPainted(false);
		add(next_year, bag);
		bag.gridx = 2;
		bag.gridy = 1;
		bag.gridwidth = 6;
		bag.gridheight = 1;
		add(cal_widget, bag);
		h_model = new HolidayModel(s);
		h_model.initialize();
		final ListSelectionModel lsm = h_table.getSelectionModel();
		lsm.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		h_table.setModel(h_model);
		h_table.setAutoCreateColumnsFromModel(false);
		h_table.setColumnModel(h_model.createColumnModel());
		h_table.setRowHeight(22);
		h_table.setVisibleRowCount(12);
		bag.gridx = 0;
		bag.gridy = 2;
		bag.gridwidth = 9;
		bag.gridheight = 1;
		JScrollPane h_pane = new JScrollPane(h_table,
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		add(h_pane, bag);
		bag.gridx = 2;
		bag.gridy = 6;
		bag.gridwidth = GridBagConstraints.REMAINDER;
		add(del_holiday, bag);
		del_plan.setEnabled(false);
		del_holiday.setEnabled(false);
		createWidgetJobs();
		listener = createDayPlanListener();
		cache.addProxyListener(listener);
	}

	/** Create a proxy listener to update day plan holiday model */
	protected ProxyListener<DayPlan> createDayPlanListener() {
		return new ProxyListener<DayPlan>() {
			public void proxyAdded(DayPlan dp) {}
			public void enumerationComplete() {}
			public void proxyRemoved(DayPlan dp) {}
			public void proxyChanged(DayPlan dp, String attrib) {
				if(attrib.equals("holidays")) {
					h_model.updateHolidays(dp);
					updateCalendarWidget();
				}
			}
		};
	}

	/** Dispose of the panel */
	public void dispose() {
		h_model.dispose();
		cache.removeProxyListener(listener);
	}

	/** Create jobs for widget actions */
	protected void createWidgetJobs() {
		new ActionJob(this, day_cbox) {
			public void perform() {
				selectDayPlan();
			}
		};
		new ActionJob(this, del_plan) {
			public void perform() {
				deleteSelectedPlan();
			}
		};
		new ActionJob(this, prev_month) {
			public void perform() {
				addMonth(-1);
				updateCalendarWidget();
			}
		};
		new ActionJob(this, next_month) {
			public void perform() {
				addMonth(1);
				updateCalendarWidget();
			}
		};
		new ActionJob(this, prev_year) {
			public void perform() {
				addYear(-1);
				updateCalendarWidget();
			}
		};
		new ActionJob(this, next_year) {
			public void perform() {
				addYear(1);
				updateCalendarWidget();
			}
		};
		new ListSelectionJob(this, h_table.getSelectionModel()) {
			public void perform() {
				if(!event.getValueIsAdjusting())
					selectHoliday();
			}
		};
		new ActionJob(this, del_holiday) {
			public void perform() throws Exception {
				deleteSelectedHoliday();
			}
		};
		cal_widget.setHighlighter(new CalendarWidget.Highlighter() {
			public boolean isHighlighted(Calendar cal) {
				DayPlan dp = getSelectedPlan();
				if(dp != null)
					return DayPlanHelper.isHoliday(dp, cal);
				else
					return false;
			}
		});
	}

	/** Add a month to calendar widget */
	protected void addMonth(int m) {
		month.add(Calendar.MONTH, m);
	}

	/** Add a year to calendar widget */
	protected void addYear(int m) {
		month.add(Calendar.YEAR, m);
	}

	/** Update the calendar widget */
	protected void updateCalendarWidget() {
		setMonthLabel();
		setYearLabel();
		cal_widget.setMonth(month);
		cal_widget.revalidate();
		prev_month.setEnabled(true);
		next_month.setEnabled(true);
	}

	/** Set the month label */
	protected void setMonthLabel() {
		month_lbl.setText(MONTH_LBL.format(month.getTime()));
	}

	/** Set the year label */
	protected void setYearLabel() {
		year_lbl.setText(YEAR_LBL.format(month.getTime()));
	}

	/** Select a day plan */
	protected void selectDayPlan() {
		Object item = day_cbox.getSelectedItem();
		if(item != null) {
			DayPlan dp = getSelectedPlan();
			h_model.setDayPlan(dp);
			del_plan.setEnabled(canRemove(dp));
			if(dp == null) {
				day_cbox.setSelectedItem(null);
				String name = item.toString().trim();
				if(name.length() > 0 && canAdd(name))
					cache.createObject(name);
			}
		} else {
			h_model.setDayPlan(null);
			del_plan.setEnabled(false);
		}
		updateCalendarWidget();
	}

	/** Get the selected day plan */
	protected DayPlan getSelectedPlan() {
		Object item = day_cbox.getSelectedItem();
		if(item != null)
			return DayPlanHelper.lookup(item.toString().trim());
		else
			return null;
	}

	/** Delete the selected day plan */
	protected void deleteSelectedPlan() {
		Object item = day_cbox.getSelectedItem();
		if(item != null) {
			String name = item.toString();
			DayPlan dp = DayPlanHelper.lookup(name);
			if(dp != null)
				dp.destroy();
			day_cbox.setSelectedItem(null);
		}
	}

	/** Change the selected holiday */
	protected void selectHoliday() {
		Holiday h = h_model.getProxy(h_table.getSelectedRow());
		del_holiday.setEnabled(h_model.canRemove(h));
	}

	/** Delete the selected holiday */
	protected void deleteSelectedHoliday() {
		ListSelectionModel s = h_table.getSelectionModel();
		int row = s.getMinSelectionIndex();
		if(row >= 0)
			h_model.deleteRow(row);
	}

	/** Check if the user can add */
	public boolean canAdd() {
		return canAdd("oname");
	}

	/** Check if the user can add */
	protected boolean canAdd(String oname) {
		return namespace.canAdd(user, new Name(DayPlan.SONAR_TYPE,
		       oname));
	}

	/** Check if the user can update */
	public boolean canUpdate() {
		return namespace.canUpdate(user, new Name(DayPlan.SONAR_TYPE,
			"oname", "aname"));
	}

	/** Check if the user can remove a day plan */
	public boolean canRemove(DayPlan dp) {
		if(dp == null)
			return false;
		return namespace.canRemove(user, new Name(DayPlan.SONAR_TYPE,
		       dp.getName()));
	}
}
