/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2022  Minnesota Department of Transportation
 * Copyright (C) 2010  AHMCT, University of California
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
import java.util.Set;
import java.util.TreeSet;
import javax.swing.JComboBox;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DmsSignGroupHelper;
import us.mn.state.dot.tms.MsgPattern;
import us.mn.state.dot.tms.MsgPatternHelper;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.utils.MultiString;
import us.mn.state.dot.tms.utils.NumericAlphaComparator;

/**
 * The message pattern combobox is a widget which allows the user to select
 * a message pattern.
 *
 * @see us.mn.state.dot.tms.MsgPattern
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class MsgPatternCBox extends JComboBox<MsgPattern> {

	/** Check if a message pattern should be included in combo box */
	static private boolean isValidMulti(MsgPattern pat) {
		MultiString ms = new MultiString(pat.getMulti());
		return ms.isValidMulti();
	}

	/** Populate the message pattern model, sorted */
	public void populateModel(DMS dms) {
		setSelectedIndex(-1);
		TreeSet<MsgPattern> msgs = createMessageSet(dms);
		// check for a fillable pattern
		boolean fillable = false;
		for (MsgPattern pat: msgs) {
			int n = MsgPatternHelper.findTextRectangles(pat).size();
			if (n > 0) {
				fillable = true;
				break;
			}
		}
		removeAllItems();
		if (!fillable)
			addItem(null);
		for (MsgPattern pat: msgs)
			addItem(pat);
	}

	/** Create a set of message patterns for the specified DMS */
	private TreeSet<MsgPattern> createMessageSet(DMS dms) {
		TreeSet<MsgPattern> msgs = new TreeSet<MsgPattern>(
			new NumericAlphaComparator<MsgPattern>());
		Set<SignGroup> groups = DmsSignGroupHelper.findGroups(dms);
		Iterator<MsgPattern> pit = MsgPatternHelper.iterator();
		while (pit.hasNext()) {
			MsgPattern pat = pit.next();
			if (groups.contains(pat.getSignGroup()) &&
			    isValidMulti(pat))
			{
				msgs.add(pat);
			}
		}
		return msgs;
	}

	/** Set the enabled status */
	@Override
	public void setEnabled(boolean e) {
		super.setEnabled(e);
		if (!e) {
			setSelectedIndex(-1);
			removeAllItems();
		}
	}

	/** Get the selected message pattern */
	public MsgPattern getSelectedPattern() {
		Object obj = getSelectedItem();
		return (obj instanceof MsgPattern)
		      ? (MsgPattern) obj
		      : null;
	}

	/** Set the pattern from a matching MULTI string */
	public void setMulti(String ms) {
		MsgPattern best = null;
		for (int i = 0; i < getItemCount(); i++) {
			MsgPattern pat = getItemAt(i);
			if (pat != null) {
				int n = MsgPatternHelper
					.findTextRectangles(pat).size();
				if (n > 0) {
					if (best != null) {
						int blen = best.getMulti()
							.length();
						int len = pat.getMulti()
							.length();
						if (len < blen)
							best = pat;
					} else
						best = pat;
				}
			}
		}
		setSelectedItem(best);
	}
}
