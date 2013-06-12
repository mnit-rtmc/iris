/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013  Minnesota Department of Transportation
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

import java.awt.Color;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import us.mn.state.dot.sched.FocusLostJob;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.GateArm;
import us.mn.state.dot.tms.GateArmState;
import us.mn.state.dot.tms.QuickMessageHelper;
import static us.mn.state.dot.tms.client.IrisClient.WORKER;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.client.comm.ControllerForm;
import us.mn.state.dot.tms.client.proxy.SonarObjectForm;
import us.mn.state.dot.tms.client.roads.LocationPanel;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IPanel;
import us.mn.state.dot.tms.client.widget.IPanel.Stretch;
import us.mn.state.dot.tms.client.widget.SmartDesktop;
import us.mn.state.dot.tms.client.widget.WrapperComboBoxModel;
import us.mn.state.dot.tms.utils.I18N;

/**
 * This is a form for viewing and editing the properties of a gate arm.
 *
 * @author Douglas Lau
 */
public class GateArmProperties extends SonarObjectForm<GateArm> {

	/** Get the controller status */
	static private String getControllerStatus(GateArm proxy) {
		Controller c = proxy.getController();
		if(c == null)
			return "???";
		else
			return c.getStatus();
	}

	/** Location panel */
	private final LocationPanel loc_pnl;

	/** Notes text area */
	private final JTextArea notes_txt = new JTextArea(3, 24);

	/** Camera action */
	private final IAction camera = new IAction("camera") {
		protected void do_perform() {
			proxy.setCamera((Camera)camera_cbx.getSelectedItem());
		}
	};

	/** Camera combo box */
	private final JComboBox camera_cbx = new JComboBox();

	/** Approach camera action */
	private final IAction approach = new IAction("gate.arm.approach") {
		protected void do_perform() {
			proxy.setApproach(
				(Camera)approach_cbx.getSelectedItem());
		}
	};

	/** Approach camera combo box */
	private final JComboBox approach_cbx = new JComboBox();

	/** Warning DMS action */
	private final IAction dms = new IAction("gate.arm.dms") {
		protected void do_perform() {
			proxy.setDms((DMS)dms_cbx.getSelectedItem());
		}
	};

	/** Warning DMS combo box */
	private final JComboBox dms_cbx = new JComboBox();

	/** Controller action */
	private final IAction controller = new IAction("controller") {
		protected void do_perform() {
			Controller c = proxy.getController();
			if(c != null) {
				SmartDesktop sd = session.getDesktop();
				sd.show(new ControllerForm(session, c));
			}
		}
	};

	/** Text field for OPEN quick message */
	private final JTextField open_msg_txt = new JTextField(20);

	/** Text field for CLOSED quick message */
	private final JTextField closed_msg_txt = new JTextField(20);

	/** Version label */
	private final JLabel version_lbl = IPanel.createValueLabel();

	/** Arm state label */
	private final JLabel arm_state_lbl = IPanel.createValueLabel();

	/** Operation description label */
	private final JLabel op_lbl = IPanel.createValueLabel();

	/** Status component */
	private final JLabel status_lbl = new JLabel();

	/** Send settings action */
	private final IAction settings = new IAction("device.send.settings") {
		protected void do_perform() {
			proxy.setDeviceRequest(DeviceRequest.
				SEND_SETTINGS.ordinal());
		}
	};

	/** Sonar state */
	private final SonarState state;

	/** Create a new gate arm properties form */
	public GateArmProperties(Session s, GateArm ga) {
		super(I18N.get("gate.arm") + ": ", s, ga);
		state = s.getSonarState();
		loc_pnl = new LocationPanel(s);
	}

	/** Get the SONAR type cache */
	@Override protected TypeCache<GateArm> getTypeCache() {
		return state.getGateArms();
	}

	/** Initialize the widgets on the form */
	@Override protected void initialize() {
		super.initialize();
		JTabbedPane tab = new JTabbedPane();
		tab.add(I18N.get("location"), createLocationPanel());
		tab.add(I18N.get("device.setup"), createSetupPanel());
		tab.add(I18N.get("device.status"), createStatusPanel());
		add(tab);
		updateAttribute(null);
		if(canUpdate())
			createUpdateJobs();
		settings.setEnabled(canUpdate("deviceRequest"));
		setBackground(Color.LIGHT_GRAY);
	}

