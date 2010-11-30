/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2010  Minnesota Department of Transportation
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

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sched.ChangeJob;
import us.mn.state.dot.sched.FocusJob;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.R_NodeTransition;
import us.mn.state.dot.tms.R_NodeType;
import us.mn.state.dot.tms.client.toast.FormPanel;

/**
 * A panel for editing the setup properties of an r_node.
 *
 * @author Douglas Lau
 */
public class R_NodeSetupPanel extends FormPanel {

	/** Node type combobox */
	protected final JComboBox type_cmb =
		new JComboBox(R_NodeType.getDescriptions());

	/** Pickable check box */
	protected final JCheckBox pick_cbx = new JCheckBox();

	/** Above check box */
	protected final JCheckBox above_cbx = new JCheckBox();

	/** Transition type combobox */
	protected final JComboBox trans_cmb =
		new JComboBox(R_NodeTransition.getDescriptions());

	/** Component for number of lanes */
	protected final JSpinner lane_spn = new JSpinner(
		new SpinnerNumberModel(2, 0, 6, 1));

	/** Attach side check box */
	protected final JCheckBox attach_cbx = new JCheckBox();

	/** Component for lane shift */
	protected final JSpinner shift_spn = new JSpinner(
		new SpinnerNumberModel(0, 0, 12, 1));

	/** Station ID text field */
	protected final JTextField station_txt = new JTextField(8);

	/** Component for speed limit */
	protected final JSpinner speed_spn = new JSpinner(
		new SpinnerNumberModel(55, 25, 120, 5));

	/** Node being edited */
	protected final R_Node node;

	/** Create the roadway node setup panel */
	public R_NodeSetupPanel(R_Node n) {
		super(true);
		node = n;
	}

	/** Initialize the panel */
	public void initialize() {
		addRow("Node type", type_cmb);
		addRow("Pickable", pick_cbx);
		addRow("Above", above_cbx);
		addRow("Transition", trans_cmb);
		addRow("Lanes", lane_spn);
		addRow("Attach side", attach_cbx);
		addRow("Shift", shift_spn);
		addRow("Station ID", station_txt);
		addRow("Speed Limit", speed_spn);
		createJobs();
	}

	/** Create the jobs */
	protected void createJobs() {
		new ActionJob(this, type_cmb) {
			public void perform() {
				node.setNodeType(type_cmb.getSelectedIndex());
			}
		};
		new ActionJob(this, pick_cbx) {
			public void perform() {
				node.setPickable(pick_cbx.isSelected());
			}
		};
		new ActionJob(this, above_cbx) {
			public void perform() {
				node.setAbove(above_cbx.isSelected());
			}
		};
		new ActionJob(this, trans_cmb) {
			public void perform() {
				node.setTransition(
					trans_cmb.getSelectedIndex());
			}
		};
		new ChangeJob(this, lane_spn) {
			public void perform() {
				Number n = (Number)lane_spn.getValue();
				node.setLanes(n.intValue());
			}
		};
		new ActionJob(this, attach_cbx) {
			public void perform() {
				node.setAttachSide(attach_cbx.isSelected());
			}
		};
		new ChangeJob(this, shift_spn) {
			public void perform() {
				Number n = (Number)shift_spn.getValue();
				node.setShift(n.intValue());
			}
		};
		new FocusJob(station_txt) {
			public void perform() {
				if(wasLost()) {
					String s = station_txt.getText().trim();
					node.setStationID(s);
				}
			}
		};
		new ChangeJob(this, speed_spn) {
			public void perform() {
				Number n = (Number)speed_spn.getValue();
				node.setSpeedLimit(n.intValue());
			}
		};
	}

	/** Update one attribute on the form */
	public void doUpdateAttribute(String a) {
		if(a == null || a.equals("nodeType"))
			type_cmb.setSelectedIndex(node.getNodeType());
		if(a == null || a.equals("pickable"))
			pick_cbx.setSelected(node.getPickable());
		if(a == null || a.equals("above"))
			above_cbx.setSelected(node.getAbove());
		if(a == null || a.equals("transition"))
			trans_cmb.setSelectedIndex(node.getTransition());
		if(a == null || a.equals("lanes"))
			lane_spn.setValue(node.getLanes());
		if(a == null || a.equals("attachSide"))
			attach_cbx.setSelected(node.getAttachSide());
		if(a == null || a.equals("shift"))
			shift_spn.setValue(node.getShift());
		if(a == null || a.equals("stationID"))
			station_txt.setText(node.getStationID());
		if(a == null || a.equals("speedLimit"))
			speed_spn.setValue(node.getSpeedLimit());
	}
}
