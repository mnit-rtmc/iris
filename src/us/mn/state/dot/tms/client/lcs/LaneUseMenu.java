/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2025  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.lcs;

import java.awt.event.ActionEvent;
import us.mn.state.dot.tms.LaneMarking;
import us.mn.state.dot.tms.Lcs;
import us.mn.state.dot.tms.TagReader;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.toll.TollZoneForm;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IMenu;
import us.mn.state.dot.tms.client.widget.SmartDesktop;

/**
 * LaneUseMenu is a menu for LCS-related items.
 *
 * @author Douglas Lau
 */
public class LaneUseMenu extends IMenu {

	/** User Session */
	private final Session session;

	/** Desktop */
	private final SmartDesktop desktop;

	/** Create a new lane use menu */
	public LaneUseMenu(final Session s) {
		super("lane.use");
		session = s;
		desktop = s.getDesktop();
		addItem(session.createTableAction(Lcs.SONAR_TYPE));
		addItem(session.createTableAction(LaneMarking.SONAR_TYPE));
		addItem(session.createTableAction(TagReader.SONAR_TYPE));
		addItem(createTollZoneItem());
	}

	/** Create a toll zone menu item action */
	private IAction createTollZoneItem() {
		return TollZoneForm.isPermitted(session) ?
			new IAction("toll_zone.title") {
				protected void doActionPerformed(ActionEvent e){
				       desktop.show(new TollZoneForm(session));
				}
			} : null;
	}
}
