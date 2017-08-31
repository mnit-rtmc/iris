/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012-2017  Minnesota Department of Transportation
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
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import us.mn.state.dot.sonar.Role;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.client.EditModeListener;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyListModel;
import us.mn.state.dot.tms.client.proxy.ProxyView;
import us.mn.state.dot.tms.client.proxy.ProxyWatcher;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IComboBoxModel;
import us.mn.state.dot.tms.client.widget.IPanel;
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
		protected final void doActionPerformed(ActionEvent e) {
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
	private final JTextField f_name_txt = new JTextField(24);

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
	private final JTextField dn_txt = new JTextField(32);

	/** Role list model */
	private final ProxyListModel<Role> r_list;

	/** Role combo model */
	private final IComboBoxModel<Role> role_mdl;

	/** Role combo box */
	private final JComboBox<Role> role_cbx;

	/** Role action */
	private final UAction role_act = new UAction("role") {
		protected void do_perform(User u) {
			u.setRole(role_mdl.getSelectedProxy());
		}
	};

	/** Enabled check box */
	private final JCheckBox enabled_chk = new JCheckBox(new UAction(null) {
		protected void do_perform(User u) {
			u.setEnabled(enabled_chk.isSelected());
		}
	});

	/** Edit mode listener */
	private final EditModeListener edit_lsnr = new EditModeListener() {
		public void editModeChanged() {
			updateEditMode();
		}
	};

	/** Create the user panel */
	public UserPanel(Session s) {
		session = s;
		TypeCache<User> cache = s.getSonarState().getUsers();
		watcher = new ProxyWatcher<User>(cache, this, false);
		r_list = new ProxyListModel<Role>(s.getSonarState().getRoles());
		role_mdl = new IComboBoxModel<Role>(r_list);
		role_cbx = new JComboBox<Role>(role_mdl);
	}

	/** Initialize the panel */
	@Override
	public void initialize() {
		super.initialize();
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
		session.addEditModeListener(edit_lsnr);
		clear();
	}

	/** Dispose of the panel */
	@Override
	public void dispose() {
		session.removeEditModeListener(edit_lsnr);
		r_list.dispose();
		watcher.dispose();
		super.dispose();
	}

	/** Create the jobs */
	private void createJobs() {
		f_name_txt.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				String n = f_name_txt.getText();
				setFullName(n.trim());
			}
		});
		dn_txt.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				setDn(dn_txt.getText().trim());
			}
		});
		role_cbx.setAction(role_act);
	}

	/** Set the user's full name */
	private void setFullName(String f) {
		User u = user;
		if (u != null)
			u.setFullName(f);
	}

	/** Set the user's dn (distinguished name) */
	private void setDn(String n) {
		User u = user;
		if (u != null)
			u.setDn(n);
	}

	/** Update the edit mode */
	private void updateEditMode() {
		f_name_txt.setEnabled(canWrite("fullName"));
		boolean cu = canWrite("password");
		passwd_txt.setEnabled(cu);
		change_pwd.setEnabled(cu);
		dn_txt.setEnabled(canWrite("dn"));
		role_act.setEnabled(canWrite("role"));
		enabled_chk.setEnabled(canWrite("enabled"));
	}

	/** Test if the user can update an attribute */
	private boolean canWrite(String a) {
		return session.canWrite(user, a);
	}

	/** Update one attribute (from ProxyView). */
	@Override
	public void update(User u, String a) {
		if (a == null) {
			user = u;
			updateEditMode();
		}
		if (a == null || a.equals("fullName"))
			f_name_txt.setText(u.getFullName());
		if (a == null || a.equals("password"))
			passwd_txt.setText("");
		if (a == null || a.equals("dn"))
			dn_txt.setText(u.getDn());
		if (a == null || a.equals("role"))
			role_mdl.setSelectedItem(u.getRole());
		if (a == null || a.equals("enabled"))
			enabled_chk.setSelected(u.getEnabled());
		repaint();
	}

	/** Clear all attributes (from ProxyView). */
	@Override
	public void clear() {
		user = null;
		updateEditMode();
		f_name_txt.setText("");
		passwd_txt.setText("");
		dn_txt.setText("");
		role_mdl.setSelectedItem(null);
		enabled_chk.setSelected(false);
	}
}
