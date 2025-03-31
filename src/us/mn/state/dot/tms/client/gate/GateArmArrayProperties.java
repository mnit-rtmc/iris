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
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.GateArm;
import us.mn.state.dot.tms.GateArmArray;
import us.mn.state.dot.tms.GateArmArrayHelper;
import us.mn.state.dot.tms.GateArmState;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyTablePanel;
import us.mn.state.dot.tms.client.proxy.SonarObjectForm;
import us.mn.state.dot.tms.client.roads.LocationPanel;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IComboBoxModel;
import us.mn.state.dot.tms.client.widget.IPanel;
import us.mn.state.dot.tms.client.widget.IPanel.Stretch;
import us.mn.state.dot.tms.utils.I18N;

/**
 * This is a form for viewing and editing the properties of a gate arm array.
 *
 * @author Douglas Lau
 */
public class GateArmArrayProperties extends SonarObjectForm<GateArmArray> {

	/** Location panel */
	private final LocationPanel loc_pnl;

	/** Notes text area */
	private final JTextArea notes_txt = new JTextArea(8, 32);

	/** Camera combo box model */
	private final IComboBoxModel<Camera> camera_mdl;

	/** Camera action */
	private final IAction camera_act = new IAction("camera") {
		protected void doActionPerformed(ActionEvent e) {
			proxy.setCamera(camera_mdl.getSelectedProxy());
		}
		@Override
		protected void doUpdateSelected() {
			camera_mdl.setSelectedItem(proxy.getCamera());
		}
	};

	/** Camera combo box */
	private final JComboBox<Camera> camera_cbx = new JComboBox<Camera>();

	/** Approach camera combo box model */
	private final IComboBoxModel<Camera> approach_mdl;

	/** Approach camera action */
	private final IAction approach_act = new IAction("gate.arm.approach") {
		protected void doActionPerformed(ActionEvent e) {
			proxy.setApproach(approach_mdl.getSelectedProxy());
		}
		@Override
		protected void doUpdateSelected() {
			approach_mdl.setSelectedItem(proxy.getApproach());
		}
	};

	/** Approach camera combo box */
	private final JComboBox<Camera> approach_cbx = new JComboBox<Camera>();

	/** Checkbox for opposing traffic flag */
	private final JCheckBox opposing_chk = new JCheckBox(new IAction(null) {
		protected void doActionPerformed(ActionEvent e) {
			proxy.setOpposing(opposing_chk.isSelected());
		}
	});

	/** Prerequisite combo box model */
	private final IComboBoxModel<GateArmArray> prereq_mdl;

	/** Prerequisite gate arm array */
	private final IAction prereq_act = new IAction("gate.arm.prereq") {
		protected void doActionPerformed(ActionEvent e) {
			GateArmArray ga = prereq_mdl.getSelectedProxy();
			if (ga != null)
				proxy.setPrereq(ga.getName());
			else
				proxy.setPrereq(null);
		}
		@Override
		protected void doUpdateSelected() {
			prereq_mdl.setSelectedItem(GateArmArrayHelper.lookup(
				proxy.getPrereq()));
		}
	};

	/** Prerequisite combo box */
	private final JComboBox<GateArmArray> prereq_cbx =
		new JComboBox<GateArmArray>();

	/** Action plan combo box model */
	private final IComboBoxModel<ActionPlan> plan_mdl;

	/** Action plan action */
	private final IAction plan_act = new IAction("gate.arm.plan") {
		protected void doActionPerformed(ActionEvent e) {
			proxy.setActionPlan(plan_mdl.getSelectedProxy());
		}
		@Override
		protected void doUpdateSelected() {
			plan_mdl.setSelectedItem(proxy.getActionPlan());
		}
	};

	/** Action plan combo box */
	private final JComboBox<ActionPlan> plan_cbx =
		new JComboBox<ActionPlan>();

	/** Gate arm table panel */
	private final ProxyTablePanel<GateArm> ga_pnl;

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

