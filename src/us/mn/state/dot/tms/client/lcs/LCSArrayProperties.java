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
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.client.proxy.SonarObjectForm;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IListSelectionAdapter;
import us.mn.state.dot.tms.client.widget.IPanel;
import us.mn.state.dot.tms.client.widget.IPanel.Stretch;
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
		protected void doActionPerformed(ActionEvent e) {
			editPressed();
		}
	};

	/** Action to delete the selected LCS */
	private final IAction delete_lcs = new IAction("lcs.delete") {
		protected void doActionPerformed(ActionEvent e) {
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
	private final JTextArea notes_txt = new JTextArea(3, 24);

	/** List of indication buttons */
	private final LinkedList<JCheckBox> indications =
		new LinkedList<JCheckBox>();

	/** LCS lock combo box component */
	private final JComboBox lock_cmb = new JComboBox(
		LCSArrayLock.getDescriptions());

	/** Operation description label */
	private final JLabel op_lbl = new JLabel();

	/** Action to send settings */
	private final IAction settings = new IAction("device.send.settings") {
		protected void doActionPerformed(ActionEvent e) {
			proxy.setDeviceRequest(DeviceRequest.
				SEND_SETTINGS.ordinal());
		}
	};

	/** Create a new lane control signal properties form */
	public LCSArrayProperties(Session s, LCSArray proxy) {
		super(I18N.get("lcs.array") + ": ", s, proxy);
		state = s.getSonarState();
		creator = new LCSIndicationCreator(s,
			state.getLcsCache().getLCSIndications());
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
		settings.setEnabled(isUpdatePermitted("deviceRequest"));
		setBackground(Color.LIGHT_GRAY);
	}

	/** Dispose of the form */
	@Override protected void dispose() {
		table_model.dispose();
		super.dispose();
	}

	/** Create setup panel */
	private JPanel createSetupPanel() {
		IPanel p = new IPanel();
		// this panel is needed to make the widgets line up
		p.add(new JPanel());
		p.add(createLanePanel());
		p.add(createIndicationPanel(), Stretch.LAST);
		p.add("device.notes");
		p.add(notes_txt, Stretch.LAST);
		return p;
	}

	/** Create lane setup panel */
	private JPanel createLanePanel() {
		initTable();
		IPanel p = new IPanel();
		p.add(lcs_table, Stretch.FULL);
		p.add(new JButton(edit_lcs));
		p.add(new JButton(delete_lcs), Stretch.LAST);
		p.add("lcs.lane.shift");
		p.add(shift_spn, Stretch.LAST);
		return p;
	}

	/** Initialize the table */
	private void initTable() {
		ListSelectionModel s = lcs_table.getSelectionModel();
		s.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		s.addListSelectionListener(new IListSelectionAdapter() {
			@Override
			public void valueChanged() {
				selectLCS();
			}
		});
		lcs_table.setAutoCreateColumnsFromModel(false);
		lcs_table.setColumnModel(table_model.createColumnModel());
		lcs_table.setModel(table_model);
		lcs_table.setVisibleRowCount(12);
	}

	/** Create jobs for updating widgets */
	private void createUpdateJobs() {
		shift_spn.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				Number n = (Number)shift_spn.getValue();
				proxy.setShift(n.intValue());
			}
		});
		notes_txt.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				proxy.setNotes(notes_txt.getText());
			}
		});
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
		IPanel p = new IPanel();
		for(LaneUseIndication i: LaneUseIndication.values()) {
			final int ind = i.ordinal();
			JCheckBox btn = new JCheckBox();
			btn.setAction(new IAction(null) {
				protected void doActionPerformed(ActionEvent e){
					toggleIndication(ind);
				}
			});
			indications.add(btn);
			p.add(new JLabel(IndicationIcon.create(LCS_SIZE, i)));
			p.add(btn);
			p.add(new JLabel(i.description), Stretch.LAST);
		}
		return p;
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
		delete_lcs.setEnabled(table_model.canRemove(lcs) &&
			ind.isEmpty());
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
		IPanel p = new IPanel();
		p.add("lcs.lock");
		p.add(lock_cmb, Stretch.LAST);
		p.add("device.operation");
		p.add(op_lbl, Stretch.LAST);
		p.add(new JButton(settings), Stretch.CENTER);
		return p;
	}

	/** Update one attribute on the form */
	@Override protected void doUpdateAttribute(String a) {
		if(a == null || a.equals("shift")) {
			shift_spn.setEnabled(canUpdate("shift"));
			shift_spn.setValue(proxy.getShift());
		}
		if(a == null || a.equals("notes")) {
			notes_txt.setEnabled(canUpdate("notes"));
			notes_txt.setText(proxy.getNotes());
		}
		if(a == null || a.equals("lcsLock")) {
			lock_cmb.setAction(null);
			Integer lk = proxy.getLcsLock();
			if(lk != null)
				lock_cmb.setSelectedIndex(lk);
			else
				lock_cmb.setSelectedIndex(0);
			lock_cmb.setEnabled(canUpdate("lcsLock"));
//			lock_cmb.setAction(new LockLcsAction(proxy, lock_cmb));
		}
		if(a == null || a.equals("operation"))
			op_lbl.setText(proxy.getOperation());
	}
}
