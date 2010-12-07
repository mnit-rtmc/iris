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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sched.ListSelectionJob;
import us.mn.state.dot.tms.Detector;
import us.mn.state.dot.tms.DetectorHelper;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.detector.DetectorPanel;
import us.mn.state.dot.tms.client.toast.TmsForm;
import us.mn.state.dot.tms.client.widget.ZTable;

/**
 * A panel for editing the detectors of an r_node.
 *
 * @author Douglas Lau
 */
public class R_NodeDetectorPanel extends JPanel {

	/** Filter a string name */
	static protected String filterName(String n, int max_len) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < n.length(); i++) {
			char c = n.charAt(i);
			if(c >= '0' && c <= '9')
				sb.append(c);
			if(c >= 'A' && c <= 'Z')
				sb.append(c);
			if(c >= 'a' && c <= 'z')
				sb.append(c);
			if(sb.length() >= max_len)
				break;
		}
		return sb.toString();
	}

	/** Detector table */
	protected final ZTable det_table = new ZTable();

	/** Detector ID text field */
	protected final JTextField det_txt = new JTextField(6);

	/** Detector label */
	protected final JLabel det_lbl = new JLabel();

	/** Button to create a new detector */
	protected final JButton create_btn = new JButton("Create");

	/** Button to transfer a detector */
	protected final JButton transfer_btn = new JButton("Transfer");

	/** Button to delete a detector */
	protected final JButton delete_btn = new JButton("Delete");

	/** User session */
	protected final Session session;

	/** R_Node detector model */
	protected R_NodeDetectorModel det_model;

	/** Set the r_node */
	public void setR_Node(R_Node n) {
		R_NodeDetectorModel m = det_model;
		if(m != null)
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
	protected final DetectorPanel det_pnl;

	/** Create a new roadway node detector panel */
	public R_NodeDetectorPanel(Session s) {
		super(new GridBagLayout());
		session = s;
		det_pnl = new DetectorPanel(s);
		setBorder(TmsForm.BORDER);
	}

	/** Initialize the widgets on the form */
	protected void initialize() {
		det_table.setAutoCreateColumnsFromModel(false);
		det_table.setRowHeight(20);
		det_table.setVisibleRowCount(5);
		GridBagConstraints bag = new GridBagConstraints();
		bag.gridwidth = 3;
		bag.weightx = 1;
		bag.weighty = 1;
		bag.fill = GridBagConstraints.BOTH;
		bag.insets.left = 2;
		bag.insets.right = 2;
		bag.insets.top = 2;
		bag.insets.bottom = 2;
		add(new JScrollPane(det_table), bag);
		bag.gridx = 3;
		bag.gridwidth = 1;
		bag.gridheight = 3;
		bag.weightx = 0;
		bag.weighty = 0;
		bag.fill = GridBagConstraints.NONE;
		add(det_pnl, bag);
		bag.gridx = 0;
		bag.gridy = 1;
		bag.gridheight = 1;
		add(det_txt, bag);
		bag.gridx = 1;
		bag.gridwidth = 2;
		bag.anchor = GridBagConstraints.WEST;
		add(det_lbl, bag);
		bag.gridx = 0;
		bag.gridy = 2;
		bag.gridwidth = 1;
		bag.anchor = GridBagConstraints.CENTER;
		add(create_btn, bag);
		bag.gridx = 1;
		add(transfer_btn, bag);
		bag.gridx = 2;
		add(delete_btn, bag);
		createJobs();
		det_pnl.initialize();
		det_txt.setEnabled(false);
		det_txt.setToolTipText("Detector name");
		create_btn.setEnabled(false);
		create_btn.setToolTipText("Create new detector");
		transfer_btn.setEnabled(false);
		transfer_btn.setToolTipText("Transfer detector to this r_node");
		delete_btn.setEnabled(false);
		delete_btn.setToolTipText("Delete selected detector");
	}

	/** Create Gui jobs */
	protected void createJobs() {
		ListSelectionModel s = det_table.getSelectionModel();
		s.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		new ListSelectionJob(this, s) {
			public void perform() {
				selectDetector();
			}
		};
		det_txt.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER)
					create_btn.doClick();
			}
			public void keyReleased(KeyEvent ke) {
				det_table.clearSelection();
				lookupDetector();
			}
		});
		new ActionJob(this, create_btn) {
			public void perform() {
				R_NodeDetectorModel m = det_model;
				if(m != null)
					m.create(getDetectorName());
				det_txt.setText("");
				lookupDetector();
			}
		};
		new ActionJob(this, transfer_btn) {
			public void perform() {
				R_NodeDetectorModel m = det_model;
				if(m != null) {
					Detector det = DetectorHelper.lookup(
						getDetectorName());
					if(det != null)
						m.transfer(det);
				}
				det_txt.setText("");
				lookupDetector();
			}
		};
		new ActionJob(this, delete_btn) {
			public void perform() throws Exception {
				Detector det = getSelectedDetector();
				if(det != null)
					det.destroy();
			}
		};
	}

	/** Get the entered detector name */
	protected String getDetectorName() {
		String name = det_txt.getText();
		String n = filterName(name, 10);
		if(!n.equals(name))
			det_txt.setText(n);
		return n;
	}

	/** Lookup the detector */
	protected void lookupDetector() {
		String name = getDetectorName();
		Detector det = DetectorHelper.lookup(name);
		if(name.length() > 0)
			det_lbl.setText(lookupLabel(det));
		else
			det_lbl.setText("");
		create_btn.setEnabled(det == null && canAddDetector(name));
		transfer_btn.setEnabled(det != null && canUpdateDetector(det));
	}

	/** Lookup a detector label */
	protected String lookupLabel(Detector det) {
		if(det != null)
			return DetectorHelper.getLabel(det);
		else
			return "";
	}

	/** Select a detector */
	protected void selectDetector() {
		Detector det = getSelectedDetector();
		det_pnl.setDetector(det);
		if(det != null) {
			det_txt.setText("");
			lookupDetector();
		}
		delete_btn.setEnabled(canRemoveDetector(det));
	}

	/** Get the currently selected detector */
	protected Detector getSelectedDetector() {
		R_NodeDetectorModel m = det_model;
		if(m != null)
			return m.getProxy(det_table.getSelectedRow());
		else
			return null;
	}

	/** Dispose of the panel */
	public void dispose() {
		det_pnl.dispose();
		if(det_model != null)
			det_model.dispose();
		removeAll();
	}

	/** Test if the user can add a detector */
	protected boolean canAddDetector(R_Node n) {
		return n != null && session.canAdd(Detector.SONAR_TYPE);
	}

	/** Test if the user can add a detector */
	protected boolean canAddDetector(String n) {
		return n.length() > 0 && session.canAdd(Detector.SONAR_TYPE, n);
	}

	/** Test if the user can update a detector r_node association */
	protected boolean canUpdateDetector(Detector d) {
		return session.canUpdate(d, "r_node");
	}

	/** Test if the user can remove a detector */
	protected boolean canRemoveDetector(Detector d) {
		return session.canRemove(d) && d.getController() == null;
	}
}
