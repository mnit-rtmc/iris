/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2024  Minnesota Department of Transportation
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

import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.Detector;
import us.mn.state.dot.tms.LaneCode;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.client.EditModeListener;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.comm.ControllerForm;
import us.mn.state.dot.tms.client.proxy.ProxyView;
import us.mn.state.dot.tms.client.proxy.ProxyWatcher;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IPanel;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A panel for editing the properties of a detector.
 *
 * @author Douglas Lau
 */
public class DetectorPanel extends IPanel implements ProxyView<Detector> {

	/** Detector action */
	abstract private class DAction extends IAction {
		protected DAction(String text_id) {
			super(text_id);
		}
		protected final void doActionPerformed(ActionEvent e) {
			Detector d = detector;
			if (d != null)
				do_perform(d);
		}
		abstract protected void do_perform(Detector d);
	}

	/** Lane code action */
	private final DAction code_act = new DAction("detector.lane.code") {
		protected void do_perform(Detector d) {
			Object item = code_cbx.getSelectedItem();
			if (item instanceof LaneCode) {
				LaneCode lc = (LaneCode) item;
				d.setLaneCode(lc.lcode);
			}
		}
		@Override
		protected void doUpdateSelected() {
			Detector d = detector;
			if (d != null) {
				LaneCode lc = LaneCode.fromCode(d.getLaneCode());
				code_cbx.setSelectedItem(lc);
			}
		}
	};

	/** Lane code combobox */
	private final JComboBox<LaneCode> code_cbx =
		new JComboBox<LaneCode>(LaneCode.values());

	/** Spinner for lane number */
	private final JSpinner lane_spn = new JSpinner(
		new SpinnerNumberModel(0, 0, 12, 1));

	/** Abandoned check box */
	private final JCheckBox aband_chk = new JCheckBox(new DAction(null) {
		protected void do_perform(Detector d) {
			d.setAbandoned(aband_chk.isSelected());
		}
	});

	/** Force fail check box */
	private final JCheckBox force_chk = new JCheckBox(new DAction(null) {
		protected void do_perform(Detector d) {
			d.setForceFail(force_chk.isSelected());
		}
	});

	/** Auto fail check box */
	private final JCheckBox auto_chk = new JCheckBox(new DAction(null) {
		protected void do_perform(Detector d) { }
	});

	/** Spinner for field length */
	private final JSpinner field_spn = new JSpinner(
		new SpinnerNumberModel(22, 1, 100, 0.01));

	/** Fake det text field */
	private final JTextField fake_txt = new JTextField(12);

	/** Note text field */
	private final JTextField note_txt = new JTextField(12);

	/** Button to display the controller */
	private final JButton controller_btn = new JButton(
		new DAction("controller")
	{
		protected void do_perform(Detector d) {
			showControllerForm(d);
		}
	});

	/** Action to display the r_node */
	private final JButton r_node_btn = new JButton(
		new DAction("r_node")
	{
		protected void do_perform(Detector d) {
			showRNode(d);
		}
	});

	/** User session */
	private final Session session;

	/** Flag to include r_node button */
	private final boolean has_r_btn;

	/** Proxy watcher */
	private final ProxyWatcher<Detector> watcher;

	/** Edit mode listener */
	private final EditModeListener edit_lsnr = new EditModeListener() {
		public void editModeChanged() {
			updateEditMode();
		}
	};

	/** Detector being edited */
	private Detector detector;

	/** Set the detector */
	public void setDetector(Detector det) {
		watcher.setProxy(det);
	}

	/** Create the detector panel */
	public DetectorPanel(Session s, boolean r) {
		session = s;
		has_r_btn = r;
		r_node_btn.setVisible(r);
		TypeCache<Detector> cache =
			s.getSonarState().getDetCache().getDetectors();
		watcher = new ProxyWatcher<Detector>(cache, this, false);
	}

	/** Initialize the panel */
	@Override
	public void initialize() {
		super.initialize();
		code_cbx.setAction(code_act);
		add("detector.lane.code");
		add(code_cbx, Stretch.LAST);
		add("detector.lane.number");
		add(lane_spn, Stretch.LAST);
		add("detector.abandoned");
		add(aband_chk);
		add("detector.force.fail");
		add(force_chk, Stretch.LAST);
		add("detector.field.len");
		add(field_spn);
		add("detector.auto.fail");
		add(auto_chk, Stretch.LAST);
		add("detector.fake");
		add(fake_txt, Stretch.END);
		add("device.notes");
		add(note_txt, Stretch.FULL);
		add(controller_btn);
		add(r_node_btn, Stretch.LAST);
		createJobs();
		watcher.initialize();
		clear();
		session.addEditModeListener(edit_lsnr);
	}

