/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011-2023  Minnesota Department of Transportation
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
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import us.mn.state.dot.tms.utils.MultiAdapter;
import us.mn.state.dot.tms.utils.MultiString;

/**
 * Helper class for finding fonts for DMS.
 *
 * @author Douglas Lau
 */
public class FontFinder {

	/** DMS to search */
	private final DMS dms;

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
	public FontFinder(DMS d) {
		dms = d;
		font_nums.add(DMSHelper.getDefaultFontNum(dms));
		findMsgPatternTags();
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

	/** Find font tags in all message patterns for the sign's hashtags */
	private void findMsgPatternTags() {
		Iterator<MsgPattern> it = MsgPatternHelper.iterator();
		while (it.hasNext()) {
			MsgPattern pat = it.next();
			String cht = pat.getComposeHashtag();
			if (DMSHelper.hasHashtag(dms, cht))
				findFontTags(pat.getMulti());
		}
	}

	/** Find font tags in all DMS actions for the sign's hashtags */
	private void findDmsActionTags() {
		Iterator<DmsAction> it = DmsActionHelper.iterator();
		while (it.hasNext()) {
			DmsAction da = it.next();
			MsgPattern pat = da.getMsgPattern();
			if (pat != null) {
				String ht = da.getDmsHashtag();
				if (DMSHelper.hasHashtag(dms, ht))
					findFontTags(pat.getMulti());
			}
		}
	}

	/** Find font tags in a MULTI string */
	private void findFontTags(String multi) {
		new MultiString(multi).parse(fontTagFinder);
	}
}
