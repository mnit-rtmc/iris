/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2016  Minnesota Department of Transportation
 * Copyright (C) 2015  Iteris Inc.
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
package us.mn.state.dot.tms.client.dms;

import java.awt.event.ActionEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.beacon.BeaconForm;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.SmartDesktop;
import us.mn.state.dot.tms.utils.I18N;

/**
 * SignMenu is a menu for DMS-related items.
 *
 * @author Douglas Lau
 */
public class SignMenu extends JMenu {

	/** User Session */
	private final Session session;

	/** Desktop */
	private final SmartDesktop desktop;

	/** Create a new sign menu */
	public SignMenu(final Session s) {
		super(I18N.get("sign.menu"));
		session = s;
		desktop = s.getDesktop();
		JMenuItem item = createDmsItem();
		if(item != null)
			add(item);
		item = createFontItem();
		if(item != null)
			add(item);
		item = createGraphicItem();
		if(item != null)
			add(item);
		item = createQuickMessageItem();
		if(item != null)
			add(item);
		item = createBeaconItem();
		if(item != null)
			add(item);
		item = createDictionaryItem();
		if(item != null)
			add(item);
	}

	/** Create the DMS menu item */
	private JMenuItem createDmsItem() {
		return DMSForm.isPermitted(session) ?
		        new JMenuItem(new IAction("dms")
		{
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new DMSForm(session));
			}
		}) : null;
	}

	/** Create the font menu item */
	private JMenuItem createFontItem() {
		if(!FontForm.isPermitted(session))
			return null;
		return new JMenuItem(new IAction("font.title") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new FontForm(session));
			}
		});
	}

	/** Create the graphics menu item */
	private JMenuItem createGraphicItem() {
		if(!GraphicForm.isPermitted(session))
			return null;
		return new JMenuItem(new IAction("graphics") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new GraphicForm(session));
			}
		});
	}

	/** Create the quick message menu item */
	private JMenuItem createQuickMessageItem() {
		if(!QuickMessageForm.isPermitted(session))
			return null;
		return new JMenuItem(new IAction("quick.messages") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new QuickMessageForm(session));
			}
		});
	}

	/** Create the beacon menu item */
	private JMenuItem createBeaconItem() {
		if(!BeaconForm.isPermitted(session))
			return null;
		return new JMenuItem(new IAction("beacons") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new BeaconForm(session));
			}
		});
	}

	/** Create the dictionary menu item */
	private JMenuItem createDictionaryItem() {
		if(!DictionaryForm.isPermitted(session))
			return null;
		return new JMenuItem(new IAction("dictionary") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new DictionaryForm(session));
			}
		});
	}
}
