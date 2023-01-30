/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2023  Minnesota Department of Transportation
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
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.MsgPattern;
import us.mn.state.dot.tms.MsgPatternHelper;
import us.mn.state.dot.tms.utils.MultiString;
import us.mn.state.dot.tms.utils.NumericAlphaComparator;
import us.mn.state.dot.tms.utils.TextRect;

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

	/** Check if a pattern has "fillable" text rectangles */
	static private boolean isFillable(MsgPattern pat, TextRect tr) {
		assert pat != null && tr != null;
		return tr.find(pat.getMulti()).size() > 0;
	}

	/** Populate the message pattern model, sorted */
	public void populateModel(DMS dms, TextRect tr) {
		DefaultComboBoxModel<MsgPattern> mdl =
			new DefaultComboBoxModel<MsgPattern>();
		TreeSet<MsgPattern> pats = findPatterns(dms);
		for (MsgPattern pat: pats)
			mdl.addElement(pat);
		mdl.setSelectedItem(null);
		setModel(mdl);
	}

	/** Find all message patterns for the specified DMS */
	private TreeSet<MsgPattern> findPatterns(DMS dms) {
		TreeSet<MsgPattern> pats = new TreeSet<MsgPattern>(
			new NumericAlphaComparator<MsgPattern>());
		if (dms == null)
			return pats;
		Iterator<MsgPattern> pit = MsgPatternHelper.iterator();
		while (pit.hasNext()) {
			MsgPattern pat = pit.next();
			String cht = pat.getComposeHashtag();
			if (cht != null && isValidMulti(pat)) {
				if (DMSHelper.hasHashtag(dms, cht))
					pats.add(pat);
			}
		}
		return pats;
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

	/** Find the best pattern for a MULTI string */
	public MsgPattern findBestPattern(String ms, TextRect tr) {
		assert tr != null;
		MsgPattern best = null;
		for (int i = 0; i < getItemCount(); i++) {
			MsgPattern pat = getItemAt(i);
			if (pat != null) {
				String multi = pat.getMulti();
				if (multi.equals(ms)) {
					best = pat;
					break;
				}
				if (isFillable(pat, tr)) {
					if (best != null) {
						int len = multi.length();
						int blen = best.getMulti()
							.length();
						if (len < blen)
							best = pat;
					} else
						best = pat;
				}
			}
		}
		return best;
	}
}
