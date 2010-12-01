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
	protected Detector detector;

	/** Set the detector */
	public void setDetector(Detector det) {
		detector = det;
		doUpdateAttribute(null);
	}

	/** Create the detector panel */
	public DetectorPanel(Session s) {
		super(false);
		namespace = s.getSonarState().getNamespace();
		user = s.getUser();
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
				setLaneType((short)type_cmb.getSelectedIndex());
			}
		};
		new ChangeJob(this, lane_spn) {
			public void perform() {
				Number n = (Number)lane_spn.getValue();
				setLaneNumber(n.shortValue());
			}
		};
		new ActionJob(this, aband_cbx) {
			public void perform() {
				setAbandoned(aband_cbx.isSelected());
			}
		};
		new ActionJob(this, fail_cbx) {
			public void perform() {
				setForceFail(fail_cbx.isSelected());
			}
		};
		new ChangeJob(this, field_spn) {
			public void perform() {
				Number n = (Number)field_spn.getValue();
				setFieldLength(n.floatValue());
			}
		};
		new FocusJob(fake_txt) {
			public void perform() {
				if(wasLost())
					setFake(fake_txt.getText().trim());
			}
		};
		new FocusJob(note_txt) {
			public void perform() {
				if(wasLost())
					setNotes(note_txt.getText().trim());
			}
		};
	}

	/** Set the detector lane type */
	protected void setLaneType(short lt) {
		Detector det = detector;
		if(det != null)
			det.setLaneType(lt);
	}

	/** Set the detector lane number */
	protected void setLaneNumber(short n) {
		Detector det = detector;
		if(det != null)
			det.setLaneNumber(n);
	}

	/** Set the detector abandoned flag */
	protected void setAbandoned(boolean a) {
		Detector det = detector;
		if(det != null)
			det.setAbandoned(a);
	}

	/** Set the detector force fail flag */
	protected void setForceFail(boolean f) {
		Detector det = detector;
		if(det != null)
			det.setForceFail(f);
	}

	/** Set the detector field length */
	protected void setFieldLength(float f) {
		Detector det = detector;
		if(det != null)
			det.setFieldLength(f);
	}

	/** Set the detector fake expression */
	protected void setFake(String f) {
		Detector det = detector;
		if(det != null)
			det.setFake(f);
	}

	/** Set the detector notes */
	protected void setNotes(String n) {
		Detector det = detector;
		if(det != null)
			det.setNotes(n);
	}

	/** Update one attribute on the form */
	protected void doUpdateAttribute(String a) {
		if(a == null || a.equals("laneType")) {
			type_cmb.setSelectedIndex(getLaneType());
			type_cmb.setEnabled(canUpdate("laneType"));
		}
		if(a == null || a.equals("laneNumber")) {
			lane_spn.setValue(getLaneNumber());
			lane_spn.setEnabled(canUpdate("laneNumber"));
		}
		if(a == null || a.equals("abandoned")) {
			aband_cbx.setSelected(getAbandoned());
			aband_cbx.setEnabled(canUpdate("abandoned"));
		}
		if(a == null || a.equals("forceFail")) {
			fail_cbx.setSelected(getForceFail());
			fail_cbx.setEnabled(canUpdate("forceFail"));
		}
		if(a == null || a.equals("fieldLength")) {
			field_spn.setValue(getFieldLength());
			field_spn.setEnabled(canUpdate("fieldLength"));
		}
		if(a == null || a.equals("fake")) {
			fake_txt.setText(getFake());
			fake_txt.setEnabled(canUpdate("fake"));
		}
		if(a == null || a.equals("notes")) {
			note_txt.setText(getNotes());
			note_txt.setEnabled(canUpdate("notes"));
		}
	}

	/** Get the detector lane type */
	protected short getLaneType() {
		Detector det = detector;
		if(det != null)
			return det.getLaneType();
		else
			return 0;
	}

	/** Get the detector lane number */
	protected short getLaneNumber() {
		Detector det = detector;
		if(det != null)
			return det.getLaneNumber();
		else
			return 0;
	}

	/** Get the detector abandoned flag */
	protected boolean getAbandoned() {
		Detector det = detector;
		if(det != null)
			return det.getAbandoned();
		else
			return false;
	}

	/** Get the detector force fail flag */
	protected boolean getForceFail() {
		Detector det = detector;
		if(det != null)
			return det.getForceFail();
		else
			return false;
	}

	/** Get the detector field length */
	protected float getFieldLength() {
		Detector det = detector;
		if(det != null)
			return det.getFieldLength();
		else
			return 22f;
	}

	/** Get the detector fake expression */
	protected String getFake() {
		Detector det = detector;
		if(det != null)
			return det.getFake();
		else
			return "";
	}

	/** Get the detector notes */
	protected String getNotes() {
		Detector det = detector;
		if(det != null)
			return det.getNotes();
		else
			return "";
	}

	/** Check if the user can update an attribute */
	protected boolean canUpdate(String aname) {
		Detector det = detector;
		if(det != null)
			return namespace.canUpdate(user, new Name(det, aname));
		else
			return false;
	}
}
