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

	/** Table model for roles */
	protected final RoleModel r_model;

	/** Table to hold the role list */
	protected final ZTable r_table = new ZTable();

	/** Table model for role capabilities */
	protected final RoleCapabilityModel rc_model;

	/** Table to hold the role capability list */
	protected final ZTable rc_table = new ZTable();

	/** Button to delete the selected role */
	protected final JButton del_role = new JButton("Delete Role");

	/** Table model for capabilities */
	protected final CapabilityModel cap_model;

	/** Table model for privileges */
	protected PrivilegeModel p_model;

	/** Table to hold the capability list */
	protected final ZTable cap_table = new ZTable();

	/** Table to hold the privilege list */
	protected final ZTable p_table = new ZTable();

	/** Button to delete the selected capability */
	protected final JButton del_capability =
		new JButton("Delete Capability");

	/** Button to delete the selected privilege */
	protected final JButton del_privilege = new JButton("Delete Privilege");

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
		rc_model = new RoleCapabilityModel(s);
		r_model = new RoleModel(s, rc_model);
		cap_model = new CapabilityModel(s);
		p_model = new PrivilegeModel(session, null);
		c_model = new ConnectionModel(session);
	}

	/** Initializze the widgets in the form */
	protected void initialize() {
		u_panel.initialize();
		rc_model.initialize();
		r_model.initialize();
		cap_model.initialize();
		p_model.initialize();
		c_model.initialize();
		tab.add("Users", u_panel);
		tab.add("Roles", createRolePanel());
		tab.add("Capabilities", createCapabilityPanel());
		tab.add("Connections", createConnectionPanel());
		add(tab);
	}

	/** Close the form */
	protected void close() {
		super.close();
		u_panel.dispose();
		rc_model.dispose();
		r_model.dispose();
		cap_model.dispose();
		p_model.dispose();
		c_model.dispose();
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
				if(!e.getValueIsAdjusting())
					selectRole();
			}
		});
		r_table.setModel(r_model);
		r_table.setAutoCreateColumnsFromModel(false);
		r_table.setColumnModel(r_model.createColumnModel());
		r_table.setVisibleRowCount(16);
		JScrollPane rpane = new JScrollPane(r_table);
		bag.gridheight = 2;
		panel.add(rpane, bag);
		rc_table.setModel(rc_model);
		rc_table.setAutoCreateColumnsFromModel(false);
		rc_table.setColumnModel(rc_model.createColumnModel());
		rc_table.setRowSelectionAllowed(false);
		rc_table.setVisibleRowCount(12);
		bag.gridheight = 1;
		bag.insets.left = 6;
		JScrollPane spane = new JScrollPane(rc_table);
		panel.add(spane, bag);
		del_role.setEnabled(false);
		del_role.setToolTipText("Delete the selected role");
		Box box = Box.createHorizontalBox();
		box.add(Box.createHorizontalGlue());
		box.add(del_role);
		box.add(Box.createHorizontalGlue());
		bag.gridx = 1;
		bag.gridy = 1;
		panel.add(box, bag);
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
		del_role.setEnabled(r_model.canRemove(r));
		rc_model.setSelectedRole(r);
	}

	/** Create capability panel */
	protected JPanel createCapabilityPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BORDER);
		GridBagConstraints bag = new GridBagConstraints();
		bag.insets.left = 4;
		bag.insets.right = 4;
		bag.insets.top = 4;
		bag.insets.bottom = 4;
		final ListSelectionModel s = cap_table.getSelectionModel();
		s.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		s.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if(!e.getValueIsAdjusting())
					selectCapability();
			}
		});
		cap_table.setModel(cap_model);
		cap_table.setAutoCreateColumnsFromModel(false);
		cap_table.setColumnModel(cap_model.createColumnModel());
		cap_table.setVisibleRowCount(16);
		JScrollPane pane = new JScrollPane(cap_table);
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
		p_table.setColumnModel(p_model.createColumnModel());
		p_table.setVisibleRowCount(16);
		pane = new JScrollPane(p_table);
		panel.add(pane, bag);
		del_capability.setEnabled(false);
		bag.gridx = 0;
		bag.gridy = 1;
		panel.add(del_capability, bag);
		new ActionJob(this, del_capability) {
			public void perform() throws Exception {
				int row = s.getMinSelectionIndex();
				if(row >= 0)
					cap_model.deleteRow(row);
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

	/** Change the selected capability */
	protected void selectCapability() {
		ListSelectionModel s = cap_table.getSelectionModel();
		Capability c = cap_model.getProxy(s.getMinSelectionIndex());
		del_capability.setEnabled(cap_model.canRemove(c));
		p_table.clearSelection();
		final PrivilegeModel pm = p_model;
		p_model = new PrivilegeModel(session, c);
		p_model.initialize();
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
}
