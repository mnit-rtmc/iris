/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2010  Minnesota Department of Transportation
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
import javax.swing.JButton;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.tms.client.IrisClient;
import us.mn.state.dot.tms.client.toast.AbstractForm;
import us.mn.state.dot.tms.client.toast.FormPanel;
import us.mn.state.dot.tms.client.toast.SmartDesktop;

/**
 * The LoginForm is a GUI authenticaion form for logging in to IRIS.
 *
 * @author Douglas Lau
 */
public class LoginForm extends AbstractForm {

	/** User name text entry component */
	protected final JTextField user_txt = new JTextField(12);

	/** Password entry component */
	protected final JPasswordField passwd_txt = new JPasswordField();

	/** Log in button */
	protected final JButton login_btn = new JButton("Log In");

	/** Iris client */
	protected final IrisClient client;

	/** Smart desktp */
	protected final SmartDesktop desktop;

	/** Create a new login form */
	public LoginForm(IrisClient ic, SmartDesktop sd) {
		super("IRIS Login");
		client = ic;
		desktop = sd;
	}

	/** Initialize the form */
	protected void initialize() {
		FormPanel panel = new FormPanel(true);
		panel.addRow("Username", user_txt);
		panel.addRow("Password", passwd_txt);
		new ActionJob(desktop, login_btn) {
			public void perform() throws Exception {
				doLogin();
			}
		};
		passwd_txt.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER)
					login_btn.doClick();
			}
		});
		panel.setCenter();
		panel.addRow(login_btn);
		add(panel);
	}

	/** Do the login authentication */
	protected void doLogin() {
		char[] pwd = passwd_txt.getPassword();
		passwd_txt.setText("");
		close();
		client.login(user_txt.getText(), pwd);
	}
}
