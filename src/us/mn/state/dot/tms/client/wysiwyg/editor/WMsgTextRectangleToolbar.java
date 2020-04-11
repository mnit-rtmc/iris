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

import java.awt.Insets;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;

import us.mn.state.dot.tms.client.widget.Icons;
import us.mn.state.dot.tms.utils.I18N;

/**
 * WYSIWYG DMS Message Editor Text Rectangle Option Panel containing buttons
 * with various options for text rectangle mode.
 *
 * @author Gordon Parikh - SRF Consulting
 */
@SuppressWarnings("serial")

public class WMsgTextRectangleToolbar extends WMsgTextToolbar {
	
	/** Buttons to move the rectangle forward (up) or backward (down) */
	private JButton moveRectUp;
	private JButton moveRectDown;
	
	public WMsgTextRectangleToolbar(WController c) {
		super(c, false);
		
		moveRectUp = new JButton(controller.moveSelectedRegionUp);
		ImageIcon moveRectUpIcon = Icons.getIconByPropName(
				"wysiwyg.epanel.move_rect_up");
		moveRectUp.setIcon(moveRectUpIcon);
		moveRectUp.setHideActionText(true);
		moveRectUp.setToolTipText(I18N.get("wysiwyg.epanel.move_rect_up"));
		moveRectUp.setMargin(new Insets(0,0,0,0));
		add(Box.createHorizontalStrut(30));
		add(moveRectUp);
		
		moveRectDown = new JButton(controller.pageMoveUp);
		ImageIcon moveRectDownIcon = Icons.getIconByPropName(
				"wysiwyg.epanel.move_rect_down");
		moveRectDown.setIcon(moveRectDownIcon);
		moveRectDown.setHideActionText(true);
		moveRectDown.setToolTipText(I18N.get("wysiwyg.epanel.move_rect_down"));
		moveRectDown.setMargin(new Insets(0,0,0,0));
		add(Box.createHorizontalStrut(10));
		add(moveRectDown);

		// TODO temporary
		moveRectUp.setEnabled(false);
		moveRectDown.setEnabled(false);
	}
}