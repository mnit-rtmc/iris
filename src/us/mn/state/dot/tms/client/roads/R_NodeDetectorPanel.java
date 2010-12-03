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

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import us.mn.state.dot.sched.ListSelectionJob;
import us.mn.state.dot.tms.Detector;
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

	/** Detector table */
	protected final ZTable det_table = new ZTable();

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
	}

	/** Detector panel */
	protected final DetectorPanel det_pnl;

	/** Create a new roadway node detector panel */
	public R_NodeDetectorPanel(Session s) {
		super(new BorderLayout());
		session = s;
		det_pnl = new DetectorPanel(s);
		setBorder(TmsForm.BORDER);
		add(new JScrollPane(det_table), BorderLayout.CENTER);
		add(det_pnl, BorderLayout.EAST);
	}

	/** Initialize the widgets on the form */
	protected void initialize() {
		det_table.setAutoCreateColumnsFromModel(false);
		det_table.setRowHeight(20);
		det_table.setVisibleRowCount(6);
		createJobs();
		det_pnl.initialize();
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
	}

	/** Select a detector */
	protected void selectDetector() {
		det_pnl.setDetector(getSelectedDetector());
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
}
