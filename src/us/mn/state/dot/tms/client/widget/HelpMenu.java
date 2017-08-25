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
package us.mn.state.dot.tms.client.widget;

import java.awt.event.ActionEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.utils.I18N;

/**
 * Menu for help information.
 *
 * @author Douglas Lau
 */
public class HelpMenu extends JMenu {

	/** Smart desktop */
	private final SmartDesktop desktop;

	/** Open trouble ticket menu item */
	private final JMenuItem ticket_item = new JMenuItem(new IAction(
		"help.trouble.ticket")
	{
		protected void doActionPerformed(ActionEvent e) {
			Help.invokeHelpUrl(SystemAttrEnum.
				HELP_TROUBLE_TICKET_URL.getString());
		}
	});

	/** Create a new HelpMenu */
	public HelpMenu(SmartDesktop sd) {
		super(I18N.get("help"));
		desktop = sd;
		addSupportItem();
		addAboutItem();
	}

	/** Add support menu item */
	private void addSupportItem() {
		add(new JMenuItem(new IAction("help.support") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new SupportForm());
			}
		}));
	}

	/** Add about menu item */
	private void addAboutItem() {
		add(new JMenuItem(new IAction("iris.about") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new AboutForm());
			}
		}));
	}

	/** Set the logged-in status */
	public void setLoggedIn(boolean in) {
		if(in && SystemAttrEnum.HELP_TROUBLE_TICKET_ENABLE.getBoolean())
			addOpenTroubleTicketItem();
	}

	/** Add the 'open trouble ticket' menu item. This menu item
	 * is inserted at the begining of the help menu and not removed
	 * when the user logs out. */
	private void addOpenTroubleTicketItem() {
		if (!isMenuComponent(ticket_item))
			insert(ticket_item, 0);
	}
}
