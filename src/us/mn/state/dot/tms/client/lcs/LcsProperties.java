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
package us.mn.state.dot.tms.client.lcs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.rmi.RemoteException;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListModel;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.LaneControlSignal;
import us.mn.state.dot.tms.SortedList;
import us.mn.state.dot.tms.TrafficDevice;
import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.toast.TrafficDeviceForm;
import us.mn.state.dot.tms.client.toast.WrapperComboBoxModel;
import us.mn.state.dot.tms.utils.TMSProxy;

/**
 * LcsForm is a dialog for entering and editing lane control signal records
 * This is a form for viewing and editing the properties of a lane control
 * signal (LCS).
 *
 * @author <a href="mailto:timothy.a.johnson@dot.state.mn.us">Tim Johnson</a>
 * @author Douglas Lau
 */
public class LcsProperties extends TrafficDeviceForm {

	/** Frame title */
	static private final String TITLE = "Lane Control Signal: ";

	/** Camera combo box */
	protected final JComboBox camera = new JComboBox();

	/** Number of Lanes field */
	protected final JLabel lanes = new JLabel(UNKNOWN);

	/** Number of lanes controlled by this LCS */
	protected int numberOfLanes;

	/** Special Function Output setup table */
	protected final JTable outputTable = new JTable();

	/** Special Function Input setup table */
	protected final JTable inputTable = new JTable();

	/** Operation description label */
	protected final JLabel operation = new JLabel();

	/** Remote lane control signal object */
	protected LaneControlSignal lcs;

	/** SONAR state */
	protected final SonarState state;

	/** Create a new lane control signal properties form */
	public LcsProperties(TmsConnection tc, String id) {
		super(TITLE + id, tc, id);
		state = tc.getSonarState();
	}

	/** Initialize the widgets on the form */
	protected void initialize() throws RemoteException {
		TMSProxy tms = connection.getProxy();
		SortedList s = (SortedList)tms.getLCSList().getList();
		lcs = (LaneControlSignal)s.getElement(id);
		ListModel cameraModel = state.getCameraModel();
		camera.setModel(new WrapperComboBoxModel(cameraModel));
		numberOfLanes = lcs.getLanes();
		setDevice(lcs);
		super.initialize();
		location.addRow("Camera", camera);
		tab.add("Setup", createSetupPanel());
		tab.add("Status", createStatusPanel());
	}

	/** Apply button is pressed */
	protected void applyPressed() throws Exception {
		super.applyPressed();
		lcs.setCamera(getCameraName((Camera)camera.getSelectedItem()));
		lcs.notifyUpdate();
	}

	/** Create status panel */
	protected JPanel createStatusPanel() {
		GridBagLayout lay = new GridBagLayout();
		JPanel panel = new JPanel(lay);
		panel.setBorder(BORDER);
		GridBagConstraints bag = new GridBagConstraints();
		bag.gridx = 0;
		bag.gridy = 0;
		bag.gridwidth = 2;
		bag.insets.top = 10;
		JLabel label = new JLabel("Operation:");
		lay.setConstraints(label, bag);
		panel.add(label);
		bag.gridx = GridBagConstraints.RELATIVE;
		bag.anchor = GridBagConstraints.WEST;
		bag.insets.left = 2;
		lay.setConstraints(operation, bag);
		panel.add(operation);
		operation.setForeground(Color.BLACK);
		return panel;
	}

	/** Create setup panel */
	protected JPanel createSetupPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BORDER);
		Box box = Box.createHorizontalBox();
		box.add(Box.createHorizontalGlue());
		JLabel label1 = new JLabel("Special Function Output Settings");
		panel.add(label1);
		panel.add(box);
		outputTable.setPreferredScrollableViewportSize(
			new Dimension(200, 150));
		outputTable.setAutoCreateColumnsFromModel(false);
		outputTable.setColumnModel(OutputModel.createColumnModel());
		outputTable.getTableHeader().setReorderingAllowed(false);
		outputTable.getTableHeader().setResizingAllowed(false);
		outputTable.setShowHorizontalLines(false);
		JScrollPane pane1 = new JScrollPane(outputTable);
		panel.add(pane1);
		JLabel label2 = new JLabel("Special Function Input Settings");
		panel.add(label2);
		inputTable.setPreferredScrollableViewportSize(
			new Dimension(200, 150));
		inputTable.setAutoCreateColumnsFromModel(false);
		inputTable.setColumnModel(InputModel.createColumnModel());
		inputTable.getTableHeader().setReorderingAllowed(false);
		inputTable.getTableHeader().setResizingAllowed(false);
		inputTable.setShowHorizontalLines(false);
		JScrollPane pane2 = new JScrollPane(inputTable);
		panel.add(pane2);
		return panel;
	}

	/** Update the form with the current state of the signal */
	protected void doUpdate() throws RemoteException {
		super.doUpdate();
		camera.setSelectedItem(state.lookupCamera(lcs.getCamera()));
		Color color = Color.GRAY;
		if(lcs.isActive())
			color = OK;
		lanes.setForeground(color);
		lanes.setText("" + lcs.getLanes());
		OutputModel outputModel = new OutputModel(lcs.getModules());
		outputTable.setModel(outputModel);
		outputTable.setDefaultRenderer(Object.class,
			outputModel.getRenderer());
		InputModel inputModel = new InputModel(lcs.getModules());
		inputTable.setModel(inputModel);
		inputTable.setDefaultRenderer(Object.class,
			inputModel.getRenderer());
	}

	/** Refresh the status of the object */
	protected void doStatus() throws RemoteException {
		super.doStatus();
		lanes.setText(String.valueOf(lcs.getLanes()));
		operation.setText(lcs.getOperation());
	}
}
