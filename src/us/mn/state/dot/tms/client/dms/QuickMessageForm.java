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
package us.mn.state.dot.tms.client.dms;

import javax.swing.JButton;
import javax.swing.ListSelectionModel;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sched.ListSelectionJob;
import us.mn.state.dot.tms.QuickMessage;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.toast.AbstractForm;
import us.mn.state.dot.tms.client.toast.FormPanel;
import us.mn.state.dot.tms.client.widget.ZTable;

/**
 * A form for displaying and editing quick messages.
 * @see QuickMessage, QuickMessageImpl
 *
 * @author Michael Darter
 * @author Doug Lau
 */
public class QuickMessageForm extends AbstractForm {

	/** Table row height */
	static protected final int ROW_HEIGHT = 20;

	/** Table model */
	protected final QuickMessageTableModel model;

	/** Table */
	protected final ZTable table = new ZTable();

	/** Button to delete the selected row */
	protected final JButton del_btn = new JButton("Delete");

	/** Create a new quick message form.
	 * @param session Session. */
	public QuickMessageForm(Session session) {
		super("Quick Message Form");
		model = new QuickMessageTableModel(session);
	}

	/** Initialize the form */
	protected void initialize() {
		add(createMessagePanel());
		del_btn.setEnabled(false);
	}

	/** Create the message panel */
	protected FormPanel createMessagePanel() {
		final ListSelectionModel s = table.getSelectionModel();
		s.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		new ListSelectionJob(this, s) {
			public void perform() {
				selectRow();
			}
		};
		table.setAutoCreateColumnsFromModel(false);
		table.setColumnModel(model.createColumnModel());
		table.setModel(model);
		table.setRowHeight(ROW_HEIGHT);
		table.setVisibleRowCount(12);
		new ActionJob(this, del_btn) {
			public void perform() throws Exception {
				int row = s.getMinSelectionIndex();
				if(row >= 0)
					model.deleteRow(row);
			}
		};
		FormPanel panel = new FormPanel(true);
		panel.addRow(table);
		panel.addRow(del_btn);
		return panel;
	}

	/** Dispose of the quick message form */
	public void dispose() {
		model.dispose();
	}

	/** Handle row selection change */
	protected void selectRow() {
		QuickMessage proxy = model.getProxy(table.getSelectedRow());
		del_btn.setEnabled(proxy == null ? false : 
			model.canRemove(proxy.getName()));
	}
}

