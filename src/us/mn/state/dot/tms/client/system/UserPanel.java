/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012  Minnesota Department of Transportation
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
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import us.mn.state.dot.sched.AbstractJob;
import us.mn.state.dot.sched.FocusJob;
import us.mn.state.dot.sonar.Role;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyListModel;
import us.mn.state.dot.tms.client.proxy.ProxyView;
import us.mn.state.dot.tms.client.proxy.ProxyWatcher;
import us.mn.state.dot.tms.client.widget.FormPanel;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.WrapperComboBoxModel;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A panel for editing the properties of a user.
 *
 * @author Douglas Lau
 */
public class UserPanel extends FormPanel implements ProxyView<User> {

	/** User action */
	abstract private class UAction extends IAction {
		protected UAction(String text_id) {
			super(text_id);
		}
		protected final void do_perform() {
			User u = user;
			if(u != null)
				do_perform(u);
		}
		abstract void do_perform(User u);
	}

	/** User session */
	private final Session session;

	/** Proxy watcher */
	private final ProxyWatcher<User> watcher;

	/** User being edited */
	private User user;

	/** Set the user */
	public void setUser(User u) {
		watcher.setProxy(u);
	}

	/** Full name text field */
	private final JTextField f_name_txt = new JTextField(30);

	/** Password entry component */
	private final JPasswordField passwd_txt = new JPasswordField(16);

	/** Action to change the password */
	private final UAction change_pwd = new UAction(
		"user.password.change")
	{
		protected void do_perform(User u) {
			String p = new String(passwd_txt.getPassword()).trim();
			passwd_txt.setText("");
			u.setPassword(p);
		}
	};

	/** Dn (distinguished name) field */
	private final JTextField dn_txt = new JTextField(48);

	/** Role list model */
	private final ProxyListModel<Role> r_list;

	/** Role combo model */
	private final WrapperComboBoxModel role_mdl;

	/** Role combo box */
	private final JComboBox role_cbx;

	/** Role action */
	private final UAction role_action = new UAction("role") {
		protected void do_perform(User u) {
			Object item = role_cbx.getSelectedItem();
			if(item instanceof Role)
				u.setRole((Role)item);
			else
				u.setRole(null);
		}
	};

	/** Enabled check box */
	private final JCheckBox enabled_chk = new JCheckBox(new UAction(null) {
		protected void do_perform(User u) {
			u.setEnabled(enabled_chk.isSelected());
		}
	});

	/** Create the user panel */
	public UserPanel(Session s) {
		super(false);
		session = s;
		TypeCache<User> cache = s.getSonarState().getUsers();
		watcher = new ProxyWatcher<User>(s, this, cache, false);
		r_list = new ProxyListModel<Role>(s.getSonarState().getRoles());
		role_mdl = new WrapperComboBoxModel(r_list, true);
		role_cbx = new JComboBox(role_mdl);
	}

	/** Initialize the panel */
	public void initialize() {
		add(new JLabel(I18N.get("user.name.full")));
		setWidth(3);
		add(f_name_txt);
		addRow(new JLabel());
		add(I18N.get("user.password"), passwd_txt);
		add(new JLabel());
		add(new JButton(change_pwd));
		addRow(new JLabel());
		addRow(I18N.get("user.dn"), dn_txt);
		add(I18N.get("role"), role_cbx);
		addRow(new JLabel());
		addRow(I18N.get("user.enabled"), enabled_chk);
		createJobs();
		watcher.initialize();
		r_list.initialize();
		doClear();
	}

	/** Dispose of the panel */
	public void dispose() {
		r_list.dispose();
		watcher.dispose();
		super.dispose();
	}

	/** Create the jobs */
	private void createJobs() {
		new FocusJob(f_name_txt) {
			public void perform() {
				if(wasLost()) {
					String n = f_name_txt.getText();
					setFullName(n.trim());
				}
			}
		};
		new FocusJob(dn_txt) {
			public void perform() {
				if(wasLost())
					setDn(dn_txt.getText().trim());
			}
		};
		role_cbx.setAction(role_action);
	}

	/** Set the user's full name */
	private void setFullName(String f) {
		User u = user;
		if(u != null)
			u.setFullName(f);
	}

	/** Set the user's dn (distinguished name) */
	private void setDn(String n) {
		User u = user;
		if(u != null)
			u.setDn(n);
	}

	/** Update one attribute */
	public final void update(final User u, final String a) {
		// Serialize on WORKER thread
		new AbstractJob() {
			public void perform() {
				doUpdate(u, a);
			}
		}.addToScheduler();
	}

	/** Update one attribute */
	private void doUpdate(User u, String a) {
		if(a == null)
			user = u;
		if(a == null || a.equals("fullName")) {
			f_name_txt.setEnabled(watcher.canUpdate(u, "fullName"));
			f_name_txt.setText(u.getFullName());
		}
		if(a == null || a.equals("password")) {
			boolean cu = watcher.canUpdate(u, "password");
			passwd_txt.setEnabled(cu);
			passwd_txt.setText("");
			change_pwd.setEnabled(cu);
		}
		if(a == null || a.equals("dn")) {
			dn_txt.setEnabled(watcher.canUpdate(u, "dn"));
			dn_txt.setText(u.getDn());
		}
		if(a == null || a.equals("role")) {
			role_cbx.setEnabled(watcher.canUpdate(u, "role"));
			role_cbx.setSelectedItem(u.getRole());
		}
		if(a == null || a.equals("enabled")) {
			enabled_chk.setEnabled(watcher.canUpdate(u, "enabled"));
			enabled_chk.setSelected(u.getEnabled());
		}
		repaint();
	}

	/** Clear all attributes */
	public final void clear() {
		// Serialize on WORKER thread
		new AbstractJob() {
			public void perform() {
				doClear();
			}
		}.addToScheduler();
	}

	/** Clear all attributes */
	private void doClear() {
		user = null;
		f_name_txt.setEnabled(false);
		f_name_txt.setText("");
		passwd_txt.setEnabled(false);
		passwd_txt.setText("");
		change_pwd.setEnabled(false);
		dn_txt.setEnabled(false);
		dn_txt.setText("");
		role_cbx.setEnabled(false);
		role_cbx.setSelectedIndex(0);
		enabled_chk.setEnabled(false);
		enabled_chk.setSelected(false);
	}
}
