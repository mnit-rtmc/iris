/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2017  Minnesota Department of Transportation
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

import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import us.mn.state.dot.tms.Detector;
import us.mn.state.dot.tms.DetectorHelper;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.detector.DetectorPanel;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IListSelectionAdapter;
import us.mn.state.dot.tms.client.widget.IPanel;
import us.mn.state.dot.tms.client.widget.IPanel.Stretch;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
import us.mn.state.dot.tms.client.widget.ZTable;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A panel for editing the detectors of an r_node.
 *
 * @author Douglas Lau
 */
public class R_NodeDetectorPanel extends JPanel {

	/** Filter a string name */
	static private String filterName(String n, int max_len) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < n.length(); i++) {
			char c = n.charAt(i);
			if (c >= '0' && c <= '9')
				sb.append(c);
			if (c >= 'A' && c <= 'Z')
				sb.append(c);
			if (c >= 'a' && c <= 'z')
				sb.append(c);
			if (sb.length() >= max_len)
				break;
		}
		return sb.toString();
	}

	/** Detector table */
	private final ZTable det_table = new ZTable();

	/** Detector ID text field */
	private final JTextField det_txt = new JTextField(6);

	/** Detector label */
	private final JLabel det_lbl = new JLabel();

	/** Action to create a new detector */
	private final IAction create_det = new IAction("detector.create") {
		protected void doActionPerformed(ActionEvent e) {
			R_NodeDetectorModel m = det_model;
			if (m != null)
				m.createObject(getDetectorName());
			det_txt.setText("");
			lookupDetector();
		}
	};

	/** Button to create a new detector */
	private final JButton create_btn = new JButton(create_det);

	/** Action to transfer a detector */
	private final IAction transfer_det = new IAction("detector.transfer") {
		protected void doActionPerformed(ActionEvent e) {
			R_NodeDetectorModel m = det_model;
			if (m != null) {
				Detector det = DetectorHelper.lookup(
					getDetectorName());
				if (det != null)
					m.transfer(det);
			}
			det_txt.setText("");
			lookupDetector();
		}
	};

	/** Action to delete a detector */
	private final IAction delete_det = new IAction("detector.delete") {
		protected void doActionPerformed(ActionEvent e) {
			Detector det = getSelectedDetector();
			if (det != null)
				det.destroy();
		}
	};

	/** User session */
	private final Session session;

	/** R_Node detector model */
	private R_NodeDetectorModel det_model;

	/** Set the r_node */
	public void setR_Node(R_Node n) {
		R_NodeDetectorModel m = det_model;
		if (m != null)
			m.dispose();
		det_model = new R_NodeDetectorModel(session, n);
		det_model.initialize();
		det_table.setModel(det_model);
		det_table.setColumnModel(det_model.createColumnModel());
		det_txt.setEnabled(canAddDetector(n));
		det_txt.setText("");
		lookupDetector();
	}

	/** Detector panel */
	private final DetectorPanel det_pnl;

	/** Create a new roadway node detector panel */
	public R_NodeDetectorPanel(Session s) {
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		session = s;
		det_pnl = new DetectorPanel(s, false);
	}

	/** Initialize the widgets on the form */
	protected void initialize() {
		det_table.setAutoCreateColumnsFromModel(false);
		det_table.setVisibleRowCount(5);
		IPanel pnl = new IPanel();
		pnl.add(det_table, Stretch.FULL);
		pnl.add(det_txt);
		pnl.add(det_lbl, Stretch.LAST);
		pnl.add(create_btn);
		pnl.add(new JButton(transfer_det));
		pnl.add(new JButton(delete_det), Stretch.LAST);
		add(pnl);
		add(det_pnl);
		createJobs();
		det_pnl.initialize();
		det_txt.setEnabled(false);
		det_txt.setToolTipText(I18N.get("detector.name"));
		create_det.setEnabled(false);
		transfer_det.setEnabled(false);
		delete_det.setEnabled(false);
	}

	/** Create Gui jobs */
	private void createJobs() {
		ListSelectionModel s = det_table.getSelectionModel();
		s.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		s.addListSelectionListener(new IListSelectionAdapter() {
			@Override
			public void valueChanged() {
				selectDetector();
			}
		});
		det_txt.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER)
					create_btn.doClick();
			}
			@Override
			public void keyReleased(KeyEvent ke) {
				det_table.clearSelection();
				lookupDetector();
			}
		});
	}

	/** Get the entered detector name */
	private String getDetectorName() {
		String name = det_txt.getText();
		String n = filterName(name, 10);
		if (!n.equals(name))
			det_txt.setText(n);
		return n;
	}

	/** Lookup the detector */
	private void lookupDetector() {
		String name = getDetectorName();
		Detector det = DetectorHelper.lookup(name);
		if (name.length() > 0)
			det_lbl.setText(lookupLabel(det));
		else
			det_lbl.setText("");
		create_det.setEnabled(det == null && canAddDetector(name));
		transfer_det.setEnabled(det != null && canWriteDetector(det));
	}

	/** Lookup a detector label */
	private String lookupLabel(Detector det) {
		if (det != null)
			return DetectorHelper.getLabel(det);
		else
			return "";
	}

	/** Select a detector */
	private void selectDetector() {
		Detector det = getSelectedDetector();
		det_pnl.setDetector(det);
		if (det != null) {
			det_txt.setText("");
			lookupDetector();
		}
		delete_det.setEnabled(canRemoveDetector(det));
	}

	/** Get the currently selected detector */
	private Detector getSelectedDetector() {
		R_NodeDetectorModel m = det_model;
		if (m != null)
			return m.getRowProxy(det_table.getSelectedRow());
		else
			return null;
	}

	/** Dispose of the panel */
	public void dispose() {
		det_pnl.dispose();
		if (det_model != null)
			det_model.dispose();
		removeAll();
	}

	/** Test if the user can add a detector */
	private boolean canAddDetector(R_Node n) {
		return n != null && session.canAdd(Detector.SONAR_TYPE);
	}

	/** Test if the user can add a detector */
	private boolean canAddDetector(String n) {
		return n.length() > 0 && session.canAdd(Detector.SONAR_TYPE, n);
	}

	/** Test if the user can update a detector r_node association */
	private boolean canWriteDetector(Detector d) {
		return session.canWrite(d, "r_node");
	}

	/** Test if the user can remove a detector */
	private boolean canRemoveDetector(Detector d) {
		return session.canRemove(d) && d.getController() == null;
	}
}
