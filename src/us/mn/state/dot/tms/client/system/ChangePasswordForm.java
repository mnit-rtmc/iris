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

import java.io.IOException;
import java.util.Arrays;
import javax.swing.JButton;
import javax.swing.JPasswordField;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.client.IrisClient;
import us.mn.state.dot.tms.client.widget.AbstractForm;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IPanel;
import us.mn.state.dot.tms.client.widget.IPanel.Stretch;
import us.mn.state.dot.tms.utils.I18N;

/**
 * The ChangePasswordForm is a GUI form for changing IRIS passwords.
 *
 * @author Douglas Lau
 */
public class ChangePasswordForm extends AbstractForm {

	/** Old password entry component */
	private final JPasswordField o_pwd_txt = new JPasswordField(16);

	/** New password entry component */
	private final JPasswordField n_pwd_txt = new JPasswordField(16);

	/** Verify password entry component */
	private final JPasswordField v_pwd_txt = new JPasswordField(16);

	/** Password change action */
	private final IAction change = new IAction("user.password.change") {
		protected void do_perform() throws Exception {
			doPasswordChange();
		}
	};

	/** Iris client */
	private final IrisClient client;

	/** Create a new login form */
	public ChangePasswordForm(IrisClient ic) {
		super(I18N.get("user.password.change"));
		client = ic;
	}

	/** Initialize the form */
	@Override protected void initialize() {
		IPanel p = new IPanel();
		p.add("user.password.old");
		p.add(o_pwd_txt, Stretch.LAST);
		p.add("user.password.new");
		p.add(n_pwd_txt, Stretch.LAST);
		p.add("user.password.verify");
		p.add(v_pwd_txt, Stretch.LAST);
		p.add(new JButton(change), Stretch.CENTER);
		add(p);
	}

	/** Do the password change */
	private void doPasswordChange() throws ChangeVetoException, IOException{
		char[] n_pwd = n_pwd_txt.getPassword();
		char[] v_pwd = v_pwd_txt.getPassword();
		if(!Arrays.equals(n_pwd, v_pwd)) {
			throw new ChangeVetoException(I18N.get(
				"user.password.mismatch"));
		}
		char[] o_pwd = o_pwd_txt.getPassword();
		o_pwd_txt.setText("");
		n_pwd_txt.setText("");
		v_pwd_txt.setText("");
		close();
		client.changePassword(o_pwd, n_pwd);
	}
}
