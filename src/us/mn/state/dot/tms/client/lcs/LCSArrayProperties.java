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
package us.mn.state.dot.tms.client.lcs;

import java.awt.Color;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.HashMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import us.mn.state.dot.sched.ChangeJob;
import us.mn.state.dot.sched.FocusLostJob;
import us.mn.state.dot.sched.ListSelectionJob;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.LaneUseIndication;
import us.mn.state.dot.tms.LCS;
import us.mn.state.dot.tms.LCSArray;
import us.mn.state.dot.tms.LCSArrayLock;
import us.mn.state.dot.tms.LCSIndication;
import us.mn.state.dot.tms.LCSIndicationHelper;
import static us.mn.state.dot.tms.client.IrisClient.WORKER;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.client.proxy.SonarObjectForm;
import us.mn.state.dot.tms.client.widget.FormPanel;
import us.mn.state.dot.tms.client.widget.IAction;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
import us.mn.state.dot.tms.client.widget.ZTable;
import us.mn.state.dot.tms.utils.I18N;

/**
 * LCSArrayProperties is a dialog for editing the properties of an LCS array.
 *
 * @author Douglas Lau
 */
public class LCSArrayProperties extends SonarObjectForm<LCSArray> {

	/** Size in pixels for each indication icon */
	static private final int LCS_SIZE = UI.scaled(18);

	/** SONAR state */
	private final SonarState state;

	/** LCS Indication creator */
	private final LCSIndicationCreator creator;

	/** LCS table model */
	private final LCSTableModel table_model;

	/** LCS table */
	private final ZTable lcs_table = new ZTable();

	/** Action to edit the selected LCS */
	private final IAction edit_lcs = new IAction("lcs.edit") {
		protected void do_perform() {
			editPressed();
		}
	};

	/** Action to delete the selected LCS */
	private final IAction delete_lcs = new IAction("lcs.delete") {
		protected void do_perform() {
			ListSelectionModel s = lcs_table.getSelectionModel();
			int row = s.getMinSelectionIndex();
			if(row >= 0)
				table_model.deleteRow(row);
		}
	};

	/** Spinner for lane shift */
	private final JSpinner shift_spn = new JSpinner(
		new SpinnerNumberModel(0, 0, 12, 1));

	/** Notes text area */
	private final JTextArea notes = new JTextArea(3, 24);

	/** List of indication buttons */
	private final LinkedList<JCheckBox> indications =
		new LinkedList<JCheckBox>();

	/** LCS lock combo box component */
	private final JComboBox lcs_lock = new JComboBox(
		LCSArrayLock.getDescriptions());

	/** Operation description label */
	private final JLabel operation = new JLabel();

	/** Action to send settings */
	private final IAction settings = new IAction("device.send.settings") {
		protected void do_perform() {
			proxy.setDeviceRequest(DeviceRequest.
				SEND_SETTINGS.ordinal());
		}
	};

	/** Create a new lane control signal properties form */
	public LCSArrayProperties(Session s, LCSArray proxy) {
		super(I18N.get("lcs.array") + ": ", s, proxy);
		state = s.getSonarState();
		User user = s.getUser();
		creator = new LCSIndicationCreator(state.getNamespace(),
			state.getLcsCache().getLCSIndications(), user);
		table_model = new LCSTableModel(s, proxy);
		table_model.initialize();
	}

	/** Get the SONAR type cache */
	@Override protected TypeCache<LCSArray> getTypeCache() {
		return state.getLcsCache().getLCSArrays();
	}

	/** Initialize the widgets on the form */
	@Override protected void initialize() {
		super.initialize();
		JTabbedPane tab = new JTabbedPane();
		tab.add(I18N.get("device.setup"), createSetupPanel());
		tab.add(I18N.get("device.status"), createStatusPanel());
		add(tab);
		updateAttribute(null);
		if(canUpdate())
			createUpdateJobs();
		if(canUpdate("lcsLock"))
			createLockJob();
		settings.setEnabled(canUpdate("deviceRequest"));
		setBackground(Color.LIGHT_GRAY);
	}

	/** Dispose of the form */
	@Override protected void dispose() {
		table_model.dispose();
		super.dispose();
	}

	/** Create setup panel */
	private JPanel createSetupPanel() {
		FormPanel panel = new FormPanel(canUpdate());
		initTable();
		FormPanel tpnl = new FormPanel(canUpdate());
		tpnl.addRow(lcs_table);
		lcs_table.setEnabled(true);
		tpnl.add(new JButton(edit_lcs));
		edit_lcs.setEnabled(false);
		tpnl.addRow(new JButton(delete_lcs));
		delete_lcs.setEnabled(false);
		tpnl.addRow(I18N.get("lcs.lane.shift"), shift_spn);
		// this panel is needed to make the widgets line up
		panel.add(new JPanel());
		panel.add(tpnl);
		panel.addRow(createIndicationPanel());
		panel.addRow(I18N.get("device.notes"), notes);
		return panel;
	}

