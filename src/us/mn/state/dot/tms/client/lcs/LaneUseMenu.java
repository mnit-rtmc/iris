/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2015  Minnesota Department of Transportation
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
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.marking.LaneMarkingForm;
import us.mn.state.dot.tms.client.toll.TagReaderForm;
import us.mn.state.dot.tms.client.toll.TollZoneForm;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.SmartDesktop;
import us.mn.state.dot.tms.utils.I18N;

/**
 * LaneUseMenu is a menu for LCS-related items.
 *
 * @author Douglas Lau
 */
public class LaneUseMenu extends JMenu {

	/** User Session */
	private final Session session;

	/** Desktop */
	private final SmartDesktop desktop;

	/** Create a new lane use menu */
	public LaneUseMenu(final Session s) {
		super(I18N.get("lane.use"));
		session = s;
		desktop = s.getDesktop();
		JMenuItem item = createLcsItem();
		if(item != null)
			add(item);
		item = createLaneUseMultiItem();
		if(item != null)
			add(item);
		item = createLaneMarkingItem();
		if(item != null)
			add(item);
		item = createTagReaderItem();
		if (item != null)
			add(item);
		item = createTollZoneItem();
		if (item != null)
			add(item);
	}

	/** Create the LCS menu item */
	protected JMenuItem createLcsItem() {
		if(!LcsForm.isPermitted(session))
			return null;
		return new JMenuItem(new IAction("lcs") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new LcsForm(session));
			}
		});
	}

	/** Create the lane-use MULTI menu item */
	protected JMenuItem createLaneUseMultiItem() {
		if(!LaneUseMultiForm.isPermitted(session))
			return null;
		return new JMenuItem(new IAction("lane.use.multi") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new LaneUseMultiForm(session));
			}
		});
	}

	/** Create the lane marking menu item */
	protected JMenuItem createLaneMarkingItem() {
		if(!LaneMarkingForm.isPermitted(session))
			return null;
		return new JMenuItem(new IAction("lane_marking.title") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new LaneMarkingForm(session));
			}
		});
	}

	/** Create the tag reader menu item */
	private JMenuItem createTagReaderItem() {
		if (TagReaderForm.isPermitted(session)) {
			return new JMenuItem(new IAction("tag_reader.title") {
				protected void doActionPerformed(ActionEvent e){
				       desktop.show(new TagReaderForm(session));
				}
			});
		} else
			return null;
	}

	/** Create the toll zone menu item */
	private JMenuItem createTollZoneItem() {
		if (TollZoneForm.isPermitted(session)) {
			return new JMenuItem(new IAction("toll_zone.title") {
				protected void doActionPerformed(ActionEvent e){
				       desktop.show(new TollZoneForm(session));
				}
			});
		} else
			return null;
	}
}
