/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2014  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client;

import java.awt.event.ActionEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import us.mn.state.dot.tms.client.system.ChangePasswordForm;
import us.mn.state.dot.tms.client.system.LoginForm;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.SmartDesktop;
import us.mn.state.dot.tms.utils.I18N;

/**
 * The sessoin menu contains menu items for logging in, logging out and exiting
 * the IRIS client.
 *
 * @author Douglas Lau
 */
public class SessionMenu extends JMenu {

	/** Log in action */
	private final IAction log_in = new IAction("connection.login") {
		protected void doActionPerformed(ActionEvent e) {
			showLoginForm();
		}
	};

	/** Logout action */
	private final IAction log_out = new IAction("connection.logout") {
		protected void doActionPerformed(ActionEvent e) {
			client.logout();
		}
	};

	/** Change password action */
	private final IAction pwd_change = new IAction("user.password.change") {
		protected void doActionPerformed(ActionEvent e) {
			Session s = client.getSession();
			if (s != null)
				desktop.show(new ChangePasswordForm(s));
		}
	};

	/** IRIS client */
	private final IrisClient client;

	/** Desktop pane */
	private final SmartDesktop desktop;

	/** Create a new session menu */
	public SessionMenu(final IrisClient ic) {
		super(I18N.get("session"));
		client = ic;
		desktop = client.getDesktop();
		setLoggedIn(false);
		add(new JMenuItem(log_in));
		add(new JMenuItem(log_out));
		add(new JSeparator());
		add(new JMenuItem(pwd_change));
		add(new JSeparator());
		add(new JMenuItem(new IAction("session.exit") {
			protected void doActionPerformed(ActionEvent e) {
				client.quit();
			}
		}));
	}

	/** Show the login form */
	public void showLoginForm() {
		Session s = client.getSession();
		if (s == null)
			desktop.show(new LoginForm(client, desktop));
	}

	/** Set the logged-in status */
	public void setLoggedIn(boolean in) {
		log_in.setEnabled(!in);
		log_out.setEnabled(in);
		pwd_change.setEnabled(in);
	}
}
