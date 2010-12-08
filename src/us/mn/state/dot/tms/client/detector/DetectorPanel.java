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

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import us.mn.state.dot.sched.AbstractJob;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sched.ChangeJob;
import us.mn.state.dot.sched.FocusJob;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.Detector;
import us.mn.state.dot.tms.LaneType;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyView;
import us.mn.state.dot.tms.client.proxy.ProxyWatcher;
import us.mn.state.dot.tms.client.toast.ControllerForm;
import us.mn.state.dot.tms.client.toast.FormPanel;

/**
 * A panel for editing the properties of a detector.
 *
 * @author Douglas Lau
 */
public class DetectorPanel extends FormPanel implements ProxyView<Detector> {

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

	/** Button to display the controller */
	protected final JButton ctrl_btn = new JButton("Controller");

	/** Button to display the r_node */
	protected final JButton rnode_btn = new JButton("R_Node");

	/** User session */
	protected final Session session;

	/** Flag to include r_node button */
	protected final boolean has_r_btn;

	/** Proxy watcher */
	protected final ProxyWatcher<Detector> watcher;

	/** Detector being edited */
	protected Detector detector;

	/** Set the detector */
	public void setDetector(Detector det) {
		watcher.setProxy(det);
	}

	/** Create the detector panel */
	public DetectorPanel(Session s, boolean r) {
		super(false);
		session = s;
		has_r_btn = r;
		TypeCache<Detector> cache =
			s.getSonarState().getDetCache().getDetectors();
		watcher = new ProxyWatcher<Detector>(s, this, cache, false);
	}

	/** Initialize the panel */
	public void initialize() {
		addRow("Lane type", type_cmb);
		addRow("Lane #", lane_spn);
		add("Abandoned", aband_cbx);
		addRow("Force Fail", fail_cbx);
		addRow("Field Len", field_spn);
		addRow("Fake", fake_txt);
		addRow("Notes", note_txt);
		setWest();
		setWidth(2);
		bag.insets.bottom = 0;
		add(ctrl_btn);
		if(has_r_btn)
			add(rnode_btn);
		finishRow();
		createJobs();
		watcher.initialize();
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
		new ActionJob(this, ctrl_btn) {
			public void perform() {
				showControllerForm(detector);
			}
		};
		new ActionJob(this, rnode_btn) {
			public void perform() {
				showRNode(detector);
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

	/** Show the controller form for a detector */
	protected void showControllerForm(Detector d) {
		ControllerForm form = createControllerForm(d);
		if(form != null)
			session.getDesktop().show(form);
	}

	/** Show the r_node for a detector */
	protected void showRNode(Detector d) {
		R_Node n = d.getR_Node();
		session.getR_NodeManager().getSelectionModel().setSelected(n);
	}

	/** Create a controller form */
	protected ControllerForm createControllerForm(Detector d) {
		if(d != null) {
			Controller c = d.getController();
			if(c != null)
				return new ControllerForm(session, c);
		}
		return null;
	}

	/** Dispose of the panel */
	public void dispose() {
		watcher.dispose();
		super.dispose();
	}

	/** Update one attribute */
	public final void update(final Detector d, final String a) {
		// Serialize on WORKER thread
		new AbstractJob() {
			public void perform() {
				doUpdate(d, a);
			}
		}.addToScheduler();
	}

	/** Update one attribute */
	protected void doUpdate(Detector d, String a) {
		if(a == null) {
			detector = d;
			ctrl_btn.setEnabled(d != null &&
			                    d.getController() != null);
			rnode_btn.setEnabled(d != null);
		}
		if(a == null || a.equals("laneType")) {
			type_cmb.setSelectedIndex(d.getLaneType());
			type_cmb.setEnabled(watcher.canUpdate(d, "laneType"));
		}
		if(a == null || a.equals("laneNumber")) {
			lane_spn.setValue(d.getLaneNumber());
			lane_spn.setEnabled(watcher.canUpdate(d, "laneNumber"));
		}
		if(a == null || a.equals("abandoned")) {
			aband_cbx.setSelected(d.getAbandoned());
			aband_cbx.setEnabled(watcher.canUpdate(d, "abandoned"));
		}
		if(a == null || a.equals("forceFail")) {
			fail_cbx.setSelected(d.getForceFail());
			fail_cbx.setEnabled(watcher.canUpdate(d, "forceFail"));
		}
		if(a == null || a.equals("fieldLength")) {
			field_spn.setValue(d.getFieldLength());
			field_spn.setEnabled(watcher.canUpdate(d,
				"fieldLength"));
		}
		if(a == null || a.equals("fake")) {
			fake_txt.setText(d.getFake());
			fake_txt.setEnabled(watcher.canUpdate(d, "fake"));
		}
		if(a == null || a.equals("notes")) {
			note_txt.setText(d.getNotes());
			note_txt.setEnabled(watcher.canUpdate(d, "notes"));
		}
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
		detector = null;
		type_cmb.setSelectedIndex(0);
		type_cmb.setEnabled(false);
		lane_spn.setValue(0);
		lane_spn.setEnabled(false);
		aband_cbx.setSelected(false);
		aband_cbx.setEnabled(false);
		fail_cbx.setSelected(false);
		fail_cbx.setEnabled(false);
		field_spn.setValue(22);
		field_spn.setEnabled(false);
		fake_txt.setText("");
		fake_txt.setEnabled(false);
		note_txt.setText("");
		note_txt.setEnabled(false);
		ctrl_btn.setEnabled(false);
		rnode_btn.setEnabled(false);
	}
}
