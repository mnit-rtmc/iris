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
import us.mn.state.dot.sched.ChangeJob;
import us.mn.state.dot.sched.FocusJob;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.EncoderType;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.client.comm.ControllerForm;
import us.mn.state.dot.tms.client.proxy.SonarObjectForm;
import us.mn.state.dot.tms.client.roads.LocationPanel;
import us.mn.state.dot.tms.client.widget.FormPanel;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.utils.I18N;

/**
 * This is a form for viewing and editing the properties of a camera.
 *
 * @author Douglas Lau
 */
public class CameraProperties extends SonarObjectForm<Camera> {

	/** Location panel */
	private final LocationPanel location;

	/** Notes text area */
	protected final JTextArea notes = new JTextArea(3, 24);

	/** Controller action */
	private final IAction controller = new IAction("controller") {
		protected void do_perform() {
			controllerPressed();
		}
	};

	/** Video stream encoder host (and port) */
	protected final JTextField encoder = new JTextField("", 20);

	/** Model for encoder channel spinner */
	protected final SpinnerNumberModel num_model =
		new SpinnerNumberModel(1, 0, 10, 1);

	/** Encoder channel spinner */
	protected final JSpinner encoder_channel = new JSpinner(num_model);

	/** Encoder type action */
	private final IAction encoder_type = new IAction("camera.encoder.type"){
		protected void do_perform() {
		      proxy.setEncoderType(enc_type_cbx.getSelectedIndex());
		}
	};

	/** Encoder type combobox */
	private final JComboBox enc_type_cbx =
		new JComboBox(EncoderType.getDescriptions());

	/** Checkbox to allow publishing camera images */
	private final JCheckBox publish_chk = new JCheckBox(new IAction(null) {
		protected void do_perform() {
			proxy.setPublish(publish_chk.isSelected());
		}
	});

	/** Create a new camera properties form */
	public CameraProperties(Session s, Camera c) {
		super(I18N.get("camera") + ": ", s, c);
		location = new LocationPanel(s);
	}

	/** Get the SONAR type cache */
	protected TypeCache<Camera> getTypeCache() {
		return state.getCamCache().getCameras();
	}

	/** Initialize the widgets on the form */
	protected void initialize() {
		super.initialize();
		JTabbedPane tab = new JTabbedPane();
		tab.add(I18N.get("location"), createLocationPanel());
		tab.add(I18N.get("device.setup"), createSetupPanel());
		add(tab);
		updateAttribute(null);
		if(canUpdate())
			createJobs();
		setBackground(Color.LIGHT_GRAY);
	}

	/** Dispose of the form */
	protected void dispose() {
		location.dispose();
		super.dispose();
	}

	/** Create the location panel */
	protected JPanel createLocationPanel() {
		location.setGeoLoc(proxy.getGeoLoc());
		location.initialize();
		location.addRow(I18N.get("device.notes"), notes);
		location.setCenter();
		location.addRow(new JButton(controller));
		return location;
	}

	/** Controller lookup button pressed */
	protected void controllerPressed() {
		Controller c = proxy.getController();
		if(c != null) {
			session.getDesktop().show(
				new ControllerForm(session, c));
		}
	}

	/** Create camera setup panel */
	protected JPanel createSetupPanel() {
		enc_type_cbx.setAction(encoder_type);
		FormPanel panel = new FormPanel(canUpdate());
		panel.addRow(I18N.get("camera.encoder"), encoder);
		panel.addRow(I18N.get("camera.encoder.channel"),
			encoder_channel);
		panel.addRow(I18N.get("camera.encoder.type"), enc_type_cbx);
		panel.addRow(I18N.get("camera.publish"), publish_chk);
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
	}

	/** Update one attribute on the form */
	protected void doUpdateAttribute(String a) {
		if(a == null || a.equals("controller"))
			controller.setEnabled(proxy.getController() != null);
		if(a == null || a.equals("notes"))
			notes.setText(proxy.getNotes());
		if(a == null || a.equals("encoder"))
			encoder.setText(proxy.getEncoder());
		if(a == null || a.equals("encoderChannel"))
			encoder_channel.setValue(proxy.getEncoderChannel());
		if(a == null || a.equals("encoderType"))
			enc_type_cbx.setSelectedIndex(proxy.getEncoderType());
		if(a == null || a.equals("publish"))
			publish_chk.setSelected(proxy.getPublish());
	}
}
