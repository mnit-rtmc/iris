/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2017  Minnesota Department of Transportation
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
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import static javax.swing.ListSelectionModel.SINGLE_SELECTION;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.DayPlan;
import us.mn.state.dot.tms.DayPlanHelper;
import us.mn.state.dot.tms.DayMatcher;
import us.mn.state.dot.tms.client.EditModeListener;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyTablePanel;
import us.mn.state.dot.tms.client.proxy.ProxyView;
import us.mn.state.dot.tms.client.proxy.ProxyWatcher;
import us.mn.state.dot.tms.client.widget.CalendarWidget;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.ILabel;
import us.mn.state.dot.tms.client.widget.IListSelectionAdapter;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;

/**
 * A panel for editing day plans.
 *
 * @author Douglas Lau
 */
public class DayPlanPanel extends JPanel {

	/** Check if the user is permitted to use the form */
	static public boolean isPermitted(Session s) {
		return s.canRead(DayPlan.SONAR_TYPE) &&
		       s.canRead(DayMatcher.SONAR_TYPE);
	}

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

	/** User session */
	private final Session session;

	/** Cache of day plans */
	private final TypeCache<DayPlan> cache;

	/** Proxy watcher */
	private final ProxyWatcher<DayPlan> watcher;

	/** Proxy view for selected day plan */
	private final ProxyView<DayPlan> view = new ProxyView<DayPlan>() {
		public void update(DayPlan dp, String a) {
			if ("dayMatchers".equals(a)) {
				updateCalendarWidget();
				dm_pnl.repaint();
			}
		}
		public void clear() {
			dm_pnl.repaint();
		}
	};

	/** Day plan label */
	private final ILabel day_lbl = new ILabel("action.plan.day");

	/** List for day plans */
	private final JList<DayPlan> day_lst = new JList<DayPlan>();

	/** Scroll pane for day list */
	private final JScrollPane day_scrl = new JScrollPane(day_lst);

	/** Day plan add text field */
	private final JTextField add_txt = new JTextField(10);

	/** Action to create a day plan */
	private final IAction add_plan = new IAction("action.plan.day.add") {
		protected void doActionPerformed(ActionEvent e) {
			createDayPlan();
		}
	};

	/** Button to create a day plan */
	private final JButton add_btn = new JButton(add_plan);

	/** Action to delete the selected day plan */
	private final IAction del_plan = new IAction("action.plan.day.delete") {
		protected void doActionPerformed(ActionEvent e) {
			deleteSelectedPlan();
		}
	};

	/** Button to delete the selected day plan */
	private final JButton del_btn = new JButton(del_plan);

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

	/** Day matcher table panel */
	private final ProxyTablePanel<DayMatcher> dm_pnl;

	/** Edit mode listener */
	private final EditModeListener edit_lsnr = new EditModeListener() {
		public void editModeChanged() {
			updateButtonPanel();
		}
	};

	/** Create a new day plan panel */
	public DayPlanPanel(Session s) {
		session = s;
		cache = s.getSonarState().getDayPlans();
		watcher = new ProxyWatcher<DayPlan>(cache, view, false);
		dm_pnl = new ProxyTablePanel<DayMatcher>(new DayMatcherModel(s,
			null));
		day_lst.setModel(s.getSonarState().getDayModel());
		day_lst.setSelectionMode(SINGLE_SELECTION);
		day_lst.addListSelectionListener(new IListSelectionAdapter() {
			@Override
			public void valueChanged() {
				selectDayPlan();
			}
		});
	}

	/** Initialize the panel */
	public void initialize() {
		setBorder(UI.border);
		dm_pnl.initialize();
		watcher.initialize();
		createWidgetJobs();
		add_txt.setMaximumSize(add_txt.getPreferredSize());
		layoutPanel();
		selectDayPlan();
		session.addEditModeListener(edit_lsnr);
	}

	/** Layout the panel */
	private void layoutPanel() {
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
		GroupLayout.ParallelGroup hg = gl.createParallelGroup();
		GroupLayout.SequentialGroup s1 = gl.createSequentialGroup();
		GroupLayout.ParallelGroup p1 = gl.createParallelGroup();
		GroupLayout.SequentialGroup s2 = gl.createSequentialGroup();
		s2.addComponent(day_lbl);
		s2.addGap(UI.hgap);
		s2.addComponent(day_scrl);
		GroupLayout.SequentialGroup s3 = gl.createSequentialGroup();
		s3.addComponent(add_txt);
		s3.addGap(UI.hgap);
		s3.addComponent(add_btn);
		s3.addGap(UI.hgap);
		s3.addComponent(del_btn);
		p1.addGroup(s2);
		p1.addGroup(s3);
		GroupLayout.ParallelGroup p2 = gl.createParallelGroup();
		GroupLayout.SequentialGroup s4 = gl.createSequentialGroup();
		s4.addComponent(prev_month_btn);
		s4.addGap(UI.hgap);
		s4.addComponent(month_lbl);
		s4.addGap(UI.hgap);
		s4.addComponent(next_month_btn);
		s4.addGap(UI.hgap);
		s4.addComponent(prev_year_btn);
		s4.addGap(UI.hgap);
		s4.addComponent(year_lbl);
		s4.addGap(UI.hgap);
		s4.addComponent(next_year_btn);
		p2.addGroup(s4);
		p2.addComponent(cal_widget);
		s1.addGroup(p1);
		s1.addGap(UI.hgap);
		s1.addGroup(p2);
		hg.addGroup(s1);
		hg.addComponent(dm_pnl);
		return hg;
	}

