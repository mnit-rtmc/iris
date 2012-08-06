/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2012  Minnesota Department of Transportation
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
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sched.FocusJob;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.MeterAlgorithm;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.RampMeterLock;
import us.mn.state.dot.tms.RampMeterQueue;
import us.mn.state.dot.tms.RampMeterType;
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
	static protected String getControllerStatus(RampMeter proxy) {
		Controller c = proxy.getController();
		if(c == null)
			return "???";
		else
			return c.getStatus();
	}

	/** Location panel */
	private final LocationPanel location;

	/** Notes text area */
	protected final JTextArea notes = new JTextArea(3, 24);

	/** Camera combo box */
	protected final JComboBox camera = new JComboBox();

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

	/** Meter type combo box component */
	protected final JComboBox meterType = new JComboBox(
		RampMeterType.getDescriptions());

	/** Field for Storage length (feet) */
	protected final JTextField storage = new JTextField();

	/** Field for Maximum wait time (seconds) */
	protected final JTextField max_wait = new JTextField();

	/** Combo box for metering algorithm */
	private final JComboBox algorithm_cbx = new JComboBox(
		MeterAlgorithm.getDescriptions());

	/** Field for AM target rate */
	protected final JTextField am_target_txt = new JTextField();

	/** Field for PM target rate */
	protected final JTextField pm_target_txt = new JTextField();

	/** Release rate component */
	protected final JLabel release = new JLabel();

	/** Cycle time component */
	protected final JLabel cycle = new JLabel();

	/** Queue label component */
	protected final JLabel queue = new JLabel();

	/** Meter lock combo box component */
	protected final JComboBox m_lock = new JComboBox(
		RampMeterLock.getDescriptions());

	/** Operation description label */
	protected final JLabel operation = new JLabel();

	/** Status component */
	protected final JLabel l_status = new JLabel();

	/** Send settings action */
	private final IAction settings = new IAction("device.send.settings") {
		protected void do_perform() {
			proxy.setDeviceRequest(DeviceRequest.
				SEND_SETTINGS.ordinal());
		}
	};

	/** Sonar state */
	protected final SonarState state;

	/** Create a new ramp meter properties form */
	public RampMeterProperties(Session s, RampMeter meter) {
		super(I18N.get("ramp.meter.long") + ": ", s, meter);
		state = s.getSonarState();
		location = new LocationPanel(s);
	}

	/** Get the SONAR type cache */
	protected TypeCache<RampMeter> getTypeCache() {
		return state.getRampMeters();
	}

	/** Initialize the widgets on the form */
	protected void initialize() {
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
	protected JPanel createLocationPanel() {
		location.setGeoLoc(proxy.getGeoLoc());
		location.initialize();
		location.addRow(I18N.get("device.notes"), notes);
		camera.setModel(new WrapperComboBoxModel(
			state.getCamCache().getCameraModel()));
		location.add(I18N.get("camera"), camera);
		location.finishRow();
		location.setCenter();
		location.addRow(new JButton(controller));
		return location;
	}

	/** Create the widget jobs */
	protected void createUpdateJobs() {
		new FocusJob(notes) {
			public void perform() {
				proxy.setNotes(notes.getText());
			}
		};
		new ActionJob(this, camera) {
			public void perform() {
				proxy.setCamera(
					(Camera)camera.getSelectedItem());
			}
		};
		new ActionJob(this, meterType) {
			public void perform() {
				int t = meterType.getSelectedIndex();
				if(t >= 0)
					proxy.setMeterType(t);
			}
		};
		new FocusJob(storage) {
			public void perform() {
				proxy.setStorage(Integer.parseInt(
					storage.getText()));
			}
		};
		new FocusJob(max_wait) {
			public void perform() {
				proxy.setMaxWait(Integer.parseInt(
					max_wait.getText()));
			}
		};
		new ActionJob(this, algorithm_cbx) {
			public void perform() {
				int a = algorithm_cbx.getSelectedIndex();
				if(a >= 0)
					proxy.setAlgorithm(a);
			}
		};
		new FocusJob(am_target_txt) {
			public void perform() {
				proxy.setAmTarget(Integer.parseInt(
					am_target_txt.getText()));
			}
		};
		new FocusJob(pm_target_txt) {
			public void perform() {
				proxy.setPmTarget(Integer.parseInt(
					pm_target_txt.getText()));
			}
		};
		m_lock.setAction(new LockMeterAction(proxy, m_lock));
	}

	/** Create ramp meter setup panel */
	protected JPanel createSetupPanel() {
		FormPanel panel = new FormPanel(canUpdate());
		panel.addRow(I18N.get("ramp.meter.type"), meterType);
		panel.addRow(I18N.get("ramp.meter.storage"), storage);
		panel.addRow(I18N.get("ramp.meter.max.wait"), max_wait);
		panel.addRow(I18N.get("ramp.meter.algorithm"), algorithm_cbx);
		panel.addRow(I18N.get("ramp.meter.target.am"), am_target_txt);
		panel.addRow(I18N.get("ramp.meter.target.pm"), pm_target_txt);
		return panel;
	}

	/** Create ramp meter status panel */
	protected JPanel createStatusPanel() {
		FormPanel panel = new FormPanel(canUpdate());
		panel.addRow(I18N.get("ramp.meter.rate"), release);
		panel.addRow(I18N.get("ramp.meter.cycle"), cycle);
		panel.addRow(I18N.get("ramp.meter.queue"), queue);
		panel.addRow(I18N.get("ramp.meter.lock"), m_lock);
		panel.addRow(I18N.get("device.operation"), operation);
		panel.addRow(I18N.get("device.status"), l_status);
		panel.addRow(new JButton(settings));
		return panel;
	}

	/** Update one attribute on the form */
	protected void doUpdateAttribute(String a) {
		if(a == null || a.equals("controller"))
			controller.setEnabled(proxy.getController() != null);
		if(a == null || a.equals("notes"))
			notes.setText(proxy.getNotes());
		if(a == null || a.equals("camera"))
			camera.setSelectedItem(proxy.getCamera());
		if(a == null || a.equals("meterType"))
			meterType.setSelectedIndex(proxy.getMeterType());
		if(a == null || a.equals("storage"))
			storage.setText("" + proxy.getStorage());
		if(a == null || a.equals("maxWait"))
			max_wait.setText("" + proxy.getMaxWait());
		if(a == null || a.equals("algorithm"))
			algorithm_cbx.setSelectedIndex(proxy.getAlgorithm());
		if(a == null || a.equals("amTarget"))
			am_target_txt.setText("" + proxy.getAmTarget());
		if(a == null || a.equals("pmTarget"))
			pm_target_txt.setText("" + proxy.getPmTarget());
		if(a == null || a.equals("rate")) {
			Integer rate = proxy.getRate();
			cycle.setText(MeterStatusPanel.formatCycle(rate));
			release.setText(MeterStatusPanel.formatRelease(rate));
		}
		if(a == null || a.equals("queue")) {
			RampMeterQueue q = RampMeterQueue.fromOrdinal(
				proxy.getQueue());
			queue.setText(q.description);
		}
		if(a == null || a.equals("mLock")) {
			Integer ml = proxy.getMLock();
			if(ml != null)
				m_lock.setSelectedIndex(ml);
			else
				m_lock.setSelectedIndex(0);
		}
		if(a == null || a.equals("operation")) {
			operation.setText(proxy.getOperation());
			String s = getControllerStatus(proxy);
			if("".equals(s)) {
				operation.setForeground(null);
				operation.setBackground(null);
			} else {
				operation.setForeground(Color.WHITE);
				operation.setBackground(Color.GRAY);
			}
			l_status.setText(s);
		}
	}
}
