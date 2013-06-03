/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012-2013  Minnesota Department of Transportation
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
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sched.FocusLostJob;
import us.mn.state.dot.sonar.Role;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.sonar.client.TypeCache;
import static us.mn.state.dot.tms.client.IrisClient.WORKER;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyListModel;
import us.mn.state.dot.tms.client.proxy.ProxyView;
import us.mn.state.dot.tms.client.proxy.ProxyWatcher;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IPanel;
import us.mn.state.dot.tms.client.widget.WrapperComboBoxModel;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A panel for editing the properties of a user.
 *
 * @author Douglas Lau
 */
public class UserPanel extends IPanel implements ProxyView<User> {

	/** User action */
	abstract private class UAction extends IAction {
		protected UAction(String text_id) {
			super(text_id);
		}
		@Override protected final void do_perform() {
			User u = user;
			if(u != null)
				do_perform(u);
		}
		abstract protected void do_perform(User u);
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
	private final JTextField f_name_txt = new JTextField(32);

	/** Password entry component */
	private final JPasswordField passwd_txt = new JPasswordField(16);

	/** Action to change the password */
	private final UAction change_pwd = new UAction(
		"user.password.change")
	{
		@Override protected void do_perform(User u) {
			String p = new String(passwd_txt.getPassword()).trim();
			passwd_txt.setText("");
			u.setPassword(p);
		}
	};

	/** Dn (distinguished name) field */
	private final JTextField dn_txt = new JTextField(64);

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
		session = s;
		TypeCache<User> cache = s.getSonarState().getUsers();
		watcher = new ProxyWatcher<User>(s, this, cache, false);
		r_list = new ProxyListModel<Role>(s.getSonarState().getRoles());
		role_mdl = new WrapperComboBoxModel(r_list, true);
		role_cbx = new JComboBox(role_mdl);
	}

	/** Initialize the panel */
	public void initialize() {
		add("user.name.full");
		add(f_name_txt, Stretch.LAST);
		add("user.password");
		add(passwd_txt);
		add(new JButton(change_pwd), Stretch.LEFT);
		add("user.dn");
		add(dn_txt, Stretch.LAST);
		add("role");
		add(role_cbx, Stretch.LAST);
		add("user.enabled");
		add(enabled_chk, Stretch.LAST);
		createJobs();
		watcher.initialize();
		r_list.initialize();
		doClear();
	}

	/** Dispose of the panel */
	@Override public void dispose() {
		r_list.dispose();
		watcher.dispose();
		super.dispose();
	}

	/** Create the jobs */
	private void createJobs() {
		f_name_txt.addFocusListener(new FocusLostJob(WORKER) {
			public void perform() {
				String n = f_name_txt.getText();
				setFullName(n.trim());
			}
		});
		dn_txt.addFocusListener(new FocusLostJob(WORKER) {
			@Override public void perform() {
				setDn(dn_txt.getText().trim());
			}
		});
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
	@Override public final void update(final User u, final String a) {
		// Serialize on WORKER thread
		WORKER.addJob(new Job() {
			public void perform() {
				doUpdate(u, a);
			}
		});
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
	@Override public final void clear() {
		// Serialize on WORKER thread
		WORKER.addJob(new Job() {
			public void perform() {
				doClear();
			}
		});
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
