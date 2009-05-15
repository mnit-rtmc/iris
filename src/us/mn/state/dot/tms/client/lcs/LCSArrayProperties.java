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
import java.util.LinkedList;
import java.util.HashMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sched.FocusJob;
import us.mn.state.dot.sched.ListSelectionJob;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.LaneUseIndication;
import us.mn.state.dot.tms.LCS;
import us.mn.state.dot.tms.LCSArray;
import us.mn.state.dot.tms.LCSArrayLock;
import us.mn.state.dot.tms.LCSHelper;
import us.mn.state.dot.tms.LCSIndication;
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

	/** LCS Indication creator */
	protected final LCSIndicationCreator creator;

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

	/** List of indication buttons */
	protected final LinkedList<JCheckBox> indications =
		new LinkedList<JCheckBox>();

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
		creator = new LCSIndicationCreator(state.getLCSIndications(),
			user);
		table_model = new LCSTableModel(proxy, state.getLCSs(),
			user);
		plan_model = new TimingPlanModel(state.getTimingPlans(), proxy);
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
		FormPanel tpnl = new FormPanel(true);
		tpnl.addRow(lcs_table);
		tpnl.addRow(delete_btn);
		// this panel is needed to make the widgets line up
		panel.add(new JPanel());
		panel.add(tpnl);
		panel.addRow(createIndicationPanel());
		panel.addRow("Notes", notes);
		return panel;
	}

	/** Initialize the table */
	protected void initTable() {
		final ListSelectionModel s = lcs_table.getSelectionModel();
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
				int row = s.getMinSelectionIndex();
				if(row >= 0)
					table_model.deleteRow(row);
			}
		};
	}

	/** Create the indication panel */
	protected JPanel createIndicationPanel() {
		FormPanel panel = new FormPanel(true);
		for(LaneUseIndication i: LaneUseIndication.values()) {
			final int ind = i.ordinal();
			JCheckBox btn = new JCheckBox();
			new ActionJob(btn) {
				public void perform() {
					toggleIndication(ind);
				}
			};
			indications.add(btn);
			panel.add(new JLabel(IndicationIcon.create(18, i)));
			panel.addRow(btn, new JLabel(i.description));
			btn.setEnabled(false);
		}
		return panel;
	}

	/** Toggle one LCS indication checkbox */
	protected void toggleIndication(int ind) {
		ListSelectionModel s = lcs_table.getSelectionModel();
		int row = s.getMinSelectionIndex();
		LCS lcs = table_model.getProxy(row);
		if(lcs != null) {
			JCheckBox btn = indications.get(ind);
			if(btn.isSelected())
				creator.create(lcs, ind);
			else
				destroyLCSIndication(lcs, ind);
		}
	}

	/** Destroy the specified LCS indication */
	protected void destroyLCSIndication(LCS lcs, final int ind) {
		LCSIndication li = LCSHelper.lookupIndication(lcs,
			new Checker<LCSIndication>()
		{
			public boolean check(LCSIndication li) {
				return li.getIndication() == ind;
			}
		});
		if(li != null)
			li.destroy();
	}

	/** Select an LCS in the table */
	protected void selectLCS() {
		final ListSelectionModel s = 
			lcs_table.getSelectionModel();
		int row = s.getMinSelectionIndex();
		LCS lcs = table_model.getProxy(row);
		if(lcs != null)
			selectLCS(lcs);
		else {
			for(JCheckBox btn: indications) {
				btn.setEnabled(false);
				btn.setSelected(false);
			}
		}
	}

	/** Select an LCS in the table */
	protected void selectLCS(LCS lcs) {
		final HashMap<Integer, LCSIndication> ind =
			new HashMap<Integer, LCSIndication>();
		LCSHelper.lookupIndication(lcs, new Checker<LCSIndication>() {
			public boolean check(LCSIndication li) {
				ind.put(li.getIndication(), li);
				return false;
			}
		});
		String name = lcs.getName();
		boolean can_add = creator.canAdd(name);
		boolean can_remove = creator.canRemove(name);
		for(LaneUseIndication i: LaneUseIndication.values()) {
			JCheckBox btn = indications.get(i.ordinal());
			if(ind.containsKey(i.ordinal())) {
				LCSIndication li = ind.get(i.ordinal());
				boolean no_c = li.getController() == null;
				btn.setEnabled(can_remove && no_c);
				btn.setSelected(true);
			} else {
				btn.setEnabled(can_add);
				btn.setSelected(false);
			}
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
