/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.client.widget.ZTable;

/**
 * A panel for editing day plans.
 *
 * @author Douglas Lau
 */
public class DayPlanPanel extends JPanel {

	/** Cache of day plans */
	protected final TypeCache<DayPlan> cache;

	/** Proxy listener to update day plan holiday model */
	protected final ProxyListener<DayPlan> listener;

	/** Combo box for day plans */
	protected final JComboBox day_cbox = new JComboBox();

	/** Button to delete the selected day plan */
	protected final JButton del_plan = new JButton("Delete");

	/** Table model for holidays */
	protected final DayPlanHolidayModel dh_model;

	/** Table to hold the day plan holidays */
	protected final ZTable dh_table = new ZTable();

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
		TypeCache<Holiday> holidays = s.getSonarState().getHolidays();
		dh_model = new DayPlanHolidayModel(holidays, namespace, user);
		dh_table.setModel(dh_model);
		dh_table.setAutoCreateColumnsFromModel(false);
		dh_table.setColumnModel(dh_model.createColumnModel());
		dh_table.setRowHeight(18);
		dh_table.setVisibleRowCount(10);
		bag.gridx = 2;
		bag.gridy = 0;
		bag.gridwidth = 1;
		bag.gridheight = 2;
		JScrollPane dh_pane = new JScrollPane(dh_table,
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		add(dh_pane, bag);
		h_model = new HolidayModel(holidays, namespace, user);
		final ListSelectionModel lsm = h_table.getSelectionModel();
		lsm.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		h_table.setModel(h_model);
		h_table.setAutoCreateColumnsFromModel(false);
		h_table.setColumnModel(h_model.createColumnModel());
		h_table.setRowHeight(22);
		h_table.setVisibleRowCount(10);
		bag.gridx = 0;
		bag.gridy = 2;
		bag.gridwidth = 4;
		bag.gridheight = 1;
		JScrollPane h_pane = new JScrollPane(h_table,
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		add(h_pane, bag);
		bag.gridx = 3;
		bag.gridy = 3;
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
				if(attrib.equals("holidays"))
					dh_model.updateHolidays(dp);
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
	}

	/** Select a day plan */
	protected void selectDayPlan() {
		Object item = day_cbox.getSelectedItem();
		if(item != null) {
			String name = item.toString().trim();
			DayPlan dp = DayPlanHelper.lookup(name);
			dh_model.setDayPlan(dp);
			if(dp == null && name.length() > 0 && canAdd(name))
				cache.createObject(name);
			del_plan.setEnabled(canRemove(dp));
		} else {
			dh_model.setDayPlan(null);
			del_plan.setEnabled(false);
		}
	}

	/** Delete the selected day plan */
	protected void deleteSelectedPlan() {
		Object item = day_cbox.getSelectedItem();
		if(item != null) {
			String name = item.toString();
			DayPlan dp = DayPlanHelper.lookup(name);
			if(dp != null)
				dp.destroy();
		}
	}

	/** Change the selected holiday */
	protected void selectHoliday() {
		int row = h_table.getSelectedRow();
		del_holiday.setEnabled(h_model.canRemove(row));
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
