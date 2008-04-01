/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2008  Minnesota Department of Transportation
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

import java.rmi.RemoteException;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.SortedList;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.toast.TrafficDeviceForm;
import us.mn.state.dot.tms.client.toast.FormPanel;

/**
 * This is a form for viewing and editing the properties of a camera.
 *
 * @author Douglas Lau
 */
public class CameraProperties extends TrafficDeviceForm {

	/** Frame title */
	static private final String TITLE = "Camera: ";

	/** Video stream encoder host (and port) */
	protected final JTextField encoder = new JTextField("", 20);

	/** Model for encoder channel spinner */
	protected final SpinnerNumberModel num_model =
		new SpinnerNumberModel(1, 0, 10, 1);

	/** Encoder channel spinner */
	protected final JSpinner encoder_channel = new JSpinner(num_model);

	/** Video stream NVR host (and port) */
	protected final JTextField nvr = new JTextField("", 20);

	/** Checkbox to allow publishing camera images */
	protected final JCheckBox publish = new JCheckBox();

	/** Remote camera object */
	protected Camera camera;

	/** Create a new camera properties form */
	public CameraProperties(TmsConnection tc, String id) {
		super(TITLE + id, tc, id);
	}

	/** Initialize the widgets on the form */
	protected void initialize() throws RemoteException {
		SortedList s = connection.getProxy().getCameraList();
		camera = (Camera)s.getElement(id);
		setDevice(camera);
		super.initialize();
		tab.add("Setup", createSetupPanel());
	}

	/** Create camera setup panel */
	protected JPanel createSetupPanel() {
		FormPanel panel = new FormPanel(admin);
		panel.addRow("Encoder", encoder);
		panel.addRow("Encoder Channel", encoder_channel);
		panel.addRow("NVR", nvr);
		panel.addRow("Publish", publish);
		return panel;
	}

	/** Update the form with the current state of the device */
	protected void doUpdate() throws RemoteException {
		super.doUpdate();
		encoder.setText(camera.getEncoder());
		encoder_channel.setValue(camera.getEncoderChannel());
		nvr.setText(camera.getNvr());
		publish.setSelected(camera.getPublish());
	}

	/** Apply button is pressed */
	protected void applyPressed() throws Exception {
		try {
			super.applyPressed();
			camera.setEncoder(encoder.getText());
			Number c = (Number)encoder_channel.getValue();
			camera.setEncoderChannel(c.intValue());
			camera.setNvr(nvr.getText());
			camera.setPublish(publish.isSelected());
		}
		finally {
			camera.notifyUpdate();
		}
	}
}
