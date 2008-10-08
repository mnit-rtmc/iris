/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.warning;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.rmi.RemoteException;
import javax.swing.JButton;
import javax.swing.ListModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sched.FocusJob;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.WarningSign;
import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.toast.ControllerForm;
import us.mn.state.dot.tms.client.toast.FormPanel;
import us.mn.state.dot.tms.client.toast.LocationPanel;
import us.mn.state.dot.tms.client.toast.SmartDesktop;
import us.mn.state.dot.tms.client.toast.SonarObjectForm;
import us.mn.state.dot.tms.client.toast.WrapperComboBoxModel;

/**
 * WarningSignForm is a Swing dialog for entering and editing warning signs 
 *
 * @author Douglas Lau
 */
public class WarningSignProperties extends SonarObjectForm<WarningSign> {

	/** Frame title */
	static private final String TITLE = "Warning Sign: ";

	/** Location panel */
	protected LocationPanel location;

	/** Notes text area */
	protected final JTextArea notes = new JTextArea(3, 24);

	/** Controller button */
	protected final JButton controller = new JButton("Controller");

	/** Camera combo box */
	protected final JComboBox camera = new JComboBox();

	/** Sign message text area */
	protected final JTextArea message = new JTextArea(3, 24);

	/** Create a new warning sign form */
	public WarningSignProperties(TmsConnection tc, WarningSign s) {
		super(TITLE, tc, s);
	}

	/** Get the SONAR type cache */
	protected TypeCache<WarningSign> getTypeCache() {
		return state.getWarningSigns();
	}

	/** Initialize the widgets on the form */
	protected void initialize() throws RemoteException {
		super.initialize();
		JTabbedPane tab = new JTabbedPane();
		tab.add("Location", createLocationPanel());
		tab.add("Setup", createSetupPanel());
		add(tab);
		updateAttribute(null);
		setBackground(Color.LIGHT_GRAY);
	}

	/** Dispose of the form */
	protected void dispose() {
		location.dispose();
		super.dispose();
	}

	/** Create the location panel */
	protected JPanel createLocationPanel() {
		location = new LocationPanel(admin, proxy.getGeoLoc(), state);
		location.initialize();
		location.addRow("Notes", notes);
		new FocusJob(notes) {
			public void perform() {
				proxy.setNotes(notes.getText());
			}
		};
		location.setCenter();
		location.addRow(controller);
		new ActionJob(this, controller) {
			public void perform() throws Exception {
				controllerPressed();
			}
		};
		return location;
	}

	/** Controller lookup button pressed */
	protected void controllerPressed() throws RemoteException {
		Controller c = proxy.getController();
		if(c == null)
			controller.setEnabled(false);
		else {
			connection.getDesktop().show(
				new ControllerForm(connection, c));
		}
	}

	/** Create the setup panel */
	protected JPanel createSetupPanel() {
		FormPanel panel = new FormPanel(admin);
		ListModel m = state.getCameraModel();
		camera.setModel(new WrapperComboBoxModel(m));
		panel.addRow("Camera", camera);
		new ActionJob(this, camera) {
			public void perform() {
				proxy.setCamera(
					(Camera)camera.getSelectedItem());
			}
		};
		panel.addRow("Sign Text", message);
		new FocusJob(message) {
			public void perform() {
				proxy.setMessage(message.getText());
			}
		};
		return panel;
	}

	/** Update one attribute on the form */
	protected void updateAttribute(String a) {
		if(a == null || a.equals("notes"))
			notes.setText(proxy.getNotes());
		if(a == null || a.equals("camera"))
			camera.setSelectedItem(proxy.getCamera());
		if(a == null || a.equals("text"))
			message.setText(proxy.getMessage());
	}
}
