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

import us.mn.state.dot.tms.ColorScheme;
import us.mn.state.dot.tms.DmsColor;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.SmartDesktop;
import us.mn.state.dot.tms.utils.I18N;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.JButton;

/**
 * WYSIWYG DMS Message Editor Color Rectangle Option Panel containing buttons
 * with various options for color rectangle mode.
 *
 * @author Gordon Parikh - SRF Consulting
 */
@SuppressWarnings("serial")

public class WMsgColorRectangleToolbar extends WToolbar {
	
	/** Color picker button */
	private JButton colorBtn;
	
	/** Current color */
	private Color color;
	
	public WMsgColorRectangleToolbar(WController c) {
		super(c);

		// use a FlowLayout with no margins to give more control of separation
		setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		
		colorBtn = new JButton(open_color_picker);
		colorBtn.setToolTipText(
				I18N.get("wysiwyg.epanel.color_rect_picker_title"));
		
		// default to the controller's current foreground color
		DmsColor fgc = controller.getForegroundColor();
		if (fgc != null) {
			color = fgc.color;
			colorBtn.setIcon(
					WMsgColorChooser.createColorIcon(color, 16, 16));
			colorBtn.setMargin(new Insets(0,0,0,0));
		}
		add(colorBtn);
		
		// disable the color button for unknown color schemes
		ColorScheme cs = controller.getColorScheme();
		if (cs == ColorScheme.UNKNOWN)
			colorBtn.setEnabled(false);
		
		addMoveRegionForwardButton();
		addMoveRegionBackwardButton();
	}
	
	WMsgColorRectangleToolbar tb = this;
	
	/** Color picker action */
	private final IAction open_color_picker =
			new IAction("wysiwyg.epanel.color_rect_picker_btn") {
		@SuppressWarnings("synthetic-access")
		protected void doActionPerformed(ActionEvent e)
				throws Exception
		{
			String title = I18N.get("wysiwyg.epanel.color_rect_picker_title");
			WMsgColorChooser ccForm = new WMsgColorChooser(controller, tb,
					title, color, WMsgColorChooser.COLOR_RECT);
			SmartDesktop desktop = controller.getDesktop();
			desktop.show(ccForm);
		}
	};
	
	/** Set the color rectangle color on the controller and change the button
	 *  appearance
	 */
	public void setColor(Color c, String mode) {
		color = c;
		controller.setColorRectangleColor(new DmsColor(color));
		colorBtn.setIcon(WMsgColorChooser.createColorIcon(color, 16, 16));
	}
}