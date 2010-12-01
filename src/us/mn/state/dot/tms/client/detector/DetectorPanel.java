/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.detector;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sched.ChangeJob;
import us.mn.state.dot.sched.FocusJob;
import us.mn.state.dot.sonar.Name;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.Detector;
import us.mn.state.dot.tms.LaneType;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.toast.FormPanel;

/**
 * A panel for editing the properties of a detector.
 *
 * @author Douglas Lau
 */
public class DetectorPanel extends FormPanel {

	/** Lane type combobox */
	protected final JComboBox type_cmb =
		new JComboBox(LaneType.getDescriptions());

	/** Spinner for lane number */
	protected final JSpinner lane_spn = new JSpinner(
		new SpinnerNumberModel(0, 0, 12, 1));

	/** Abandoned check box */
	protected final JCheckBox aband_cbx = new JCheckBox();

	/** Force fail check box */
	protected final JCheckBox fail_cbx = new JCheckBox();

	/** Spinner for field length */
	protected final JSpinner field_spn = new JSpinner(
		new SpinnerNumberModel(22, 1, 100, 0.01));

	/** Fake det text field */
	protected final JTextField fake_txt = new JTextField(12);

	/** Note text field */
	protected final JTextField note_txt = new JTextField(12);

	/** SONAR namespace */
	protected final Namespace namespace;

	/** SONAR user */
	protected final User user;

	/** Detector being edited */
	protected final Detector detector;

	/** Create the detector panel */
	public DetectorPanel(Session s, Detector d) {
		super(false);
		namespace = s.getSonarState().getNamespace();
		user = s.getUser();
		detector = d;
	}

	/** Initialize the panel */
	public void initialize() {
		addRow("Lane type", type_cmb);
		addRow("Lane #", lane_spn);
		addRow("Abandoned", aband_cbx);
		addRow("Force Fail", fail_cbx);
		addRow("Field Len", field_spn);
		addRow("Fake", fake_txt);
		addRow("Notes", note_txt);
		createJobs();
	}

	/** Create the jobs */
	protected void createJobs() {
		new ActionJob(this, type_cmb) {
			public void perform() {
				detector.setLaneType(
					(short)type_cmb.getSelectedIndex());
			}
		};
		new ChangeJob(this, lane_spn) {
			public void perform() {
				Number n = (Number)lane_spn.getValue();
				detector.setLaneNumber(n.shortValue());
			}
		};
		new ActionJob(this, aband_cbx) {
			public void perform() {
				detector.setAbandoned(aband_cbx.isSelected());
			}
		};
		new ActionJob(this, fail_cbx) {
			public void perform() {
				detector.setForceFail(fail_cbx.isSelected());
			}
		};
		new ChangeJob(this, field_spn) {
			public void perform() {
				Number n = (Number)field_spn.getValue();
				detector.setFieldLength(n.floatValue());
			}
		};
		new FocusJob(fake_txt) {
			public void perform() {
				if(wasLost()) {
					String s = fake_txt.getText().trim();
					detector.setFake(s);
				}
			}
		};
		new FocusJob(note_txt) {
			public void perform() {
				if(wasLost()) {
					String s = note_txt.getText().trim();
					detector.setNotes(s);
				}
			}
		};
	}

	/** Update one attribute on the form */
	public void doUpdateAttribute(String a) {
		if(a == null || a.equals("laneType")) {
			type_cmb.setSelectedIndex(detector.getLaneType());
			type_cmb.setEnabled(canUpdate("laneType"));
		}
		if(a == null || a.equals("laneNumber")) {
			lane_spn.setValue(detector.getLaneNumber());
			lane_spn.setEnabled(canUpdate("laneNumber"));
		}
		if(a == null || a.equals("abandoned")) {
			aband_cbx.setSelected(detector.getAbandoned());
			aband_cbx.setEnabled(canUpdate("abandoned"));
		}
		if(a == null || a.equals("forceFail")) {
			fail_cbx.setSelected(detector.getForceFail());
			fail_cbx.setEnabled(canUpdate("forceFail"));
		}
		if(a == null || a.equals("fieldLength")) {
			field_spn.setValue(detector.getFieldLength());
			field_spn.setEnabled(canUpdate("fieldLength"));
		}
		if(a == null || a.equals("fake")) {
			fake_txt.setText(detector.getFake());
			fake_txt.setEnabled(canUpdate("fake"));
		}
		if(a == null || a.equals("notes")) {
			note_txt.setText(detector.getNotes());
			note_txt.setEnabled(canUpdate("notes"));
		}
	}

	/** Check if the user can update an attribute */
	protected boolean canUpdate(String aname) {
		return namespace.canUpdate(user, new Name(detector, aname));
	}
}
