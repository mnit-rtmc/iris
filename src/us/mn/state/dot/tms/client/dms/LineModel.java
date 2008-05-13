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

import java.util.TreeSet;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import us.mn.state.dot.tms.SignText;

/**
 * Model for a sign text line combo box
 *
 * @author Douglas Lau
 */
public class LineModel extends AbstractListModel implements ComboBoxModel {

	protected final TreeSet<SignText> items =
		new TreeSet<SignText>(new SignTextComparator());

	protected LineModel() {
		items.add(new BlankSignText());
	}

	public Object getElementAt(int index) {
		int i = 0;
		for(SignText t: items) {
			if(i == index)
				return t;
			i++;
		}
		return null;
	}

	public int getSize() {
		return items.size();
	}

	protected Object selected;

	public Object getSelectedItem() {
		return selected;
	}

	public void setSelectedItem(Object s) {
		if(s instanceof String)
			s = lookupMessage((String)s);
		selected = s;
	}

	protected SignText lookupMessage(String t) {
		for(SignText st: items) {
			if(st.getMessage().equals(t))
				return st;
		}
		return null;
	}

	protected int find(SignText t) {
		int i = 0;
		for(SignText st: items) {
			if(st.equals(t))
				return i;
			i++;
		}
		return -1;
	}

	protected void add(SignText t) {
		items.add(t);
		int i = find(t);
		assert i >= 0;
		fireIntervalAdded(this, i, i);
	}

	protected void remove(SignText t) {
		int i = find(t);
		if(i >= 0) {
			items.remove(t);
			if(t.equals(selected))
				selected = null;
			fireIntervalRemoved(this, i, i);
		}
	}

	protected void change(SignText t) {
		int i0 = find(t);
		if(i0 >= 0) {
			items.remove(t);
			items.add(t);
			int i1 = find(t);
			assert i1 >= 0;
			fireContentsChanged(this, Math.min(i0, i1),
				Math.max(i0, i1));
		}
	}
}
