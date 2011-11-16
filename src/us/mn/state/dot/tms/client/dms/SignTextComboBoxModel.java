/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2011  Minnesota Department of Transportation
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

import java.util.TreeSet;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.SwingUtilities;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.MultiParser;
import us.mn.state.dot.tms.SignText;
import us.mn.state.dot.tms.SignGroup;

/**
 * Model for a sign text line combo box
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class SignTextComboBoxModel extends AbstractListModel
	implements ComboBoxModel
{
	/** Priority for on-the-fly created sign messages */
	static protected final short ON_THE_FLY_PRIORITY = 99;

	/** Blank client-side sign text object */
	static protected final SignText BLANK_SIGN_TEXT =
		new ClientSignText("");

	/** Set of sorted SignText items in the line model */
	protected final TreeSet<SignText> m_items =
		new TreeSet<SignText>(new SignTextComparator());

	/** combobox line number */
	protected final short m_cbline;

	/** Shortcut to container */
	protected final SignTextModel m_signTextModel;

	/** 
	 * Create a new line model.
	 * @param cbline Combobox line number associated with this
	 * SignTextComboBoxModel.
	 * @param smm The container.
	 */
	protected SignTextComboBoxModel(short cbline, SignTextModel stm) {
		m_cbline = cbline;
		m_signTextModel = stm;
		m_items.add(BLANK_SIGN_TEXT);
	}

	/** Get the element at the specified index */
	public Object getElementAt(int index) {
		int i = 0;
		for(SignText t: m_items) {
			if(i == index)
				return t;
			i++;
		}
		return null;
	}

	/** Get the number of elements in the model */
	public int getSize() {
		return m_items.size();
	}

	/** Selected item, either a String or SignText */
	protected SignText m_selected;

	/** Get the selected item */
	public Object getSelectedItem() {
		SignText st = m_selected;
		// filter lines that should be ignored
		if(st != null && st instanceof ClientSignText) {
			if(DMSHelper.ignoreLineFilter(st.getMulti()))
				return BLANK_SIGN_TEXT;
		}
		return st;
	}

	/** 
	 * Set the selected item. This method is called by the combobox when:
	 * 	-the focus leaves the combobox with a String arg when editable.
	 *      -a combobox item is clicked on via the mouse.
	 *      -a combobox item is moved to via the cursor keys.
	 */
	public void setSelectedItem(Object s) {
		if(s instanceof String)
			m_selected = getSignText((String)s);
		else if(s instanceof SignText)
			m_selected = (SignText)s;
		else {
			m_selected = null;
		}
		// this results in a call to the editor's setSelectedItem method
		fireContentsChanged(this, -1, -1);
	}

	/** Get or create a sign text for the given string */
	protected SignText getSignText(String s) {
		if(s.length() == 0)
			return BLANK_SIGN_TEXT;
		SignText st = lookupMessage(s);
		if(st != null)
			return st;
		else {
			return new ClientSignText(MultiParser.normalize(
				s.trim()));
		}
	}

	/** 
	 * Lookup a sign text message 
	 * @return SignText of existing message else null if message doesn't
	 * exist.
	 */
	protected SignText lookupMessage(String t) {
		for(SignText st: m_items) {
			if(t.equals(st.getMulti()))
				return st;
		}
		return null;
	}

	/** Find the index of an item */
	protected int find(SignText t) {
		int i = 0;
		for(SignText st: m_items) {
			if(st.equals(t))
				return i;
			i++;
		}
		return -1;
	}

	/** Add a SignText to the model */
	protected void add(SignText t) {
		// fails if item already exists in list
		if(!m_items.add(t))
			return;
		final int i = find(t);
		assert i >= 0 : "Failed to find SignText after just adding in SignTextComboBoxModel.add()";
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				fireIntervalAdded(this, i, i);
			}
		});
	}

	/** Remove a sign text from the model */
	protected void remove(SignText t) {
		final int i = find(t);
		if(i >= 0) {
			m_items.remove(t);
			if(t.equals(m_selected))
				m_selected = null;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					fireIntervalRemoved(this, i, i);
				}
			});
		}
	}

	/** Update the message library with the currently selected messages */
	public void updateMessageLibrary() {
		SignText st = m_selected;
		if(st instanceof ClientSignText && st != BLANK_SIGN_TEXT)
			addMsgToLib(st.getMulti());
	}

	/** Add a message to the local sign group library */
	protected void addMsgToLib(String multi) {
		m_signTextModel.createSignText(m_cbline, multi,
			ON_THE_FLY_PRIORITY);
	}
}
