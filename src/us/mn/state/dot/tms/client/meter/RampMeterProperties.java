/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sched.FocusJob;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.RampMeterLock;
import us.mn.state.dot.tms.RampMeterQueue;
import us.mn.state.dot.tms.RampMeterType;
import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.toast.ControllerForm;
import us.mn.state.dot.tms.client.toast.FormPanel;
import us.mn.state.dot.tms.client.toast.LocationPanel;
import us.mn.state.dot.tms.client.toast.SonarObjectForm;
import us.mn.state.dot.tms.client.toast.WrapperComboBoxModel;

/**
 * This is a form for viewing and editing the properties of a ramp meter.
 *
 * @author Douglas Lau
 */
public class RampMeterProperties extends SonarObjectForm<RampMeter> {

	/** Frame title */
	static protected final String TITLE = "Ramp Meter: ";

	/** Format the meter cycle time from the given release rate */
	static protected String formatCycle(float rate) {
		int i_cycle = Math.round(36000 / rate);
		return "" + (i_cycle / 10) + "." + (i_cycle % 10) + " seconds";
	}

	/** Get the controller status */
	static protected String getControllerStatus(RampMeter proxy) {
		Controller c = proxy.getController();
		if(c == null)
			return "???";
		else
			return c.getStatus();
	}

	/** Location panel */
	protected LocationPanel location;

	/** Notes text area */
	protected final JTextArea notes = new JTextArea(3, 24);

	/** Camera combo box */
	protected final JComboBox camera = new JComboBox();

	/** Controller button */
	protected final JButton controllerBtn = new JButton("Controller");

	/** Meter type combo box component */
	protected final JComboBox meterType = new JComboBox(
		RampMeterType.getDescriptions());

	/** Field for Storage length (feet) */
	protected final JTextField storage = new JTextField();

	/** Field for Maximum wait time (seconds) */
	protected final JTextField wait = new JTextField();

	/** Timing plan table component */
	protected final JTable plan_table = new JTable();

	/** Release rate component */
	protected final JLabel release = new JLabel();

	/** Cycle time component */
	protected final JLabel cycle = new JLabel();

	/** Queue label component */
	protected final JLabel queue = new JLabel();

	/** Meter lock combo box component */
	protected final JComboBox lock = new JComboBox(
		RampMeterLock.getDescriptions());

	/** Operation description label */
	protected final JLabel operation = new JLabel();

	/** Status component */
	protected final JLabel l_status = new JLabel();

	/** Sonar state */
	protected final SonarState state;

	/** Create a new ramp meter properties form */
	public RampMeterProperties(TmsConnection tc, RampMeter meter) {
		super(TITLE, tc, meter);
		state = tc.getSonarState();
	}

	/** Get the SONAR type cache */
	protected TypeCache<RampMeter> getTypeCache() {
		return state.getRampMeters();
	}

	/** Initialize the widgets on the form */
	protected void initialize() {
		super.initialize();
		JTabbedPane tab = new JTabbedPane();
		tab.add("Location", createLocationPanel());
		tab.add("Setup", createSetupPanel());
		tab.add("Timing Plans", createTimingPlanPanel());
		tab.add("Status", createStatusPanel());
		add(tab);
		updateAttribute(null);
		setBackground(Color.LIGHT_GRAY);
	}

	/** Create the location panel */
	protected JPanel createLocationPanel() {
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
		new ActionJob(this, controllerBtn) {
			public void perform() {
				controllerPressed();
			}
		};
		location = new LocationPanel(true, proxy.getGeoLoc(), state);
		location.initialize();
		location.addRow("Notes", notes);
		camera.setModel(new WrapperComboBoxModel(
			state.getCameraModel()));
		location.addRow("Camera", camera);
		location.setCenter();
		location.addRow(controllerBtn);
		return location;
	}

	/** Controller lookup button pressed */
	protected void controllerPressed() {
		Controller c = proxy.getController();
		if(c == null)
			controllerBtn.setEnabled(false);
		else {
			connection.getDesktop().show(
				new ControllerForm(connection, c));
		}
	}

	/** Create ramp meter setup panel */
	protected JPanel createSetupPanel() {
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
		new FocusJob(wait) {
			public void perform() {
				proxy.setMaxWait(Integer.parseInt(
					wait.getText()));
			}
		};
		FormPanel panel = new FormPanel(true);
		panel.addRow("Meter Type", meterType);
		panel.addRow("Storage (feet)", storage);
		panel.addRow("Max Wait (seconds)", wait);
		return panel;
	}

	/** Create timing plan panel */
	protected JPanel createTimingPlanPanel() {
		FormPanel panel = new FormPanel(true);
		panel.addRow(plan_table);
		return panel;
	}

	/** Create ramp meter status panel */
	protected JPanel createStatusPanel() {
		FormPanel panel = new FormPanel(true);
		panel.addRow("Release rate", release);
		panel.addRow("Cycle time", cycle);
		panel.addRow("Queue", queue);
		panel.addRow("Lock", lock);
		panel.addRow("Operation", operation);
		panel.addRow("Status", l_status);
		return panel;
	}

	/** Update one attribute on the form */
	protected void updateAttribute(String a) {
		if(a == null || a.equals("notes"))
			notes.setText(proxy.getNotes());
		if(a == null || a.equals("camera"))
			camera.setSelectedItem(proxy.getCamera());
		if(a == null || a.equals("meterType"))
			meterType.setSelectedIndex(proxy.getMeterType());
		if(a == null || a.equals("storage"))
			storage.setText("" + proxy.getStorage());
		if(a == null || a.equals("wait"))
			wait.setText("" + proxy.getMaxWait());
		if(a == null || a.equals("timingPlan"))
			plan_table.setModel(new TimingPlanModel(proxy));
		if(a == null || a.equals("release")) {
			Integer rate = proxy.getRate();
			if(rate !=  null) {
				release.setText(rate.toString());
				cycle.setText(formatCycle(rate));
			} else {
				release.setText("N/A");
				cycle.setText("N/A");
			}
		}
		if(a == null || a.equals("queue")) {
			RampMeterQueue q = RampMeterQueue.fromOrdinal(
				proxy.getQueue());
			queue.setText(q.description);
		}
		if(a == null || a.equals("lock"))
			lock.setSelectedIndex(proxy.getLock());
		if(a == null || a.equals("operation")) {
			operation.setText(proxy.getOperation());
			String s = getControllerStatus(proxy);
			if("".equals(s)) {
				l_status.setForeground(null);
				l_status.setBackground(null);
			} else {
				l_status.setForeground(Color.WHITE);
				l_status.setBackground(Color.GRAY);
			}
			l_status.setText(s);
		}
	}
}
