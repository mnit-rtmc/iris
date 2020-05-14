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
		
		/* Ctrl + Enter OR Ctrl + Shift + PgDown - add new page (after 
		 * selected) */		
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,
				KeyEvent.CTRL_DOWN_MASK), "pageAdd");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN,
				KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK),
				"pageAdd");
		actionMap.put("pageAdd", wc.pageAdd);
		
		/* Ctrl + S - save */
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				KeyEvent.CTRL_DOWN_MASK), "save");
		actionMap.put("save", wc.saveMessage);
		
		/* Ctrl + Shift + S - save as */
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S,
			KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK), "saveAs");
		actionMap.put("saveAs", wc.saveMessageAs);
		
		/* F6 - toggle active tab */
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0),
				"toggleActiveTab");
		actionMap.put("toggleActiveTab",
				wc.getEditorForm().getEditorPanel().toggleActiveTab);		
		
		/* F7 - toggle edit mode */
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0),
				"toggleNonTextTagMode");
		actionMap.put("toggleNonTextTagMode", wc.toggleNonTextTagMode);
		
		/* Ctrl + Tab - toggle focus */		
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB,
				KeyEvent.CTRL_DOWN_MASK), "toggleFocus");
		actionMap.put("toggleFocus", wc.toggleFocus);
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
		
		/* Ctrl + Enter OR Ctrl + Shift + PgDown - add new page (after 
		 * selected) */		
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,
				KeyEvent.CTRL_DOWN_MASK), "pageAdd");
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
		
		/* Ctrl + Left/Right arrow keys - jump caret left/right */
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,
				KeyEvent.CTRL_DOWN_MASK), "jumpCaretLeft");
		actionMap.put("jumpCaretLeft", wc.jumpCaretLeft);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,
				KeyEvent.CTRL_DOWN_MASK), "jumpCaretRight");
		actionMap.put("jumpCaretRight", wc.jumpCaretRight);
		
		/* Shift + Left/Right arrow keys - select left/right */
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,
				KeyEvent.SHIFT_DOWN_MASK), "selectTextLeft");
		actionMap.put("selectTextLeft", wc.selectTextLeft);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,
				KeyEvent.SHIFT_DOWN_MASK), "selectTextRight");
		actionMap.put("selectTextRight", wc.selectTextRight);
		
		/* Ctrl + Shift + Left/Right arrow keys - select word left/right */
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,
				KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK),
				"selectWordLeft");
		actionMap.put("selectWordLeft", wc.selectWordLeft);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,
				KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK),
				"selectWordRight");
		actionMap.put("selectWordRight", wc.selectWordRight);
		
		/* Up/Down arrow keys - move caret up/down or change selected region */
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0),
				"upArrow");
		actionMap.put("upArrow", wc.upArrow);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0),
				"downArrow");
		actionMap.put("downArrow", wc.downArrow);
		
		/* Shift + Up/Down arrow keys - select from caret up/down */
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 
				KeyEvent.SHIFT_DOWN_MASK), "selectUp");
		actionMap.put("selectUp", wc.selectUp);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 
				KeyEvent.SHIFT_DOWN_MASK), "selectDown");
		actionMap.put("selectDown", wc.selectDown);
		
		/* Home/End keys - move caret to beginning/end of line */
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0),
				"moveCaretLineBeginning");
		actionMap.put("moveCaretLineBeginning", wc.moveCaretLineBeginning);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_END, 0),
				"moveCaretLineEnd");
		actionMap.put("moveCaretLineEnd", wc.moveCaretLineEnd);
		
		/* Ctrl + Home/End keys - move caret to beginning/end of text rect. */
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 
				KeyEvent.CTRL_DOWN_MASK), "moveCaretTrBeginning");
		actionMap.put("moveCaretTrBeginning", wc.moveCaretTrBeginning);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_END, 
				KeyEvent.CTRL_DOWN_MASK), "moveCaretTrEnd");
		actionMap.put("moveCaretTrEnd", wc.moveCaretTrEnd);
		
		/* Shift + Home/End keys - select to beginning/end of line. */
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 
				KeyEvent.SHIFT_DOWN_MASK), "selectToLineStart");
		actionMap.put("selectToLineStart", wc.selectToLineStart);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_END, 
				KeyEvent.SHIFT_DOWN_MASK), "selectToLineEnd");
		actionMap.put("selectToLineEnd", wc.selectToLineEnd);
		
		/* Ctrl + Shift + Home/End - select to text rect. start/end */
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME,
				KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK),
				"selectToTrStart");
		actionMap.put("selectToTrStart", wc.selectToTrStart);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_END,
				KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK),
				"selectToTrEnd");
		actionMap.put("selectToTrEnd", wc.selectToTrEnd);
		
		/* Ctrl + A - select all */
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, 
				KeyEvent.CTRL_DOWN_MASK), "selectAll");
		actionMap.put("selectAll", wc.selectAll);
		
		/* Enter key - add newline */
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
				"addNewLine");
		actionMap.put("addNewLine", wc.addNewLine);
		
		/** F2 - Edit non-text tags */
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0),
				"editTag");
		actionMap.put("editTag", wc.editTag);

		/* Alt + PgUp - Move region forwards */
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP,
				KeyEvent.ALT_DOWN_MASK), "moveSelectedRegionForward");
		actionMap.put("moveSelectedRegionForward",
				wc.moveSelectedRegionForward);
		
		/* Alt + PgDown - Move region backwards */
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN,
				KeyEvent.ALT_DOWN_MASK), "moveSelectedRegionBackward");
		actionMap.put("moveSelectedRegionBackward",
				wc.moveSelectedRegionBackward);
		
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