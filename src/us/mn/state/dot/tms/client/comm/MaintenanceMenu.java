/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2017  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.comm;

import java.awt.event.ActionEvent;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IMenu;
import us.mn.state.dot.tms.client.widget.SmartDesktop;

/**
 * MaintenanceMenu is a menu for maintenance items.
 *
 * @author Douglas Lau
 */
public class MaintenanceMenu extends IMenu {

	/** User Session */
	private final Session session;

	/** Desktop */
	private final SmartDesktop desktop;

	/** Create a new maintenance menu */
	public MaintenanceMenu(final Session s) {
		super("maintenance");
		session = s;
		desktop = s.getDesktop();
		addItem(createCommLinkItem());
		addItem(createModemItem());
		addItem(createAlarmItem());
		addItem(createCabinetStyleItem());
	}

	/** Create a comm link menu item action */
	private IAction createCommLinkItem() {
		return CommLinkForm.isPermitted(session) ?
		    new IAction("comm.links") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new CommLinkForm(session));
			}
		    } : null;
	}

	/** Create a modem menu item action */
	private IAction createModemItem() {
		return ModemForm.isPermitted(session) ?
		    new IAction("modems") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new ModemForm(session));
			}
		    } : null;
	}

	/** Create an alarm menu item action */
	private IAction createAlarmItem() {
		return AlarmForm.isPermitted(session) ?
		    new IAction("alarm.plural") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new AlarmForm(session));
			}
		    } : null;
	}

	/** Create a cabinet style menu item action */
	private IAction createCabinetStyleItem() {
		return CabinetStyleForm.isPermitted(session) ?
		    new IAction("cabinet.styles") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new CabinetStyleForm(session));
			}
		    } : null;
	}
}
