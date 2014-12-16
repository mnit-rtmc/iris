/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2014  Minnesota Department of Transportation
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
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.DayPlan;
import us.mn.state.dot.tms.DayPlanHelper;
import us.mn.state.dot.tms.Holiday;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyTablePanel;
import us.mn.state.dot.tms.client.proxy.ProxyView;
import us.mn.state.dot.tms.client.proxy.ProxyWatcher;
import us.mn.state.dot.tms.client.widget.CalendarWidget;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IComboBoxModel;
import us.mn.state.dot.tms.client.widget.ILabel;

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
	static private final SimpleDateFormat MONTH_LBL =
		new SimpleDateFormat("MMMM");

	/** Formatter for year labels */
	static private final SimpleDateFormat YEAR_LBL =
		new SimpleDateFormat("yyyy");

	/** Cache of day plans */
	private final TypeCache<DayPlan> cache;

	/** Proxy watcher */
	private final ProxyWatcher<DayPlan> watcher;

	/** Proxy view for selected day plan */
	private final ProxyView<DayPlan> view = new ProxyView<DayPlan>() {
		public void update(DayPlan dp, String a) {
			if ("holidays".equals(a)) {
				updateCalendarWidget();
				hol_pnl.repaint();
			}
		}
		public void clear() {
			hol_pnl.repaint();
		}
	};

	/** Action for day plans */
	private final IAction day = new IAction("action.plan.day") {
		protected void doActionPerformed(ActionEvent e) {
			selectDayPlan();
		}
	};

	/** Combo box for day plans */
	private final JComboBox day_cbx = new JComboBox();

	/** Action to delete the selected day plan */
	private final IAction del_plan = new IAction("action.plan.day.delete") {
		protected void doActionPerformed(ActionEvent e) {
			deleteSelectedPlan();
		}
	};

	/** Month to display on calendar widget */
	private final Calendar month = Calendar.getInstance();

	/** Action to select previous month */
	private final IAction prev_month =new IAction("action.plan.month.prev"){
		protected void doActionPerformed(ActionEvent e) {
			month.add(Calendar.MONTH, -1);
			updateCalendarWidget();
		}
	};

	/** Month label */
	private final JLabel month_lbl = new JLabel();

	/** Action to select next month */
	private final IAction next_month =new IAction("action.plan.month.next"){
		protected void doActionPerformed(ActionEvent e) {
			month.add(Calendar.MONTH, 1);
			updateCalendarWidget();
		}
	};

	/** Action to select previous year */
	private final IAction prev_year = new IAction("action.plan.year.prev") {
		protected void doActionPerformed(ActionEvent e) {
			month.add(Calendar.YEAR, -1);
			updateCalendarWidget();
		}
	};

	/** Year label */
	private final JLabel year_lbl = new JLabel();

	/** Action to select next year */
	private final IAction next_year = new IAction("action.plan.year.next") {
		protected void doActionPerformed(ActionEvent e) {
			month.add(Calendar.YEAR, 1);
			updateCalendarWidget();
		}
	};

	/** Calendar widget */
	private final CalendarWidget cal_widget = new CalendarWidget();

	/** Holiday table panel */
	private final ProxyTablePanel<Holiday> hol_pnl;

	/** User session */
	private final Session session;

	/** Create a new day plan panel */
	public DayPlanPanel(Session s) {
		super(new GridBagLayout());
		session = s;
		cache = s.getSonarState().getDayPlans();
		watcher = new ProxyWatcher<DayPlan>(cache, view, false);
		hol_pnl = new ProxyTablePanel<Holiday>(new HolidayModel(s,
			null));
		day_cbx.setAction(day);
		day_cbx.setPrototypeDisplayValue("0123456789");
		day_cbx.setModel(new IComboBoxModel(
			s.getSonarState().getDayModel()));
		day_cbx.setEditable(canAdd());
		GridBagConstraints bag = new GridBagConstraints();
		bag.insets.top = 2;
		bag.insets.bottom = 2;
		bag.insets.left = 2;
		bag.insets.right = 2;
		add(new ILabel("action.plan.day"), bag);
		add(day_cbx, bag);
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
		bag.gridx = 0;
		bag.gridy = 2;
		bag.gridwidth = 9;
		bag.gridheight = 1;
		add(hol_pnl, bag);
		del_plan.setEnabled(false);
	}

	/** Create a calendar button */
	private JButton createCalButton(IAction a) {
		JButton btn = new JButton(a);
		btn.setContentAreaFilled(false);
		btn.setRolloverEnabled(true);
		btn.setBorderPainted(false);
		return btn;
	}

	/** Initialize the panel */
	public void initialize() {
		hol_pnl.initialize();
		watcher.initialize();
		createWidgetJobs();
	}

	/** Dispose of the panel */
	public void dispose() {
		watcher.dispose();
		hol_pnl.dispose();
	}

	/** Create jobs for widget actions */
	private void createWidgetJobs() {
		cal_widget.setHighlighter(new CalendarWidget.Highlighter() {
			public boolean isHighlighted(Calendar cal) {
				DayPlan dp = getSelectedPlan();
				if (dp != null)
					return DayPlanHelper.isHoliday(dp, cal);
				else
					return false;
			}
		});
	}

	/** Update the calendar widget */
	private void updateCalendarWidget() {
		setMonthLabel();
		setYearLabel();
		cal_widget.setMonth(month);
		cal_widget.revalidate();
		prev_month.setEnabled(true);
		next_month.setEnabled(true);
	}

	/** Set the month label */
	private void setMonthLabel() {
		month_lbl.setText(MONTH_LBL.format(month.getTime()));
	}

	/** Set the year label */
	private void setYearLabel() {
		year_lbl.setText(YEAR_LBL.format(month.getTime()));
	}

	/** Select a day plan */
	private void selectDayPlan() {
		Object item = day_cbx.getSelectedItem();
		if (item != null) {
			DayPlan dp = getSelectedPlan();
			hol_pnl.setModel(new HolidayModel(session, dp));
			watcher.setProxy(dp);
			del_plan.setEnabled(canRemove(dp));
			if (dp == null) {
				day_cbx.setSelectedItem(null);
				String name = item.toString().trim();
				if (name.length() > 0 && canAdd(name))
					cache.createObject(name);
			}
		} else {
			hol_pnl.setModel(new HolidayModel(session, null));
			watcher.setProxy(null);
			del_plan.setEnabled(false);
		}
		updateCalendarWidget();
	}

	/** Get the selected day plan */
	private DayPlan getSelectedPlan() {
		Object item = day_cbx.getSelectedItem();
		if (item != null)
			return DayPlanHelper.lookup(item.toString().trim());
		else
			return null;
	}

	/** Delete the selected day plan */
	private void deleteSelectedPlan() {
		Object item = day_cbx.getSelectedItem();
		if (item != null) {
			String name = item.toString();
			DayPlan dp = DayPlanHelper.lookup(name);
			if (dp != null)
				dp.destroy();
			day_cbx.setSelectedItem(null);
		}
	}

	/** Check if the user can add */
	public boolean canAdd() {
		return canAdd("oname");
	}

	/** Check if the user can add */
	private boolean canAdd(String oname) {
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
