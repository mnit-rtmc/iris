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

import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;
import java.awt.event.MouseEvent;

public class WMsgMouseInputAdapter extends MouseInputAdapter {
	
	// TODO still need to fill out most of this
	/* Handle to editor form */
	WMsgEditorForm editor;
	
	public WMsgMouseInputAdapter(WMsgEditorForm ef) {
		editor = ef;
	}
	
	// TODO still just for testing
	@Override
	public void mouseClicked(MouseEvent e) {
		editor.updateWysiwygPanel();
		int b = e.getButton();
		int x = e.getX();
		int y = e.getY();
		System.out.println(String.format("Mouse button %d clicked at (%d, %d) ...", b, x, y));
	}
	
	@Override
	public void mouseMoved(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
//		System.out.println(String.format("Mouse moved to (%d, %d) ...", x, y));
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		String b = "";
		if (SwingUtilities.isLeftMouseButton(e))
			b = "left";
		else if (SwingUtilities.isRightMouseButton(e))
			b = "right";
		else if (SwingUtilities.isMiddleMouseButton(e))
			b = "middle";
			
		int x = e.getX();
		int y = e.getY();
//		System.out.println(String.format("Mouse dragged with %s button to (%d, %d) ...", b, x, y));
	}
}