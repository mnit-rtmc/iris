/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.roads;

import java.awt.Color;
import java.awt.Dimension;
import java.rmi.RemoteException;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.map.MapBean;
import us.mn.state.dot.tms.IndexedList;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.R_NodeMap;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.toast.DetectorForm;
import us.mn.state.dot.tms.client.toast.FormPanel;
import us.mn.state.dot.tms.client.toast.LocationPanel;
import us.mn.state.dot.tms.client.toast.TMSObjectForm;

/**
 * R_NodeProperties is a form for viewing and editing roadway node parameters.
 *
 * @author Douglas Lau
 */
public class R_NodeProperties extends TMSObjectForm {

	/** Map for selecting points (FIXME) */
	static public MapBean map;

	/** Frame title */
	static protected final String TITLE = "R_Node: ";

	/** Roadway node being displayed */
	protected final R_Node r_node;

	/** Location panel */
	protected LocationPanel location;

	/** Component for editing notes */
	protected final JTextArea notes = new JTextArea(3, 20);

	/** Node type combobox */
	protected final JComboBox node_type = new JComboBox(R_Node.TYPES);

	/** Pickable check box */
	protected final JCheckBox pickable = new JCheckBox();

	/** Transition type combobox */
	protected final JComboBox transition =
		new JComboBox(R_Node.TRANSITIONS);

	/** Component for number of lanes */
	protected final JSpinner lanes = new JSpinner(
		new SpinnerNumberModel(2, 0, 6, 1));

	/** Attach side check box */
	protected final JCheckBox attach_side = new JCheckBox();

	/** Component for lane shift */
	protected final JSpinner shift = new JSpinner(
		new SpinnerNumberModel(0, 0, 12, 1));

	/** Station ID text field */
	protected final JTextField station_id = new JTextField(8);

	/** Component for speed limit */
	protected final JSpinner slimit = new JSpinner(
		new SpinnerNumberModel(55, 25, 120, 5));

	/** Remote detector list */
	protected IndexedList det_list;

	/** Detector model */
	protected R_NodeDetectorModel det_model;

	/** Detector table */
	protected final JTable det_table = new JTable();

	/** Button to find all matching detectors */
	protected final JButton match = new JButton("Match");

	/** Button to edit the selected detector */
	protected final JButton edit = new JButton("Edit");

	/** Button to remove the selected detector */
	protected final JButton remove = new JButton("Remove");

	/** Apply button */
	protected final JButton apply = new JButton("Apply Changes");

	/** Create a new roadway node properties form */
	public R_NodeProperties(TmsConnection tc, R_Node n, Integer oid) {
		super(TITLE + oid, tc);
		r_node = n;
	}

