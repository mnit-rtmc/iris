/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011-2022  Minnesota Department of Transportation
 * Copyright (C) 2009-2010  AHMCT, University of California
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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.ComboBoxEditor;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import us.mn.state.dot.tms.SignText;
import us.mn.state.dot.tms.utils.MultiString;

/**
 * Message composer combo box for one line of sign text.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class SignTextCBox extends JComboBox<SignText> {

	/** Message combo box text editor */
	static private class Editor extends JTextField
		implements ComboBoxEditor
	{
		@Override public Component getEditorComponent() {
			return this;
		}
		@Override public Object getItem() {
			return getMulti(getText());
		}
		@Override public void setItem(Object item) {
			setText(getMulti(item));
		}
	}

	/** Get the MULTI string of an item */
	static private String getMulti(Object item) {
		if (item != null) {
			String ms = (item instanceof SignText)
				? ((SignText) item).getMulti()
				: item.toString();
			return new MultiString(ms.trim())
				.normalizeLine()
				.toString();
		}
		return "";
	}

	/** Prototype sign text */
	static private final SignText PROTOTYPE_SIGN_TEXT =
		new ClientSignText("12345678901234567890");

	/** Combo box editor */
	private final Editor editor = new Editor();

	/** Key listener for key events */
	private final KeyAdapter key_listener = new KeyAdapter() {
		@Override public void keyTyped(KeyEvent ke) {
			if (EditMode.getMode() == EditMode.AFTERKEY &&
			    !isEditable())
			{
				key_event = ke;
				setEditable(true);
			}
		}
	};

	/** Key event saved when making combobox editable */
	private KeyEvent key_event;

	/** Focus listener for editor focus events */
	private final FocusAdapter focus_listener = new FocusAdapter() {
		@Override public void focusGained(FocusEvent fe) {
			if (key_event != null) {
				editor.dispatchEvent(key_event);
				key_event = null;
			}
		}
		@Override public void focusLost(FocusEvent fe) {
			if (EditMode.getMode() == EditMode.AFTERKEY)
				setEditable(false);
			fireActionEvent();
		}
	};

	/** Action listener for editor events */
	private final ActionListener editor_listener = new ActionListener() {
		@Override public void actionPerformed(ActionEvent ae) {
			if (EditMode.getMode() == EditMode.AFTERKEY)
				setEditable(false);
		}
	};

	/** Create a sign text combo box */
	public SignTextCBox() {
		setMaximumRowCount(21);
		// NOTE: We use a prototype display value so that combo boxes
		//       are always the same size.  This prevents all the
		//       widgets from being rearranged whenever a new sign is
		//       selected.
		setPrototypeDisplayValue(PROTOTYPE_SIGN_TEXT);
		setRenderer(new SignTextCellRenderer());
		setEditMode();
		setEditor(editor);
		addKeyListener(key_listener);
		editor.addFocusListener(focus_listener);
		editor.addActionListener(editor_listener);
	}

	/** Dispose of the message combo box */
	public void dispose() {
		editor.removeActionListener(editor_listener);
		editor.removeFocusListener(focus_listener);
		removeKeyListener(key_listener);
	}

	/** Set the edit mode */
	public void setEditMode() {
		setEditable(EditMode.getMode() == EditMode.ALWAYS);
	}

	/** Get message text */
	public String getMessage() {
		Object item = (EditMode.getMode() == EditMode.ALWAYS)
			? editor.getItem()
			: getSelectedItem();
		return getMulti(item);
	}
}
