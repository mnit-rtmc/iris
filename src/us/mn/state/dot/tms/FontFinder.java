/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011-2022  Minnesota Department of Transportation
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
package us.mn.state.dot.tms;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;
import us.mn.state.dot.tms.utils.MultiAdapter;
import us.mn.state.dot.tms.utils.MultiString;

/**
 * Helper class for finding fonts for DMS.
 *
 * @author Douglas Lau
 */
public class FontFinder {

	/** List of all sign groups for the DMS */
	private final ArrayList<SignGroup> groups;

	/** Set of font numbers found */
	private final HashSet<Integer> font_nums = new HashSet<Integer>();

	/** MULTI adapter which records font numbers from tags */
	private final MultiAdapter fontTagFinder = new MultiAdapter() {
		@Override
		public void setFont(Integer f_num, String f_id) {
			if (f_num != null)
				font_nums.add(f_num);
		}
	};

	/** Create a font finder for a DMS */
	public FontFinder(DMS dms) {
		font_nums.add(DMSHelper.getDefaultFontNum(dms));
		groups = findGroups(dms);
		findQuickMessageTags();
		findSignTextTags();
		findDmsActionTags();
	}

	/** Get a map of all fonts found */
	public TreeMap<Integer, Font> getFonts() {
		TreeMap<Integer, Font> fonts = new TreeMap<Integer, Font>();
		for (Integer fn: font_nums) {
			Font f = FontHelper.find(fn);
			if (f != null)
				fonts.put(fn, f);
		}
		return fonts;
	}

	/** Find all sign groups for the DMS */
	private ArrayList<SignGroup> findGroups(DMS dms) {
		ArrayList<SignGroup> g = new ArrayList<SignGroup>();
		Iterator<DmsSignGroup> it = DmsSignGroupHelper.iterator();
		while (it.hasNext()) {
			DmsSignGroup dsg = it.next();
			if (dsg.getDms() == dms)
				g.add(dsg.getSignGroup());
		}
		return g;
	}

	/** Find font tags in all quick messages for the sign's groups */
	private void findQuickMessageTags() {
		Iterator<QuickMessage> it = QuickMessageHelper.iterator();
		while (it.hasNext()) {
			QuickMessage qm = it.next();
			SignGroup sg = qm.getSignGroup();
			if (sg != null && groups.contains(sg))
				findFontTags(qm.getMulti());
		}
	}

	/** Find font tags in all SignText for the sign's groups */
	private void findSignTextTags() {
		Iterator<SignText> it = SignTextHelper.iterator();
		while (it.hasNext()) {
			SignText st = it.next();
			if (groups.contains(st.getSignGroup()))
				findFontTags(st.getMulti());
		}
	}

	/** Find font tags in all DMS actions for the sign's groups */
	private void findDmsActionTags() {
		Iterator<DmsAction> it = DmsActionHelper.iterator();
		while (it.hasNext()) {
			DmsAction da = it.next();
			SignGroup sg = da.getSignGroup();
			QuickMessage qm = da.getQuickMessage();
			if (sg != null && qm != null && groups.contains(sg))
				findFontTags(qm.getMulti());
		}
	}

	/** Find font tags in a MULTI string */
	private void findFontTags(String multi) {
		new MultiString(multi).parse(fontTagFinder);
	}
}
