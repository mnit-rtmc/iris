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
import us.mn.state.dot.sched.ChangeJob;
import us.mn.state.dot.sched.FocusJob;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.map.MapBean;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.R_NodeTransition;
import us.mn.state.dot.tms.R_NodeType;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.toast.DetectorForm;
import us.mn.state.dot.tms.client.toast.FormPanel;
import us.mn.state.dot.tms.client.toast.LocationPanel;
import us.mn.state.dot.tms.client.toast.SonarObjectForm;

/**
 * R_NodeProperties is a form for viewing and editing roadway node parameters.
 *
 * @author Douglas Lau
 */
public class R_NodeProperties extends SonarObjectForm<R_Node> {

	/** Map for selecting points (FIXME) */
	static public MapBean map;

	/** Frame title */
	static protected final String TITLE = "R_Node: ";

	/** Location panel */
	protected LocationPanel location;

	/** Component for editing notes */
	protected final JTextArea notes = new JTextArea(3, 20);

	/** Node type combobox */
	protected final JComboBox node_type =
		new JComboBox(R_NodeType.getDescriptions());

	/** Pickable check box */
	protected final JCheckBox pickable = new JCheckBox();

	/** Transition type combobox */
	protected final JComboBox transition =
		new JComboBox(R_NodeTransition.getDescriptions());

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

	/** Detector table */
	protected final JTable det_table = new JTable();

	/** Button to edit the selected detector */
	protected final JButton edit = new JButton("Edit");

	/** Button to remove the selected detector */
	protected final JButton remove = new JButton("Remove");

	/** Create a new roadway node properties form */
	public R_NodeProperties(TmsConnection tc, R_Node n) {
		super(TITLE, tc, n);
	}

	/** Get the SONAR type cache */
	protected TypeCache<R_Node> getTypeCache(SonarState st) {
		return st.getR_Nodes();
	}

	/** Initialize the widgets on the form */
	protected void initialize() throws RemoteException {
		location = new LocationPanel(admin, proxy.getGeoLoc(),
			connection.getSonarState());
		det_table.setAutoCreateColumnsFromModel(false);
//		det_table.setColumnModel(
//			R_NodeDetectorModel.createColumnModel());
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
		location.addSelectPointButton(map);
		location.addRow("Notes", notes);
		new FocusJob(notes) {
			public void perform() {
				if(wasLost())
					proxy.setNotes(notes.getText());
			}
		};
		return location;
	}

	/** Create the roadway node setup panel */
	protected FormPanel createSetupPanel() {
		FormPanel panel = new FormPanel(admin);
		panel.addRow("Node type", node_type);
		new ActionJob(this, node_type) {
			public void perform() {
				proxy.setNodeType(node_type.getSelectedIndex());
			}
		};
		panel.addRow("Pickable", pickable);
		new ActionJob(this, pickable) {
			public void perform() {
				proxy.setPickable(pickable.isSelected());
			}
		};
		panel.addRow("Transition", transition);
		new ActionJob(this, transition) {
			public void perform() {
				proxy.setTransition(
					transition.getSelectedIndex());
			}
		};
		panel.addRow("Lanes", lanes);
		new ChangeJob(this, lanes) {
			public void perform() {
				Number n = (Number)lanes.getValue();
				proxy.setLanes(n.intValue());
			}
		};
		panel.addRow("Attach side", attach_side);
		new ActionJob(this, attach_side) {
			public void perform() {
				proxy.setAttachSide(attach_side.isSelected());
			}
		};
		panel.addRow("Shift", shift);
		new ChangeJob(this, shift) {
			public void perform() {
				Number n = (Number)shift.getValue();
				proxy.setShift(n.intValue());
			}
		};
		panel.addRow("Station ID", station_id);
		new FocusJob(station_id) {
			public void perform() {
				if(wasLost()) {
					String sid = station_id.getText();
					proxy.setStationID(sid);
				}
			}
		};
		panel.addRow("Speed Limit", slimit);
		new ChangeJob(this, slimit) {
			public void perform() {
				Number n = (Number)slimit.getValue();
				proxy.setSpeedLimit(n.intValue());
			}
		};
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
/*			Integer did = det_model.getDetectorID(s);
			if(did != null) {
				connection.getDesktop().show(
					new DetectorForm(connection, did));
			} */
		}
	}

	/** Remove the currently selected detector */
	protected void doRemove() throws RemoteException {
/*		int r = det_table.getSelectedRow();
		if(r >= 0)
			det_model.removeRow(r); */
	}

	/** Update one attribute on the form */
	protected void updateAttribute(String a) {
		if(a == null || a.equals("notes"))
			notes.setText(proxy.getNotes());
		if(a == null || a.equals("node_type"))
			node_type.setSelectedIndex(proxy.getNodeType());
		if(a == null || a.equals("pickable"))
			pickable.setSelected(proxy.getPickable());
		if(a == null || a.equals("transition"))
			transition.setSelectedIndex(proxy.getTransition());
		if(a == null || a.equals("lanes"))
			lanes.setValue(proxy.getLanes());
		if(a == null || a.equals("attach_side"))
			attach_side.setSelected(proxy.getAttachSide());
		if(a == null || a.equals("shift"))
			shift.setValue(proxy.getShift());
		if(a == null || a.equals("station_id"))
			station_id.setText(proxy.getStationID());
		if(a == null || a.equals("speed_limit"))
			slimit.setValue(proxy.getSpeedLimit());
	}
}
