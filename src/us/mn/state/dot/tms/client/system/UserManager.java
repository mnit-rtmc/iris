/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import javax.swing.JButton;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import us.mn.state.dot.sched.AbstractJob;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sched.Scheduler;
import us.mn.state.dot.sonar.ConfigurationError;
import us.mn.state.dot.sonar.FlushError;
import us.mn.state.dot.sonar.Role;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.client.SimpleHandler;
import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.client.toast.AbstractForm;
import us.mn.state.dot.tms.client.toast.FormPanel;
import us.mn.state.dot.tms.client.toast.SmartDesktop;
import us.mn.state.dot.tms.utils.PropertyFile;

/**
 * The UserManager is used to display a login prompt and to verify that the
 * user logging in has appropriate rights to use the client.  Also used to log
 * out of the connection when exiting the client.  Auto-login functionality is
 * enabled by adding properties 'autologin.username' and 'autologin.password'
 * to the client properties file.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 * @author Michael Darter
 */
public class UserManager {

	/** Login scheduler */
	static protected final Scheduler LOGIN = new Scheduler("LOGIN");

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

	/** Currently logged in user */
	protected User user = null;

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
	public User getUser() {
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

	/** auto-login the user if enabled */
	public void autoLogin() {
		if(isLoggedIn())
			return;
		if(props == null)
			return;

		// get login values
		String un = PropertyFile.get(props, "autologin.username");
		String pws = PropertyFile.get(props, "autologin.password");
		if(un == null || pws == null)
			return;
		if(un.length() <= 0 || pws.length() <= 0)
			return;
		char[] pw = pws.toCharArray();
		pws = null;

		// auto login enabled?
		if(un.length() > 0 && pw.length > 0) {
			try {
				doUserLogin(un, pw);
			}
			catch(Exception ex) {
				System.err.println("Auto-login failed.");
				ex.printStackTrace();
			}
		}

		// blank the password
		for(int i = 0; i < pw.length; ++i)
			pw[i] = ' ';
		pw = null;
	}

	/** Perform user login */
	protected void doUserLogin(String un, char[] pw) throws Exception {
		state = createSonarState();
		user = createUser(un, pw);
		new AbstractJob(LOGIN) {
			public void perform() throws Exception {
				fireLogin();
			}
		}.addToScheduler();
	}

	/** Logout the current user */
	public void logout() {
		SonarState s = state;
		if(s != null)
			s.quit();
		state = null;
		user = null;
		new AbstractJob(LOGIN) {
			public void perform() {
				fireLogout();
			}
		}.addToScheduler();
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
			new ActionJob(desktop, b_log_in) {
				public void perform() throws Exception {
					doLogin();
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
			close();
			doUserLogin(user_name.getText(), pwd);

			// blank the password
			for(int i = 0; i < pwd.length; ++i)
				pwd[i] = ' ';
			pwd = null;
		}
	}

	/** Create a new SONAR state */
	protected SonarState createSonarState() throws IOException,
		ConfigurationError, NoSuchFieldException, IllegalAccessException
	{
		return new SonarState(props, new SimpleHandler());
	}

	/** Create a new user */
	protected User createUser(String userName, char[] pwd)
		throws SonarException, NoSuchFieldException,
		IllegalAccessException
	{
		state.login(userName, new String(pwd));
		state.populateCaches();
		return state.lookupUser(userName);
	}
}
