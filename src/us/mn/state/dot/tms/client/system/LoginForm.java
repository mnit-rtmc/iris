/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2013  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.client.IrisClient;
import us.mn.state.dot.tms.client.widget.AbstractForm;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IPanel;
import us.mn.state.dot.tms.client.widget.IPanel.Stretch;
import us.mn.state.dot.tms.client.widget.SmartDesktop;
import us.mn.state.dot.tms.utils.I18N;

/**
 * The LoginForm is a GUI authenticaion form for logging in to IRIS.
 *
 * @author Douglas Lau
 */
public class LoginForm extends AbstractForm {

	/** User name text entry component */
	private final JTextField user_txt = new JTextField(15);

	/** Password entry component */
	private final JPasswordField passwd_txt = new JPasswordField(16);

	/** Log in action */
	private final IAction login = new IAction("connection.login") {
		protected void do_perform() {
			doLogin();
		}
	};

	/** Iris client */
	private final IrisClient client;

	/** Smart desktp */
	private final SmartDesktop desktop;

	/** Create a new login form */
	public LoginForm(IrisClient ic, SmartDesktop sd) {
		super(I18N.get("connection.login.form"));
		client = ic;
		desktop = sd;
	}

	/** Initialize the form */
	@Override protected void initialize() {
		final JButton login_btn = new JButton(login);
		passwd_txt.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER)
					login_btn.doClick();
			}
		});
		IPanel p = new IPanel();
		p.add("user.name");
		p.add(user_txt, Stretch.LAST);
		p.add("user.password");
		p.add(passwd_txt, Stretch.LAST);
		p.add(login_btn, Stretch.CENTER);
		add(p);
	}

	/** Do the login authentication */
	protected void doLogin() {
		char[] pwd = passwd_txt.getPassword();
		passwd_txt.setText("");
		close();
		client.login(user_txt.getText(), pwd);
	}
}
