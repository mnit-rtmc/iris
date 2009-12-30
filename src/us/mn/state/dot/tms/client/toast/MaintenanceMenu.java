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
package us.mn.state.dot.tms.client.toast;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.toast.SmartDesktop;

/**
 * MaintenanceMenu is a menu for maintenance items.
 *
 * @author Douglas Lau
 */
public class MaintenanceMenu extends JMenu {

	/** User Session */
	protected final Session session;

	/** Desktop */
	protected final SmartDesktop desktop;

	/** Create a new maintenance menu */
	public MaintenanceMenu(final Session s) {
		super("Maintenance");
		session = s;
		desktop = s.getDesktop();
		JMenuItem item = createCommLinkItem();
		if(item != null)
			add(item);
		item = createAlarmItem();
		if(item != null)
			add(item);
		item = createCabinetStyleItem();
		if(item != null)
			add(item);
	}

	/** Create the comm link menu item */
	protected JMenuItem createCommLinkItem() {
		if(!CommLinkForm.isPermitted(session))
			return null;
		JMenuItem item = new JMenuItem("Comm Links");
		item.setMnemonic('L');
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new CommLinkForm(session));
			}
		};
		return item;
	}

	/** Create the alarm menu item */
	protected JMenuItem createAlarmItem() {
		if(!AlarmForm.isPermitted(session))
			return null;
		JMenuItem item = new JMenuItem("Alarms");
		item.setMnemonic('a');
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new AlarmForm(session));
			}
		};
		return item;
	}

	/** Create the cabinet style menu item */
	protected JMenuItem createCabinetStyleItem() {
		if(!CabinetStyleForm.isPermitted(session))
			return null;
		JMenuItem item = new JMenuItem("Cabinet Styles");
		item.setMnemonic('s');
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new CabinetStyleForm(session));
			}
		};
		return item;
	}
}
