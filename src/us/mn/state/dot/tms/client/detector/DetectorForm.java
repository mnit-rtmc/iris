/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2009  Minnesota Department of Transportation
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
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sched.ListSelectionJob;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Detector;
import us.mn.state.dot.tms.client.toast.AbstractForm;
import us.mn.state.dot.tms.client.toast.FormPanel;
import us.mn.state.dot.tms.client.widget.ZTable;

/**
 * A form for displaying and editing detectors
 *
 * @author Douglas Lau
 */
public class DetectorForm extends AbstractForm {

	/** Frame title */
	static protected final String TITLE = "Detectors";

	/** Table model for detectors */
	protected final DetectorModel model;

	/** Table to hold the detector list */
	protected final ZTable table = new ZTable();

	/** Button to delete the selected detector */
	protected final JButton del_button = new JButton("Delete");

	/** Create a new detector form */
	public DetectorForm(TypeCache<Detector> c, Namespace ns, User u) {
		super(TITLE);
		model = new DetectorModel(c, ns, u);
	}

	/** Initializze the widgets in the form */
	protected void initialize() {
		model.initialize();
		add(createDetectorPanel());
	}

	/** Dispose of the form */
	protected void dispose() {
		model.dispose();
	}

	/** Create detector panel */
	protected JPanel createDetectorPanel() {
		final ListSelectionModel s = table.getSelectionModel();
		s.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		new ListSelectionJob(this, s) {
			public void perform() {
				if(!event.getValueIsAdjusting())
					selectDetector();
			}
		};
		new ActionJob(this, del_button) {
			public void perform() throws Exception {
				int row = s.getMinSelectionIndex();
				if(row >= 0)
					model.deleteRow(row);
			}
		};
		table.setModel(model);
		table.setAutoCreateColumnsFromModel(false);
		table.setColumnModel(model.createColumnModel());
		table.setRowHeight(20);
		table.setVisibleRowCount(16);
		FormPanel panel = new FormPanel(true);
		panel.addRow(table);
		panel.addRow(del_button);
		del_button.setEnabled(false);
		return panel;
	}

	/** Change the selected detector */
	protected void selectDetector() {
		Detector d = model.getProxy(table.getSelectedRow());
		del_button.setEnabled(model.canRemove(d));
	}
}
