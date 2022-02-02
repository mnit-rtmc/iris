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

import java.awt.Color;
import java.awt.Insets;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

import us.mn.state.dot.tms.DmsColor;
import us.mn.state.dot.tms.client.widget.Icons;
import us.mn.state.dot.tms.utils.I18N;

/**
 * Abstract toolbar class for WYSIWYG DMS Message Editor
 *
 * @author Gordon Parikh - SRF Consulting
 */
@SuppressWarnings("serial")

abstract public class WToolbar extends JPanel {
	
	/** Handle to the controller */
	protected WController controller;
	
	/** Buttons to move regions (text/color rectangles and graphics forward or
	 *  backward
	 */
	protected JButton moveRegionForward;
	protected JButton moveRegionBackward;
	
	public WToolbar(WController c) {
		controller = c;
	}
	
	abstract public void setColor(Color c, String mode);
	
	abstract public void setColor(DmsColor c, String mode);

	abstract public void setColor(int c, String mode);

	protected void addMoveRegionForwardButton() {
		moveRegionForward = new JButton(controller.moveSelectedRegionForward);
		ImageIcon moveRectFwIcon = Icons.getIconByPropName(
				"wysiwyg.epanel.move_region_fw");
		moveRegionForward.setIcon(moveRectFwIcon);
		moveRegionForward.setHideActionText(true);
		moveRegionForward.setToolTipText(
				I18N.get("wysiwyg.epanel.move_region_fw"));
		moveRegionForward.setMargin(new Insets(0,0,0,0));
		add(Box.createHorizontalStrut(30));
		add(moveRegionForward);
	}
	
	protected void addMoveRegionBackwardButton() {
		moveRegionBackward = new JButton(
				controller.moveSelectedRegionBackward);
		ImageIcon moveRectBwIcon = Icons.getIconByPropName(
				"wysiwyg.epanel.move_region_bw");
		moveRegionBackward.setIcon(moveRectBwIcon);
		moveRegionBackward.setHideActionText(true);
		moveRegionBackward.setToolTipText(
				I18N.get("wysiwyg.epanel.move_region_bw"));
		moveRegionBackward.setMargin(new Insets(0,0,0,0));
		add(Box.createHorizontalStrut(10));
		add(moveRegionBackward);
	}
}