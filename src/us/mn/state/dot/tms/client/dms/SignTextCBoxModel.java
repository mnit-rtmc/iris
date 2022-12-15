/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2022  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.SignText;
import us.mn.state.dot.tms.utils.MultiString;

/**
 * Model for a sign text line combo box.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class SignTextCBoxModel extends AbstractListModel<SignText>
	implements ComboBoxModel<SignText>
{
	/** Rank for on-the-fly created sign messages */
	static private final short ON_THE_FLY_RANK = 99;

	/** Blank client-side sign text object */
	static private final SignText BLANK_SIGN_TEXT =
		new ClientSignText("");

	/** Set of sorted SignText items */
	private final TreeSet<SignText> items =
		new TreeSet<SignText>(new SignTextComparator());

	/** Sign text line number */
	private final short line;

	/** Create a new sign text combo box model.
	 * @param ln Sign line number. */
	public SignTextCBoxModel(short ln) {
		line = ln;
		items.add(BLANK_SIGN_TEXT);
	}

	/** Create a blank sign text combo box model */
	public SignTextCBoxModel() {
		line = 0;
	}

	/** Add a SignText to the model.
	 * NOTE: Do not call this after using in SignTextCBox */
	public void add(SignText st) {
		items.add(st);
	}

	/** Get the element at the specified index */
	@Override
	public SignText getElementAt(int index) {
		int i = 0;
		for (SignText t: items) {
			if (i == index)
				return t;
			i++;
		}
		return null;
	}

	/** Get the number of elements in the model */
	@Override
	public int getSize() {
		return items.size();
	}

	/** Selected SignText item */
	private SignText selected;

	/** Get the selected item */
	@Override
	public Object getSelectedItem() {
		SignText st = selected;
		// filter lines that should be ignored
		if (st != null && st instanceof ClientSignText) {
			if (DMSHelper.ignoreLineFilter(st.getMulti()))
				return BLANK_SIGN_TEXT;
		}
		return st;
	}

	/**
	 * Set the selected item.  This method is called by the combobox when:
	 *   -the focus leaves the combobox with a String arg when editable.
	 *   -a combobox item is clicked on via the mouse.
	 *   -a combobox item is moved to via the cursor keys.
	 */
	@Override
	public void setSelectedItem(Object item) {
		if (item instanceof SignText)
			selected = (SignText) item;
		else if (item instanceof String)
			selected = getSignText((String) item);
		else
			selected = null;
		// this results in a call to the editor's setItem method
		fireContentsChanged(this, -1, -1);
	}

	/** Get or create a sign text for the given string */
	private SignText getSignText(String s) {
		MultiString multi = new MultiString(s.trim()).normalizeLine();
		if (multi.isBlank())
			return BLANK_SIGN_TEXT;
		String ms = multi.toString();
		SignText st = lookupMessage(ms);
		return (st != null)
		      ? st
		      : new ClientSignText(ms, line, ON_THE_FLY_RANK);
	}

	/** Lookup a sign text.
	 * @return Existing SignText, or null if not found. */
	private SignText lookupMessage(String ms) {
		for (SignText st: items) {
			if (ms.equals(st.getMulti()))
				return st;
		}
		return null;
	}
}
