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

import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sched.ListSelectionJob;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.ActionPlan;
import us.mn.state.dot.tms.DmsAction;
import us.mn.state.dot.tms.LaneAction;
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

	/** Plan panel */
	protected final JPanel p_panel = new JPanel(new GridLayout(1, 2));

	/** Table model for time plans */
	protected TimeActionModel t_model;

	/** Table to hold time actions */
	protected final ZTable t_table = new ZTable();

	/** Button to delete the selected time action */
	protected final JButton del_t_btn = new JButton("Delete");

	/** Table model for DMS actions */
	protected DmsActionModel d_model;

	/** Table to hold DMS actions */
	protected final ZTable d_table = new ZTable();

	/** Button to delete the selected DMS action */
	protected final JButton del_d_btn = new JButton("Delete");

	/** Table model for lane actions */
	protected LaneActionModel l_model;

	/** Table to hold lane actions */
	protected final ZTable l_table = new ZTable();

	/** Button to delete the selected lane action */
	protected final JButton del_l_btn = new JButton("Delete");

	/** User session */
	protected final Session session;

	/** Day plan model */
	protected final ListModel day_model;

	/** Create a new action plan panel */
	public ActionPlanPanel(Session s) {
		super(true);
		session = s;
		p_model = new ActionPlanModel(s);
		day_model = s.getSonarState().getDayModel();
	}

	/** Initializze the widgets on the panel */
	protected void initialize() {
		p_model.initialize();
		addActionPlanJobs();
		addTimeActionJobs();
		addDmsActionJobs();
		addLaneActionJobs();
		p_table.setModel(p_model);
		p_table.setAutoCreateColumnsFromModel(false);
		p_table.setColumnModel(p_model.createColumnModel());
		p_table.setRowHeight(ROW_HEIGHT);
		p_table.setVisibleRowCount(6);
		addRow(p_table);
		addRow(del_p_btn);
		del_p_btn.setEnabled(false);
		FormPanel t_panel = new FormPanel(true);
		t_table.setAutoCreateColumnsFromModel(false);
		t_table.setRowHeight(ROW_HEIGHT);
		t_table.setVisibleRowCount(12);
		t_panel.addRow(t_table);
		t_panel.addRow(del_t_btn);
		del_t_btn.setEnabled(false);
		p_panel.add(t_panel);
		FormPanel d_panel = new FormPanel(true);
		d_table.setAutoCreateColumnsFromModel(false);
		d_table.setRowHeight(ROW_HEIGHT);
		d_table.setVisibleRowCount(10);
		d_panel.addRow(d_table);
		d_panel.addRow(del_d_btn);
		del_d_btn.setEnabled(false);
		JTabbedPane tab = new JTabbedPane();
		tab.add("DMS Actions", d_panel);
		FormPanel l_panel = new FormPanel(true);
		l_table.setAutoCreateColumnsFromModel(false);
		l_table.setRowHeight(ROW_HEIGHT);
		l_table.setVisibleRowCount(10);
		l_panel.addRow(l_table);
		l_panel.addRow(del_l_btn);
		del_l_btn.setEnabled(false);
		tab.add("Lane Actions", l_panel);
		p_panel.add(tab);
		setFill();
		addRow(p_panel);
	}

	/** Add jobs for action plan table */
	protected void addActionPlanJobs() {
		final ListSelectionModel sm = p_table.getSelectionModel();
		sm.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		new ListSelectionJob(this, sm) {
			public void perform() {
				if(!event.getValueIsAdjusting())
					selectActionPlan();
			}
		};
		new ActionJob(this, del_p_btn) {
			public void perform() throws Exception {
				int row = sm.getMinSelectionIndex();
				if(row >= 0)
					p_model.deleteRow(row);
			}
		};
	}

	/** Add jobs for time action table */
	protected void addTimeActionJobs() {
		final ListSelectionModel sm = t_table.getSelectionModel();
		sm.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		new ListSelectionJob(this, sm) {
			public void perform() {
				if(!event.getValueIsAdjusting())
					selectTimeAction();
			}
		};
		new ActionJob(this, del_t_btn) {
			public void perform() throws Exception {
				int row = sm.getMinSelectionIndex();
				if(row >= 0)
					t_model.deleteRow(row);
			}
		};
	}

	/** Add jobs for DMS action table */
	protected void addDmsActionJobs() {
		final ListSelectionModel sm = d_table.getSelectionModel();
		sm.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		new ListSelectionJob(this, sm) {
			public void perform() {
				if(!event.getValueIsAdjusting())
					selectDmsAction();
			}
		};
		new ActionJob(this, del_d_btn) {
			public void perform() throws Exception {
				int row = sm.getMinSelectionIndex();
				if(row >= 0)
					d_model.deleteRow(row);
			}
		};
	}

	/** Add jobs for lane action table */
	protected void addLaneActionJobs() {
		final ListSelectionModel sm = l_table.getSelectionModel();
		sm.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		new ListSelectionJob(this, sm) {
			public void perform() {
				if(!event.getValueIsAdjusting())
					selectLaneAction();
			}
		};
		new ActionJob(this, del_l_btn) {
			public void perform() throws Exception {
				int row = sm.getMinSelectionIndex();
				if(row >= 0)
					l_model.deleteRow(row);
			}
		};
	}

	/** Dispose of the form */
	protected void dispose() {
		p_model.dispose();
		if(t_model != null) {
			t_model.dispose();
			t_model = null;
		}
		if(d_model != null) {
			d_model.dispose();
			d_model = null;
		}
		if(l_model != null) {
			l_model.dispose();
			l_model = null;
		}
	}

	/** Change the selected action plan */
	protected void selectActionPlan() {
		ActionPlan ap = p_model.getProxy(p_table.getSelectedRow());
		del_p_btn.setEnabled(p_model.canRemove(ap));
		del_t_btn.setEnabled(false);
		TimeActionModel ot_model = t_model;
		t_model = new TimeActionModel(session, ap, day_model);
		t_model.initialize();
		t_table.setColumnModel(t_model.createColumnModel());
		t_table.setModel(t_model);
		if(ot_model != null)
			ot_model.dispose();
		del_d_btn.setEnabled(false);
		DmsActionModel od_model = d_model;
		d_model = new DmsActionModel(session, ap);
		d_model.initialize();
		d_table.setColumnModel(d_model.createColumnModel());
		d_table.setModel(d_model);
		if(od_model != null)
			od_model.dispose();
		del_l_btn.setEnabled(false);
		LaneActionModel ol_model = l_model;
		l_model = new LaneActionModel(session, ap);
		l_model.initialize();
		l_table.setColumnModel(l_model.createColumnModel());
		l_table.setModel(l_model);
		if(ol_model != null)
			ol_model.dispose();
	}

	/** Change the selected time action */
	protected void selectTimeAction() {
		TimeAction ta = t_model.getProxy(t_table.getSelectedRow());
		del_t_btn.setEnabled(t_model.canRemove(ta));
	}

	/** Change the selected DMS action */
	protected void selectDmsAction() {
		DmsAction da = d_model.getProxy(d_table.getSelectedRow());
		del_d_btn.setEnabled(d_model.canRemove(da));
	}

	/** Change the selected lane action */
	protected void selectLaneAction() {
		LaneAction la = l_model.getProxy(l_table.getSelectedRow());
		del_l_btn.setEnabled(l_model.canRemove(la));
	}
}