	/** Create the jobs */
	private void createJobs() {
		lane_spn.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Number n = (Number)lane_spn.getValue();
				setLaneNumber(n.shortValue());
			}
		});
		field_spn.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Number n = (Number)field_spn.getValue();
				setFieldLength(n.floatValue());
			}
		});
		fake_txt.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				setFake(fake_txt.getText().trim());
			}
		});
		note_txt.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				setNotes(note_txt.getText().trim());
			}
		});
	}

	/** Set the detector lane number */
	private void setLaneNumber(short n) {
		Detector det = detector;
		if (det != null)
			det.setLaneNumber(n);
	}

	/** Set the detector field length */
	private void setFieldLength(float f) {
		Detector det = detector;
		if (det != null)
			det.setFieldLength(f);
	}

	/** Set the detector fake expression */
	private void setFake(String f) {
		Detector det = detector;
		if (det != null)
			det.setFake(f);
	}

	/** Set the detector notes */
	private void setNotes(String n) {
		Detector det = detector;
		if (det != null)
			det.setNotes((n.length() > 0) ? n : null);
	}

	/** Show the controller form for a detector */
	private void showControllerForm(Detector d) {
		ControllerForm form = createControllerForm(d);
		if (form != null)
			session.getDesktop().show(form);
	}

	/** Show the r_node for a detector */
	private void showRNode(Detector d) {
		R_Node n = d.getR_Node();
		if (n == null)
			return;
		session.getR_NodeManager().getSelectionModel().setSelected(n);
	}

	/** Create a controller form */
	private ControllerForm createControllerForm(Detector d) {
		if (d != null) {
			Controller c = d.getController();
			if (c != null)
				return new ControllerForm(session, c);
		}
		return null;
	}

	/** Dispose of the panel */
	@Override
	public void dispose() {
		clear();
		session.removeEditModeListener(edit_lsnr);
		watcher.dispose();
		super.dispose();
	}

	/** Update the edit mode */
	public void updateEditMode() {
		Detector d = detector;
		code_act.setEnabled(session.canWrite(d, "laneCode"));
		lane_spn.setEnabled(session.canWrite(d, "laneNumber"));
		aband_chk.setEnabled(session.canWrite(d, "abandoned"));
		force_chk.setEnabled(session.canWrite(d, "forceFail"));
		auto_chk.setEnabled(false);
		field_spn.setEnabled(session.canWrite(d, "fieldLength"));
		fake_txt.setEnabled(session.canWrite(d, "fake"));
		note_txt.setEnabled(session.canWrite(d, "notes"));
		controller_btn.setEnabled(d != null &&
		                          d.getController() != null);
		r_node_btn.setEnabled(d != null && d.getR_Node() != null);
	}

	/** Called when all proxies have been enumerated (from ProxyView). */
	@Override
	public void enumerationComplete() { }

	/** Update one attribute (from ProxyView). */
	@Override
	public void update(Detector d, String a) {
		if (a == null) {
			detector = d;
			updateEditMode();
		}
		if (a == null || a.equals("laneCode"))
			code_act.updateSelected();
		if (a == null || a.equals("laneNumber"))
			lane_spn.setValue(d.getLaneNumber());
		if (a == null || a.equals("abandoned"))
			aband_chk.setSelected(d.getAbandoned());
		if (a == null || a.equals("forceFail"))
			force_chk.setSelected(d.getForceFail());
		if (a == null || a.equals("autoFail"))
			auto_chk.setSelected(d.getAutoFail());
		if (a == null || a.equals("fieldLength"))
			field_spn.setValue(d.getFieldLength());
		if (a == null || a.equals("fake"))
			fake_txt.setText(d.getFake());
		if (a == null || a.equals("notes")) {
			String n = d.getNotes();
			note_txt.setText((n != null) ? n : "");
		}
	}

	/** Clear all attributes (from ProxyView). */
	@Override
	public void clear() {
		detector = null;
		code_act.setEnabled(false);
		code_cbx.setSelectedIndex(0);
		lane_spn.setEnabled(false);
		lane_spn.setValue(0);
		aband_chk.setEnabled(false);
		aband_chk.setSelected(false);
		force_chk.setEnabled(false);
		force_chk.setSelected(false);
		auto_chk.setEnabled(false);
		auto_chk.setSelected(false);
		field_spn.setEnabled(false);
		field_spn.setValue(22);
		fake_txt.setEnabled(false);
		fake_txt.setText("");
		note_txt.setEnabled(false);
		note_txt.setText("");
		controller_btn.setEnabled(false);
		r_node_btn.setEnabled(false);
	}
}
