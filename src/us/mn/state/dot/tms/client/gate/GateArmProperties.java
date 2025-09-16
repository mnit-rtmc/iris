/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013-2025  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.gate;

import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.ActionPlan;
import us.mn.state.dot.tms.CameraPreset;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.GateArm;
import us.mn.state.dot.tms.GateArmHelper;
import us.mn.state.dot.tms.GateArmState;
import us.mn.state.dot.tms.Hashtags;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.camera.PresetComboRenderer;
import us.mn.state.dot.tms.client.proxy.SonarObjectForm;
import us.mn.state.dot.tms.client.roads.LocationPanel;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IComboBoxModel;
import us.mn.state.dot.tms.client.widget.IPanel;
import us.mn.state.dot.tms.client.widget.IPanel.Stretch;
import us.mn.state.dot.tms.utils.I18N;

/**
 * This is a form for viewing and editing the properties of a gate arm.
 *
 * @author Douglas Lau
 */
public class GateArmProperties extends SonarObjectForm<GateArm> {

	/** Location panel */
	private final LocationPanel loc_pnl;

	/** Camera preset combo box model */
	private final IComboBoxModel<CameraPreset> preset_mdl;

	/** Camera preset action */
	private final IAction preset_act = new IAction("camera.preset") {
		protected void doActionPerformed(ActionEvent e) {
			proxy.setPreset(preset_mdl.getSelectedProxy());
		}
		@Override
		protected void doUpdateSelected() {
			preset_mdl.setSelectedItem(proxy.getPreset());
		}
	};

	/** Camera preset combo box */
	private final JComboBox<CameraPreset> preset_cbx =
		new JComboBox<CameraPreset>();

	/** Notes text area */
	private final JTextArea notes_txt = new JTextArea(8, 32);

	/** Opposing traffic checkbox */
	private final JCheckBox opposing_chk = new JCheckBox(new IAction(null) {
		protected void doActionPerformed(ActionEvent e) {
			proxy.setOpposing(opposing_chk.isSelected());
		}
	});

	/** Downstream hashtag text field */
	private final JTextField downstream_txt = new JTextField(12);

	/** Arm state label */
	private final JLabel arm_state_lbl = IPanel.createValueLabel();

	/** Interlock label */
	private final JLabel interlock_lbl = IPanel.createValueLabel();

	/** Operation description label */
	private final JLabel op_lbl = IPanel.createValueLabel();

	/** Send settings action */
	private final IAction settings = new IAction("device.send.settings") {
		protected void doActionPerformed(ActionEvent e) {
			proxy.setDeviceRequest(DeviceRequest.
				SEND_SETTINGS.ordinal());
		}
	};

	/** System disable action */
	private final IAction disable = new IAction("gate.arm.disable.system") {
		protected void doActionPerformed(ActionEvent e) {
			proxy.setDeviceRequest(DeviceRequest.
				DISABLE_SYSTEM.ordinal());
		}
	};

	/** Create a new gate arm properties form */
	public GateArmProperties(Session s, GateArm ga) {
		super(I18N.get("gate_arm") + ": ", s, ga);
		preset_mdl = new IComboBoxModel<CameraPreset>(
			state.getCamCache().getPresetModel());
		loc_pnl = new LocationPanel(s);
	}

	/** Get the SONAR type cache */
	@Override
	protected TypeCache<GateArm> getTypeCache() {
		return state.getGateArms();
	}

	/** Initialize the widgets on the form */
	@Override
	protected void initialize() {
		super.initialize();
		preset_cbx.setModel(preset_mdl);
		preset_cbx.setAction(preset_act);
		preset_cbx.setRenderer(new PresetComboRenderer());
		loc_pnl.initialize();
		loc_pnl.add("camera.preset");
		loc_pnl.add(preset_cbx, Stretch.LAST);
		loc_pnl.setGeoLoc(proxy.getGeoLoc());
		JTabbedPane tab = new JTabbedPane();
		tab.add(I18N.get("location"), loc_pnl);
		tab.add(I18N.get("device.setup"), createSetupPanel());
		tab.add(I18N.get("device.status"), createStatusPanel());
		add(tab);
		createUpdateJobs();
		settings.setEnabled(isWritePermitted("deviceRequest"));
	}

	/** Create the widget jobs */
	private void createUpdateJobs() {
		notes_txt.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				String n = notes_txt.getText().trim();
				proxy.setNotes((n.length() > 0) ? n : null);
			}
		});
		downstream_txt.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				proxy.setDownstream(Hashtags.normalize(
					downstream_txt.getText()));
			}
		});
	}

	/** Create gate arm setup panel */
	private JPanel createSetupPanel() {
		IPanel p = new IPanel();
		p.add("device.notes");
		p.add(notes_txt, Stretch.FULL);
		p.add("gate.arm.opposing");
		p.add(opposing_chk, Stretch.LAST);
		p.add("gate.arm.downstream");
		p.add(downstream_txt, Stretch.LAST);
		return p;
	}

	/** Create ramp meter status panel */
	private JPanel createStatusPanel() {
		interlock_lbl.setOpaque(true);
		IPanel p = new IPanel();
		p.add("gate.arm.state");
		p.add(arm_state_lbl, Stretch.LAST);
		p.add("gate.arm.interlock");
		p.add(interlock_lbl, Stretch.LAST);
		p.add("device.operation");
		p.add(op_lbl, Stretch.LAST);
		p.add(new JButton(settings), Stretch.RIGHT);
		p.add(new JButton(disable), Stretch.RIGHT);
		return p;
	}

	/** Dispose of the form */
	@Override
	protected void dispose() {
		loc_pnl.dispose();
		super.dispose();
	}

	/** Update the edit mode */
	@Override
	protected void updateEditMode() {
		loc_pnl.updateEditMode();
		preset_act.setEnabled(canWrite("preset"));
		notes_txt.setEnabled(canWrite("notes"));
		opposing_chk.setEnabled(canWrite("opposing"));
		downstream_txt.setEnabled(canWrite("downstream"));
		disable.setEnabled(canWrite("deviceRequest"));
	}

	/** Update one attribute on the form */
	@Override
	protected void doUpdateAttribute(String a) {
		if (a == null || a.equals("preset"))
			preset_act.updateSelected();
		if (null == a || a.equals("notes")) {
			String n = proxy.getNotes();
			notes_txt.setText((n != null) ? n : "");
		}
		if (null == a || a.equals("opposing"))
			opposing_chk.setSelected(proxy.getOpposing());
		if (null == a || a.equals("downstream")) {
			String d = proxy.getDownstream();
			downstream_txt.setText((d != null) ? d : "");
		}
		if (null == a || a.equals("armState")) {
			arm_state_lbl.setText(GateArmState.fromOrdinal(
				proxy.getArmState()).toString());
		}
		if (null == a || a.equals("interlock")) {
			InterlockStyle st =
				new InterlockStyle(proxy.getInterlock());
			interlock_lbl.setForeground(st.foreground());
			interlock_lbl.setBackground(st.background());
			interlock_lbl.setText(st.text());
		}
		if (null == a || a.equals("operation"))
			op_lbl.setText(proxy.getOperation());
	}
}
