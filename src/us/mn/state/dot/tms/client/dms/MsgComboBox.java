/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.dms;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.ComboBoxEditor;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.SignText;
import us.mn.state.dot.tms.SystemAttrEnum;

/**
 * GUI for composing DMS messages.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class MsgComboBox extends JComboBox {

	/** Combobox edit mode.  These values correspond to the
	 * dms_composer_edit_mode system attribute. */
	public enum EditMode {
		NEVER, ALWAYS, AFTERKEY;

		/** Convert an int to enum */
		static public EditMode fromOrdinal(int o) {
			for(EditMode em: values()) {
				if(em.ordinal() == o)
					return em;
			}
			return NEVER;
		}

		/** Get the edit mode */
		static public EditMode getEditMode() {
			return fromOrdinal(
				SystemAttrEnum.DMS_COMPOSER_EDIT_MODE.getInt());
		}
	}

	/** Prototype sign text */
	static protected final SignText PROTOTYPE_SIGN_TEXT =
		new ClientSignText("12345678901234567890");

	/** Format an item as a string */
	static protected String formatItem(Object o) {
		String txt = "";
		if(o instanceof SignText)
			txt = ((SignText)o).getMessage();
		else if(o != null)
			txt = o.toString();
		return new MultiString(txt).normalize();
	}

	/** Sign message composer containing the combo box */
	protected final SignMessageComposer composer;

	/** Edit mode for combo box */
	protected final EditMode edit_mode;

	/** Combo box editor */
	protected final Editor editor;

	/** Key listener for key events */
	protected final KeyListener keyListener;

	/** Focus listener for editor focus events */
	protected final FocusListener focusListener;

	/** Action listener for editor events */
	protected final ActionListener editorListener;

	/** Listener for combo box events */
	protected final ActionListener comboListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			composer.updateMessage();
		}
	};

	/** Create a message combo box.
	 * @param c Sign message composer.
	 * @param cam Flag to indicate messages can be added. */
	public MsgComboBox(SignMessageComposer c, boolean cam) {
		composer = c;
		edit_mode = getEditMode(cam);
		setMaximumRowCount(21);
		// NOTE: We use a prototype display value so that combo boxes
		//       are always the same size.  This prevents all the
		//       widgets from being rearranged whenever a new sign is
		//       selected.
		setPrototypeDisplayValue(PROTOTYPE_SIGN_TEXT);
		setRenderer(new SignTextCellRenderer());
		editor = createEditor();
		keyListener = createKeyListener();
		focusListener = createFocusListener();
		editorListener = createEditorListener();
	}

	/** Initialize the message combo box */
	public void initialize() {
		if(editor != null)
			setEditor(editor);
		addActionListener(comboListener);
		if(keyListener != null)
			addKeyListener(keyListener);
		if(focusListener != null)
			editor.addFocusListener(focusListener);
		if(editorListener != null)
			editor.addActionListener(editorListener);
		if(!isEditable() && edit_mode == EditMode.ALWAYS)
			setEditable(true);
	}

	/** Dispose of the message combo box */
	public void dispose() {
		if(editorListener != null)
			editor.removeActionListener(editorListener);
		if(focusListener != null)
			editor.removeFocusListener(focusListener);
		if(keyListener != null)
			removeKeyListener(keyListener);
		removeActionListener(comboListener);
	}

	/** Get the edit mode.
	 * @param cam Flag to indicate the user can add messages */
	protected EditMode getEditMode(boolean cam) {
		EditMode em = EditMode.getEditMode();
		if(em == EditMode.AFTERKEY && !cam)
			return EditMode.NEVER;
		return em;
	}

	/** Create the editor */
	protected Editor createEditor() {
		switch(edit_mode) {
		case NEVER:
			return null;
		default:
			return new Editor();
		}
	}

	/** Key event saved when making combobox editable */
	protected KeyEvent key_event;

	/** Create a key listener for AFTERKEY edit mode */
	protected KeyListener createKeyListener() {
		if(edit_mode == EditMode.AFTERKEY) {
			return new KeyAdapter() {
				public void keyTyped(KeyEvent ke) {
					if(!isEditable()) {
						setEditable(true);
						key_event = ke;
					}
				}
			};
		} else
			return null;
	}

	/** Create a focus listener for the editor component */
	protected FocusListener createFocusListener() {
		if(editor != null) {
			return new FocusAdapter() {
				public void focusGained(FocusEvent e) {
					if(key_event != null) {
						editor.dispatchEvent(key_event);
						key_event = null;
					}
				}
				public void focusLost(FocusEvent e) {
					if(edit_mode == EditMode.AFTERKEY)
						setEditable(false);
					composer.updateMessage();
				}
			};
		} else
			return null;
	}

	/** Create an action listener for the editor component */
	protected ActionListener createEditorListener() {
		if(edit_mode == EditMode.AFTERKEY) {
			return new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setEditable(false);
				}
			};
		} else
			return null;
	}

	/** Get message text */
	public String getMessage() {
		Object o = getSelectedItem();
		if(o instanceof SignText)
			return ((SignText)o).getMessage();
		else
			return "";
	}

	/** Editor for message combo box */
	protected class Editor extends JTextField implements ComboBoxEditor {

		/** Last set value of the editor */
		protected Object value;

		/** Get the component for the combo box editor */
		public Component getEditorComponent() {
			return this;
		}

		/** Return the edited item */
		public Object getItem() {
			String nv = formatItem(getText());
			if(value instanceof SignText) {
				if(nv.equals(formatItem(value)))
					return value;
			}
			return nv;
		}

		/** Set the item that should be edited.
		 * @param item New value of item */
		public void setItem(Object item) {
			setText(formatItem(item));
			value = item;
		}
	}
}
