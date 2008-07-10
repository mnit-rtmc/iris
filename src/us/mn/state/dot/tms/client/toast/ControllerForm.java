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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.rmi.RemoteException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sched.ItemJob;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.Circuit;
import us.mn.state.dot.tms.CommunicationLine;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.Controller170;
import us.mn.state.dot.tms.ControllerIO;
import us.mn.state.dot.tms.Detector;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.ErrorCounter;
import us.mn.state.dot.tms.LaneControlSignal;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.SortedList;
import us.mn.state.dot.tms.TMSObject;
import us.mn.state.dot.tms.TrafficDevice;
import us.mn.state.dot.tms.WarningSign;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.camera.CameraProperties;
import us.mn.state.dot.tms.client.dms.DMSProperties;
import us.mn.state.dot.tms.client.lcs.LcsProperties;
import us.mn.state.dot.tms.client.meter.RampMeterProperties;
import us.mn.state.dot.tms.client.warning.WarningSignProperties;

/**
 * ControllerForm is a Swing dialog for editing Controller records
 *
 * @author Douglas Lau
 */
public class ControllerForm extends TMSObjectForm {

	/** Frame title */
	static protected final String TITLE = "Controller @ ";

	/** Create a controller form */
	static ControllerForm create(TmsConnection tc, Controller c,
		String label)
	{
		if(c instanceof Controller170)
			return new Controller170Form(tc, (Controller170)c,
				label);
		else
			return new ControllerForm(tc, c, label);
	}

	/** Remote controller object */
	protected final Controller contr;

	/** Remote location object */
	protected LocationPanel location;

	/** Controller notes text */
	protected final JTextArea notes = new JTextArea();

	/** Mile point text field */
	protected final JTextField mile = new JTextField(10);

	/** Active checkbox */
	protected final JCheckBox active = new JCheckBox();

	/** Communication line label */
	protected final JLabel line_label = new JLabel();

	/** Model for drop address spinner */
	protected final SpinnerNumberModel drop_model =
		new SpinnerNumberModel(1, 1, 1024, 1);

	/** Drop address spinner */
	protected final JSpinner drop_address = new JSpinner(drop_model);

	/** Device lookup button */
	protected final JButton deviceButton = new JButton( "Device" );

	/** Device combo box */
	protected final JComboBox deviceBox = new JComboBox();

	/** Circuit lookup button */
	protected final JButton circuitButton = new JButton( "Circuit" );

	/** Circuit combo box */
	protected final JComboBox circuitBox = new JComboBox();
	protected final JLabel firmware = new JLabel();

	/** Controller communication status label */
	protected final JLabel c_status = new JLabel();

	/** Controller setup label */
	protected final JLabel c_setup = new JLabel();

	/** Error counter table (on status tab) */
	protected final JTable error_table = new JTable();

	/** Test communication checkbox */
	protected final JCheckBox test = new JCheckBox("Test Communication");

	/** Download button */
	protected final JButton download = new JButton("Download");

	/** Reset button */
	protected final JButton reset = new JButton("Reset");

	/** Apply button */
	protected final JButton apply = new JButton( "Apply Changes" );

	/** Circuit box circuitModel */
	protected DefaultComboBoxModel circuitModel =
		new DefaultComboBoxModel();

	/** Detector input list model */
	protected final DefaultListModel model = new DefaultListModel();

	/** Detector input list */
	protected final JList inputs = new JList(model);

	/** Assigned input flag array */
	protected final boolean assigned[] = new boolean[25];

	/** Detector assign button */
	protected final JButton assign = new JButton("Assign");

	/** Detector edit button */
	protected final JButton edit = new JButton("Edit");

	/** Detector remove button */
	protected final JButton remove = new JButton("Remove");

	/** Available device list */
	protected SortedList devices;

	/** Available detector list */
	protected SortedList avail;

	/** Available detector combo box */
	protected final JComboBox availBox = new JComboBox();

	/** Alarm model */
	protected AlarmModel alarm_model;

