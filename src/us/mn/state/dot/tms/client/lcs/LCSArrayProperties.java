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
package us.mn.state.dot.tms.client.lcs;

import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.HashMap;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.LaneUseIndication;
import us.mn.state.dot.tms.LCS;
import us.mn.state.dot.tms.LCSArray;
import us.mn.state.dot.tms.LCSArrayLock;
import us.mn.state.dot.tms.LCSIndication;
import us.mn.state.dot.tms.LCSIndicationHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyTablePanel;
import us.mn.state.dot.tms.client.proxy.SonarObjectForm;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.ILabel;
import us.mn.state.dot.tms.client.widget.IPanel;
import us.mn.state.dot.tms.client.widget.IPanel.Stretch;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
import us.mn.state.dot.tms.utils.I18N;

/**
 * LCSArrayProperties is a dialog for editing the properties of an LCS array.
 *
 * @author Douglas Lau
 */
public class LCSArrayProperties extends SonarObjectForm<LCSArray> {

	/** Size in pixels for each indication icon */
	static private final int LCS_SIZE = UI.scaled(18);

	/** LCS Indication creator */
	private final LCSIndicationCreator creator;

	/** LCS table panel */
	private final ProxyTablePanel<LCS> lcs_pnl;

	/** Lane shift label */
	private final ILabel shift_lbl = new ILabel("lcs.lane.shift");

	/** Spinner for lane shift */
	private final JSpinner shift_spn = new JSpinner(
		new SpinnerNumberModel(0, 0, 12, 1));

	/** Notes label */
	private final ILabel notes_lbl = new ILabel("device.notes");

	/** Notes text area */
	private final JTextArea notes_txt = new JTextArea(3, 24);

	/** Indication panel */
	private final JPanel ind_pnl;

	/** List of indication buttons */
	private final LinkedList<JCheckBox> indications =
		new LinkedList<JCheckBox>();

