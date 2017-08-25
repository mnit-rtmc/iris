/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2017  Minnesota Department of Transportation
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
import javax.swing.JMenuBar;
import us.mn.state.dot.tms.client.widget.HelpMenu;
import us.mn.state.dot.tms.client.widget.SmartDesktop;

/**
 * IRIS menu bar
 *
 * @author Douglas Lau
 */
public class IMenuBar extends JMenuBar {

	/** Session menu */
	private final SessionMenu session_menu;

	/** View menu */
	private JMenu view_menu;

	/** Help menu */
	private final HelpMenu help_menu;

	/** Create a new Iris menu bar */
	public IMenuBar(IrisClient ic, SmartDesktop desktop) {
		session_menu = new SessionMenu(ic);
		view_menu = null;
		help_menu = new HelpMenu(desktop);
		add(session_menu);
		add(help_menu);
	}

	/** Disable the session menu */
	public void disableSessionMenu() {
		session_menu.setEnabled(false);
	}

	/** Show the login form */
	public void showLoginForm() {
		session_menu.showLoginForm();
	}

	/** Set the current session */
	public void setSession(Session s) {
		JMenu vm = view_menu;
		if (vm != null)
			remove(vm);
		boolean in = (s != null);
		if (in) {
			view_menu = new ViewMenu(s);
			add(view_menu, 1);
			session_menu.setUser(s.getUser());
		} else {
			view_menu = null;
			session_menu.setUser(null);
		}
		help_menu.setLoggedIn(in);
		session_menu.setEnabled(true);
	}
}
