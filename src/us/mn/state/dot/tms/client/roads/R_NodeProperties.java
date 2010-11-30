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

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import us.mn.state.dot.sched.FocusJob;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.toast.LocationPanel;
import us.mn.state.dot.tms.client.toast.SonarObjectForm;

/**
 * R_NodeProperties is a form for viewing and editing roadway node parameters.
 *
 * @author Douglas Lau
 */
public class R_NodeProperties extends SonarObjectForm<R_Node> {

	/** Frame title */
	static protected final String TITLE = "R_Node: ";

	/** Location panel */
	protected LocationPanel location;

	/** Component for editing notes */
	protected final JTextArea notes = new JTextArea(3, 20);

	/** Setup panel */
	protected final R_NodeSetupPanel setup_pnl;

	/** Detector table */
	protected final JTable det_table = new JTable();

	/** R_Node detector modell */
	protected final R_NodeDetectorModel det_model;

	/** Create a new roadway node properties form */
	public R_NodeProperties(Session s, R_Node n) {
		super(TITLE, s, n);
		det_model = new R_NodeDetectorModel(session, proxy);
		setup_pnl = new R_NodeSetupPanel(n);
	}

	/** Get the SONAR type cache */
	protected TypeCache<R_Node> getTypeCache() {
		return state.getDetCache().getR_Nodes();
	}

	/** Initialize the widgets on the form */
	protected void initialize() {
		location = new LocationPanel(session, proxy.getGeoLoc());
		det_model.initialize();
		det_table.setAutoCreateColumnsFromModel(false);
		det_table.setModel(det_model);
		det_table.setColumnModel(det_model.createColumnModel());
		det_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		det_table.setRowHeight(20);
		det_table.setPreferredScrollableViewportSize(new Dimension(
			det_table.getPreferredSize().width,
			det_table.getRowHeight() * 8));
		super.initialize();
		location.initialize();
		JTabbedPane tab = new JTabbedPane();
		tab.add("Location", createLocationPanel());
		tab.add("Setup", setup_pnl);
		tab.add("Detectors", createDetectorPanel());
		add(tab);
		updateAttribute(null);
		setBackground(Color.LIGHT_GRAY);
		createJobs();
	}

	/** Dispose of the form */
	protected void dispose() {
		location.dispose();
		super.dispose();
	}

	/** Create the location panel */
	protected JPanel createLocationPanel() {
		location.addRow("Notes", notes);
		return location;
	}

	/** Create the jobs */
	protected void createJobs() {
		new FocusJob(notes) {
			public void perform() {
				if(wasLost())
					proxy.setNotes(notes.getText());
			}
		};
		setup_pnl.createJobs();
	}

	/** Create the detector panel */
	protected JPanel createDetectorPanel() {
		JPanel dpanel = new JPanel();
		dpanel.setBorder(BORDER);
		dpanel.add(new JScrollPane(det_table));
		return dpanel;
	}

	/** Update one attribute on the form */
	protected void doUpdateAttribute(String a) {
		if(a == null || a.equals("notes"))
			notes.setText(proxy.getNotes());
		setup_pnl.doUpdateAttribute(a);
	}
}
