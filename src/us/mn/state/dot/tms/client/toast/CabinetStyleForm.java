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
package us.mn.state.dot.tms.client.toast;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sched.ListSelectionJob;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.CabinetStyle;
import us.mn.state.dot.tms.client.widget.ZTable;

/**
 * A form for displaying and editing cabinet styles
 *
 * @author Douglas Lau
 */
public class CabinetStyleForm extends AbstractForm {

	/** Frame title */
	static protected final String TITLE = "Cabinet Styles";

	/** Table model for cabinet styles */
	protected final CabinetStyleModel model;

	/** Table to hold the cabinet style list */
	protected final ZTable table = new ZTable();

	/** Button to delete the selected cabinet style */
	protected final JButton del_button = new JButton("Delete");

	/** Create a new cabinet style form */
	public CabinetStyleForm(TypeCache<CabinetStyle> c, Namespace ns,
		User u)
	{
		super(TITLE);
		model = new CabinetStyleModel(c, ns, u);
	}

	/** Initializze the widgets in the form */
	protected void initialize() {
		model.initialize();
		add(createCabinetStylePanel());
	}

	/** Dispose of the form */
	protected void dispose() {
		model.dispose();
	}

	/** Create cabinet style panel */
	protected JPanel createCabinetStylePanel() {
		table.setModel(model);
		table.setAutoCreateColumnsFromModel(false);
		table.setColumnModel(model.createColumnModel());
		table.setVisibleRowCount(12);
		final ListSelectionModel s = table.getSelectionModel();
		s.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		new ListSelectionJob(this, s) {
			public void perform() {
				if(!event.getValueIsAdjusting())
					selectCabinetStyle();
			}
		};
		new ActionJob(this, del_button) {
			public void perform() throws Exception {
				int row = s.getMinSelectionIndex();
				if(row >= 0)
					model.deleteRow(row);
			}
		};
		FormPanel panel = new FormPanel(true);
		panel.addRow(table);
		panel.addRow(del_button);
		del_button.setEnabled(false);
		return panel;
	}

	/** Change the selected cabinet style */
	protected void selectCabinetStyle() {
		CabinetStyle cs = model.getProxy(table.getSelectedRow());
		del_button.setEnabled(model.canRemove(cs));
	}
}