	/** Alarm table */
	protected final JTable alarm_table = new JTable();

	/** Create a new controller form */
	protected ControllerForm(TmsConnection tc, Controller c, String label) {
		super(TITLE + label, tc);
		contr = c;
		obj = contr;
	}

	/** Initialize the widgets on the form */
	protected void initialize() throws RemoteException {
		location = new LocationPanel(admin, contr.getLocation(),
			connection.getSonarState());
		devices = (SortedList)tms.getDevices().getList();
		deviceBox.setModel(new WrapperComboBoxModel(
			tms.getDevices().getModel()));
		circuitBox.setModel(circuitModel);
		Circuit circuit = contr.getCircuit();
		circuitBox.setSelectedItem( circuit.getId() );
		avail = (SortedList)tms.getAvailable().getList();
		availBox.setModel(new WrapperComboBoxModel(
			tms.getAvailable().getModel()));
		for(int inp = 0; inp < Controller170.DETECTOR_INPUTS; inp++)
			model.addElement("");
		alarm_table.setAutoCreateColumnsFromModel(false);
		alarm_table.setColumnModel(AlarmModel.createColumnModel());
		alarm_table.setPreferredScrollableViewportSize(
			new Dimension(280, 10 * alarm_table.getRowHeight()));
		super.initialize();
		location.initialize();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		JTabbedPane tab = new JTabbedPane();
		tab.add("Location", createLocationPanel());
		tab.add("Devices", createDevicePanel());
		tab.add("Detectors", createDetectorPanel());
		tab.add("Alarms", createAlarmPanel());
		tab.add("Status", createStatusPanel());
		add(tab);
		if(connection.isAdmin() || connection.isActivate()) {
			add(Box.createVerticalStrut(VGAP));
			add(apply);
			new ActionJob(this, apply) {
				public void perform() throws Exception {
					applyPressed();
				}
			};
		}
		setBackground(Color.LIGHT_GRAY);
	}

	/** Create the controller location panel */
	protected JPanel createLocationPanel() {
		location.addRow("Milepoint", mile);
		location.addNote(notes);
		location.addRow("Active", active);
		active.setEnabled(connection.isAdmin() ||
			connection.isActivate());
		return location;
	}

	/** Create the device panel */
	protected JPanel createDevicePanel() {
		FormPanel panel = new FormPanel(admin);
		new ActionJob(this, circuitButton) {
			public void perform() throws Exception {
				circuitPressed();
			}
		};
		panel.addRow(circuitButton, circuitBox);
		circuitBox.setEditable(true);
		panel.add(new JLabel("Line"));
		panel.setWidth(2);
		panel.addRow(line_label);
		panel.addRow("Drop", drop_address);
		new ActionJob(this, deviceButton) {
			public void perform() throws Exception {
				devicePressed();
			}
		};
		panel.addRow(deviceButton, deviceBox);
		return panel;
	}

