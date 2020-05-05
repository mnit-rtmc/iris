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
package us.mn.state.dot.tms.client.wysiwyg.editor;

import java.awt.event.ActionEvent;

import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IMenu;

/**
 * WMsgGraphicMenu is a JMenu which contains items TODO TBD.
 *
 * @author Gordon Parikh - SRF Consulting
 */
public class WMsgGraphicMenu extends IMenu {
	/** Create a new graphic menu */
	public WMsgGraphicMenu() {
		super("wysiwyg.editor.menu.graphic");
		
		// TODO
		addItem(createPlaceholderItem());
	}
	
	private IAction createPlaceholderItem() {
		return new IAction("wysiwyg.editor.menu.placeholder") {
			protected void doActionPerformed(ActionEvent e) {
				System.out.println("TODO");
			}
		};
	}
}
