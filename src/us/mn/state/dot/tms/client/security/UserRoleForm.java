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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.sonar.Connection;
import us.mn.state.dot.sonar.Name;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.sonar.Privilege;
import us.mn.state.dot.sonar.Role;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.client.toast.AbstractForm;
import us.mn.state.dot.tms.client.widget.ZTable;

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
	protected final ZTable u_table = new ZTable();

	/** Table model for user roles */
	protected UserRoleModel ur_model;

	/** Table to hold the user role list */
	protected final ZTable ur_table = new ZTable();

	/** Button to delete the selected user */
	protected final JButton del_user = new JButton("Delete User");

	/** Table model for roles */
	protected RoleModel r_model;

	/** Table model for privileges */
	protected PrivilegeModel p_model;

	/** Table to hold the role list */
	protected final ZTable r_table = new ZTable();

	/** Table to hold the privilege list */
	protected final ZTable p_table = new ZTable();

	/** Button to delete the selected role */
	protected final JButton del_role = new JButton("Delete Role");

	/** Button to delete the selected privilege */
	protected final JButton del_privilege = new JButton("Delete Privilege");

	/** Table model for connections */
	protected ConnectionModel c_model;

	/** Table to hold the connection list */
	protected final ZTable c_table = new ZTable();

	/** Button to delete the selected connection */
	protected final JButton del_conn = new JButton("Disconnect");

	/** SONAR namespace */
	protected final Namespace namespace;

	/** SONAR User */
	protected final User user;

	/** User type cache */
	protected final TypeCache<User> cache;

	/** Role type cache */
	protected final TypeCache<Role> rcache;

	/** Privilege type cache */
	protected final TypeCache<Privilege> pcache;

	/** Connection type cache */
	protected final TypeCache<Connection> ccache;

	/** Create a new user role form */
	public UserRoleForm(SonarState st, User u) {
		super(TITLE);
		setHelpPageName("Help.UserRoleForm");
		namespace = st.getNamespace();
		user = u;
		cache = st.getUsers();
		rcache = st.getRoles();
		pcache = st.getPrivileges();
		ccache = st.getConnections();
	}

	/** Initializze the widgets in the form */
	protected void initialize() {
		ur_model = new UserRoleModel(rcache, namespace, user);
		u_model = new UserModel(cache, ur_model, namespace, user);
		r_model = new RoleModel(rcache, namespace, user);
		p_model = new PrivilegeModel(pcache, null, namespace, user);
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
		p_model.dispose();
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
		u_table.setVisibleRowCount(16);
		JScrollPane upane = new JScrollPane(u_table);
		bag.gridheight = 2;
		panel.add(upane, bag);
		ur_table.setModel(ur_model);
		ur_table.setAutoCreateColumnsFromModel(false);
		ur_table.setColumnModel(ur_model.createColumnModel());
		ur_table.setRowSelectionAllowed(false);
		ur_table.setVisibleRowCount(12);
		bag.gridheight = 1;
		bag.insets.left = 6;
		JScrollPane spane = new JScrollPane(ur_table);
		panel.add(spane, bag);
		del_user.setEnabled(false);
		del_user.setToolTipText("Delete the selected user, which " +
			"is active only if all roles are deactivated.");
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
		del_user.setEnabled(u_model.canRemove(u));
		ur_model.setSelectedUser(u);
	}

	/** Create role panel */
	protected JPanel createRolePanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BORDER);
		GridBagConstraints bag = new GridBagConstraints();
		bag.insets.left = 4;
		bag.insets.right = 4;
		bag.insets.top = 4;
		bag.insets.bottom = 4;
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
		r_table.setVisibleRowCount(16);
		JScrollPane pane = new JScrollPane(r_table);
		panel.add(pane, bag);
		final ListSelectionModel sp = p_table.getSelectionModel();
		sp.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		sp.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if(e.getValueIsAdjusting())
					return;
				selectPrivilege();
			}
		});
		p_table.setModel(p_model);
		p_table.setAutoCreateColumnsFromModel(false);
		p_table.setColumnModel(PrivilegeModel.createColumnModel());
		p_table.setVisibleRowCount(16);
		pane = new JScrollPane(p_table);
		panel.add(pane, bag);
		del_role.setEnabled(false);
		bag.gridx = 0;
		bag.gridy = 1;
		panel.add(del_role, bag);
		new ActionJob(this, del_role) {
			public void perform() throws Exception {
				int row = s.getMinSelectionIndex();
				if(row >= 0)
					r_model.deleteRow(row);
			}
		};
		del_privilege.setEnabled(false);
		bag.gridx = 1;
		panel.add(del_privilege, bag);
		new ActionJob(this, del_privilege) {
			public void perform() throws Exception {
				int row = sp.getMinSelectionIndex();
				if(row >= 0)
					p_model.deleteRow(row);
			}
		};
		return panel;
	}

	/** Change the selected role */
	protected void selectRole() {
		ListSelectionModel s = r_table.getSelectionModel();
		Role r = r_model.getProxy(s.getMinSelectionIndex());
		del_role.setEnabled(r_model.canRemove(r));
		p_table.clearSelection();
		final PrivilegeModel pm = p_model;
		p_model = new PrivilegeModel(pcache, r, namespace, user);
		p_table.setModel(p_model);
		pm.dispose();
	}

	/** Select a privilege */
	protected void selectPrivilege() {
		Privilege p = getSelectedPrivilege();
		del_privilege.setEnabled(p_model.canRemove(p));
	}

	/** Get the selected privilege */
	protected Privilege getSelectedPrivilege() {
		final PrivilegeModel pm = p_model;	// Avoid race
		ListSelectionModel s = p_table.getSelectionModel();
		return pm.getProxy(s.getMinSelectionIndex());
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

	/** Check if the user can remove the specified name */
	protected boolean canRemove(Name name) {
		return namespace.canRemove(user, name);
	}
}
