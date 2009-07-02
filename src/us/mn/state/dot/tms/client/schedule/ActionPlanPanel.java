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

import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sched.ListSelectionJob;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.ActionPlan;
import us.mn.state.dot.tms.TimeAction;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.toast.FormPanel;
import us.mn.state.dot.tms.client.widget.ZTable;

/**
 * A form for displaying and editing action plans.
 *
 * @author Douglas Lau
 */
public class ActionPlanPanel extends FormPanel {

	/** Table row height */
	static protected final int ROW_HEIGHT = 20;

	/** Table model for action plans */
	protected final ActionPlanModel p_model;

	/** Table to hold the action plans */
	protected final ZTable p_table = new ZTable();

	/** Button to delete the selected action plan */
	protected final JButton del_p_btn = new JButton("Delete Plan");

	/** Table model for time plans */
	protected TimeActionModel t_model;

	/** Table to hold time actions */
	protected final ZTable t_table = new ZTable();

	/** Button to delete the selected time action */
	protected final JButton del_t_btn = new JButton("Delete");

	/** SONAR namespace */
	protected final Namespace namespace;

	/** Logged-in user */
	protected final User user;

	/** Action plan type cache */
	protected final TypeCache<ActionPlan> cache;

	/** Time action type cache */
	protected final TypeCache<TimeAction> t_cache;

	/** Create a new action plan panel */
	public ActionPlanPanel(Session s) {
		super(true);
		namespace = s.getSonarState().getNamespace();
		user = s.getUser();
		cache = s.getSonarState().getActionPlans();
		t_cache = s.getSonarState().getTimeActions();
		p_model = new ActionPlanModel(cache, namespace, user);
	}

	/** Initializze the widgets on the panel */
	protected void initialize() {
		final ListSelectionModel s = p_table.getSelectionModel();
		s.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		new ListSelectionJob(this, s) {
			public void perform() {
				if(!event.getValueIsAdjusting())
					selectActionPlan();
			}
		};
		new ActionJob(this, del_p_btn) {
			public void perform() throws Exception {
				int row = s.getMinSelectionIndex();
				if(row >= 0)
					p_model.deleteRow(row);
			}
		};
		final ListSelectionModel cs = t_table.getSelectionModel();
		cs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		new ListSelectionJob(this, cs) {
			public void perform() {
				if(!event.getValueIsAdjusting())
					selectTimeAction();
			}
		};
		new ActionJob(this, del_t_btn) {
			public void perform() throws Exception {
				int row = cs.getMinSelectionIndex();
				if(row >= 0)
					t_model.deleteRow(row);
			}
		};
		p_table.setModel(p_model);
		p_table.setAutoCreateColumnsFromModel(false);
		p_table.setColumnModel(ActionPlanModel.createColumnModel());
		p_table.setRowHeight(ROW_HEIGHT);
		p_table.setVisibleRowCount(10);
		addRow(p_table);
		del_p_btn.setEnabled(false);
		addRow(del_p_btn);
		JPanel panel = new JPanel(new FlowLayout());
		FormPanel t_panel = new FormPanel(true);
		t_table.setAutoCreateColumnsFromModel(false);
		t_table.setColumnModel(TimeActionModel.createColumnModel());
		t_table.setRowHeight(ROW_HEIGHT);
		t_table.setVisibleRowCount(6);
		t_panel.addRow(t_table);
		del_t_btn.setEnabled(false);
		t_panel.addRow(del_t_btn);
		panel.add(t_panel);
		// FIXME: add tab pane containing dms actions, etc.
		addRow(panel);
	}

	/** Dispose of the form */
	protected void dispose() {
		p_model.dispose();
		if(t_model != null) {
			t_model.dispose();
			t_model = null;
		}
	}

	/** Change the selected action plan */
	protected void selectActionPlan() {
		int row = p_table.getSelectedRow();
		ActionPlan ap = p_model.getProxy(row);
		del_p_btn.setEnabled(p_model.canRemove(row));
		del_t_btn.setEnabled(false);
		TimeActionModel old_model = t_model;
		t_model = new TimeActionModel(t_cache, ap, namespace, user);
		t_table.setModel(t_model);
		if(old_model != null)
			old_model.dispose();
	}

	/** Change the selected time action */
	protected void selectTimeAction() {
		int row = t_table.getSelectedRow();
		del_t_btn.setEnabled(t_model.canRemove(row));
	}
}
