/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2011  Minnesota Department of Transportation
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
import us.mn.state.dot.sched.AbstractJob;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sched.ChangeJob;
import us.mn.state.dot.sched.FocusJob;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.R_NodeTransition;
import us.mn.state.dot.tms.R_NodeType;
import us.mn.state.dot.tms.client.Session;
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

	/** Active check box */
	protected final JCheckBox active_cbx = new JCheckBox();

	/** Station ID text field */
	protected final JTextField station_txt = new JTextField(8);

	/** Component for speed limit */
	protected final JSpinner speed_spn = new JSpinner(
		new SpinnerNumberModel(55, 25, 120, 5));

	/** User session */
	protected final Session session;

	/** Node being edited */
	protected R_Node node;

	/** Create the roadway node setup panel */
	public R_NodeSetupPanel(Session s) {
		super(false);
		session = s;
	}

	/** Initialize the panel */
	public void initialize() {
		add("Node type", type_cmb);
		addRow("Pickable", pick_cbx);
		add("Transition", trans_cmb);
		addRow("Above", above_cbx);
		add("Lanes", lane_spn);
		addRow("Attach side", attach_cbx);
		add("Shift", shift_spn);
		addRow("Active", active_cbx);
		add("Station ID", station_txt);
		finishRow();
		add("Speed Limit", speed_spn);
		createJobs();
		clear();
	}

	/** Create the jobs */
	protected void createJobs() {
		new ActionJob(this, type_cmb) {
			public void perform() {
				setNodeType(type_cmb.getSelectedIndex());
			}
		};
		new ActionJob(this, pick_cbx) {
			public void perform() {
				setPickable(pick_cbx.isSelected());
			}
		};
		new ActionJob(this, above_cbx) {
			public void perform() {
				setAbove(above_cbx.isSelected());
			}
		};
		new ActionJob(this, trans_cmb) {
			public void perform() {
				setTransition(trans_cmb.getSelectedIndex());
			}
		};
		new ChangeJob(this, lane_spn) {
			public void perform() {
				Number n = (Number)lane_spn.getValue();
				setLanes(n.intValue());
			}
		};
		new ActionJob(this, attach_cbx) {
			public void perform() {
				setAttachSide(attach_cbx.isSelected());
			}
		};
		new ChangeJob(this, shift_spn) {
			public void perform() {
				Number n = (Number)shift_spn.getValue();
				setShift(n.intValue());
			}
		};
		new ActionJob(this, active_cbx) {
			public void perform() {
				setActive(active_cbx.isSelected());
			}
		};
		new FocusJob(station_txt) {
			public void perform() {
				if(wasLost()) {
					String s = station_txt.getText().trim();
					setStationID(s);
				}
			}
		};
		new ChangeJob(this, speed_spn) {
			public void perform() {
				Number n = (Number)speed_spn.getValue();
				setSpeedLimit(n.intValue());
			}
		};
	}

	/** Set the node type */
	protected void setNodeType(int t) {
		R_Node n = node;
		if(n != null)
			n.setNodeType(t);
	}

	/** Set the pickable flag */
	protected void setPickable(boolean p) {
		R_Node n = node;
		if(n != null)
			n.setPickable(p);
	}

	/** Set the above flag */
	protected void setAbove(boolean a) {
		R_Node n = node;
		if(n != null)
			n.setAbove(a);
	}

	/** Set the transition */
	protected void setTransition(int t) {
		R_Node n = node;
		if(n != null)
			n.setTransition(t);
	}

	/** Set the number of lanes */
	protected void setLanes(int l) {
		R_Node n = node;
		if(n != null)
			n.setLanes(l);
	}

	/** Set the attach side */
	protected void setAttachSide(boolean a) {
		R_Node n = node;
		if(n != null)
			n.setAttachSide(a);
	}

	/** Set the active state */
	protected void setActive(boolean a) {
		R_Node n = node;
		if(n != null)
			n.setActive(a);
	}

	/** Set the lane shift */
	protected void setShift(int s) {
		R_Node n = node;
		if(n != null)
			n.setShift(s);
	}

	/** Set the station ID */
	protected void setStationID(String s) {
		R_Node n = node;
		if(n != null)
			n.setStationID(s);
	}

	/** Set the speed limit */
	protected void setSpeedLimit(int s) {
		R_Node n = node;
		if(n != null)
			n.setSpeedLimit(s);
	}

	/** Update one attribute */
	public final void update(final R_Node n, final String a) {
		// Serialize on WORKER thread
		new AbstractJob() {
			public void perform() {
				doUpdate(n, a);
			}
		}.addToScheduler();
	}

	/** Update one attribute */
	protected void doUpdate(R_Node n, String a) {
		if(a == null)
			node = n;
		if(a == null || a.equals("nodeType")) {
			type_cmb.setEnabled(canUpdate(n, "nodeType"));
			type_cmb.setSelectedIndex(n.getNodeType());
		}
		if(a == null || a.equals("pickable")) {
			pick_cbx.setEnabled(canUpdate(n, "pickable"));
			pick_cbx.setSelected(n.getPickable());
		}
		if(a == null || a.equals("above")) {
			above_cbx.setEnabled(canUpdate(n, "above"));
			above_cbx.setSelected(n.getAbove());
		}
		if(a == null || a.equals("transition")) {
			trans_cmb.setEnabled(canUpdate(n, "transition"));
			trans_cmb.setSelectedIndex(n.getTransition());
		}
		if(a == null || a.equals("lanes")) {
			lane_spn.setEnabled(canUpdate(n, "lanes"));
			lane_spn.setValue(n.getLanes());
		}
		if(a == null || a.equals("attachSide")) {
			attach_cbx.setEnabled(canUpdate(n, "attachSide"));
			attach_cbx.setSelected(n.getAttachSide());
		}
		if(a == null || a.equals("shift")) {
			shift_spn.setEnabled(canUpdate(n, "shift"));
			shift_spn.setValue(n.getShift());
		}
		if(a == null || a.equals("active")) {
			active_cbx.setEnabled(canUpdate(n, "active"));
			active_cbx.setSelected(n.getActive());
		}
		if(a == null || a.equals("stationID")) {
			station_txt.setEnabled(canUpdate(n, "stationID"));
			station_txt.setText(n.getStationID());
		}
		if(a == null || a.equals("speedLimit")) {
			speed_spn.setEnabled(canUpdate(n, "speedLimit"));
			speed_spn.setValue(n.getSpeedLimit());
		}
	}

	/** Test if the user can update an attribute */
	protected boolean canUpdate(R_Node n, String a) {
		return session.canUpdate(n, a);
	}

	/** Clear all attributes */
	public final void clear() {
		// Serialize on WORKER thread
		new AbstractJob() {
			public void perform() {
				doClear();
			}
		}.addToScheduler();
	}

	/** Clear all attributes */
	protected void doClear() {
		node = null;
		type_cmb.setEnabled(false);
		type_cmb.setSelectedIndex(0);
		pick_cbx.setEnabled(false);
		pick_cbx.setSelected(false);
		above_cbx.setEnabled(false);
		above_cbx.setSelected(false);
		trans_cmb.setEnabled(false);
		trans_cmb.setSelectedIndex(0);
		lane_spn.setEnabled(false);
		lane_spn.setValue(0);
		attach_cbx.setEnabled(false);
		attach_cbx.setSelected(false);
		shift_spn.setEnabled(false);
		shift_spn.setValue(0);
		active_cbx.setEnabled(false);
		active_cbx.setSelected(false);
		station_txt.setEnabled(false);
		station_txt.setText("");
		speed_spn.setEnabled(false);
		speed_spn.setValue(55);
	}
}
