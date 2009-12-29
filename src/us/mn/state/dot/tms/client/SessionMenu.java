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
package us.mn.state.dot.tms.client;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.tms.client.system.LoginListener;
import us.mn.state.dot.tms.client.system.UserManager;

/** 
 * The sessoin menu contains menu items for logging in, logging out and exiting
 * the IRIS client.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public class SessionMenu extends JMenu {

	/** UserManager to use for logging in users */
	protected final UserManager userManager;

	/** Log in menu item */
	protected final JMenuItem log_in = new JMenuItem("Log In");

	/** Logout menu item */
	protected final JMenuItem log_out = new JMenuItem("Log Out");
	
	/** Create a new session menu */	
	public SessionMenu(final UserManager um) {
		super("Session");
		userManager = um;
		userManager.addLoginListener(new LoginListener() {
			public void login() {
				log_in.setEnabled(false);
				log_out.setEnabled(true);
			}
			public void logout() {
				log_in.setEnabled(true);
				log_out.setEnabled(false);
			}
		});
		setMnemonic('e');
		log_in.setMnemonic('L');
		new ActionJob(log_in) {
			public void perform() throws Exception {
				userManager.login();
			}
		};
		add(log_in);
		log_out.setMnemonic('O');
		new ActionJob(log_out) {
			public void perform() throws Exception {
				userManager.logout();
			}
		};
		log_out.setEnabled(false);
		add(log_out);
		add(new JSeparator());
		JMenuItem item = new JMenuItem("Exit");
		item.setMnemonic('x');
		new ActionJob(item) {
			public void perform() throws Exception {
				userManager.quit();
			}
		};
		add(item);
	}
}
