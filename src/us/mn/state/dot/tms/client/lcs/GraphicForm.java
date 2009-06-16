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
import us.mn.state.dot.tms.Graphic;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.toast.AbstractForm;
import us.mn.state.dot.tms.client.toast.FormPanel;
import us.mn.state.dot.tms.client.widget.ZTable;

/**
 * A form for displaying a table of graphics.
 *
 * @author Douglas Lau
 */
public class GraphicForm extends AbstractForm {

	/** Frame title */
	static protected final String TITLE = "Graphics";

	/** Table model for graphics */
	protected GraphicModel model;

	/** Table to hold the Graphic list */
	protected final ZTable table = new ZTable();

	/** Button to create a new graphic */
	protected final JButton createBtn = new JButton("Create");

	/** Button to delete the selected proxy */
	protected final JButton deleteBtn = new JButton("Delete");

	/** User session */
	protected final Session session;

	/** Type cache */
	protected final TypeCache<Graphic> cache;

	/** Create a new graphic form */
	public GraphicForm(Session s, TypeCache<Graphic> c) {
		super(TITLE);
		session = s;
		cache = c;
	}

	/** Initializze the widgets in the form */
	protected void initialize() {
		model = new GraphicModel(cache);
		add(createGraphicPanel());
		table.setVisibleRowCount(6);
	}

	/** Dispose of the form */
	protected void dispose() {
		model.dispose();
	}

	/** Create graphic panel */
	protected JPanel createGraphicPanel() {
		final ListSelectionModel s = table.getSelectionModel();
		s.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		new ListSelectionJob(this, s) {
			public void perform() {
				if(!event.getValueIsAdjusting())
					selectProxy();
			}
		};
		new ActionJob(this, createBtn) {
			public void perform() throws Exception {
				createGraphic();
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
		table.setRowHeight(64);
		table.setModel(model);
		table.setAutoCreateColumnsFromModel(false);
		table.setColumnModel(model.createColumnModel());
		panel.addRow(table);
		panel.add(createBtn);
		panel.addRow(deleteBtn);
		deleteBtn.setEnabled(false);
		return panel;
	}

	/** Change the selected proxy */
	protected void selectProxy() {
		int row = table.getSelectedRow();
		deleteBtn.setEnabled(row >= 0 && !model.isLastRow(row));
	}

	/** Create a new graphic */
	protected void createGraphic() {
		// FIXME: select file
	}
}
