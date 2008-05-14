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
import us.mn.state.dot.tms.SignText;

/**
 * Model for a sign text line combo box
 *
 * @author Douglas Lau
 */
public class LineModel extends AbstractListModel implements ComboBoxModel {

	/** Set of items in the line model */
	protected final TreeSet<SignText> items =
		new TreeSet<SignText>(new SignTextComparator());

	/** Create a new line model */
	protected LineModel() {
		items.add(new BlankSignText());
	}

	/** Get the element at the specified index */
	public Object getElementAt(int index) {
		int i = 0;
		for(SignText t: items) {
			if(i == index)
				return t;
			i++;
		}
		return null;
	}

	/** Get the number of elements in the model */
	public int getSize() {
		return items.size();
	}

	/** Selected item */
	protected SignText selected;

	/** Get the selected item */
	public Object getSelectedItem() {
		return selected;
	}

	/** Set the selected item */
	public void setSelectedItem(Object s) {
		if(s instanceof String)
			selected = lookupMessage((String)s);
		else if(s instanceof SignText)
			selected = (SignText)s;
		else
			selected = null;
	}

	/** Lookup a sign text message */
	protected SignText lookupMessage(String t) {
		for(SignText st: items) {
			if(st.getMessage().equals(t))
				return st;
		}
		return null;
	}

	/** Find the index of an item */
	protected int find(SignText t) {
		int i = 0;
		for(SignText st: items) {
			if(st.equals(t))
				return i;
			i++;
		}
		return -1;
	}

	/** Add a sign text to the model */
	protected void add(SignText t) {
		items.add(t);
		final int i = find(t);
		assert i >= 0;
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
			items.remove(t);
			if(t.equals(selected))
				selected = null;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					fireIntervalRemoved(this, i, i);
				}
			});
		}
	}

	/** Change a sign text in the model */
	protected void change(SignText t) {
		final int i0 = preChangeRow(t);
		if(i0 >= 0) {
			items.add(t);
			final int i1 = find(t);
			assert i1 >= 0;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					fireContentsChanged(this,
						Math.min(i0, i1),
						Math.max(i0, i1));
				}
			});
		}
	}

	/** Find and remove a sign text from the model */
	protected int preChangeRow(SignText t) {
		// we cannot trust the TreeSet to remove the item,
		// because the sort order may have changed
		Iterator<SignText> it = items.iterator();
		for(int i = 0; it.hasNext(); i++) {
			if(t.equals(it.next())) {
				it.remove();
				return i;
			}
		}
		return -1;
	}
}
