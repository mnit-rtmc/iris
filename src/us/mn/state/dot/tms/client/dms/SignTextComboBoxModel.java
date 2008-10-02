/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008  Minnesota Department of Transportation
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

import java.util.Iterator;
import java.util.TreeSet;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.SwingUtilities;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.SignText;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.utils.SDMS;

/**
 * Model for a sign text line combo box
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class SignTextComboBoxModel extends AbstractListModel
	implements ComboBoxModel
{
	/** Default priority for newly created sign messages */
	static protected final short DEFAULT_PRIORITY = 50;

	/** Set of sorted SignText items in the line model */
	protected final TreeSet<SignText> m_items =
		new TreeSet<SignText>(new SignTextComparator());

	/** combobox line number */
	protected final short m_cbline;

	/** shortcut to container */
	protected final SignMessageModel m_signMsgModel;

	/** Tms Connection */
	protected final TmsConnection m_tmsConnection;

	/** 
	 * Create a new line model.
	 * @param cbline Combobox line number associated with this
	 * SignTextComboBoxModel.
	 * @param smm The container.
	 */
	protected SignTextComboBoxModel(short cbline, SignMessageModel smm,
		TmsConnection tmsConnection)
	{
		m_cbline = cbline;
		m_signMsgModel = smm;
		m_tmsConnection = tmsConnection;
		m_items.add(new BlankSignText());
	}

	/** Get the element at the specified index */
	public Object getElementAt(int index) {
		int i = 0;
		for(SignText t: m_items) {
			if(i == index) {
				// this is a hack, see the note in
				// ignoreLineHack()
				if(t != null && SDMS.ignoreLineHack(t.toString()))
					return "";
				return t;
			}
			i++;
		}
		return null;
	}

	/** Get the number of elements in the model */
	public int getSize() {
		return m_items.size();
	}

	/** Selected item, either a String or SignText */
	protected Object m_selected;

	/** Get the selected item */
	public Object getSelectedItem() {
		// this is a hack, see the note in ignoreLineHack()
		if(m_selected != null && SDMS.ignoreLineHack(m_selected.toString()))
			return "";
		return m_selected;
	}

	/** 
	 * Set the selected item. This method is called by the combobox when:
	 * 	-the focus leaves the combobox with a String arg when editable.
	 *      -a combobox item is clicked on via the mouse.
	 *      -a combobox item is moved to via the cursor keys.
	 */
	public void setSelectedItem(Object s) {
		if(s instanceof String) {
			// new item entered via editable combobox
			SignText st = lookupMessage((String)s);
			if(st == null) {
				// string not in lib, add it
				addMsgToLib((String)s);
				// note: adding to the lib results in a listener
				// eventually being called which loads the new
				// SignText item into the combobox. If adding to
				// the lib failed (e.g. user not an admin) then
				// a String is loaded into the combobox below
				// anyway.
				m_selected = (String)s;
			} else {
				// string in lib, use SignText as current
				m_selected = st;
			}
		} else if(s instanceof SignText) {
			// SignText already in the list
			SignText st = (SignText)s;
			m_selected = st;
		} else {
			m_selected = null;
			String msg="WARNING: unknown arg type in setSelectedItem(Object)";
			System.err.println(msg);
			assert false : msg;
		}

		// this results in a call to the editor's setSelectedItem method
		fireContentsChanged(this, -1, -1);
	}

	/** 
	 * Add a message line to the persistent library, which will trigger 
	 * notification that the sonar cache type has changed.
	 */
	protected void addMsgToLib(String message) {
		// only admins can add to lib
		if(!m_tmsConnection.isAdmin())
			return;

		// only add message if an identity sign group exists
		SignGroup isg = m_signMsgModel.getIdentitySignGroup();
		if(isg == null)
			return;

		m_signMsgModel.createSignText(isg, m_cbline, message,
			DEFAULT_PRIORITY);
	}

	/** 
	 * Lookup a sign text message 
	 * @return SignText of existing message else null if message doesn't
	 * exist.
	 */
	protected SignText lookupMessage(String t) {
		for(SignText st: m_items) {
			if(st.getMessage().equals(t))
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
}
