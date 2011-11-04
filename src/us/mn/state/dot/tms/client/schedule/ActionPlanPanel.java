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
import us.mn.state.dot.tms.MeterAction;
import us.mn.state.dot.tms.TimeAction;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.toast.FormPanel;
import us.mn.state.dot.tms.client.widget.ZTable;

/**
 * A form for displaying and editing action plans.
 *
 * @author Douglas Lau
 */
public class ActionPlanPanel extends JPanel {

	/** Check if the user is permitted to use the form */
	static public boolean isPermitted(Session s) {
		return s.canRead(ActionPlan.SONAR_TYPE) &&
		       s.canRead(DmsAction.SONAR_TYPE) &&
		       s.canRead(LaneAction.SONAR_TYPE) &&
		       s.canRead(MeterAction.SONAR_TYPE) &&
		       s.canRead(TimeAction.SONAR_TYPE);
	}

	/** Table row height */
	static protected final int ROW_HEIGHT = 22;

	/** Table model for action plans */
	protected final ActionPlanModel p_model;

	/** Table to hold the action plans */
	protected final ZTable p_table = new ZTable();

	/** Button to delete the selected action plan */
	protected final JButton del_p_btn = new JButton("Delete Plan");

	/** Time action table panel */
	private final PlanTablePanel<TimeAction> t_panel;

	/** DMS action table panel */
	private final PlanTablePanel<DmsAction> d_panel;

	/** Lane action table panel */
	private final PlanTablePanel<LaneAction> l_panel;

	/** Meter action table panel */
	private final PlanTablePanel<MeterAction> m_panel;

	/** User session */
	protected final Session session;

	/** Day plan model */
	protected final ListModel day_model;

	/** Create a new action plan panel */
	public ActionPlanPanel(Session s) {
		super(new GridLayout(2, 1));
		session = s;
		p_model = new ActionPlanModel(s);
		day_model = s.getSonarState().getDayModel();
		t_panel = new PlanTablePanel<TimeAction>();
		d_panel = new PlanTablePanel<DmsAction>();
		l_panel = new PlanTablePanel<LaneAction>();
		m_panel = new PlanTablePanel<MeterAction>();
	}

	/** Initializze the widgets on the panel */
	protected void initialize() {
		p_model.initialize();
		addActionPlanJobs();
		add(createActionPlanPanel());
		JTabbedPane tab = new JTabbedPane();
		tab.add("Schedule", t_panel);
		tab.add("DMS Actions", d_panel);
		tab.add("Lane Actions", l_panel);
		tab.add("Meter Actions", m_panel);
		add(tab);
	}

	/** Create the main action plan panel */
	private JPanel createActionPlanPanel() {
		FormPanel p_panel = new FormPanel(true);
		p_table.setModel(p_model);
		p_table.setAutoCreateColumnsFromModel(false);
		p_table.setColumnModel(p_model.createColumnModel());
		p_table.setRowHeight(ROW_HEIGHT);
		p_table.setVisibleRowCount(8);
		p_panel.addRow(p_table);
		p_panel.addRow(del_p_btn);
		del_p_btn.setEnabled(false);
		return p_panel;
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

	/** Dispose of the form */
	public void dispose() {
		p_model.dispose();
		t_panel.dispose();
		d_panel.dispose();
		l_panel.dispose();
		m_panel.dispose();
	}

	/** Change the selected action plan */
	protected void selectActionPlan() {
		ActionPlan ap = p_model.getProxy(p_table.getSelectedRow());
		del_p_btn.setEnabled(p_model.canRemove(ap));
		t_panel.setTableModel(new TimeActionModel(session, ap,
			day_model));
		d_panel.setTableModel(new DmsActionModel(session, ap));
		l_panel.setTableModel(new LaneActionModel(session, ap));
		m_panel.setTableModel(new MeterActionModel(session, ap));
	}
}
