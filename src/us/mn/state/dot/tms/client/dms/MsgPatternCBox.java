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
import java.util.List;
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

	/** Compare two message patterns.
	 * Patterns with a trailing text rectangle are preferred. */
	static private MsgPattern betterPattern(MsgPattern p0, MsgPattern p1) {
		if (p0 == null)
			return p1;
		if (p1 == null)
			return p0;
		String t0 = new MultiString(p0.getMulti())
			.trailingTextRectangle();
		String t1 = new MultiString(p1.getMulti())
			.trailingTextRectangle();
		if (t0 == null && t1 != null)
			return p1;
		else if (t1 == null && t0 != null)
			return p0;
		else {
			int l0 = p0.getMulti().length();
			int l1 = p1.getMulti().length();
			return (l0 < l1) ? p0 : p1;
		}
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
			setSelectedItem(null);
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
			String multi = pat.getMulti();
			// check for perfect match
			if (multi.length() > 0 && multi.equals(ms))
				return pat;
			// check if pattern has "fillable" text rectangles
			if (tr.find(multi).size() > 0)
				best = betterPattern(best, pat);
		}
		return best;
	}

	/** Find a substitute pattern containing message lines */
	public MsgPattern findSubstitutePattern(TextRect tr,
		MsgPattern pattern)
	{
		assert tr != null;
		List<TextRect> rects = tr.find(pattern.getMulti());
		return (rects.size() > 0)
		      ? findSubstitutePattern(tr, rects.get(0).getLineCount())
		      : null;
	}

	/** Find a substitute pattern containing message lines */
	private MsgPattern findSubstitutePattern(TextRect tr, int n_lines) {
		for (int i = 0; i < getItemCount(); i++) {
			MsgPattern pat = getItemAt(i);
			List<TextRect> rects = tr.find(pat.getMulti());
			if (rects.size() > 0 &&
			    rects.get(0).getLineCount() == n_lines &&
			    MsgPatternHelper.hasLines(pat))
				return pat;
		}
		return null;
	}
}
