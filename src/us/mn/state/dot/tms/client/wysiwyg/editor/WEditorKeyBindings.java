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
	
	/** Invalid characters that will be discarded */
	private static final String invalidChars = "`";
	
	/** Initialize all key bindings in an InputMap/ActionMap pair.
	 *  To activate key bindings on a panel, use the getInputMap and
	 *  getActionMap methods to retrieve the InputMap and ActionMap, then use
	 *  panel.setInputMap and panel.setActionMap methods to set them on the
	 *  panel.
	 */
	public WEditorKeyBindings(WController c) {
		wc = c;
		
		// set global key bindings first - the WysiwygPanel will take care of
		// editor-panel-specific ones
		setFormGlobalKeyBindings();
	}
	
	/** Set key bindings that apply when any part of the editor form is in
	 *  focus. Key bindings are applied to the controller's editor form
	 *  whenever it is the ancestor of a focused component.
	 */
	public void setFormGlobalKeyBindings() {
		// get the input/action maps for the form and disable focus traversal
		// keys
		WMsgEditorForm e = wc.getEditorForm();
		inputMap = e.getInputMap(
				JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		actionMap = e.getActionMap();
		e.setFocusTraversalKeysEnabled(false);
		
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
		
		/* Ctrl + Shift + PgDown - add new page (after selected) */
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN,
				KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK),
				"pageAdd");
		actionMap.put("pageAdd", wc.pageAdd);
	}
	
	/** Set key bindings that apply when the page list is in focus. */
	public void setPageListKeyBindings(WPageList pList, int condition) {
		inputMap = pList.getInputMap(condition);
		actionMap = pList.getActionMap();
		pList.setFocusTraversalKeysEnabled(false);
		
		/* F2 - edit page timing */
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0),
				"editPageTiming");
		actionMap.put("editPageTiming", wc.editPageTimingAction);
		
		/* Alt + PgUp - Move page up */
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP,
				KeyEvent.ALT_DOWN_MASK), "pageMoveUp");
		actionMap.put("pageMoveUp", wc.pageMoveUp);
		
		/* Alt + PgDown - Move page down */
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN,
				KeyEvent.ALT_DOWN_MASK), "pageMoveDown");
		actionMap.put("pageMoveDown", wc.pageMoveDown);
		
		/* Ctrl + Shift + PgDown - add new page (after selected) */
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN,
				KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK),
				"pageAdd");
		actionMap.put("pageAdd", wc.pageAdd);
		
		/* Delete - Delete Page */
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
				"pageDelete");
		actionMap.put("pageDelete", wc.pageDelete);
	}
	
	/** Set key bindings that apply when the editor panel (WImagePanel) is in
	 *  focus.
	 */
	public void setEditorPanelKeyBindings(WImagePanel sPanel, int condition) {
		inputMap = sPanel.getInputMap(condition);
		actionMap = sPanel.getActionMap();
		sPanel.setFocusTraversalKeysEnabled(false);
		
		/* Backspace key - delete selected token or token behind caret */
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0),
				"backspace");
		actionMap.put("backspace", wc.backspace);
		
		/* Delete key - delete selected token or token in front of caret */
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete");
		actionMap.put("delete", wc.delete);
		
		/* Left/Right arrow keys - move caret left/right */
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0),
				"moveCaretLeft");
		actionMap.put("moveCaretLeft", wc.moveCaretLeft);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0),
				"moveCaretRight");
		actionMap.put("moveCaretRight", wc.moveCaretRight);
		
		/* Up/Down arrow keys - move caret up/down */
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0),
				"moveCaretUp");
		actionMap.put("moveCaretUp", wc.moveCaretUp);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0),
				"moveCaretDown");
		actionMap.put("moveCaretDown", wc.moveCaretDown);

		/* Home/End keys - move caret to beginning/end of line */
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0),
				"moveCaretLineBeginning");
		actionMap.put("moveCaretLineBeginning", wc.moveCaretLineBeginning);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_END, 0),
				"moveCaretLineEnd");
		actionMap.put("moveCaretLineEnd", wc.moveCaretLineEnd);
		
		/* Enter key - add newline */
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
				"addNewLine");
		actionMap.put("addNewLine", wc.addNewLine);
		
		/** F2 - Edit non-text tags */
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0),
				"editTag");
		actionMap.put("editTag", wc.editTag);
		
		/* ASCII Text Characters - pass character to controller
		 * Note that 32 is space, 122 is z, and most things in between are
		 * readable characters.
		 */
		for (int i = 32; i < 123; ++i) {
			// get a character from the ASCII code
			char c = (char) i;
			
			// exclude invalid characters
			if (invalidChars.indexOf(c) < 0) {
				// if it's a lower-case character, map it to the upper-case
				// action
				char uc = c;
				if (Character.isLowerCase(i))
					uc = (char) Character.toUpperCase(i);
				
				// make a KeyAction for that character and add entries to the
				// input/action maps
				inputMap.put(KeyStroke.getKeyStroke(c), uc);
				actionMap.put(uc, new KeyAction(uc));
			}
		}
	}
	
	private class KeyAction extends AbstractAction {
		private char ch;
		
		public KeyAction(char c) {
			ch = c;
		}
		
		public void actionPerformed(ActionEvent e) {
			// pass the character to the controller - it knows what to do
			wc.typeChar(ch);
		}
	}
}