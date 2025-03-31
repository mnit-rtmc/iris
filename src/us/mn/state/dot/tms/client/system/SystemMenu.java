/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2024  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.roads.RoadForm;
import us.mn.state.dot.tms.client.roads.RoadAffixForm;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IMenu;
import us.mn.state.dot.tms.client.widget.SmartDesktop;

/**
 * SystemMenu is a menu for system configuration items.
 *
 * @author Douglas Lau
 */
public class SystemMenu extends IMenu {

	/** User Session */
	protected final Session session;

	/** Desktop */
	protected final SmartDesktop desktop;

	/** Create a new system menu */
	public SystemMenu(final Session s) {
		super("system");
		session = s;
		desktop = s.getDesktop();
		addItem(createSystemAttributesItem());
		addItem(createEventConfigItem());
		addItem(createUsersAndRolesItem());
		addItem(createMapExtentsItem());
		addItem(createRoadItem());
		addItem(createRoadAffixItem());
	}

	/** Create a system attributes menu item action */
	private IAction createSystemAttributesItem() {
		return SystemAttributeForm.isPermitted(session) ?
		    new IAction("system.attributes") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new SystemAttributeForm(session));
			}
		    } : null;
	}

	/** Create an event config menu item action */
	private IAction createEventConfigItem() {
		return SystemAttributeForm.isPermitted(session) ?
		    new IAction("event.config") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new EventConfigForm(session));
			}
		    } : null;
	}

	/** Create a users and roles menu item action */
	private IAction createUsersAndRolesItem() {
		return UserRoleForm.isPermitted(session) ?
		    new IAction("user.menu") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new UserRoleForm(session));
			}
		    } : null;
	}

	/** Create a map extents menu item action */
	private IAction createMapExtentsItem() {
		return MapExtentForm.isPermitted(session) ?
		    new IAction("location.map.extents") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new MapExtentForm(session,
					desktop.client));
			}
		    } : null;
	}

	/** Create a road menu item action */
	private IAction createRoadItem() {
		return RoadForm.isPermitted(session) ?
		    new IAction("location.road.plural") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new RoadForm(session));
			}
		    } : null;
	}

	/** Create a road affix menu item action */
	private IAction createRoadAffixItem() {
		return RoadAffixForm.isPermitted(session) ?
		    new IAction("location.road.affix.plural") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new RoadAffixForm(session));
			}
		    } : null;
	}
}