	/** Create the vertical group */
	private GroupLayout.Group createVerticalGroup(GroupLayout gl) {
		GroupLayout.SequentialGroup vg = gl.createSequentialGroup();
		GroupLayout.ParallelGroup p1 = gl.createParallelGroup();
		GroupLayout.SequentialGroup s1 = gl.createSequentialGroup();
		GroupLayout.ParallelGroup p2 = gl.createParallelGroup();
		p2.addComponent(day_lbl);
		p2.addComponent(day_scrl);
		GroupLayout.ParallelGroup p3 = gl.createParallelGroup(
			GroupLayout.Alignment.BASELINE);
		p3.addComponent(add_txt);
		p3.addComponent(add_btn);
		p3.addComponent(del_btn);
		s1.addGroup(p2);
		s1.addGap(UI.vgap);
		s1.addGroup(p3);
		GroupLayout.SequentialGroup s2 = gl.createSequentialGroup();
		GroupLayout.ParallelGroup p4 = gl.createParallelGroup(
			GroupLayout.Alignment.BASELINE);
		p4.addComponent(prev_month_btn);
		p4.addComponent(month_lbl);
		p4.addComponent(next_month_btn);
		p4.addComponent(prev_year_btn);
		p4.addComponent(year_lbl);
		p4.addComponent(next_year_btn);
		s2.addGroup(p4);
		s2.addGap(UI.vgap);
		s2.addComponent(cal_widget);
		p1.addGroup(s1);
		p1.addGroup(s2);
		vg.addGroup(p1);
		vg.addGap(UI.vgap);
		vg.addComponent(dm_pnl);
		return vg;
	}

	/** Dispose of the panel */
	public void dispose() {
		session.removeEditModeListener(edit_lsnr);
		watcher.dispose();
		dm_pnl.dispose();
	}

	/** Create jobs for widget actions */
	private void createWidgetJobs() {
		cal_widget.setHighlighter(new CalendarWidget.Highlighter() {
			public boolean isHighlighted(Calendar cal) {
				DayPlan dp = day_lst.getSelectedValue();
				if (dp != null)
					return DayPlanHelper.isHoliday(dp, cal);
				else
					return false;
			}
		});
	}

	/** Update the button panel */
	private void updateButtonPanel() {
		add_plan.setEnabled(canAdd());
		del_plan.setEnabled(canRemove(day_lst.getSelectedValue()));
	}

	/** Update the calendar widget */
	private void updateCalendarWidget() {
		month_lbl.setText(MONTH_LBL.format(month.getTime()));
		year_lbl.setText(YEAR_LBL.format(month.getTime()));
		cal_widget.setMonth(month);
		cal_widget.revalidate();
		prev_month.setEnabled(true);
		next_month.setEnabled(true);
	}

	/** Select a day plan */
	private void selectDayPlan() {
		DayPlan dp = day_lst.getSelectedValue();
		dm_pnl.setModel(new DayMatcherModel(session, dp));
		watcher.setProxy(dp);
		updateCalendarWidget();
		updateButtonPanel();
	}

	/** Create a new day plan */
	private void createDayPlan() {
		String name = add_txt.getText().trim();
		add_txt.setText("");
		if (name.length() > 0 && canAdd(name))
			cache.createObject(name);
	}

	/** Delete the selected day plan */
	private void deleteSelectedPlan() {
		DayPlan dp = day_lst.getSelectedValue();
		if (dp != null)
			dp.destroy();
	}

	/** Check if the user can add */
	public boolean canAdd() {
		return canAdd("oname");
	}

	/** Check if the user can add */
	private boolean canAdd(String oname) {
		return session.canAdd(DayPlan.SONAR_TYPE, oname);
	}

	/** Check if the user can write */
	public boolean canWrite() {
		return session.canWrite(DayPlan.SONAR_TYPE, "aname");
	}

	/** Check if the user can remove a day plan */
	public boolean canRemove(DayPlan dp) {
		return session.canWrite(dp);
	}
}
