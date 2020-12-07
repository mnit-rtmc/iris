/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group
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

package us.mn.state.dot.tms.client.alert;

import java.awt.event.ActionEvent;

import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IMenu;
import us.mn.state.dot.tms.client.widget.SmartDesktop;

/**
 * AlertMenu is a menu for items related to automated alert systems.
 *
 * @author Gordon Parikh
 */
@SuppressWarnings("serial")
public class AlertMenu extends IMenu {
	
	/** User Session */
	private final Session session;
	
	/** Desktop */
	private final SmartDesktop desktop;

	public AlertMenu(final Session s) {
		super("alert");
		session = s;
		desktop = s.getDesktop();
		
		addItem(createAlertConfigItem());
		addItem(createCapResponseTypeItem());
		addItem(createCapUrgencyItem());
		// TODO more items will come
	}

	/** Create an alert config menu item action */
	private IAction createAlertConfigItem() {
		return AlertConfigForm.isPermitted(session) ?
			new IAction("alert.config") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new AlertConfigForm(session));
			}
		} : null;
	}
	
	/** Create a CAP response type substitution value menu item action */
	private IAction createCapResponseTypeItem() {
		return AlertConfigForm.isPermitted(session) ?
				new IAction("alert.cap.response_type_substitutions") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new CapResponseTypeForm(session));
			}
		} : null;
	}
	
	/** Create a CAP urgency substitution value menu item action */
	private IAction createCapUrgencyItem() {
		return AlertConfigForm.isPermitted(session) ?
				new IAction("alert.cap.urgency_substitutions") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new CapUrgencyForm(session));
			}
		} : null;
	}
}
