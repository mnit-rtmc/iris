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
package us.mn.state.dot.tms.client.lcs;

import java.awt.Color;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sched.FocusJob;
import us.mn.state.dot.sched.ListSelectionJob;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.LCSArray;
import us.mn.state.dot.tms.LCSArrayLock;
import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.schedule.TimingPlanModel;
import us.mn.state.dot.tms.client.toast.FormPanel;
import us.mn.state.dot.tms.client.toast.SonarObjectForm;
import us.mn.state.dot.tms.client.toast.ZTable;

/**
 * LCSArrayProperties is a dialog for editing the properties of an LCS array.
 *
 * @author Douglas Lau
 */
public class LCSArrayProperties extends SonarObjectForm<LCSArray> {

	/** Frame title */
	static private final String TITLE = "LCS Array: ";

	/** SONAR state */
	protected final SonarState state;

	/** LCS table model */
	protected final LCSTableModel table_model;

	/** LCS table */
	protected final ZTable lcs_table = new ZTable();

	/** Button to delete the selected LCS */
	protected final JButton delete_btn = new JButton("Delete");

	/** Notes text area */
	protected final JTextArea notes = new JTextArea(3, 24);

	/** Timing plan table component */
	protected final ZTable plan_table = new ZTable();

	/** Timing plan model */
	protected final TimingPlanModel plan_model;

	/** Button to delete the selected timing plan */
	protected final JButton delete_plan_btn = new JButton("Delete");

	/** LCS lock combo box component */
	protected final JComboBox lcs_lock = new JComboBox(
		LCSArrayLock.getDescriptions());

	/** Operation description label */
	protected final JLabel operation = new JLabel();

	/** Create a new lane control signal properties form */
	public LCSArrayProperties(TmsConnection tc, LCSArray proxy) {
		super(TITLE, tc, proxy);
		state = tc.getSonarState();
		User user = state.lookupUser(tc.getUser().getName());
		table_model = new LCSTableModel(proxy, state.getLCSs(),
			user);
	}

	/** Get the SONAR type cache */
	protected TypeCache<LCSArray> getTypeCache() {
		return state.getLCSArrays();
	}

	/** Initialize the widgets on the form */
	protected void initialize() {
		super.initialize();
		JTabbedPane tab = new JTabbedPane();
		tab.add("Setup", createSetupPanel());
		tab.add("Timing Plans", createTimingPlanPanel());
		tab.add("Status", createStatusPanel());
		add(tab);
		updateAttribute(null);
		setBackground(Color.LIGHT_GRAY);
	}

	/** Dispose of the form */
	protected void dispose() {
		table_model.dispose();
		super.dispose();
	}

	/** Create setup panel */
	protected JPanel createSetupPanel() {
		new FocusJob(notes) {
			public void perform() {
				proxy.setNotes(notes.getText());
			}
		};
		FormPanel panel = new FormPanel(true);
		initTable();
		panel.addRow(lcs_table);
		panel.addRow(delete_btn);
		panel.addRow("Notes", notes);
		return panel;
	}

	/** Initialize the table */
	protected void initTable() {
		ListSelectionModel s = lcs_table.getSelectionModel();
		s.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		new ListSelectionJob(this, s) {
			public void perform() {
				selectLCS();
			}
		};
		lcs_table.setAutoCreateColumnsFromModel(false);
		lcs_table.setColumnModel(LCSTableModel.createColumnModel());
		lcs_table.setModel(table_model);
		lcs_table.setVisibleRowCount(12);
		new ActionJob(this, delete_btn) {
			public void perform() throws Exception {
				final ListSelectionModel s = 
					lcs_table.getSelectionModel();
				int row = s.getMinSelectionIndex();
				if(row >= 0)
					table_model.deleteRow(row);
			}
		};
	}

	/** Select an LCS in the table */
	protected void selectLCS() {
		final ListSelectionModel s = 
			lcs_table.getSelectionModel();
		int row = s.getMinSelectionIndex();
		if(row >= 0) {
			// FIXME: update indications
		}
	}

	/** Create timing plan panel */
	protected JPanel createTimingPlanPanel() {
		final ListSelectionModel s = plan_table.getSelectionModel();
		s.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		new ActionJob(delete_plan_btn) {
			public void perform() {
				int row = s.getMinSelectionIndex();
				if(row >= 0)
					plan_model.deleteRow(row);
			}
		};
		plan_table.setAutoCreateColumnsFromModel(false);
		plan_table.setModel(plan_model);
		plan_table.setColumnModel(TimingPlanModel.createColumnModel());
		FormPanel panel = new FormPanel(true);
		panel.addRow(plan_table);
		panel.addRow(delete_plan_btn);
		return panel;
	}

	/** Create status panel */
	protected JPanel createStatusPanel() {
		lcs_lock.setAction(new LockLcsAction(proxy, lcs_lock));
		FormPanel panel = new FormPanel(true);
		panel.addRow("Lock", lcs_lock);
		panel.addRow("Operation", operation);
		return panel;
	}

	/** Update one attribute on the form */
	protected void updateAttribute(String a) {
		if(a == null || a.equals("notes"))
			notes.setText(proxy.getNotes());
		if(a == null || a.equals("lcsLock")) {
			Integer lk = proxy.getLcsLock();
			if(lk != null)
				lcs_lock.setSelectedIndex(lk);
			else
				lcs_lock.setSelectedIndex(0);
		}
		if(a == null || a.equals("operation"))
			operation.setText(proxy.getOperation());
	}
}
