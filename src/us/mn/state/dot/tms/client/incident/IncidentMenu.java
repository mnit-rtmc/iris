/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016  Minnesota Department of Transportation
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
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.SmartDesktop;
import us.mn.state.dot.tms.utils.I18N;

/**
 * IncidentMenu is a menu for incident-related items.
 *
 * @author Douglas Lau
 */
public class IncidentMenu extends JMenu {

	/** User Session */
	private final Session session;

	/** Desktop */
	private final SmartDesktop desktop;

	/** Create a new incident menu */
	public IncidentMenu(final Session s) {
		super(I18N.get("incident"));
		session = s;
		desktop = s.getDesktop();
		if (IncidentDetailForm.isPermitted(session))
			add(createIncidentDetailItem());
		if (IncDescriptorForm.isPermitted(session))
			add(createIncDescriptorItem());
		if (IncLocatorForm.isPermitted(session))
			add(createIncLocatorItem());
		if (IncAdviceForm.isPermitted(session))
			add(createIncAdviceItem());
	}

	/** Create the incident detail menu item */
	private JMenuItem createIncidentDetailItem() {
		return new JMenuItem(new IAction("incident.details") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new IncidentDetailForm(session));
			}
		});
	}

	/** Create the incident descriptor menu item */
	private JMenuItem createIncDescriptorItem() {
		return new JMenuItem(new IAction("incident.descriptors") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new IncDescriptorForm(session));
			}
		});
	}

	/** Create the incident locator menu item */
	private JMenuItem createIncLocatorItem() {
		return new JMenuItem(new IAction("incident.locators") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new IncLocatorForm(session));
			}
		});
	}

	/** Create the incident advice menu item */
	private JMenuItem createIncAdviceItem() {
		return new JMenuItem(new IAction("incident.advice") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new IncAdviceForm(session));
			}
		});
	}
}
