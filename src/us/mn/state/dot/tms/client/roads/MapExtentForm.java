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
package us.mn.state.dot.tms.client.roads;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sched.ListSelectionJob;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.MapExtent;
import us.mn.state.dot.tms.client.toast.AbstractForm;
import us.mn.state.dot.tms.client.toast.FormPanel;
import us.mn.state.dot.tms.client.widget.ZTable;

/**
 * A form for editing map extents
 *
 * @author Douglas Lau
 */
public class MapExtentForm extends AbstractForm {

	/** Frame title */
	static protected final String TITLE = "Map Extents";

	/** Table model */
	protected final MapExtentModel model;

	/** Table to hold the map extents */
	protected final ZTable table = new ZTable();

	/** Button to delete the selected map extent */
	protected final JButton del_btn = new JButton("Delete");

	/** Create a new map extent form */
	public MapExtentForm(TypeCache<MapExtent> c, Namespace ns, User u) {
		super(TITLE);
		model = new MapExtentModel(c, ns, u);
	}

	/** Initializze the widgets in the form */
	protected void initialize() {
		model.initialize();
		add(createMapExtentPanel());
	}

	/** Dispose of the form */
	protected void dispose() {
		model.dispose();
	}

	/** Create map extent panel */
	protected JPanel createMapExtentPanel() {
		final ListSelectionModel s = table.getSelectionModel();
		s.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		new ListSelectionJob(this, s) {
			public void perform() {
				if(!event.getValueIsAdjusting())
					selectMapExtent();
			}
		};
		new ActionJob(this, del_btn) {
			public void perform() throws Exception {
				int row = s.getMinSelectionIndex();
				if(row >= 0)
					model.deleteRow(row);
			}
		};
		table.setModel(model);
		table.setAutoCreateColumnsFromModel(false);
		table.setColumnModel(MapExtentModel.createColumnModel());
		table.setVisibleRowCount(20);
		FormPanel panel = new FormPanel(true);
		panel.addRow(table);
		panel.addRow(del_btn);
		del_btn.setEnabled(false);
		return panel;
	}

	/** Change the selected map extent */
	protected void selectMapExtent() {
		MapExtent me = model.getProxy(table.getSelectedRow());
		del_btn.setEnabled(model.canRemove(me));
	}
}
