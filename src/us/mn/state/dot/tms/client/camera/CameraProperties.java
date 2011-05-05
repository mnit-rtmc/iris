/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2011  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.camera;

import java.awt.Color;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sched.ChangeJob;
import us.mn.state.dot.sched.FocusJob;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.EncoderType;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.client.toast.ControllerForm;
import us.mn.state.dot.tms.client.toast.FormPanel;
import us.mn.state.dot.tms.client.toast.LocationPanel;
import us.mn.state.dot.tms.client.toast.SonarObjectForm;

/**
 * This is a form for viewing and editing the properties of a camera.
 *
 * @author Douglas Lau
 */
public class CameraProperties extends SonarObjectForm<Camera> {

	/** Frame title */
	static private final String TITLE = "Camera: ";

	/** Location panel */
	protected LocationPanel location;

	/** Notes text area */
	protected final JTextArea notes = new JTextArea(3, 24);

	/** Controller button */
	protected final JButton controller = new JButton("Controller");

	/** Video stream encoder host (and port) */
	protected final JTextField encoder = new JTextField("", 20);

	/** Model for encoder channel spinner */
	protected final SpinnerNumberModel num_model =
		new SpinnerNumberModel(1, 0, 10, 1);

	/** Encoder channel spinner */
	protected final JSpinner encoder_channel = new JSpinner(num_model);

	/** Encoder type combobox */
	protected final JComboBox type_cmb =
		new JComboBox(EncoderType.getDescriptions());

	/** Checkbox to allow publishing camera images */
	protected final JCheckBox publish = new JCheckBox();

	/** Create a new camera properties form */
	public CameraProperties(Session s, Camera c) {
		super(TITLE, s, c);
	}

	/** Get the SONAR type cache */
	protected TypeCache<Camera> getTypeCache() {
		return state.getCamCache().getCameras();
	}

	/** Initialize the widgets on the form */
	protected void initialize() {
		super.initialize();
		JTabbedPane tab = new JTabbedPane();
		tab.add("Location", createLocationPanel());
		tab.add("Setup", createSetupPanel());
		add(tab);
		updateAttribute(null);
		if(canUpdate())
			createJobs();
		controller.setEnabled(true);
		setBackground(Color.LIGHT_GRAY);
	}

	/** Dispose of the form */
	protected void dispose() {
		location.dispose();
		super.dispose();
	}

	/** Create the location panel */
	protected JPanel createLocationPanel() {
		location = new LocationPanel(session, proxy.getGeoLoc());
		location.initialize();
		location.addRow("Notes", notes);
		location.setCenter();
		location.addRow(controller);
		new ActionJob(this, controller) {
			public void perform() {
				controllerPressed();
			}
		};
		return location;
	}

	/** Controller lookup button pressed */
	protected void controllerPressed() {
		Controller c = proxy.getController();
		if(c == null)
			controller.setEnabled(false);
		else {
			session.getDesktop().show(
				new ControllerForm(session, c));
		}
	}

	/** Create camera setup panel */
	protected JPanel createSetupPanel() {
		FormPanel panel = new FormPanel(canUpdate());
		panel.addRow("Encoder", encoder);
		panel.addRow("Encoder Channel", encoder_channel);
		panel.addRow("Encoder Type", type_cmb);
		panel.addRow("Publish", publish);
		return panel;
	}

	/** Create jobs */
	protected void createJobs() {
		new FocusJob(notes) {
			public void perform() {
				proxy.setNotes(notes.getText());
			}
		};
		new FocusJob(encoder) {
			public void perform() {
				proxy.setEncoder(encoder.getText());
			}
		};
		new ChangeJob(this, encoder_channel) {
			public void perform() {
				Number c = (Number)encoder_channel.getValue();
				proxy.setEncoderChannel(c.intValue());
			}
		};
		new ActionJob(this, type_cmb) {
			public void perform() {
			      proxy.setEncoderType(type_cmb.getSelectedIndex());
			}
		};
		new ActionJob(this, publish) {
			public void perform() {
				proxy.setPublish(publish.isSelected());
			}
		};
	}

	/** Update one attribute on the form */
	protected void doUpdateAttribute(String a) {
		if(a == null || a.equals("notes"))
			notes.setText(proxy.getNotes());
		if(a == null || a.equals("encoder"))
			encoder.setText(proxy.getEncoder());
		if(a == null || a.equals("encoderChannel"))
			encoder_channel.setValue(proxy.getEncoderChannel());
		if(a == null || a.equals("encoderType"))
			type_cmb.setSelectedIndex(proxy.getEncoderType());
		if(a == null || a.equals("publish"))
			publish.setSelected(proxy.getPublish());
	}
}
