/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2009  Minnesota Department of Transportation
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

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sched.ListSelectionJob;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Road;
import us.mn.state.dot.tms.client.toast.AbstractForm;
import us.mn.state.dot.tms.client.toast.FormPanel;
import us.mn.state.dot.tms.client.widget.ZTable;

/**
 * A form for displaying and editing roads
 *
 * @author Douglas Lau
 */
public class RoadForm extends AbstractForm {

	/** Frame title */
	static protected final String TITLE = "Roads";

	/** Table model for roads */
	protected final RoadModel model;

	/** Table to hold the road list */
	protected final ZTable table = new ZTable();

	/** Button to delete the selected road */
	protected final JButton del_road = new JButton("Delete");

	/** Road type cache */
	protected final TypeCache<Road> cache;

	/** Create a new road form */
	public RoadForm(TypeCache<Road> c) {
		super(TITLE);
		cache = c;
		model = new RoadModel(cache);
	}

	/** Initializze the widgets in the form */
	protected void initialize() {
		model.initialize();
		add(createRoadPanel());
	}

	/** Dispose of the form */
	protected void dispose() {
		model.dispose();
	}

	/** Create road panel */
	protected JPanel createRoadPanel() {
		final ListSelectionModel s = table.getSelectionModel();
		s.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		new ListSelectionJob(this, s) {
			public void perform() {
				if(!event.getValueIsAdjusting())
					selectRoad();
			}
		};
		new ActionJob(this, del_road) {
			public void perform() throws Exception {
				int row = s.getMinSelectionIndex();
				if(row >= 0)
					model.deleteRow(row);
			}
		};
		table.setModel(model);
		table.setAutoCreateColumnsFromModel(false);
		table.setColumnModel(model.createColumnModel());
		table.setVisibleRowCount(20);
		FormPanel panel = new FormPanel(true);
		panel.addRow(table);
		panel.addRow(del_road);
		del_road.setEnabled(false);
		return panel;
	}

	/** Change the selected road */
	protected void selectRoad() {
		int row = table.getSelectedRow();
		del_road.setEnabled(row >= 0 && !model.isLastRow(row));
	}
}
