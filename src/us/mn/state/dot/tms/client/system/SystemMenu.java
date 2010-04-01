/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2010  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.client.roads.RoadForm;
import us.mn.state.dot.tms.client.toast.SmartDesktop;

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
		super("System");
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
		JMenuItem item = new JMenuItem("System Attributes");
		item.setMnemonic('S');
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new SystemAttributeForm(session));
			}
		};
		return item;
	}

	/** Create the incident detail menu item */
	protected JMenuItem createIncidentDetailItem() {
		if(!IncidentDetailForm.isPermitted(session))
			return null;
		JMenuItem item = new JMenuItem("Incident Detail");
		item.setMnemonic('i');
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new IncidentDetailForm(session));
			}
		};
		return item;
	}

	/** Create the users and roles menu item */
	protected JMenuItem createUsersAndRolesItem() {
		if(!UserRoleForm.isPermitted(session))
			return null;
		JMenuItem item = new JMenuItem("Users and Roles");
		item.setMnemonic('U');
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new UserRoleForm(session));
			}
		};
		return item;
	}

	/** Create the map extents menu item */
	protected JMenuItem createMapExtentsItem() {
		if(!MapExtentForm.isPermitted(session))
			return null;
		JMenuItem item = new JMenuItem("Map extents");
		item.setMnemonic('e');
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new MapExtentForm(session));
			}
		};
		return item;
	}

	/** Create the road menu item */
	protected JMenuItem createRoadItem() {
		if(!RoadForm.isPermitted(session))
			return null;
		JMenuItem item = new JMenuItem("Roadways");
		item.setMnemonic('R');
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new RoadForm(session));
			}
		};
		return item;
	}
}
