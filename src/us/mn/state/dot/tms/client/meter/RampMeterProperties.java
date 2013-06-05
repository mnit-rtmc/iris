/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2013  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.meter;

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
import us.mn.state.dot.tms.MeterAlgorithm;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.RampMeterLock;
import us.mn.state.dot.tms.RampMeterQueue;
import us.mn.state.dot.tms.RampMeterType;
import static us.mn.state.dot.tms.client.IrisClient.WORKER;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.client.comm.ControllerForm;
import us.mn.state.dot.tms.client.proxy.SonarObjectForm;
import us.mn.state.dot.tms.client.roads.LocationPanel;
import us.mn.state.dot.tms.client.widget.FormPanel;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.SmartDesktop;
import us.mn.state.dot.tms.client.widget.WrapperComboBoxModel;
import us.mn.state.dot.tms.utils.I18N;

/**
 * This is a form for viewing and editing the properties of a ramp meter.
 *
 * @author Douglas Lau
 */
public class RampMeterProperties extends SonarObjectForm<RampMeter> {

	/** Get the controller status */
	static private String getControllerStatus(RampMeter proxy) {
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

	/** Meter type action */
	private final IAction meter_type = new IAction("ramp.meter.type") {
		protected void do_perform() {
			int t = meter_type_cbx.getSelectedIndex();
			if(t >= 0)
				proxy.setMeterType(t);
		}
	};

	/** Meter type combo box component */
	private final JComboBox meter_type_cbx = new JComboBox(
		RampMeterType.getDescriptions());

	/** Field for Storage length (feet) */
	private final JTextField storage_txt = new JTextField();

	/** Field for Maximum wait time (seconds) */
	private final JTextField max_wait_txt = new JTextField();

	/** Metering algorithm action */
	private final IAction algorithm = new IAction("ramp.meter.algorithm") {
		protected void do_perform() {
			int a = algorithm_cbx.getSelectedIndex();
			if(a >= 0)
				proxy.setAlgorithm(a);
		}
	};

	/** Combo box for metering algorithm */
	private final JComboBox algorithm_cbx = new JComboBox(
		MeterAlgorithm.getDescriptions());

	/** Field for AM target rate */
	private final JTextField am_target_txt = new JTextField();

	/** Field for PM target rate */
	private final JTextField pm_target_txt = new JTextField();

	/** Release rate component */
	private final JLabel release_lbl = new JLabel();

	/** Cycle time component */
	private final JLabel cycle_lbl = new JLabel();

	/** Queue label component */
	private final JLabel queue_lbl = new JLabel();

	/** Meter lock combo box component */
	private final JComboBox lock_cmb = new JComboBox(
		RampMeterLock.getDescriptions());

	/** Operation description label */
	private final JLabel op_lbl = new JLabel();

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

	/** Create a new ramp meter properties form */
	public RampMeterProperties(Session s, RampMeter meter) {
		super(I18N.get("ramp.meter.long") + ": ", s, meter);
		state = s.getSonarState();
		loc_pnl = new LocationPanel(s);
	}

	/** Get the SONAR type cache */
	@Override protected TypeCache<RampMeter> getTypeCache() {
		return state.getRampMeters();
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
		camera_cbx.setAction(camera);
		camera_cbx.setModel(new WrapperComboBoxModel(
			state.getCamCache().getCameraModel()));
		loc_pnl.setGeoLoc(proxy.getGeoLoc());
		loc_pnl.initialize();
		loc_pnl.addRow(I18N.get("device.notes"), notes_txt);
		loc_pnl.add(I18N.get("camera"), camera_cbx);
		loc_pnl.finishRow();
		loc_pnl.setCenter();
		loc_pnl.addRow(new JButton(controller));
		return loc_pnl;
	}

	/** Create the widget jobs */
	private void createUpdateJobs() {
		notes_txt.addFocusListener(new FocusLostJob(WORKER) {
			@Override public void perform() {
				proxy.setNotes(notes_txt.getText());
			}
		});
		storage_txt.addFocusListener(new FocusLostJob(WORKER) {
			@Override public void perform() {
				proxy.setStorage(Integer.parseInt(
					storage_txt.getText()));
			}
		});
		max_wait_txt.addFocusListener(new FocusLostJob(WORKER) {
			@Override public void perform() {
				proxy.setMaxWait(Integer.parseInt(
					max_wait_txt.getText()));
			}
		});
		am_target_txt.addFocusListener(new FocusLostJob(WORKER) {
			@Override public void perform() {
				proxy.setAmTarget(Integer.parseInt(
					am_target_txt.getText()));
			}
		});
		pm_target_txt.addFocusListener(new FocusLostJob(WORKER) {
			@Override public void perform() {
				proxy.setPmTarget(Integer.parseInt(
					pm_target_txt.getText()));
			}
		});
		lock_cmb.setAction(new LockMeterAction(proxy, lock_cmb));
	}

	/** Create ramp meter setup panel */
	private JPanel createSetupPanel() {
		meter_type_cbx.setAction(meter_type);
		algorithm_cbx.setAction(algorithm);
		FormPanel panel = new FormPanel(canUpdate());
		panel.addRow(I18N.get("ramp.meter.type"), meter_type_cbx);
		panel.addRow(I18N.get("ramp.meter.storage"), storage_txt);
		panel.addRow(I18N.get("ramp.meter.max.wait"), max_wait_txt);
		panel.addRow(I18N.get("ramp.meter.algorithm"), algorithm_cbx);
		panel.addRow(I18N.get("ramp.meter.target.am"), am_target_txt);
		panel.addRow(I18N.get("ramp.meter.target.pm"), pm_target_txt);
		return panel;
	}

	/** Create ramp meter status panel */
	private JPanel createStatusPanel() {
		FormPanel panel = new FormPanel(canUpdate());
		panel.addRow(I18N.get("ramp.meter.rate"), release_lbl);
		panel.addRow(I18N.get("ramp.meter.cycle"), cycle_lbl);
		panel.addRow(I18N.get("ramp.meter.queue"), queue_lbl);
		panel.addRow(I18N.get("ramp.meter.lock"), lock_cmb);
		panel.addRow(I18N.get("device.operation"), op_lbl);
		panel.addRow(I18N.get("device.status"), status_lbl);
		panel.addRow(new JButton(settings));
		return panel;
	}

	/** Update one attribute on the form */
	@Override protected void doUpdateAttribute(String a) {
		if(a == null || a.equals("controller"))
			controller.setEnabled(proxy.getController() != null);
		if(a == null || a.equals("notes"))
			notes_txt.setText(proxy.getNotes());
		if(a == null || a.equals("camera"))
			camera_cbx.setSelectedItem(proxy.getCamera());
		if(a == null || a.equals("meterType"))
			meter_type_cbx.setSelectedIndex(proxy.getMeterType());
		if(a == null || a.equals("storage"))
			storage_txt.setText("" + proxy.getStorage());
		if(a == null || a.equals("maxWait"))
			max_wait_txt.setText("" + proxy.getMaxWait());
		if(a == null || a.equals("algorithm"))
			algorithm_cbx.setSelectedIndex(proxy.getAlgorithm());
		if(a == null || a.equals("amTarget"))
			am_target_txt.setText("" + proxy.getAmTarget());
		if(a == null || a.equals("pmTarget"))
			pm_target_txt.setText("" + proxy.getPmTarget());
		if(a == null || a.equals("rate")) {
			Integer rate = proxy.getRate();
			cycle_lbl.setText(MeterStatusPanel.formatCycle(rate));
			release_lbl.setText(MeterStatusPanel.formatRelease(
				rate));
		}
		if(a == null || a.equals("queue")) {
			RampMeterQueue q = RampMeterQueue.fromOrdinal(
				proxy.getQueue());
			queue_lbl.setText(q.description);
		}
		if(a == null || a.equals("mLock")) {
			Integer ml = proxy.getMLock();
			if(ml != null)
				lock_cmb.setSelectedIndex(ml);
			else
				lock_cmb.setSelectedIndex(0);
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
