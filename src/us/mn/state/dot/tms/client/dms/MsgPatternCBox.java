/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2025  Minnesota Department of Transportation
 * Copyright (C) 2010       AHMCT, University of California
 * Copyright (C) 2025       SRF Consulting Group
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

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.MsgPattern;
import us.mn.state.dot.tms.MsgPatternHelper;
import us.mn.state.dot.tms.utils.MultiString;
import us.mn.state.dot.tms.utils.TextRect;

/**
 * The message pattern combobox is a widget which allows the user to select
 * a message pattern.
 *
 * @see us.mn.state.dot.tms.MsgPattern
 *
 * @author Douglas Lau
 * @author Michael Darter
 * @author John L. Stanley - SRF Consulting
 */
public class MsgPatternCBox extends JComboBox<MsgPattern> {

	/** Get page count of a MULTI string */
	static private Integer getPageCount(String ms) {
		MultiString multi = new MultiString(ms);
		return multi.isBlank() ? null : multi.getNumPages();
	}

	/** Populate the message pattern model, sorted */
	public void populateModel(DMS dms) {
		DefaultComboBoxModel<MsgPattern> mdl =
			new DefaultComboBoxModel<MsgPattern>();
		for (MsgPattern pat: MsgPatternHelper.findAllCompose(dms))
			mdl.addElement(pat);
		mdl.setSelectedItem(null);
		setModel(mdl);
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
		Integer pageCnt = getPageCount(ms);
		MsgPattern best = null;
		for (int i = 0; i < getItemCount(); i++) {
			MsgPattern pat = getItemAt(i);
			String multi = pat.getMulti();
			// Make sure pattern has same number of pages
			if (pageCnt != null) {
				Integer pc = getPageCount(multi);
				if (pc != null && pageCnt != pc)
					continue;
			}
			// check for perfect match
			if (multi.length() > 0 && multi.equals(ms))
				return pat;
			// check if pattern has "fillable" text rectangles
			if (tr.find(multi).size() > 0)
				best = MsgPatternHelper.better(best, pat);
		}
		return best;
	}
}
