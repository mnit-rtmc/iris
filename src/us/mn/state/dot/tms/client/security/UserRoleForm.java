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
package us.mn.state.dot.tms.client.security;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.sonar.Connection;
import us.mn.state.dot.sonar.Role;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.client.toast.AbstractForm;

/**
 * A form for displaying and editing the users and roles
 *
 * @author Douglas Lau
 */
public class UserRoleForm extends AbstractForm {

	/** Frame title */
	static protected final String TITLE = "Users and Roles";

	/** Tabbed pane */
	protected final JTabbedPane tab = new JTabbedPane(JTabbedPane.TOP);

	/** Table model for users */
	protected UserModel u_model;

	/** Table to hold the user list */
	protected final JTable u_table = new JTable();

	/** Table model for user roles */
	protected UserRoleModel ur_model;

	/** Table to hold the user role list */
	protected final JTable ur_table = new JTable();

	/** Button to delete the selected user */
	protected final JButton del_user = new JButton("Delete User");

	/** Table model for roles */
	protected RoleModel r_model;

	/** Table to hold the role list */
	protected final JTable r_table = new JTable();

	/** Button to delete the selected role */
	protected final JButton del_role = new JButton("Delete Role");

	/** Table model for connections */
	protected ConnectionModel c_model;

	/** Table to hold the connection list */
	protected final JTable c_table = new JTable();

	/** Button to delete the selected connection */
	protected final JButton del_conn = new JButton("Disconnect");

	/** User type cache */
	protected final TypeCache<User> cache;

	/** Role type cache */
	protected final TypeCache<Role> rcache;

	/** Connection type cache */
	protected final TypeCache<Connection> ccache;

	/** Create a new user role form */
	public UserRoleForm(TypeCache<User> uc, TypeCache<Role> rc,
		TypeCache<Connection> cc)
	{
		super(TITLE);
		setHelpPageName("Help.UserRoleForm");
		cache = uc;
		rcache = rc;
		ccache = cc;
	}

	/** Initializze the widgets in the form */
	protected void initialize() {
		ur_model = new UserRoleModel(rcache);
		u_model = new UserModel(cache, ur_model);
		r_model = new RoleModel(rcache);
		c_model = new ConnectionModel(ccache);
		tab.add("Users", createUserPanel());
		tab.add("Roles", createRolePanel());
		tab.add("Connections", createConnectionPanel());
		add(tab);
	}

	/** Close the form */
	protected void close() {
		super.close();
		u_model.dispose();
		ur_model.dispose();
		r_model.dispose();
		c_model.dispose();
	}

	/** Create user panel */
	protected JPanel createUserPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BORDER);
		GridBagConstraints bag = new GridBagConstraints();
		final ListSelectionModel s = u_table.getSelectionModel();
		s.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		s.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if(e.getValueIsAdjusting())
					return;
				selectUser();
			}
		});
		u_table.setModel(u_model);
		u_table.setAutoCreateColumnsFromModel(false);
		u_table.setColumnModel(u_model.createColumnModel());
		JScrollPane upane = new JScrollPane(u_table);
		upane.setPreferredSize(new Dimension(720, 400));
		bag.gridheight = 2;
		panel.add(upane, bag);
		ur_table.setModel(ur_model);
		ur_table.setAutoCreateColumnsFromModel(false);
		ur_table.setColumnModel(ur_model.createColumnModel());
		ur_table.setRowSelectionAllowed(false);
		bag.gridheight = 1;
		bag.insets.left = 6;
		JScrollPane spane = new JScrollPane(ur_table);
		spane.setPreferredSize(new Dimension(140, 300));
		panel.add(spane, bag);
		del_user.setEnabled(false);
		Box box = Box.createHorizontalBox();
		box.add(Box.createHorizontalGlue());
		box.add(del_user);
		box.add(Box.createHorizontalGlue());
		bag.gridx = 1;
		bag.gridy = 1;
		panel.add(box, bag);
		new ActionJob(this, del_user) {
			public void perform() throws Exception {
				int row = s.getMinSelectionIndex();
				if(row >= 0)
					u_model.deleteRow(row);
			}
		};
		return panel;
	}

	/** Change the selected user */
	protected void selectUser() {
		ListSelectionModel s = u_table.getSelectionModel();
		User u = u_model.getProxy(s.getMinSelectionIndex());
		del_user.setEnabled(u != null && u.getRoles().length == 0);
		ur_model.setUser(u);
	}

	/** Create role panel */
	protected JPanel createRolePanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BORDER);
		GridBagConstraints bag = new GridBagConstraints();
		final ListSelectionModel s = r_table.getSelectionModel();
		s.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		s.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if(e.getValueIsAdjusting())
					return;
				selectRole();
			}
		});
		r_table.setModel(r_model);
		r_table.setAutoCreateColumnsFromModel(false);
		r_table.setColumnModel(r_model.createColumnModel());
		JScrollPane pane = new JScrollPane(r_table);
		panel.add(pane, bag);
		del_role.setEnabled(false);
		bag.insets.left = 6;
		panel.add(del_role, bag);
		new ActionJob(this, del_role) {
			public void perform() throws Exception {
				int row = s.getMinSelectionIndex();
				if(row >= 0)
					r_model.deleteRow(row);
			}
		};
		return panel;
	}

	/** Change the selected role */
	protected void selectRole() {
		ListSelectionModel s = r_table.getSelectionModel();
		Role r = r_model.getProxy(s.getMinSelectionIndex());
		del_role.setEnabled(r != null);
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
