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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sonar.Capability;
import us.mn.state.dot.sonar.Connection;
import us.mn.state.dot.sonar.Privilege;
import us.mn.state.dot.sonar.Role;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.toast.AbstractForm;
import us.mn.state.dot.tms.client.widget.ZTable;

/**
 * A form for displaying and editing the users and roles
 *
 * @author Douglas Lau
 */
public class UserRoleForm extends AbstractForm {

	/** Check if the user is permitted to use the form */
	static public boolean isPermitted(Session s) {
		return s.canUpdate(User.SONAR_TYPE) ||
		       s.canUpdate(Role.SONAR_TYPE) ||
		       s.canUpdate(Capability.SONAR_TYPE) ||
		       s.canUpdate(Privilege.SONAR_TYPE);
	}

	/** Frame title */
	static protected final String TITLE = "Users and Roles";

	/** Tabbed pane */
	protected final JTabbedPane tab = new JTabbedPane(JTabbedPane.TOP);

	/** User panel */
	protected final UserPanel u_panel;

	/** Role panel */
	protected final RolePanel r_panel;

	/** Capability panel */
	protected final CapabilityPanel cap_panel;

	/** Table model for connections */
	protected final ConnectionModel c_model;

	/** Table to hold the connection list */
	protected final ZTable c_table = new ZTable();

	/** Button to delete the selected connection */
	protected final JButton del_conn = new JButton("Disconnect");

	/** User session */
	protected final Session session;

	/** Create a new user role form */
	public UserRoleForm(Session s) {
		super(TITLE);
		session = s;
		setHelpPageName("Help.UserRoleForm");
		u_panel = new UserPanel(s);
		r_panel = new RolePanel(s);
		cap_panel = new CapabilityPanel(s);
		c_model = new ConnectionModel(session);
	}

	/** Initializze the widgets in the form */
	protected void initialize() {
		u_panel.initialize();
		r_panel.initialize();
		cap_panel.initialize();
		c_model.initialize();
		tab.add("Users", u_panel);
		tab.add("Roles", r_panel);
		tab.add("Capabilities", cap_panel);
		tab.add("Connections", createConnectionPanel());
		add(tab);
	}

	/** Close the form */
	protected void close() {
		super.close();
		u_panel.dispose();
		r_panel.dispose();
		cap_panel.dispose();
		c_model.dispose();
	}

	/** Create connection panel */
	protected JPanel createConnectionPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BORDER);
		GridBagConstraints bag = new GridBagConstraints();
		final ListSelectionModel s = c_table.getSelectionModel();
		s.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		s.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if(e.getValueIsAdjusting())
					return;
				selectConnection();
			}
		});
		c_table.setModel(c_model);
		c_table.setAutoCreateColumnsFromModel(false);
		c_table.setColumnModel(c_model.createColumnModel());
		c_table.setVisibleRowCount(16);
		JScrollPane pane = new JScrollPane(c_table);
		panel.add(pane, bag);
		if(false) {
			del_conn.setEnabled(false);
			bag.insets.left = 6;
			panel.add(del_conn, bag);
			new ActionJob(this, del_conn) {
				public void perform() throws Exception {
					int row = s.getMinSelectionIndex();
					if(row >= 0)
						c_model.deleteRow(row);
				}
			};
		}
		return panel;
	}

	/** Change the selected connection */
	protected void selectConnection() {
		ListSelectionModel s = c_table.getSelectionModel();
		Connection c = c_model.getProxy(s.getMinSelectionIndex());
		del_conn.setEnabled(c != null);
	}
}
