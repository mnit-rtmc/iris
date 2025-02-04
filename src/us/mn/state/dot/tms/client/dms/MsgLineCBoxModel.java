/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2025  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.MsgLine;
import us.mn.state.dot.tms.TransMsgLine;
import us.mn.state.dot.tms.utils.MultiString;

/**
 * Model for a message line combo box.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class MsgLineCBoxModel extends AbstractListModel<MsgLine>
	implements ComboBoxModel<MsgLine>
{
	/** Rank for free-form messages */
	static private final short FREE_FORM_RANK = 99;

	/** Blank client-side message line */
	static private final MsgLine BLANK_LINE =
		new TransMsgLine("");

	/** Set of sorted MsgLine items */
	private final TreeSet<MsgLine> items =
		new TreeSet<MsgLine>(new MsgLineComparator());

	/** Message line number */
	private final short line;

	/** Create a new message line combo box model.
	 * @param ln Line number. */
	public MsgLineCBoxModel(short ln) {
		line = ln;
		items.add(BLANK_LINE);
	}

	/** Create a blank message line combo box model */
	public MsgLineCBoxModel() {
		line = 0;
	}

	/** Add a MsgLine to the model.
	 * NOTE: Do not call this after using in MsgLineCBox */
	public void add(MsgLine ml) {
		items.add(ml);
	}

	/** Get the element at the specified index */
	@Override
	public MsgLine getElementAt(int index) {
		int i = 0;
		for (MsgLine ml: items) {
			if (i == index)
				return ml;
			i++;
		}
		return null;
	}

	/** Get the number of elements in the model */
	@Override
	public int getSize() {
		return items.size();
	}

	/** Selected MsgLine item */
	private MsgLine selected;

	/** Get the selected item */
	@Override
	public Object getSelectedItem() {
		MsgLine ml = selected;
		// filter lines that should be ignored
		if (ml != null && ml instanceof TransMsgLine) {
			if (DMSHelper.ignoreLineFilter(ml.getMulti()))
				return BLANK_LINE;
		}
		return ml;
	}

	/**
	 * Set the selected item.  This method is called by the combobox when:
	 *   -the focus leaves the combobox with a String arg when editable.
	 *   -a combobox item is clicked on via the mouse.
	 *   -a combobox item is moved to via the cursor keys.
	 */
	@Override
	public void setSelectedItem(Object item) {
		if (item instanceof MsgLine)
			selected = (MsgLine) item;
		else if (item instanceof String)
			selected = getMsgLine((String) item);
		else
			selected = null;
		// this results in a call to the editor's setItem method
		fireContentsChanged(this, -1, -1);
	}

	/** Get or create a message line for the given string */
	private MsgLine getMsgLine(String s) {
		MultiString multi = new MultiString(s).normalizeLine();
		if (multi.isBlank())
			return BLANK_LINE;
		String ms = multi.toString();
		MsgLine ml = lookupMsgLine(ms);
		return (ml != null)
		      ? ml
		      : new TransMsgLine(ms, line, FREE_FORM_RANK);
	}

	/** Lookup a message line.
	 * @return Existing MsgLine, or null if not found. */
	private MsgLine lookupMsgLine(String ms) {
		for (MsgLine ml: items) {
			if (ms.equals(ml.getMulti()))
				return ml;
		}
		return null;
	}
}
