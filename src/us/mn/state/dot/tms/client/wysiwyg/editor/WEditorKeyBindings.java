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
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

/**
 * WEditorKeyBindings - Contains an InputMap and ActionMap that together
 * implement all editor-focused key bindings in the WYSIWYG DMS Editor.
 * 
 * TODO: Page list key bindings are implemented in a separate class.
 * 
 * @author Gordon Parikh - SRF Consulting
 */
@SuppressWarnings("serial")
public class WEditorKeyBindings {
	
	/** Controller for manipulating message. */
	WController wc;
	
	/** InputMap for handling key presses. */
	private InputMap inputMap;
	
	/** ActionMap for mapping key presses to actions. */
	private ActionMap actionMap;
	
	/** Initialize all key bindings in an InputMap/ActionMap pair.
	 *  To activate key bindings on a panel, use the getInputMap and
	 *  getActionMap methods to retrieve the InputMap and ActionMap, then use
	 *  panel.setInputMap and panel.setActionMap methods to set them on the
	 *  panel.
	 */
	public WEditorKeyBindings(WController c) {
		wc = c;
	}
	
	public void setKeyBindings(JComponent comp, int condition) {
//		System.out.println("Setting up key bindings on component " + comp.toString());
		inputMap = comp.getInputMap(condition);
		actionMap = comp.getActionMap();
		comp.setFocusTraversalKeysEnabled(false);
		
//		System.out.println("InputMap: " + inputMap.toString());
//		System.out.println("ActionMap: " + actionMap.toString());
		
		/* Backspace key - delete selected token or token behind caret */
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0),
				"backspace");
		actionMap.put("backspace", wc.backspace);
		
		/* Delete key - delete selected token or token in front of caret */
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete");
		actionMap.put("delete", wc.delete);
		
		/* Ctrl + Z - undo */
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
				KeyEvent.CTRL_DOWN_MASK), "undo");
		actionMap.put("undo", wc.undo);

		/* Ctrl + Shift + Z  OR  Ctrl + Y - redo */
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
				KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK), "redo");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y,
				KeyEvent.CTRL_DOWN_MASK), "redo");
		actionMap.put("redo", wc.redo);
		
		/* Left/Right arrow keys - move caret left/right */
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "moveCaretLeft");
		actionMap.put("moveCaretLeft", wc.moveCaretLeft);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "moveCaretRight");
		actionMap.put("moveCaretRight", wc.moveCaretRight);
	}
	
	public InputMap getInputMap() {
		return inputMap;
	}
	
	public ActionMap getActionMap() {
		return actionMap;
	}
}