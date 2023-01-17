/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2023  Minnesota Department of Transportation
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
	/** Rank for on-the-fly messages */
	static private final short ON_THE_FLY_RANK = 99;

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
	public void add(MsgLine st) {
		items.add(st);
	}

	/** Get the element at the specified index */
	@Override
	public MsgLine getElementAt(int index) {
		int i = 0;
		for (MsgLine t: items) {
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

	/** Selected MsgLine item */
	private MsgLine selected;

	/** Get the selected item */
	@Override
	public Object getSelectedItem() {
		MsgLine mt = selected;
		// filter lines that should be ignored
		if (mt != null && mt instanceof TransMsgLine) {
			if (DMSHelper.ignoreLineFilter(mt.getMulti()))
				return BLANK_LINE;
		}
		return mt;
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
		MultiString multi = new MultiString(s.trim()).normalizeLine();
		if (multi.isBlank())
			return BLANK_LINE;
		String ms = multi.toString();
		MsgLine mt = lookupMessage(ms);
		return (mt != null)
		      ? mt
		      : new TransMsgLine(ms, line, ON_THE_FLY_RANK);
	}

	/** Lookup a message line.
	 * @return Existing MsgLine, or null if not found. */
	private MsgLine lookupMessage(String ms) {
		for (MsgLine mt: items) {
			if (ms.equals(mt.getMulti()))
				return mt;
		}
		return null;
	}
}