	/** Create a new gate arm array properties form */
	public GateArmArrayProperties(Session s, GateArmArray ga) {
		super(I18N.get("gate_arm_array") + ": ", s, ga);
		camera_mdl = new IComboBoxModel<Camera>(
			state.getCamCache().getCameraModel());
		approach_mdl = new IComboBoxModel<Camera>(
			state.getCamCache().getCameraModel());
		prereq_mdl = new IComboBoxModel<GateArmArray>(
			state.getGateArmArrayModel());
		plan_mdl = new IComboBoxModel<ActionPlan>(state.getPlanModel());
		loc_pnl = new LocationPanel(s);
		ga_pnl = new ProxyTablePanel<GateArm>(new GateArmTableModel(
			s, ga));
	}

	/** Get the SONAR type cache */
	@Override
	protected TypeCache<GateArmArray> getTypeCache() {
		return state.getGateArmArrays();
	}

	/** Initialize the widgets on the form */
	@Override
	protected void initialize() {
		super.initialize();
		loc_pnl.initialize();
		ga_pnl.initialize();
		JTabbedPane tab = new JTabbedPane();
		tab.add(I18N.get("location"), createLocationPanel());
		tab.add(I18N.get("device.setup"), createSetupPanel());
		tab.add(I18N.get("gate_arm.title"), ga_pnl);
		tab.add(I18N.get("device.status"), createStatusPanel());
		add(tab);
		createUpdateJobs();
		settings.setEnabled(isWritePermitted("deviceRequest"));
	}

	/** Create the location panel */
	private JPanel createLocationPanel() {
		camera_cbx.setModel(camera_mdl);
		camera_cbx.setAction(camera_act);
		approach_cbx.setModel(approach_mdl);
		approach_cbx.setAction(approach_act);
		loc_pnl.add("device.notes");
		loc_pnl.add(notes_txt, Stretch.FULL);
		loc_pnl.add("camera");
		loc_pnl.add(camera_cbx, Stretch.LAST);
		loc_pnl.add("gate.arm.approach");
		loc_pnl.add(approach_cbx, Stretch.LAST);
		loc_pnl.setGeoLoc(proxy.getGeoLoc());
		return loc_pnl;
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
	}

	/** Create gate arm setup panel */
	private JPanel createSetupPanel() {
		prereq_cbx.setModel(prereq_mdl);
		prereq_cbx.setAction(prereq_act);
		plan_cbx.setModel(plan_mdl);
		plan_cbx.setAction(plan_act);
		IPanel p = new IPanel();
		p.add("gate.arm.opposing");
		p.add(opposing_chk, Stretch.LAST);
		p.add("gate.arm.prereq");
		p.add(prereq_cbx, Stretch.LAST);
		p.add("gate.arm.plan");
		p.add(plan_cbx, Stretch.LAST);
		return p;
	}

	/** Create ramp meter status panel */
	private JPanel createStatusPanel() {
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
		// Prevent plan being cleared on close
		plan_act.setEnabled(false);
		ga_pnl.dispose();
		loc_pnl.dispose();
		super.dispose();
	}

	/** Update the edit mode */
	@Override
	protected void updateEditMode() {
		loc_pnl.updateEditMode();
		notes_txt.setEnabled(canWrite("notes"));
		camera_act.setEnabled(canWrite("camera"));
		approach_act.setEnabled(canWrite("approach"));
		opposing_chk.setEnabled(canWrite("opposing"));
		prereq_act.setEnabled(canWrite("prereq"));
		plan_act.setEnabled(canWrite("actionPlan"));
		disable.setEnabled(canWrite("deviceRequest"));
	}

	/** Update one attribute on the form */
	@Override
	protected void doUpdateAttribute(String a) {
		if (a == null || a.equals("notes")) {
			String n = proxy.getNotes();
			notes_txt.setText((n != null) ? n : "");
		}
		if (a == null || a.equals("camera"))
			camera_act.updateSelected();
		if (a == null || a.equals("approach"))
			approach_act.updateSelected();
		if (null == a || a.equals("opposing"))
			opposing_chk.setSelected(proxy.getOpposing());
		if (null == a || a.equals("prereq"))
			prereq_act.updateSelected();
		if (null == a || a.equals("actionPlan"))
			plan_act.updateSelected();
		if (null == a || a.equals("armState")) {
			arm_state_lbl.setText(GateArmState.fromOrdinal(
				proxy.getArmState()).toString());
		}
		if (null == a || a.equals("interlock")) {
			interlock_lbl.setText(
				GateArmDispatcher.getInterlockText(proxy));
		}
		if (a == null || a.equals("operation"))
			op_lbl.setText(proxy.getOperation());
	}
}
