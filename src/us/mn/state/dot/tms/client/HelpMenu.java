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
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.tms.client.toast.Help;
import us.mn.state.dot.tms.client.toast.SmartDesktop;
import us.mn.state.dot.tms.SystemAttributeHelper;

/** 
 * Menu for help information.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public class HelpMenu extends JMenu {

	/** open trouble ticket menu item */
	JMenuItem m_opentroubleticket;

	/** Create a new HelpMenu */
	public HelpMenu(final SmartDesktop desktop) { 
		super("Help");
		setMnemonic('H');

		JMenuItem item;

		// add menu item: Support
		item = new JMenuItem("Support");
		item.setMnemonic('S');
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new Support());
			}
		};
		add(item);

		// add menu item: About IRIS
		item = new JMenuItem("About IRIS");
		item.setMnemonic('A');
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new AboutForm());
			}
		};
		add(item);
	}

	/** add additional menu items to the existing help menu */
	public void add(final SmartDesktop desktop) { 
		addOpenTroubleTicket(desktop);
	}

	/** Add the 'open trouble ticket' menu item. This menu item
	 *  is inserted at the begining of the help menu and not removed
	 *  when the user logs out. */
	protected void addOpenTroubleTicket(final SmartDesktop desktop) { 
	   	if(!SystemAttributeHelper.useMenuItemHelpTroubleTicket())
			return;

		// it's already on the menu
		if(isMenuComponent(m_opentroubleticket))
			return;

		final String url = SystemAttributeHelper.
			menuItemHelpTroubleTicketUrl();
		m_opentroubleticket = new JMenuItem(
			"Open Trouble Ticket");
		m_opentroubleticket.setMnemonic('T');
		new ActionJob(m_opentroubleticket) {
			public void perform() throws Exception {
				Help.invokeHelpWithUrl(url);
			}
		};
		// add as 0th item in menu
		insert(m_opentroubleticket, 0);
	}
}
