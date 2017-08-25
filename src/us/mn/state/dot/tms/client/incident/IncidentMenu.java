/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016-2017  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.incident;

import java.awt.event.ActionEvent;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IMenu;
import us.mn.state.dot.tms.client.widget.SmartDesktop;

/**
 * IncidentMenu is a menu for incident-related items.
 *
 * @author Douglas Lau
 */
public class IncidentMenu extends IMenu {

	/** User Session */
	private final Session session;

	/** Desktop */
	private final SmartDesktop desktop;

	/** Create a new incident menu */
	public IncidentMenu(final Session s) {
		super("incident");
		session = s;
		desktop = s.getDesktop();
		addItem(createIncidentDetailItem());
		addItem(createIncDescriptorItem());
		addItem(createIncLocatorItem());
		addItem(createIncAdviceItem());
	}

	/** Create an incident detail menu item action */
	private IAction createIncidentDetailItem() {
		return IncidentDetailForm.isPermitted(session) ?
		    new IAction("incident.details") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new IncidentDetailForm(session));
			}
		    } : null;
	}

	/** Create an incident descriptor menu item action */
	private IAction createIncDescriptorItem() {
		return IncDescriptorForm.isPermitted(session) ?
		    new IAction("incident.descriptors") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new IncDescriptorForm(session));
			}
		    } : null;
	}

	/** Create an incident locator menu item action */
	private IAction createIncLocatorItem() {
		return IncLocatorForm.isPermitted(session) ?
		    new IAction("incident.locators") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new IncLocatorForm(session));
			}
		    } : null;
	}

	/** Create an incident advice menu item action */
	private IAction createIncAdviceItem() {
		return IncAdviceForm.isPermitted(session) ?
		    new IAction("incident.advice") {
		        protected void doActionPerformed(ActionEvent e) {
				desktop.show(new IncAdviceForm(session));
			}
		    } : null;
	}
}
