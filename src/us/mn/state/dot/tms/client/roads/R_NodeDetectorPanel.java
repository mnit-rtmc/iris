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

import java.awt.Dimension;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.toast.TmsForm;

/**
 * A panel for editing the detectors of an r_node.
 *
 * @author Douglas Lau
 */
public class R_NodeDetectorPanel extends JPanel {

	/** Detector table */
	protected final JTable det_table = new JTable();

	/** R_Node detector model */
	protected final R_NodeDetectorModel det_model;

	/** Create a new roadway node detector panel */
	public R_NodeDetectorPanel(Session s, R_Node n) {
		det_model = new R_NodeDetectorModel(s, n);
		setBorder(TmsForm.BORDER);
		add(new JScrollPane(det_table));
	}

	/** Initialize the widgets on the form */
	protected void initialize() {
		det_model.initialize();
		det_table.setAutoCreateColumnsFromModel(false);
		det_table.setModel(det_model);
		det_table.setColumnModel(det_model.createColumnModel());
		det_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		det_table.setRowHeight(20);
		det_table.setPreferredScrollableViewportSize(new Dimension(
			det_table.getPreferredSize().width,
			det_table.getRowHeight() * 8));
	}

	/** Dispose of the panel */
	public void dispose() {
		det_model.dispose();
		removeAll();
	}
}
