/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2013  Minnesota Department of Transportation
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
import javax.swing.JButton;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import us.mn.state.dot.sched.ListSelectionJob;
import us.mn.state.dot.sonar.client.ProxyListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.DayPlan;
import us.mn.state.dot.tms.DayPlanHelper;
import us.mn.state.dot.tms.Holiday;
import static us.mn.state.dot.tms.client.IrisClient.WORKER;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.widget.CalendarWidget;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.ILabel;
import us.mn.state.dot.tms.client.widget.WrapperComboBoxModel;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
import us.mn.state.dot.tms.client.widget.ZTable;

/**
 * A panel for editing day plans.
 *
 * @author Douglas Lau
 */
public class DayPlanPanel extends JPanel {

	/** Table row height */
	static private final int ROW_HEIGHT = UI.scaled(22);

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

	/** Action for day plans */
	private final IAction day = new IAction("action.plan.day") {
		@Override protected void do_perform() {
			selectDayPlan();
		}
	};

	/** Combo box for day plans */
	private final JComboBox day_cbox = new JComboBox();

	/** Action to delete the selected day plan */
	private final IAction del_plan = new IAction("action.plan.day.delete") {
		@Override protected void do_perform() {
			deleteSelectedPlan();
		}
	};

	/** Month to display on calendar widget */
	protected final Calendar month = Calendar.getInstance();

	/** Action to select previous month */
	private final IAction prev_month =new IAction("action.plan.month.prev"){
		@Override protected void do_perform() {
			month.add(Calendar.MONTH, -1);
			updateCalendarWidget();
		}
	};

	/** Month label */
	protected final JLabel month_lbl = new JLabel();

	/** Action to select next month */
	private final IAction next_month =new IAction("action.plan.month.next"){
		@Override protected void do_perform() {
			month.add(Calendar.MONTH, 1);
			updateCalendarWidget();
		}
	};

	/** Action to select previous year */
	private final IAction prev_year = new IAction("action.plan.year.prev") {
		@Override protected void do_perform() {
			month.add(Calendar.YEAR, -1);
			updateCalendarWidget();
		}
	};

	/** Year label */
	protected final JLabel year_lbl = new JLabel();

	/** Action to select next year */
	private final IAction next_year = new IAction("action.plan.year.next") {
		@Override protected void do_perform() {
			month.add(Calendar.YEAR, 1);
			updateCalendarWidget();
		}
	};

	/** Calendar widget */
	protected final CalendarWidget cal_widget = new CalendarWidget();

	/** Table model for holidays */
	protected final HolidayModel h_model;

	/** Table to hold the holiday list */
	protected final ZTable h_table = new ZTable();

	/** Action to delete the selected holiday */
	private final IAction del_holiday = new IAction(
		"action.plan.holiday.delete")
	{
		@Override protected void do_perform() {
			deleteSelectedHoliday();
		}
	};

	/** User session */
	private final Session session;

	/** Create a new day plan panel */
	public DayPlanPanel(Session s) {
		super(new GridBagLayout());
		session = s;
		cache = s.getSonarState().getDayPlans();
		ListModel m = s.getSonarState().getDayModel();
		day_cbox.setAction(day);
		day_cbox.setPrototypeDisplayValue("0123456789");
		day_cbox.setModel(new WrapperComboBoxModel(m));
		day_cbox.setEditable(canAdd());
		GridBagConstraints bag = new GridBagConstraints();
		bag.insets.top = 2;
		bag.insets.bottom = 2;
		bag.insets.left = 2;
		bag.insets.right = 2;
		add(new ILabel("action.plan.day"), bag);
		add(day_cbox, bag);
		bag.gridx = 0;
		bag.gridy = 1;
		bag.gridwidth = 2;
		add(new JButton(del_plan), bag);
		bag.gridx = 2;
		bag.gridy = 0;
		bag.gridwidth = 1;
		bag.gridheight = 1;
		add(createCalButton(prev_month), bag);
		bag.gridx = 3;
		setMonthLabel();
		add(month_lbl, bag);
		bag.gridx = 4;
		add(createCalButton(next_month), bag);
		bag.gridx = 5;
		add(createCalButton(prev_year), bag);
		bag.gridx = 6;
		setYearLabel();
		add(year_lbl, bag);
		bag.gridx = 7;
		add(createCalButton(next_year), bag);
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
		h_table.setRowHeight(ROW_HEIGHT);
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
		add(new JButton(del_holiday), bag);
		del_plan.setEnabled(false);
		del_holiday.setEnabled(false);
		createWidgetJobs();
		listener = createDayPlanListener();
		cache.addProxyListener(listener);
	}

	/** Create a calendar button */
	private JButton createCalButton(IAction a) {
		JButton btn = new JButton(a);
		btn.setContentAreaFilled(false);
		btn.setRolloverEnabled(true);
		btn.setBorderPainted(false);
		return btn;
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
		h_table.getSelectionModel().addListSelectionListener(
			new ListSelectionJob(WORKER)
		{
			@Override public void perform() {
				selectHoliday();
			}
		});
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
		return session.canAdd(DayPlan.SONAR_TYPE, oname);
	}

	/** Check if the user can update */
	public boolean canUpdate() {
		return session.canUpdate(DayPlan.SONAR_TYPE, "aname");
	}

	/** Check if the user can remove a day plan */
	public boolean canRemove(DayPlan dp) {
		return session.canRemove(dp);
	}
}
