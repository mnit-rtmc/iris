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
package us.mn.state.dot.tms.client.toast;

import java.awt.Color;
import java.rmi.RemoteException;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.Detector;
import us.mn.state.dot.tms.IndexedList;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.utils.ActionJob;
import us.mn.state.dot.tms.utils.TMSProxy;

/**
 * DetectorForm is a Swing dialog for entering and editing Detector records
 *
 * @author Douglas Lau
 */
public class DetectorForm extends TMSObjectForm {

	/** Frame title */
	static protected final String TITLE = "Detector ";

	/** Detector index */
	protected final int index;

	/** Remote detector list object */
	protected IndexedList dList;

	/** Remote detector object */
	protected Detector detector;

	/** Detector name label component */
	protected final JLabel name = new JLabel("<Undefined>");

	/** Location panel */
	protected LocationPanel location;

	/** Lane type combo box */
	protected final JComboBox lane = new JComboBox(Detector.LANE_TYPE);

	/** Model for lane number spinner */
	protected final SpinnerNumberModel num_model =
		new SpinnerNumberModel(0, 0, 8, 1);

	/** Lane number spinner */
	protected final JSpinner number = new JSpinner(num_model);

	/** Notes text area */
	protected final JTextArea notes = new JTextArea(3, 24);

	/** Controller button */
	protected final JButton controller = new JButton( "Controller" );

	/** Station button */
	protected final JButton station = new JButton("Station");

	/** Abandoned status check box */
	protected final JCheckBox abandoned = new JCheckBox();

	/** Force fail status check box */
	protected final JCheckBox forceFail = new JCheckBox();

	/** Average field length text field */
	protected final JTextField field = new JTextField("", 8);

	/** Fake detector text field */
	protected final JTextField fake = new JTextField("", 24);

	/** Apply changes button */
	protected final JButton apply = new JButton( "Apply Changes" );

	/** Create a new DetectorForm */
	public DetectorForm(TmsConnection tc, int i) {
		super(TITLE + i, tc);
		index = i;
	}

	/** Initialize the widgets on the form */
	protected void initialize() throws RemoteException {
		TMSProxy tms = connection.getProxy();
		dList = (IndexedList)tms.getDetectors().getList();
		obj = dList.getElement(index);
		detector = (Detector)obj;
		super.initialize();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(name);
		add(Box.createVerticalStrut(VGAP));
		JTabbedPane tab = new JTabbedPane(JTabbedPane.TOP);
		add(tab);
		tab.add("Location", createLocationPanel());
		tab.add("Setup", createSetupPanel());
		if(admin) {
			add(Box.createVerticalStrut(VGAP));
			new ActionJob(this, apply) {
				public void perform() throws Exception {
					applyPressed();
				}
			};
			add(apply);
		}
		setBackground(Color.LIGHT_GRAY);
	}

	/** Create the Location panel */
	protected JPanel createLocationPanel() throws RemoteException {
		location = new LocationPanel(admin, detector.getLocation(),tms);
		location.initialize();
		location.addNote(notes);
		Box box = new Box(BoxLayout.X_AXIS);
		box.add(controller);
		new ActionJob(this, controller) {
			public void perform() throws Exception {
				controllerPressed();
			}
		};
		box.add(Box.createHorizontalStrut(HGAP));
		box.add(station);
		new ActionJob(this, station) {
			public void perform() throws Exception {
				stationPressed();
			}
		};
		location.setCenter();
		location.addRow(box);
		return location;
	}

	/** Create detector setup panel */
	protected JPanel createSetupPanel() {
		FormPanel panel = new FormPanel(admin);
		panel.add("Lane", lane);
		panel.addRow(number);
		panel.addRow("Avg field length", field);
		panel.add(new JLabel("Fake"));
		panel.setWidth(3);
		panel.setWest();
		panel.addRow(fake);
		panel.addRow("Abandoned", abandoned);
		panel.addRow("Force Fail", forceFail);
		return panel;
	}

	/** Update the form with the current state of the detector */
	protected void doUpdate() throws RemoteException {
		location.doUpdate();
		name.setText(detector.getLabel(false));
		notes.setText(detector.getNotes());
		controller.setEnabled(detector.getController() != null);
		station.setEnabled(detector.getStation() != null);
		lane.setSelectedIndex(detector.getLaneType());
		number.setValue(detector.getLaneNumber());
		field.setText(String.valueOf(detector.getFieldLength()));
		fake.setText(detector.getFakeDetector());
		abandoned.setSelected(detector.isAbandoned());
		forceFail.setSelected(detector.getForceFail());
	}

	/** Apply button pressed */
	protected void applyPressed() throws Exception {
		try {
			float afl = Float.parseFloat(field.getText());
			location.applyPressed();
			detector.setFakeDetector(fake.getText());
			detector.setLaneType(
				(short)lane.getSelectedIndex());
			detector.setLaneNumber(
				((Number)number.getValue()).shortValue());
			detector.setNotes(notes.getText());
			detector.setFieldLength(afl);
			detector.setAbandoned(abandoned.isSelected());
			detector.setForceFail(forceFail.isSelected());
		}
		finally {
			detector.notifyUpdate();
			dList.update(index);
		}
	}

	/** Controller lookup button pressed */
	protected void controllerPressed() throws Exception {
		Controller c = detector.getController();
		if(c == null)
			controller.setEnabled(false);
		else {
			connection.getDesktop().show(ControllerForm.create(
				connection, c, c.getOID().toString()));
		}
	}

	/** Station lookup button pressed */
	protected void stationPressed() throws Exception {
		// FIXME: bring up the r_node for the station
	}
}
