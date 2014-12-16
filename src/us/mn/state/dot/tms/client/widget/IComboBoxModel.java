/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2014  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.widget;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataListener;
import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.tms.client.proxy.ProxyListModel;

/**
 * IComboBoxModel is a ComboBoxModel which can be used as a wrapper for a
 * ProxyListModel.  It is useful for making multiple JComboBoxes which have
 * the same underlying ListModel, but have a different "selection".
 *
 * @author Douglas Lau
 */
public class IComboBoxModel<T extends SonarObject> extends AbstractListModel
	implements ComboBoxModel
{
	/** Blank entry in combo box */
	static private final String BLANK = " ";

	/** Underlying list model */
	private final ProxyListModel<T> model;

	/** Flag to allow null selection */
	private final boolean null_allowed;

	/** Create a new IComboBoxModel */
	public IComboBoxModel(ProxyListModel<T> m) {
		this(m, true);
	}

	/** Create a new IComboBoxModel */
	public IComboBoxModel(ProxyListModel<T> m, boolean na) {
		model = m;
		null_allowed = na;
	}

	/** Add a list data listener */
	@Override
	public void addListDataListener(ListDataListener l) {
		model.addListDataListener(l);
		super.addListDataListener(l);
	}

	/** Remove a list data listener */
	@Override
	public void removeListDataListener(ListDataListener l) {
		model.removeListDataListener(l);
		super.removeListDataListener(l);
	}

	/** Get an element from the list model */
	@Override
	public Object getElementAt(int i) {
		if (extra != null) {
			if (i == 0)
				return extra;
			i--;
		}
		if (null_allowed)
			return (i > 0) ? model.getElementAt(i - 1) : BLANK;
		else
			return model.getElementAt(i);
	}

	/** Get the size of the model */
	@Override
	public int getSize() {
		return model.getSize() + getExtraCount();
	}

	/** Get count of extra elements in model */
	private int getExtraCount() {
		if (null_allowed)
			return (extra != null) ? 2 : 1;
		else
			return (extra != null) ? 1 : 0;
	}

	/** Selected item in the list */
	private T selected = null;

	/** Extra item is an item which is not in the underlying list model,
	 * but was selected with setSelectedItem() */
	private T extra = null;

	/** Set the selected item */
	@Override
	public void setSelectedItem(Object s) {
		T sel = (!BLANK.equals(s)) ? (T)s : null;
		extra = isExtra(sel) ? sel : null;
		selected = sel;
		fireContentsChanged(this, -1, -1);
	}

	/** Check if a selected item is extra */
	private boolean isExtra(T s) {
		return (s != null) && model.getIndex(s) < 0;
	}

	/** Get the selected item */
	@Override
	public Object getSelectedItem() {
		return selected;
	}

	/** Get the selected proxy */
	public T getSelectedProxy() {
		return selected;
	}
}
