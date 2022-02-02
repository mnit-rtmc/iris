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

import us.mn.state.dot.tms.DmsColor;
import us.mn.state.dot.tms.Graphic;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.utils.I18N;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import javax.swing.Box;
import javax.swing.JButton;

/**
 * WYSIWYG DMS Message Editor Graphic Option Panel containing buttons with
 * various options for graphic insert mode.
 *
 * @author Gordon Parikh - SRF Consulting
 */
@SuppressWarnings("serial")

public class WMsgGraphicToolbar extends WToolbar {
	
	/** Graphics list */
	private WGraphicMenu graphicList;
	
	/** "Add" button */
	private JButton addBtn;
	
	public WMsgGraphicToolbar(WController c) {
		super(c);

		// use a FlowLayout with no margins to give more control of separation
		setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		
		// make the graphic menu and add it
		graphicList = new WGraphicMenu(controller);
		add(graphicList);
		
		// add the "add" button
		addBtn = new JButton(addGraphic);
		addBtn.setToolTipText(I18N.get("wysiwyg.epanel.add_graphic_tooltip"));
		add(Box.createHorizontalStrut(10));
		add(addBtn);
		
		addMoveRegionForwardButton();
		addMoveRegionBackwardButton();
	}
	
	/** Action to add the graphic */
	private final IAction addGraphic = new IAction(
			"wysiwyg.epanel.add_graphic_button") {
		protected void doActionPerformed(ActionEvent e)
				throws Exception {
			// get the selected graphic and add it using the controller
			Graphic g = graphicList.getSelectedItem();
			WController.println("Adding graphic %d", g.getGNumber());
			controller.addGraphic(g);
		}
	};
	
	/** Does nothing in graphic toolbar. */
	@Override
	public void setColor(Color c, String mode) { }
	
	/** Does nothing in graphic toolbar. */
	@Override
	public void setColor(DmsColor c, String mode) { }

	/** Does nothing in graphic toolbar. */
	@Override
	public void setColor(int c, String mode) { }
}