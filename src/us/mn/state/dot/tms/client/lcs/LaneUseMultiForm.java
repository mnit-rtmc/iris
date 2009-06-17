/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.lcs;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sched.ListSelectionJob;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.LaneUseMulti;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.toast.AbstractForm;
import us.mn.state.dot.tms.client.toast.FormPanel;
import us.mn.state.dot.tms.client.widget.ZTable;

/**
 * A form for displaying a table of lane-use MULTI strings.
 *
 * @author Douglas Lau
 */
public class LaneUseMultiForm extends AbstractForm {

	/** Frame title */
	static protected final String TITLE = "Lane-Use MULTI";

	/** Table model for lane-use MULTI */
	protected LaneUseMultiModel model;

	/** Table to hold the Graphic list */
	protected final ZTable table = new ZTable();

	/** Button to delete the selected proxy */
	protected final JButton deleteBtn = new JButton("Delete");

	/** Type cache */
	protected final TypeCache<LaneUseMulti> cache;

	/** Create a new graphic form */
	public LaneUseMultiForm(Session s) {
		super(TITLE);
		cache = s.getSonarState().getLcsCache().getLaneUseMultis();
	}

	/** Initializze the widgets in the form */
	protected void initialize() {
		model = new LaneUseMultiModel(cache);
		add(createLaneUseMultiPanel());
		table.setRowHeight(22);
		table.setVisibleRowCount(10);
	}

	/** Dispose of the form */
	protected void dispose() {
		model.dispose();
	}

	/** Create lane-use MULTI panel */
	protected JPanel createLaneUseMultiPanel() {
		final ListSelectionModel s = table.getSelectionModel();
		s.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		new ListSelectionJob(this, s) {
			public void perform() {
				if(!event.getValueIsAdjusting())
					selectProxy();
			}
		};
		new ActionJob(this, deleteBtn) {
			public void perform() throws Exception {
				int row = s.getMinSelectionIndex();
				if(row >= 0)
					model.deleteRow(row);
			}
		};
		FormPanel panel = new FormPanel(true);
		table.setModel(model);
		table.setAutoCreateColumnsFromModel(false);
		table.setColumnModel(model.createColumnModel());
		panel.addRow(table);
		panel.addRow(deleteBtn);
		deleteBtn.setEnabled(false);
		return panel;
	}

	/** Change the selected proxy */
	protected void selectProxy() {
		int row = table.getSelectedRow();
		deleteBtn.setEnabled(row >= 0 && !model.isLastRow(row));
	}
}