	/** Create the Detector panel */
	protected JPanel createDetectorPanel() {
		JPanel detectors = new JPanel();
		detectors.setBorder(BORDER);
		detectors.setLayout(new BoxLayout(detectors, BoxLayout.Y_AXIS));
		detectors.add(new JScrollPane(inputs));
		inputs.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if(e.getValueIsAdjusting()) return;
				updateButtons();
			}
		} );
		detectors.add(Box.createVerticalStrut(VGAP));
		JButton dummy = new JButton();
		Box box = new Box(BoxLayout.X_AXIS);
		if(admin) {
			box.add(assign);
			assign.setEnabled(false);
			assign.addActionListener(new ActionJob(this, dummy) {
				public void perform() throws Exception {
					assignPressed();
				}
			});
			box.add(Box.createHorizontalStrut(HGAP));
		}
		if(!admin)
			edit.setText("View");
		box.add(edit);
		edit.setEnabled(false);
		edit.addActionListener(new ActionJob(this, dummy) {
			public void perform() throws Exception {
				editPressed();
			}
		} );
		if(admin) {
			box.add(Box.createHorizontalStrut(HGAP));
			box.add(remove);
			remove.setEnabled(false);
			remove.addActionListener(new ActionJob(this, dummy) {
				public void perform() throws Exception {
					removePressed();
				}
			});
		}
		detectors.add(box);
		if(admin) {
			detectors.add(Box.createVerticalStrut(VGAP));
			box = new Box(BoxLayout.X_AXIS);
			JLabel label = new JLabel("Available");
			label.setAlignmentY(0.5f);
			box.add(label);
			box.add(Box.createHorizontalStrut(2));
			availBox.setAlignmentY(0.5f);
			box.add(availBox);
			availBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					updateButtons();
				}
			});
			detectors.add(box);
		}
		return detectors;
	}

	/** Create the alarm panel */
	protected JPanel createAlarmPanel() {
		JPanel apanel = new JPanel();
		apanel.setBorder(BORDER);
		apanel.add(new JScrollPane(alarm_table));
		return apanel;
	}

	/** Create the controller status panel */
	protected JPanel createStatusPanel() {
		JPanel panel = new JPanel();
		panel.setLayout( new BoxLayout( panel, BoxLayout.Y_AXIS ) );
		panel.setBorder( BORDER );
		Box box = new Box(BoxLayout.X_AXIS);
		box.add(Box.createHorizontalGlue());
		box.add(new JLabel("Firmware:"));
		box.add(Box.createHorizontalStrut(HGAP));
		box.add(firmware);
		box.add(Box.createHorizontalGlue());
		panel.add(box);
		box = new Box(BoxLayout.X_AXIS);
		box.add(Box.createHorizontalGlue());
		box.add(new JLabel("Status:"));
		box.add(Box.createHorizontalStrut(HGAP));
		box.add(c_status);
		box.add(Box.createHorizontalGlue());
		panel.add(box);
		box = new Box(BoxLayout.X_AXIS);
		box.add(Box.createHorizontalGlue());
		box.add(new JLabel("Setup:"));
		box.add(Box.createHorizontalStrut(HGAP));
		box.add(c_setup);
		box.add(Box.createHorizontalGlue());
		panel.add(box);
		error_table.setPreferredScrollableViewportSize(
			new Dimension( 200, 150 ) );
		error_table.setAutoCreateColumnsFromModel( false );
		error_table.setColumnModel( CounterModel.createColumnModel() );
		JScrollPane pane = new JScrollPane( error_table );
		panel.add( pane );
		panel.add(Box.createVerticalStrut(VGAP));
		box = new Box(BoxLayout.X_AXIS);
		box.add(Box.createHorizontalGlue());
		box.add(test);
		box.add(Box.createHorizontalGlue());
		panel.add(box);
		panel.add(Box.createVerticalStrut(VGAP));
		box = new Box(BoxLayout.X_AXIS);
		box.add(Box.createHorizontalGlue());
		box.add(download);
		box.add(Box.createHorizontalStrut(HGAP));
		box.add(reset);
		box.add(Box.createHorizontalGlue());
		panel.add(box);
		new ItemJob(this, test) {
			public void perform() throws Exception {
				contr.testCommunications(ItemEvent.SELECTED ==
					event.getStateChange());
			}
		};
		new ActionJob(this, download) {
			public void perform() throws Exception {
				contr.download(false);
			}
		};
		new ActionJob(this, reset) {
			public void perform() throws Exception {
				contr.download(true);
			}
		};
		return panel;
	}

	/** Update the form with the current state of the controller */
	protected void doUpdate() throws RemoteException {
		location.doUpdate();
		CommunicationLine line = contr.getLine();
		line_label.setText("" + line.getIndex() + " (" +
			line.getDescription() + ")");
		Circuit circuit = contr.getCircuit();
		circuitModel.removeAllElements();
		for(Circuit c: line.getCircuits())
			circuitModel.addElement(c.getId());
		drop_model.setValue(contr.getDrop());
		notes.setText(contr.getNotes());
		mile.setText(String.valueOf(contr.getMile()));
		active.setSelected(contr.isActive());
		TrafficDevice device = getDevice();
		if( device != null ) {
			deviceButton.setEnabled( true );
			deviceBox.getModel().setSelectedItem( device.getId() );
		} else {
			deviceButton.setEnabled( false );
			deviceBox.getModel().setSelectedItem( null );
		}
		if( circuit != null ) {
			circuitButton.setEnabled( true );
			circuitBox.setSelectedItem( circuit.getId() );
		} else {
			circuitButton.setEnabled( false );
			circuitBox.setSelectedItem( null );
		}
		for(int inp = 0; inp < Controller170.DETECTOR_INPUTS; inp++) {
			int pin = getDetectorPin(inp);
			Detector det = lookupDetector(pin);
			model.set(inp, getInputLabel(inp, det));
			assigned[inp] = det != null;
		}
		alarm_model = new AlarmModel(contr, admin);
		alarm_table.setModel(alarm_model);
		updateButtons();
	}

	/** Get the label for a particular detector input */
	protected String getInputLabel(int inp, Detector det)
		throws RemoteException
	{
		StringBuilder buf = new StringBuilder(20);
		buf.append(inp + 1);
		buf.append("> ");
		while(buf.length() < 4)
			buf.insert(0, " ");
		if(det != null) {
			int index = det.getIndex();
			buf.append(index);
			buf.append(" - ");
			buf.append(det.getLabel(false));
		}
		return buf.toString();
	}

	/** Refresh the status of the controller */
	protected void doStatus() throws RemoteException {
		Color color = OK;
		String version = contr.getVersion();
		if(version.equals(UNKNOWN))
			color = TmsForm.ERROR;
		firmware.setForeground(color);
		firmware.setText(version);
		String stat = contr.getStatus();
		if(stat.equals("OK"))
			color = OK;
		else
			color = TmsForm.ERROR;
		c_status.setForeground(color);
		c_status.setText(stat);
		String setup = contr.getSetup();
		if(setup.equals("OK"))
			color = OK;
		else
			color = TmsForm.ERROR;
		c_setup.setForeground(color);
		c_setup.setText(setup);
		CounterModel count_model = new CounterModel(
			((ErrorCounter)contr).getCounters());
		error_table.setModel(count_model);
		error_table.setDefaultRenderer(Object.class,
			count_model.getRenderer());
		if(test.isSelected())
			contr.testCommunications(true);
	}

	/** Get the selected controller IO device */
	protected ControllerIO getSelectedDevice() throws Exception {
		String d = (String)deviceBox.getSelectedItem();
		if(d == null)
			return null;
		TMSObject o = devices.getElement(d);
		if(o instanceof ControllerIO)
			return (ControllerIO)o;
		else
			return null;
	}

	/** Set the selected device */
	protected void setSelectedDevice() throws Exception {
		ControllerIO cio = getSelectedDevice();
		if(cio != null) {
			cio.setController(null);
			cio.setPin(Controller.DEVICE_PIN);
			cio.setController(contr);
		}
	}

	/** Called when the 'apply' button is pressed */
	protected void applyPressed() throws Exception {
		if(admin) {
			float mp = Float.parseFloat(mile.getText());
			location.applyPressed();
			contr.setNotes(notes.getText());
			contr.setMile(mp);
			contr.setCircuit((String)circuitBox.getSelectedItem());
			contr.setDrop(
				((Number)drop_address.getValue()).shortValue());
			setSelectedDevice();
		}
		contr.setActive(active.isSelected());
		contr.notifyUpdate();
		contr.getLine().notifyUpdate();
	}

	/** Get the first device */
	protected TrafficDevice getDevice() throws RemoteException {
		ControllerIO[] io_pins = contr.getIO();
		ControllerIO io = io_pins[Controller.DEVICE_PIN];
		if(io instanceof TrafficDevice)
			return (TrafficDevice)io;
		else
			return null;
	}

	/** Called when device lookup button is pressed */
	protected void devicePressed() throws Exception {
		TrafficDevice device = getDevice();
		if(device == null) {
			deviceButton.setEnabled(false);
			return;
		}
		SmartDesktop desktop = connection.getDesktop();
		String id = device.getId();
		if(device instanceof RampMeter)
			desktop.show(new RampMeterProperties(connection, id));
		if(device instanceof DMS)
			desktop.show(new DMSProperties(connection, id));
		if(device instanceof WarningSign)
			desktop.show(new WarningSignProperties(connection, id));
		if(device instanceof LaneControlSignal)
			desktop.show(new LcsProperties(connection, id));
		if(device instanceof Camera)
			desktop.show(new CameraProperties(connection, id));
	}

	/** Called when circuit lookup button is pressed */
	protected void circuitPressed() throws Exception {
		Circuit circuit = contr.getCircuit();
		if(circuit == null) {
			circuitButton.setEnabled(false);
			return;
		}
		connection.getDesktop().show(new SonetRingForm(connection));
	}

	/** Get the selected detector */
	protected Detector getSelectedDetector() throws RemoteException {
		String det = (String)availBox.getSelectedItem();
		if(det == null)
			return null;
		try {
			int index = Integer.parseInt(det.trim());
			TMSObject o = avail.getElement(String.valueOf(index));
			if(o instanceof Detector)
				return (Detector)o;
			else
				return null;
		}
		catch(NumberFormatException e) {
			return null;
		}
	}

	/** Called when the 'assign' button is pressed */
	protected void assignPressed() throws Exception {
		disableButtons();
		try {
			int pin = getSelectedPin();
			if(pin < 0)
				return;
			Detector det = getSelectedDetector();
			if(det == null)
				return;
			det.setPin(pin);
			det.setController(contr);
			contr.notifyUpdate();
		}
		finally {
			updateButtons();
		}
	}

	/** Get the selected pin for a detector input */
	protected int getSelectedPin() {
		int inp = inputs.getSelectedIndex();
		if(inp >= 0)
			return getDetectorPin(inp);
		else
			return inp;
	}

	/** Get the pin for the given detector input */
	protected int getDetectorPin(int inp) {
		return inp + 1;
	}

	/** Lookup a detector on a controller */
	protected Detector lookupDetector(int pin) throws RemoteException {
		ControllerIO[] io_pins = contr.getIO();
		ControllerIO io = io_pins[pin];
		if(io instanceof Detector)
			return (Detector)io;
		else
			return null;
	}

	/** Called when the 'edit' button is pressed */
	protected void editPressed() throws Exception {
		disableButtons();
		try {
			int pin = getSelectedPin();
			if(pin < 0)
				return;
			Detector det = lookupDetector(pin);
			if(det != null) {
				int index = det.getIndex();
				connection.getDesktop().show(
					new DetectorForm(connection, index));
			}
		}
		finally {
			updateButtons();
		}
	}

	/** Called when the 'remove' button is pressed */
	protected void removePressed() throws Exception {
		disableButtons();
		try {
			int pin = getSelectedPin();
			if(pin < 0)
				return;
			Detector det = lookupDetector(pin);
			if(det != null) {
				det.setController(null);
				contr.notifyUpdate();
			}
		}
		finally {
			updateButtons();
		}
	}

	/** Disable the detector buttons */
	protected final void disableButtons() {
		assign.setEnabled(false);
		edit.setEnabled(false);
		remove.setEnabled(false);
	}

	/** Update the buttons' enabled state */
	protected final void updateButtons() {
		disableButtons();
		if(inputs.isSelectionEmpty())
			return;
		int inp = inputs.getSelectedIndex();
		if(assigned[inp]) {
			edit.setEnabled(true);
			remove.setEnabled(true);
		} else {
			Object item = availBox.getModel().getSelectedItem();
			if(item != null)
				if(!item.toString().trim().equals(""))
					assign.setEnabled(true);
		}
	}
}
