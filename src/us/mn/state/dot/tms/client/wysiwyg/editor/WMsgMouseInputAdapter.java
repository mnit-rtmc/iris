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

import javax.swing.event.MouseInputAdapter;
import java.awt.event.MouseEvent;

public class WMsgMouseInputAdapter extends MouseInputAdapter {
	
	// TODO still need to fill out most of this
	/* Handle to WController */
	private WController controller;
	
	public WMsgMouseInputAdapter(WController c) {
		controller = c;
	}
	
	// TODO still just for testing
	@Override
	public void mouseClicked(MouseEvent e) {
		controller.handleClick(e);
	}
	
	@Override
	public void mouseMoved(MouseEvent e) {
		controller.handleMouseMove(e);
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		controller.handleMouseDrag(e);
	}
}