	/** LCS lock combo box component */
	private final JComboBox<LCSArrayLock> lock_cbx = new JComboBox
		<LCSArrayLock>(LCSArrayLock.values());

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
		super(I18N.get("lcs_array") + ": ", s, proxy);
		creator = new LCSIndicationCreator(s,
			state.getLcsCache().getLCSIndications());
		lcs_pnl = new ProxyTablePanel<LCS>(new LCSTableModel(s, proxy))
		{
			protected void selectProxy() {
				selectLCS();
				super.selectProxy();
			}
			public boolean canRemove(LCS proxy) {
				return super.canRemove(proxy) &&
				       lookupIndications(proxy).isEmpty();
			}
		};
		ind_pnl = createIndicationPanel();
	}

	/** Get the SONAR type cache */
	@Override
	protected TypeCache<LCSArray> getTypeCache() {
		return state.getLcsCache().getLCSArrays();
	}

	/** Initialize the widgets on the form */
	@Override
	protected void initialize() {
		lcs_pnl.initialize();
		JTabbedPane tab = new JTabbedPane();
		tab.add(I18N.get("device.setup"), createSetupPanel());
		tab.add(I18N.get("device.status"), createStatusPanel());
		add(tab);
		createUpdateJobs();
		settings.setEnabled(isUpdatePermitted("deviceRequest"));
		super.initialize();
	}

	/** Dispose of the form */
	@Override
	protected void dispose() {
		lcs_pnl.dispose();
		super.dispose();
	}

	/** Create setup panel */
	private JPanel createSetupPanel() {
		JPanel p = new JPanel();
		GroupLayout gl = new GroupLayout(p);
		gl.setAutoCreateGaps(false);
		gl.setAutoCreateContainerGaps(false);
		gl.setHorizontalGroup(createHorizontalGroup(gl));
		gl.setVerticalGroup(createVerticalGroup(gl));
		p.setLayout(gl);
		gl.linkSize(shift_lbl, notes_lbl);
		return p;
	}

	/** Create the horizontal group */
	private GroupLayout.Group createHorizontalGroup(GroupLayout gl) {
		GroupLayout.SequentialGroup hg = gl.createSequentialGroup();
		hg.addGap(UI.hgap);
		GroupLayout.ParallelGroup g0 = gl.createParallelGroup();
		g0.addComponent(lcs_pnl);
		GroupLayout.SequentialGroup g1 = gl.createSequentialGroup();
		g1.addComponent(shift_lbl);
		g1.addGap(UI.hgap);
		g1.addComponent(shift_spn);
		g1.addGap(UI.hgap, UI.hgap, 1000);
		g0.addGroup(g1);
		GroupLayout.SequentialGroup g2 = gl.createSequentialGroup();
		g2.addComponent(notes_lbl);
		g2.addGap(UI.hgap);
		g2.addComponent(notes_txt);
		g0.addGroup(g2);
		hg.addGroup(g0);
		hg.addGap(UI.hgap);
		hg.addComponent(ind_pnl);
		hg.addGap(UI.hgap);
		return hg;
	}

	/** Create the vertical group */
	private GroupLayout.Group createVerticalGroup(GroupLayout gl) {
		GroupLayout.ParallelGroup vg = gl.createParallelGroup();
		GroupLayout.SequentialGroup g0 = gl.createSequentialGroup();
		g0.addComponent(lcs_pnl);
		g0.addGap(UI.vgap);
		GroupLayout.ParallelGroup g1 = gl.createBaselineGroup(false,
			false);
		g1.addComponent(shift_lbl);
		g1.addComponent(shift_spn);
		g0.addGroup(g1);
		g0.addGap(UI.vgap);
		GroupLayout.ParallelGroup g2 = gl.createBaselineGroup(false,
			false);
		g2.addComponent(notes_lbl);
		g2.addComponent(notes_txt);
		g0.addGroup(g2);
		g0.addGap(UI.vgap);
		vg.addGroup(g0);
		vg.addComponent(ind_pnl);
		return vg;
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

	/** Create the indication panel */
	private JPanel createIndicationPanel() {
		IPanel p = new IPanel();
		for (LaneUseIndication i: LaneUseIndication.values()) {
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
		LCS lcs = lcs_pnl.getSelectedProxy();
		if (lcs != null) {
			JCheckBox btn = indications.get(ind);
			if (btn.isSelected())
				creator.create(lcs, ind);
			else
				destroyLCSIndication(lcs, ind);
		}
	}

	/** Destroy the specified LCS indication */
	private void destroyLCSIndication(LCS lcs, int ind) {
		Iterator<LCSIndication> it = LCSIndicationHelper.iterator();
		while (it.hasNext()) {
			LCSIndication li = it.next();
			if (li.getLcs() == lcs && li.getIndication() == ind) {
				li.destroy();
				return;
			}
		}
	}

	/** Select an LCS in the table */
	private void selectLCS() {
		LCS lcs = lcs_pnl.getSelectedProxy();
		if (lcs != null)
			selectLCS(lcs);
		else {
			for (JCheckBox btn: indications) {
				btn.setEnabled(false);
				btn.setSelected(false);
			}
		}
	}

	/** Select an LCS in the table */
	private void selectLCS(LCS lcs) {
		HashMap<Integer, LCSIndication> ind = lookupIndications(lcs);
		String name = lcs.getName();
		boolean can_write = creator.canWrite(name);
		for (LaneUseIndication i: LaneUseIndication.values()) {
			JCheckBox btn = indications.get(i.ordinal());
			if (ind.containsKey(i.ordinal())) {
				LCSIndication li = ind.get(i.ordinal());
				boolean no_c = li.getController() == null;
				btn.setEnabled(can_write && no_c);
				btn.setSelected(true);
			} else {
				btn.setEnabled(can_write);
				btn.setSelected(false);
			}
		}
	}

	/** Lookup the indications for the specified LCS */
	private HashMap<Integer, LCSIndication> lookupIndications(LCS lcs) {
		HashMap<Integer, LCSIndication> ind =
			new HashMap<Integer, LCSIndication>();
		Iterator<LCSIndication> it = LCSIndicationHelper.iterator();
		while (it.hasNext()) {
			LCSIndication li = it.next();
			if (li.getLcs() == lcs)
				ind.put(li.getIndication(), li);
		}
		return ind;
	}

	/** Create status panel */
	private JPanel createStatusPanel() {
		IPanel p = new IPanel();
		p.add("lcs.lock");
		p.add(lock_cbx, Stretch.LAST);
		p.add("device.operation");
		p.add(op_lbl, Stretch.LAST);
		p.add(new JButton(settings), Stretch.CENTER);
		return p;
	}

	/** Update the edit mode */
	@Override
	protected void updateEditMode() {
		shift_spn.setEnabled(canWrite("shift"));
		notes_txt.setEnabled(canWrite("notes"));
		lock_cbx.setEnabled(canWrite("lcsLock"));
	}

	/** Update one attribute on the form */
	@Override
	protected void doUpdateAttribute(String a) {
		if (a == null || a.equals("shift"))
			shift_spn.setValue(proxy.getShift());
		if (a == null || a.equals("notes"))
			notes_txt.setText(proxy.getNotes());
		if (a == null || a.equals("lcsLock")) {
			lock_cbx.setAction(null);
			Integer lk = proxy.getLcsLock();
			if (lk != null)
				lock_cbx.setSelectedIndex(lk);
			else
				lock_cbx.setSelectedIndex(0);
//			lock_cbx.setAction(new LockLcsAction(proxy, lock_cbx));
		}
		if (a == null || a.equals("operation"))
			op_lbl.setText(proxy.getOperation());
	}
}
