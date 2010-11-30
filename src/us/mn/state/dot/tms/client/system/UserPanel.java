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
package us.mn.state.dot.tms.client.system;

import javax.swing.JButton;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.toast.FormPanel;
import us.mn.state.dot.tms.client.widget.ZTable;

/**
 * A panel for editing users.
 *
 * @author Douglas Lau
 */
public class UserPanel extends FormPanel {

	/** Table model for users */
	protected final UserModel u_model;

	/** Table to hold the users */
	protected final ZTable u_table = new ZTable();

	/** Button to delete the selected user */
	protected final JButton del_user = new JButton("Delete User");

	/** Create a new user panel */
	public UserPanel(Session s) {
		super(true);
		u_model = new UserModel(s);
		u_table.setModel(u_model);
		u_table.setAutoCreateColumnsFromModel(false);
		u_table.setColumnModel(u_model.createColumnModel());
		u_table.setVisibleRowCount(16);
		addRow(u_table);
		del_user.setEnabled(false);
		del_user.setToolTipText("Delete the selected user");
		addRow(del_user);
	}

	/** Initializze the widgets in the form */
	protected void initialize() {
		u_model.initialize();
		final ListSelectionModel s = u_table.getSelectionModel();
		s.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		s.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if(!e.getValueIsAdjusting())
					selectUser();
			}
		});
		new ActionJob(this, del_user) {
			public void perform() throws Exception {
				int row = s.getMinSelectionIndex();
				if(row >= 0)
					u_model.deleteRow(row);
			}
		};
	}

	/** Dispose of the panel */
	public void dispose() {
		u_model.dispose();
		super.dispose();
	}

	/** Change the selected user */
	protected void selectUser() {
		ListSelectionModel s = u_table.getSelectionModel();
		User u = u_model.getProxy(s.getMinSelectionIndex());
		del_user.setEnabled(u_model.canRemove(u));
	}
}
