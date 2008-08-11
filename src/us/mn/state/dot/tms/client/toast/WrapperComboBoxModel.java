/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.toast;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;

/**
 * WrapperComboBoxModel is a ComboBoxModel which can be used as a wrapper for
 * any ListModel.  It is useful for making multiple JComboBoxes which have
 * the same underlying ListModel, but have a different "selection".
 *
 * @author Douglas Lau
 */
public class WrapperComboBoxModel extends AbstractListModel
	implements ComboBoxModel
{
	/** Blank entry in combo box */
	static public final String BLANK = " ";

	/** Underlying list model */
	protected final ListModel model;

	/** Are blank selections allowed? */
	protected final boolean blanks;

	/** Create a new WrapperComboBoxModel */
	public WrapperComboBoxModel(ListModel m) {
		this(m, true);
	}

	/** Create a new WrapperComboBoxModel */
	public WrapperComboBoxModel(ListModel m, boolean b) {
		model = m;
		blanks = b;
	}

	/** Add a list data listener */
	public void addListDataListener(ListDataListener l) {
		model.addListDataListener(l);
		super.addListDataListener(l);
	}

	/** Remove a list data listener */
	public void removeListDataListener(ListDataListener l) {
		model.removeListDataListener(l);
		super.removeListDataListener(l);
	}

	/** Get an element from the list model */
	public Object getElementAt(int index) {
		try {
			synchronized(model) {
				if(extra != null) {
					if(index == 0)
						return extra;
					index--;
				}
				if(blanks) {
					if(index == 0)
						return BLANK;
					return model.getElementAt(index - 1);
				} else
					return model.getElementAt(index);
			}
		}
		catch(ArrayIndexOutOfBoundsException e) {
			return null;
		}
	}

	/** Get the size of the model */
	public int getSize() {
		int b = blanks ? 1 : 0;
		int e = extra != null ? 1 : 0;
		synchronized(model) {
			return model.getSize() + b + e;
		}
	}

	/** Selected item in the list */
	protected Object selected = null;

	/** Extra item is an item which is not in the underlying list model,
	 * but was selected with setSelectedItem() */
	protected Object extra = null;

	/** Set the selected item */
	public void setSelectedItem(Object s) {
		synchronized(model) {
			boolean isExtra = true;
			if(BLANK.equals(s)) {
				s = null;
				isExtra = false;
			} else {
				int c = model.getSize();
				for(int i = 0; i < c; i++) {
					Object o = model.getElementAt(i);
					if(o.equals(s)) {
						isExtra = false;
						break;
					}
				}
			}
			if(isExtra)
				extra = s;
			selected = s;
		}
		fireContentsChanged(this, -1, -1);
	}

	/** Get the selected item */
	public Object getSelectedItem() {
		return selected;
	}
}
