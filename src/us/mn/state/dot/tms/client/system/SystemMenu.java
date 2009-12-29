/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.toast.SmartDesktop;

/**
 * SystemMenu is a menu for system configuration items.
 *
 * @author Douglas Lau
 */
public class SystemMenu extends JMenu {

	/** Session */
	protected final Session session;

	/** Create a new system menu */
	public SystemMenu(final Session s) {
		super("System");
		session = s;
		final SmartDesktop desktop = s.getDesktop();

		JMenuItem item = new JMenuItem("System Attributes");
		item.setMnemonic('S');
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new SystemAttributeForm(session));
			}
		};
		add(item);
		item = new JMenuItem("Users and Roles");
		item.setMnemonic('U');
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new UserRoleForm(session));
			}
		};
		add(item);
		item = new JMenuItem("Map extents");
		item.setMnemonic('e');
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new MapExtentForm(session));
			}
		};
		add(item);
	}
}
