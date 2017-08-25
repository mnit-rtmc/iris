/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2017  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.beacon.BeaconForm;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IMenu;
import us.mn.state.dot.tms.client.widget.SmartDesktop;

/**
 * SignMenu is a menu for DMS-related items.
 *
 * @author Douglas Lau
 */
public class SignMenu extends IMenu {

	/** User Session */
	private final Session session;

	/** Desktop */
	private final SmartDesktop desktop;

	/** Create a new sign menu */
	public SignMenu(final Session s) {
		super("sign.menu");
		session = s;
		desktop = s.getDesktop();
		addItem(createDmsItem());
		addItem(createSignConfigItem());
		addItem(createFontItem());
		addItem(createGraphicItem());
		addItem(createQuickMessageItem());
		addItem(createBeaconItem());
		addItem(createDictionaryItem());
	}

	/** Create a DMS menu item action */
	private IAction createDmsItem() {
		return DMSForm.isPermitted(session) ?
		    new IAction("dms") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new DMSForm(session));
			}
		    } : null;
	}

	/** Create a sign config menu item action */
	private IAction createSignConfigItem() {
		return SignConfigForm.isPermitted(session) ?
		    new IAction("dms.config") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new SignConfigForm(session));
			}
		    } : null;
	}

	/** Create a font menu item action */
	private IAction createFontItem() {
		return FontForm.isPermitted(session) ?
		    new IAction("font.title") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new FontForm(session));
			}
		    } : null;
	}

	/** Create a graphics menu item action */
	private IAction createGraphicItem() {
		return GraphicForm.isPermitted(session) ?
		    new IAction("graphics") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new GraphicForm(session));
			}
		    } : null;
	}

	/** Create a quick message menu item action */
	private IAction createQuickMessageItem() {
		return QuickMessageForm.isPermitted(session) ?
		    new IAction("quick.messages") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new QuickMessageForm(session));
			}
		    } : null;
	}

	/** Create a beacon menu item action */
	private IAction createBeaconItem() {
		return BeaconForm.isPermitted(session) ?
		    new IAction("beacons") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new BeaconForm(session));
			}
		    } : null;
	}

	/** Create a dictionary menu item action */
	private IAction createDictionaryItem() {
		return DictionaryForm.isPermitted(session) ?
		    new IAction("dictionary") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new DictionaryForm(session));
			}
		    } : null;
	}
}