	/** Initialize the widgets on the form */
	protected void initialize() throws RemoteException {
		det_list = (IndexedList)tms.getDetectors().getList();
		location = new LocationPanel(admin, r_node.getGeoLoc(),
			connection.getSonarState());
		det_table.setAutoCreateColumnsFromModel(false);
		det_table.setColumnModel(
			R_NodeDetectorModel.createColumnModel());
		det_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		det_table.setPreferredScrollableViewportSize(
			new Dimension(280, 8 * det_table.getRowHeight()));
		super.initialize();
		location.initialize();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		JTabbedPane tab = new JTabbedPane();
		tab.add("Location", createLocationPanel());
		tab.add("Setup", createSetupPanel());
		tab.add("Detectors", createDetectorPanel());
		add(tab);
		if(admin) {
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

	/** Dispose of the form */
	protected void dispose() {
		location.dispose();
		super.dispose();
	}

	/** Create the location panel */
	protected JPanel createLocationPanel() {
		location.addSelectPointButton(map);
		location.addRow("Notes", notes);
		return location;
	}

	/** Create the roadway node setup panel */
	protected FormPanel createSetupPanel() {
		FormPanel panel = new FormPanel(admin);
		panel.addRow("Node type", node_type);
		panel.addRow("Pickable", pickable);
		panel.addRow("Transition", transition);
		panel.addRow("Lanes", lanes);
		panel.addRow("Attach side", attach_side);
		panel.addRow("Shift", shift);
		panel.addRow("Station ID", station_id);
		panel.addRow("Speed Limit", slimit);
		return panel;
	}

	/** Create the detector panel */
	protected JPanel createDetectorPanel() {
		JPanel dpanel = new JPanel();
		dpanel.setBorder(BORDER);
		dpanel.add(new JScrollPane(det_table));
		if(admin) {
			Box box = Box.createVerticalBox();
			box.add(Box.createVerticalGlue());
			box.add(match);
			new ActionJob(this, match) {
				public void perform() throws Exception {
					doMatch();
				}
			};
			box.add(Box.createVerticalStrut(VGAP));
			box.add(edit);
			new ActionJob(this, edit) {
				public void perform() throws RemoteException {
					doEdit();
				}
			};
			box.add(Box.createVerticalStrut(VGAP));
			box.add(remove);
			new ActionJob(this, remove) {
				public void perform() throws RemoteException {
					doRemove();
				}
			};
			box.add(Box.createVerticalGlue());
			dpanel.add(box);
			det_table.getSelectionModel().addListSelectionListener(
				new ListSelectionListener()
			{
				public void valueChanged(ListSelectionEvent e) {
					if(!e.getValueIsAdjusting())
						doButtonEnable();
				}
			});
		}
		return dpanel;
	}

	/** Lookup detectors with matching location */
	protected void doMatch() throws RemoteException {
		det_model = new R_NodeDetectorModel(det_list,
			r_node.getMatchingDetectors(), admin);
		det_table.setModel(det_model);
	}

	/** Enable/disable buttons depending on selected row */
	protected void doButtonEnable() {
		int s = det_table.getSelectedRow() + 1;
		boolean e = (s > 0) && (s < det_table.getRowCount());
		edit.setEnabled(e);
		remove.setEnabled(e);
	}

	/** Edit the currently selected detector */
	protected void doEdit() throws RemoteException {
		int s = det_table.getSelectedRow();
		if(s >= 0) {
			Integer did = det_model.getDetectorID(s);
			if(did != null) {
				connection.getDesktop().show(
					new DetectorForm(connection, did));
			}
		}
	}

	/** Remove the currently selected detector */
	protected void doRemove() throws RemoteException {
		int r = det_table.getSelectedRow();
		if(r >= 0)
			det_model.removeRow(r);
	}

	/** Update the form with the current state of the roadway node */
	protected void doUpdate() throws RemoteException {
		location.doUpdate();
		notes.setText(r_node.getNotes());
		node_type.setSelectedIndex(r_node.getNodeType());
		pickable.setSelected(r_node.isPickable());
		transition.setSelectedIndex(r_node.getTransition());
		lanes.setValue(r_node.getLanes());
		attach_side.setSelected(r_node.getAttachSide());
		shift.setValue(r_node.getShift());
		station_id.setText(r_node.getStationID());
		slimit.setValue(r_node.getSpeedLimit());
		det_model = new R_NodeDetectorModel(det_list,
			r_node.getDetectors(), admin);
		det_table.setModel(det_model);
	}

	/** Apply button pressed */
	protected void applyPressed() throws Exception {
		location.applyPressed();
		r_node.setNotes(notes.getText());
		r_node.setNodeType(node_type.getSelectedIndex());
		r_node.setPickable(pickable.isSelected());
		r_node.setTransition(transition.getSelectedIndex());
		r_node.setLanes(((Number)lanes.getValue()).intValue());
		r_node.setAttachSide(attach_side.isSelected());
		r_node.setShift(((Number)shift.getValue()).intValue());
		r_node.setStationID(station_id.getText());
		r_node.setSpeedLimit(((Number)slimit.getValue()).intValue());
		r_node.setDetectors(det_model.getDetectors());
		r_node.notifyUpdate();
	}
}