	/** Create the location panel */
	private JPanel createLocationPanel() {
		camera_cbx.setModel(new WrapperComboBoxModel(
			state.getCamCache().getCameraModel()));
		approach_cbx.setModel(new WrapperComboBoxModel(
			state.getCamCache().getCameraModel()));
		loc_pnl.setGeoLoc(proxy.getGeoLoc());
		loc_pnl.initialize();
		loc_pnl.add("device.notes");
		loc_pnl.add(notes_txt, Stretch.FULL);
		loc_pnl.add("camera");
		loc_pnl.add(camera_cbx, Stretch.LAST);
		loc_pnl.add("gate.arm.approach");
		loc_pnl.add(approach_cbx, Stretch.LAST);
		loc_pnl.add(new JButton(controller), Stretch.RIGHT);
		return loc_pnl;
	}

	/** Create the widget jobs */
	private void createUpdateJobs() {
		notes_txt.addFocusListener(new FocusLostJob(WORKER) {
			public void perform() {
				proxy.setNotes(notes_txt.getText());
			}
		});
		open_msg_txt.addFocusListener(new FocusLostJob(WORKER) {
			public void perform() {
				proxy.setOpenMsg(QuickMessageHelper.lookup(
					open_msg_txt.getText()));
			}
		});
		closed_msg_txt.addFocusListener(new FocusLostJob(WORKER) {
			public void perform() {
				proxy.setClosedMsg(QuickMessageHelper.lookup(
					closed_msg_txt.getText()));
			}
		});
	}

	/** Create gate arm setup panel */
	private JPanel createSetupPanel() {
		dms_cbx.setModel(new WrapperComboBoxModel(
			state.getDmsCache().getDMSModel()));
		IPanel p = new IPanel();
		p.add("gate.arm.dms");
		p.add(dms_cbx, Stretch.LAST);
		p.add("gate.arm.open.msg");
		p.add(open_msg_txt, Stretch.LAST);
		p.add("gate.arm.closed.msg");
		p.add(closed_msg_txt, Stretch.LAST);
		return p;
	}

	/** Create ramp meter status panel */
	private JPanel createStatusPanel() {
		IPanel p = new IPanel();
		p.add("controller.version");
		p.add(version_lbl, Stretch.LAST);
		p.add("gate.arm.state");
		p.add(arm_state_lbl, Stretch.LAST);
		p.add("device.operation");
		p.add(op_lbl, Stretch.LAST);
		p.add("device.status");
		p.add(status_lbl, Stretch.LAST);
		p.add(new JButton(settings), Stretch.RIGHT);
		return p;
	}

	/** Update one attribute on the form */
	@Override protected void doUpdateAttribute(String a) {
		if(a == null || a.equals("controller"))
			controller.setEnabled(proxy.getController() != null);
		if(a == null || a.equals("notes")) {
			notes_txt.setEnabled(canUpdate("notes"));
			notes_txt.setText(proxy.getNotes());
		}
		if(a == null || a.equals("camera")) {
			camera_cbx.setAction(null);
			camera_cbx.setEnabled(canUpdate("camera"));
			camera_cbx.setSelectedItem(proxy.getCamera());
			camera_cbx.setAction(camera);
		}
		if(a == null || a.equals("approach")) {
			approach_cbx.setAction(null);
			approach_cbx.setEnabled(canUpdate("approach"));
			approach_cbx.setSelectedItem(proxy.getApproach());
			approach_cbx.setAction(approach);
		}
		if(a == null || a.equals("dms")) {
			dms_cbx.setAction(null);
			dms_cbx.setEnabled(canUpdate("dms"));
			dms_cbx.setSelectedItem(proxy.getDms());
			dms_cbx.setAction(dms);
		}
		if(a == null || a.equals("openMsg")) {
			open_msg_txt.setEnabled(canUpdate("openMsg"));
			open_msg_txt.setText(proxy.getOpenMsg().toString());
		}
		if(a == null || a.equals("closedMsg")) {
			closed_msg_txt.setEnabled(canUpdate("closedMsg"));
			closed_msg_txt.setText(proxy.getClosedMsg().toString());
		}
		if(a == null || a.equals("version"))
			version_lbl.setText(proxy.getVersion());
		if(a == null || a.equals("armState")) {
			arm_state_lbl.setText(GateArmState.fromOrdinal(
				proxy.getArmState()).toString());
		}
		if(a == null || a.equals("operation")) {
			op_lbl.setText(proxy.getOperation());
			String s = getControllerStatus(proxy);
			if("".equals(s)) {
				op_lbl.setForeground(null);
				op_lbl.setBackground(null);
			} else {
				op_lbl.setForeground(Color.WHITE);
				op_lbl.setBackground(Color.GRAY);
			}
			status_lbl.setText(s);
		}
	}
}
