/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2008  Minnesota Department of Transportation
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

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import javax.naming.AuthenticationException;
import javax.swing.JButton;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sonar.ConfigurationError;
import us.mn.state.dot.sonar.Role;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.sonar.client.ShowHandler;
import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.client.toast.AbstractForm;
import us.mn.state.dot.tms.client.toast.FormPanel;
import us.mn.state.dot.tms.client.toast.SmartDesktop;
import us.mn.state.dot.tms.utils.ExceptionDialog;

/**
 * The UserManager is used to display a login prompt and to verify that the
 * user logging in has appropriate rights to use the client.  Checks for
 * administrative privaleges.  Also used to log out of the connection when
 * exiting the client.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public class UserManager {

	/** Smart desktp */
	protected final SmartDesktop desktop;

	/** Client properties */
	protected final Properties props;

	/** Registered LoginListeners */
	protected final List<LoginListener> listeners =
		new LinkedList<LoginListener>();

	/** Add a LoginListener */
	public void addLoginListener(LoginListener l) {
		listeners.add(l);
	}

	/** Remove a LoginListener */
	public void removeLoginListener(LoginListener l) {
		listeners.remove(l);
	}

	/** Sonar state */
	protected SonarState state = null;

	/** Get the SONAR state */
	public SonarState getSonarState() {
		return state;
	}

	/** Information of the currently logged in user */
	protected IrisUser user = null;

	/** Create a new user manager */
	public UserManager(SmartDesktop d, Properties p) {
		desktop = d;
		props = p;
	}

	/** Test if a user is logged in */
	public boolean isLoggedIn() {
		return user != null;
	}

	/** Get the user that is currently logged in */
	public IrisUser getUser() {
		return user;
	}

	/** Log out the current user and calls quit on the client */
	public void quit() {
		logout();
		System.exit(0);
	}

	/** Fire a login event to all listeners */
	protected void fireLogin() throws Exception {
		for(LoginListener l: listeners)
			l.login();
	}

	/** Fire a logout event to all listeners */
	protected void fireLogout() {
		for(LoginListener l: listeners)
			l.logout();
	}

	/** Show the login dialog to the user */
	public void login() throws Exception {
		if(!isLoggedIn())
			desktop.show(new LoginForm());
	}

	/** Logout the current user */
	public void logout() {
		SonarState s = state;
		if(s != null)
			s.quit();
		state = null;
		user = null;
		fireLogout();
	}

	/** Form to allow user to log in */
	protected class LoginForm extends AbstractForm {

		/** User name text entry component */
		protected final JTextField user_name = new JTextField(12);

		/** Password entry component */
		protected final JPasswordField password = new JPasswordField();

		/** Log in button */
		protected final JButton b_log_in = new JButton("Log In");

		/** Create a new login form */
		protected LoginForm() {
			super("IRIS Login");
		}

		/** Initialize the form */
		protected void initialize() {
			FormPanel panel = new FormPanel(true);
			panel.addRow("Username", user_name);
			panel.addRow("Password", password);
			new ActionJob(b_log_in) {
				public void perform() throws Exception {
					try {
						doLogin();
					}
					catch(AuthenticationException e) {
						// ShowHandler will take care
						// of displaying a message to
						// the user; don't do it again
					}
				}
			};
			password.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode() == KeyEvent.VK_ENTER)
						b_log_in.doClick();
				}
			});
			panel.setCenter();
			panel.addRow(b_log_in);
			add(panel);
		}

		/** Do the login authentication */
		protected void doLogin() throws Exception {
			char[] pwd = password.getPassword();
			password.setText("");
			state = createSonarState();
			user = createUser(user_name.getText(), pwd);
			close();
			fireLogin();
		}
	}

	/** Create a new SONAR state */
	protected SonarState createSonarState() throws IOException,
		ConfigurationError, NoSuchFieldException, IllegalAccessException
	{
		return new SonarState(props, new ShowHandler() {
			public void display(String m) {
				Exception e = createException(m);
				new ExceptionDialog(e).setVisible(true);
			}
		});
	}

	/** Create an exception from a SONAR show message */
	static protected Exception createException(String m) {
		if(m.equals("Permission denied: Authentication failed"))
			return new AuthenticationException(m);
		else
			return new SonarException(m);
	}

	/** Create a new user */
	protected IrisUser createUser(String userName, char[] pwd)
		throws SonarException, NoSuchFieldException,
		IllegalAccessException, AuthenticationException
	{
		state.login(userName, new String(pwd));
		User user = state.lookupUser(userName);
		IrisUser iris_user = new IrisUser(userName, user.getFullName());
		for(Role r: user.getRoles())
			iris_user.addRolePermission(r.getName());
		if(iris_user.hasNoPermissions())
			throw new SonarException("User has no permissions");
		return iris_user;
	}
}
