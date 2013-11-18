/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2013  Minnesota Department of Transportation
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

import java.awt.event.ActionEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.roads.RoadForm;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.SmartDesktop;
import us.mn.state.dot.tms.utils.I18N;

/**
 * SystemMenu is a menu for system configuration items.
 *
 * @author Douglas Lau
 */
public class SystemMenu extends JMenu {

	/** User Session */
	protected final Session session;

	/** Desktop */
	protected final SmartDesktop desktop;

	/** Create a new system menu */
	public SystemMenu(final Session s) {
		super(I18N.get("system"));
		session = s;
		desktop = s.getDesktop();
		JMenuItem item = createSystemAttributesItem();
		if(item != null)
			add(item);
		item = createIncidentDetailItem();
		if(item != null)
			add(item);
		item = createUsersAndRolesItem();
		if(item != null)
			add(item);
		item = createMapExtentsItem();
		if(item != null)
			add(item);
		item = createRoadItem();
		if(item != null)
			add(item);
	}

	/** Create the system attributes menu item */
	protected JMenuItem createSystemAttributesItem() {
		if(!SystemAttributeForm.isPermitted(session))
			return null;
		return new JMenuItem(new IAction("system.attributes") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new SystemAttributeForm(session));
			}
		});
	}

	/** Create the incident detail menu item */
	protected JMenuItem createIncidentDetailItem() {
		if(!IncidentDetailForm.isPermitted(session))
			return null;
		return new JMenuItem(new IAction("incident.details") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new IncidentDetailForm(session));
			}
		});
	}

	/** Create the users and roles menu item */
	protected JMenuItem createUsersAndRolesItem() {
		if(!UserRoleForm.isPermitted(session))
			return null;
		return new JMenuItem(new IAction("user.menu") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new UserRoleForm(session));
			}
		});
	}

	/** Create the map extents menu item */
	protected JMenuItem createMapExtentsItem() {
		if(!MapExtentForm.isPermitted(session))
			return null;
		return new JMenuItem(new IAction("location.map.extents") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new MapExtentForm(session,
					desktop.client));
			}
		});
	}

	/** Create the road menu item */
	protected JMenuItem createRoadItem() {
		if(!RoadForm.isPermitted(session))
			return null;
		return new JMenuItem(new IAction("location.road.plural") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new RoadForm(session));
			}
		});
	}
}
