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
package us.mn.state.dot.tms.client.lcs;

import java.awt.event.ActionEvent;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.marking.LaneMarkingForm;
import us.mn.state.dot.tms.client.toll.TagReaderForm;
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
		addItem(createLcsItem());
		addItem(createLaneUseMultiItem());
		addItem(createLaneMarkingItem());
		addItem(createTagReaderItem());
		addItem(createTollZoneItem());
	}

	/** Create a LCS menu item action */
	private IAction createLcsItem() {
		return LcsForm.isPermitted(session) ?
		    new IAction("lcs") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new LcsForm(session));
			}
		    } : null;
	}

	/** Create a lane-use MULTI menu item action */
	private IAction createLaneUseMultiItem() {
		return LaneUseMultiForm.isPermitted(session) ?
		    new IAction("lane.use.multi") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new LaneUseMultiForm(session));
			}
		    } : null;
	}

	/** Create a lane marking menu item action */
	private IAction createLaneMarkingItem() {
		return LaneMarkingForm.isPermitted(session) ?
		    new IAction("lane_marking.title") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new LaneMarkingForm(session));
			}
		    } : null;
	}

	/** Create a tag reader menu item action */
	private IAction createTagReaderItem() {
		return TagReaderForm.isPermitted(session) ?
			new IAction("tag_reader.title") {
				protected void doActionPerformed(ActionEvent e){
				       desktop.show(new TagReaderForm(session));
				}
			} : null;
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
