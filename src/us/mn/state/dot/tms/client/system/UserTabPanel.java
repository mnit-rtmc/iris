/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2013  Minnesota Department of Transportation
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

import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.ListSelectionModel;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IListSelectionAdapter;
import us.mn.state.dot.tms.client.widget.IPanel;
import us.mn.state.dot.tms.client.widget.ZTable;

/**
 * A panel for editing users.
 *
 * @author Douglas Lau
 */
public class UserTabPanel extends IPanel {

	/** Table model for users */
	private final UserModel u_model;

	/** Table to hold the users */
	private final ZTable u_table = new ZTable();

	/** User panel */
	private final UserPanel user_pnl;

	/** Action to delete the selected user */
	private final IAction del_user = new IAction("user.delete") {
		protected void doActionPerformed(ActionEvent e) {
			ListSelectionModel s = u_table.getSelectionModel();
			int row = s.getMinSelectionIndex();
			if(row >= 0)
				u_model.deleteRow(row);
		}
	};

	/** Create a new user tab panel */
	public UserTabPanel(Session s) {
		u_model = new UserModel(s);
		user_pnl = new UserPanel(s);
		u_table.setModel(u_model);
		u_table.setAutoCreateColumnsFromModel(false);
		u_table.setColumnModel(u_model.createColumnModel());
		u_table.setVisibleRowCount(16);
		add(u_table, Stretch.SOME);
		add(user_pnl, Stretch.LAST);
		add(new JButton(del_user), Stretch.RIGHT);
	}

	/** Initializze the widgets in the form */
	protected void initialize() {
		u_model.initialize();
		user_pnl.initialize();
		ListSelectionModel s = u_table.getSelectionModel();
		s.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		s.addListSelectionListener(new IListSelectionAdapter() {
			@Override
			public void valueChanged() {
				selectUser();
			}
		});
	}

	/** Dispose of the panel */
	@Override public void dispose() {
		u_model.dispose();
		user_pnl.dispose();
		super.dispose();
	}

	/** Change the selected user */
	private void selectUser() {
		ListSelectionModel s = u_table.getSelectionModel();
		User u = u_model.getProxy(s.getMinSelectionIndex());
		user_pnl.setUser(u);
		del_user.setEnabled(u_model.canRemove(u));
	}
}