	/** Create jobs for updating widgets */
	private void createUpdateJobs() {
		shift_spn.addChangeListener(new ChangeJob(WORKER) {
			@Override public void perform() {
				Number n = (Number)shift_spn.getValue();
				proxy.setShift(n.intValue());
			}
		});
		notes.addFocusListener(new FocusLostJob(WORKER) {
			@Override public void perform() {
				proxy.setNotes(notes.getText());
			}
		});
	}

	/** Initialize the table */
	private void initTable() {
		ListSelectionModel s = lcs_table.getSelectionModel();
		s.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		s.addListSelectionListener(new ListSelectionJob(WORKER) {
			@Override public void perform() {
				selectLCS();
			}
		});
		lcs_table.setAutoCreateColumnsFromModel(false);
		lcs_table.setColumnModel(table_model.createColumnModel());
		lcs_table.setModel(table_model);
		lcs_table.setVisibleRowCount(12);
	}

	/** Edit button pressed */
	private void editPressed() {
		LCS lcs = getSelectedLCS();
		if(lcs != null) {
			DMS dms = DMSHelper.lookup(lcs.getName());
			if(dms != null)
				session.getDMSManager().showPropertiesForm(dms);
		}
	}

	/** Get the selected LCS */
	private LCS getSelectedLCS() {
		ListSelectionModel s = lcs_table.getSelectionModel();
		return table_model.getProxy(s.getMinSelectionIndex());
	}

	/** Create the indication panel */
	private JPanel createIndicationPanel() {
		FormPanel panel = new FormPanel();
		for(LaneUseIndication i: LaneUseIndication.values()) {
			final int ind = i.ordinal();
			JCheckBox btn = new JCheckBox();
			btn.setAction(new IAction(null) {
				protected void do_perform() {
					toggleIndication(ind);
				}
			});
			indications.add(btn);
			panel.add(new JLabel(IndicationIcon.create(LCS_SIZE,
				i)));
			panel.addRow(btn, new JLabel(i.description));
			btn.setEnabled(false);
		}
		return panel;
	}

	/** Toggle one LCS indication checkbox */
	private void toggleIndication(int ind) {
		LCS lcs = getSelectedLCS();
		if(lcs != null) {
			JCheckBox btn = indications.get(ind);
			if(btn.isSelected())
				creator.create(lcs, ind);
			else
				destroyLCSIndication(lcs, ind);
		}
	}

	/** Destroy the specified LCS indication */
	private void destroyLCSIndication(LCS lcs, int ind) {
		Iterator<LCSIndication> it = LCSIndicationHelper.iterator();
		while(it.hasNext()) {
			LCSIndication li = it.next();
			if(li.getLcs() == lcs && li.getIndication() == ind) {
				li.destroy();
				return;
			}
		}
	}

	/** Select an LCS in the table */
	private void selectLCS() {
		LCS lcs = getSelectedLCS();
		if(lcs != null)
			selectLCS(lcs);
		else {
			edit_lcs.setEnabled(false);
			delete_lcs.setEnabled(false);
			for(JCheckBox btn: indications) {
				btn.setEnabled(false);
				btn.setSelected(false);
			}
		}
	}

	/** Select an LCS in the table */
	private void selectLCS(LCS lcs) {
		edit_lcs.setEnabled(true);
		HashMap<Integer, LCSIndication> ind = lookupIndications(lcs);
		delete_lcs.setEnabled(ind.isEmpty());
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

	/** Lookup the indications for the specified LCS */
	private HashMap<Integer, LCSIndication> lookupIndications(LCS lcs) {
		HashMap<Integer, LCSIndication> ind =
			new HashMap<Integer, LCSIndication>();
		Iterator<LCSIndication> it = LCSIndicationHelper.iterator();
		while(it.hasNext()) {
			LCSIndication li = it.next();
			if(li.getLcs() == lcs)
				ind.put(li.getIndication(), li);
		}
		return ind;
	}

	/** Create status panel */
	private JPanel createStatusPanel() {
		FormPanel panel = new FormPanel(false);
		panel.addRow(I18N.get("lcs.lock"), lcs_lock);
		panel.addRow(I18N.get("device.operation"), operation);
		JButton btn = new JButton(settings);
		panel.addRow(btn);
		btn.setEnabled(true);
		return panel;
	}

	/** Create lock job */
	private void createLockJob() {
		lcs_lock.setAction(new LockLcsAction(proxy, lcs_lock));
		lcs_lock.setEnabled(true);
	}

	/** Update one attribute on the form */
	@Override protected void doUpdateAttribute(String a) {
		if(a == null || a.equals("shift"))
			shift_spn.setValue(proxy.getShift());
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
