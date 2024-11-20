/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2024  Minnesota Department of Transportation
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

import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import us.mn.state.dot.tms.DayPlan;
import us.mn.state.dot.tms.DayPlanHelper;
import us.mn.state.dot.tms.client.widget.CalendarWidget;
import us.mn.state.dot.tms.client.widget.IAction;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;

/**
 * A panel for displaying a calendar.
 *
 * @author Douglas Lau
 */
public class CalendarPanel extends JPanel {

	/** Formatter for month labels */
	static private final SimpleDateFormat MONTH_LBL =
		new SimpleDateFormat("MMMM");

	/** Formatter for year labels */
	static private final SimpleDateFormat YEAR_LBL =
		new SimpleDateFormat("yyyy");

	/** Create a calendar button */
	static private JButton createCalButton(IAction a) {
		JButton btn = new JButton(a);
		btn.setContentAreaFilled(false);
		btn.setRolloverEnabled(true);
		btn.setBorderPainted(false);
		return btn;
	}

	/** Month to display on calendar widget */
	private final Calendar month = Calendar.getInstance();

	/** Action to select previous month */
	private final IAction prev_month =new IAction("action.plan.month.prev"){
		protected void doActionPerformed(ActionEvent e) {
			month.add(Calendar.MONTH, -1);
			updateCalendarWidget();
		}
	};

	/** Previous month button */
	private final JButton prev_month_btn = createCalButton(prev_month);

	/** Month label */
	private final JLabel month_lbl = new JLabel();

	/** Action to select next month */
	private final IAction next_month =new IAction("action.plan.month.next"){
		protected void doActionPerformed(ActionEvent e) {
			month.add(Calendar.MONTH, 1);
			updateCalendarWidget();
		}
	};

	/** Next month button */
	private final JButton next_month_btn = createCalButton(next_month);

	/** Action to select previous year */
	private final IAction prev_year = new IAction("action.plan.year.prev") {
		protected void doActionPerformed(ActionEvent e) {
			month.add(Calendar.YEAR, -1);
			updateCalendarWidget();
		}
	};

	/** Previous year button */
	private final JButton prev_year_btn = createCalButton(prev_year);

	/** Year label */
	private final JLabel year_lbl = new JLabel();

	/** Action to select next year */
	private final IAction next_year = new IAction("action.plan.year.next") {
		protected void doActionPerformed(ActionEvent e) {
			month.add(Calendar.YEAR, 1);
			updateCalendarWidget();
		}
	};

	/** Next year button */
	private final JButton next_year_btn = createCalButton(next_year);

	/** Calendar widget */
	private final CalendarWidget cal_widget = new CalendarWidget();

	/** Create a new calendar panel */
	public CalendarPanel() {
		GroupLayout gl = new GroupLayout(this);
		gl.setHonorsVisibility(false);
		gl.setAutoCreateGaps(false);
		gl.setAutoCreateContainerGaps(false);
		gl.setHorizontalGroup(createHorizontalGroup(gl));
		gl.setVerticalGroup(createVerticalGroup(gl));
		setLayout(gl);
	}

	/** Create the horizontal group */
	private GroupLayout.Group createHorizontalGroup(GroupLayout gl) {
		GroupLayout.ParallelGroup pg = gl.createParallelGroup();
		GroupLayout.SequentialGroup sg = gl.createSequentialGroup();
		sg.addComponent(prev_month_btn);
		sg.addGap(UI.hgap);
		sg.addComponent(month_lbl);
		sg.addGap(UI.hgap);
		sg.addComponent(next_month_btn);
		sg.addGap(UI.hgap, UI.hgap, 100);
		sg.addComponent(prev_year_btn);
		sg.addGap(UI.hgap);
		sg.addComponent(year_lbl);
		sg.addGap(UI.hgap);
		sg.addComponent(next_year_btn);
		pg.addGroup(sg);
		pg.addComponent(cal_widget);
		return pg;
	}

	/** Create the vertical group */
	private GroupLayout.Group createVerticalGroup(GroupLayout gl) {
		GroupLayout.SequentialGroup sg = gl.createSequentialGroup();
		GroupLayout.ParallelGroup pg = gl.createParallelGroup(
			GroupLayout.Alignment.BASELINE);
		pg.addComponent(prev_month_btn);
		pg.addComponent(month_lbl);
		pg.addComponent(next_month_btn);
		pg.addComponent(prev_year_btn);
		pg.addComponent(year_lbl);
		pg.addComponent(next_year_btn);
		sg.addGroup(pg);
		sg.addGap(UI.vgap);
		sg.addComponent(cal_widget);
		return sg;
	}

	/** Update the calendar widget */
	private void updateCalendarWidget() {
		month_lbl.setText(MONTH_LBL.format(month.getTime()));
		year_lbl.setText(YEAR_LBL.format(month.getTime()));
		cal_widget.setMonth(month);
		cal_widget.revalidate();
	}

	/** Select a day plan */
	public void selectDayPlan(final DayPlan plan) {
		cal_widget.setHighlighter(new CalendarWidget.Highlighter() {
			public boolean isHighlighted(Calendar cal) {
				return (plan != null) &&
					DayPlanHelper.isHoliday(plan, cal);
			}
		});
		updateCalendarWidget();
	}
}
