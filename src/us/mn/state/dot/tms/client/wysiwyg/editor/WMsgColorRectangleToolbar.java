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

import javax.swing.JPanel;
import java.awt.FlowLayout;

// TODO TEMPORARY
import javax.swing.JLabel;

/**
 * WYSIWYG DMS Message Editor Color Rectangle Option Panel containing buttons
 * with various options for color rectangle mode.
 *
 * @author Gordon Parikh - SRF Consulting
 */
@SuppressWarnings("serial")

public class WMsgColorRectangleToolbar extends JPanel {
	
	/** Handle to the controller */
	private WController controller;
	
	/** TODO PLACEHOLDER */
	private JLabel placeholder;
	
	public WMsgColorRectangleToolbar(WController c) {
		controller = c;

		// use a FlowLayout with no margins to give more control of separation
		setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		
		placeholder = new JLabel("<COLOR RECTANGLE OPTIONS>");
		add(placeholder);
	}
}