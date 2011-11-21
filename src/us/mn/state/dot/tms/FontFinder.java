/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011  Minnesota Department of Transportation
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

import java.util.Collection;
import java.util.LinkedList;
import us.mn.state.dot.sonar.Checker;

/**
 * Helper class for finding fonts for DMS.
 *
 * @author Douglas Lau
 */
public class FontFinder {

	/** Collection of all sign groups for the DMS */
	private final Collection<SignGroup> groups;

	/** List of fonts numbers found */
	private final LinkedList<Integer> fonts = new LinkedList<Integer>();

	/** MULTI adapter which records font numbers from tags */
	private final MultiAdapter fontTagFinder = new MultiAdapter() {
		public void setFont(int f_num, String f_id) {
			if(!fonts.contains(f_num))
				fonts.add(f_num);
		}
	};

	/** Create a font finder for a DMS */
	public FontFinder(DMS dms) {
		Font df = dms.getDefaultFont();
		if(df != null)
			fonts.add(df.getNumber());
		groups = SignGroupHelper.find(dms);
		findQuickMessageTags();
		findSignTextTags();
		findDmsActionTags();
	}

	/** Get a list of all fonts found */
	public LinkedList<Font> getFonts() {
		LinkedList<Font> _fonts = new LinkedList<Font>();
		for(Integer fn: fonts) {
			Font f = FontHelper.find(fn);
			if(f != null)
				_fonts.add(f);
		}
		return _fonts;
	}

	/** Find font tags in all quick messages for the sign's groups */
	private void findQuickMessageTags() {
		QuickMessageHelper.find(new Checker<QuickMessage>() {
			public boolean check(QuickMessage qm) {
				SignGroup sg = qm.getSignGroup();
				if(sg != null && groups.contains(sg))
					findFontTags(qm.getMulti());
				return false;
			}
		});
	}

	/** Find font tags in all SignText for the sign's groups */
	private void findSignTextTags() {
		SignTextHelper.find(new Checker<SignText>() {
			public boolean check(SignText st) {
				if(groups.contains(st.getSignGroup()))
					findFontTags(st.getMulti());
				return false;
			}
		});
	}

	/** Find font tags in all DMS actions for the sign's groups */
	private void findDmsActionTags() {
		DmsActionHelper.find(new Checker<DmsAction>() {
			public boolean check(DmsAction da) {
				SignGroup sg = da.getSignGroup();
				QuickMessage qm = da.getQuickMessage();
				if(sg != null && qm != null &&
				   groups.contains(sg))
					findFontTags(qm.getMulti());
				return false;
			}
		});
	}

	/** Find font tags in a MULTI string */
	private void findFontTags(String multi) {
		MultiParser.parse(multi, fontTagFinder);
	}
}
