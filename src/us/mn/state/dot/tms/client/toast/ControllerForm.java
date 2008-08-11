/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.toast;

import java.awt.Color;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.client.TmsConnection;

/**
 * ControllerForm is a Swing dialog for editing Controller records
 *
 * @author Douglas Lau
 */
public class ControllerForm extends SonarObjectForm<Controller> {

	/** Frame title */
	static protected final String TITLE = "Controller: ";

	/** Comm link combo box */
	protected final JComboBox comm_link = new JComboBox();

	/** Model for drop address spinner */
	protected final SpinnerNumberModel drop_model =
		new SpinnerNumberModel(1, 1, 1024, 1);

	/** Drop address spinner */
	protected final JSpinner drop_id = new JSpinner(drop_model);

	/** Controller notes text */
	protected final JTextArea notes = new JTextArea();

	/** Active checkbox */
	protected final JCheckBox active = new JCheckBox();

	/** Location panel */
	protected LocationPanel location;

	/** Mile point text field */
	protected final JTextField mile = new JTextField(10);

	/** Cabinet style combo box */
	protected final JComboBox cab_style = new JComboBox();

	/** Reset button */
	protected final JButton reset = new JButton("Reset");

	/** Create a new controller form */
	public ControllerForm(TmsConnection tc, Controller c) {
		super(TITLE, tc, c);
	}

	/** Get the SONAR type cache */
	protected TypeCache<Controller> getTypeCache(SonarState st) {
		return st.getControllers();
	}

	/** Initialize the widgets on the form */
	protected void initialize() {
		super.initialize();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		JTabbedPane tab = new JTabbedPane();
		tab.add("Setup", createSetupPanel());
		tab.add("Cabinet", createCabinetPanel());
		tab.add("I/O", createIOPanel());
		add(tab);
		setBackground(Color.LIGHT_GRAY);
		location.initialize();
	}

	/** Create the controller setup panel */
	protected JPanel createSetupPanel() {
		FormPanel panel = new FormPanel(admin);
		panel.addRow("Comm Link", comm_link);
		panel.addRow("Drop", drop_id);
		panel.addRow("Notes", notes);
		panel.addRow("Active", active);
		active.setEnabled(connection.isAdmin() ||
			connection.isActivate());
		return panel;
	}

	/** Create the cabinet panel */
	protected JPanel createCabinetPanel() {
		location = new LocationPanel(admin,
			proxy.getCabinet().getGeoLoc(),
			connection.getSonarState());
		location.addRow("Milepoint", mile);
		location.addRow("Style", cab_style);
//		return location;
		return new FormPanel(admin);
	}

	/** Create the I/O panel */
	protected JPanel createIOPanel() {
		FormPanel panel = new FormPanel(admin);
		return panel;
	}

	/** Update one attribute on the form */
	protected void updateAttribute(String a) {
		// FIXME: update the specified attribute
	}
}
