/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011-2016  Minnesota Department of Transportation
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
import java.util.LinkedList;
import us.mn.state.dot.tms.utils.MultiAdapter;
import us.mn.state.dot.tms.utils.MultiString;

/**
 * Helper class for finding fonts for DMS.
 *
 * @author Douglas Lau
 */
public class FontFinder {

	/** List of all sign groups for the DMS */
	private final LinkedList<SignGroup> groups;

	/** List of fonts numbers found */
	private final LinkedList<Integer> fonts = new LinkedList<Integer>();

	/** MULTI adapter which records font numbers from tags */
	private final MultiAdapter fontTagFinder = new MultiAdapter() {
		@Override
		public void setFont(int f_num, String f_id) {
			if(!fonts.contains(f_num))
				fonts.add(f_num);
		}
	};

	/** Create a font finder for a DMS */
	public FontFinder(DMS dms) {
		SignConfig sc = dms.getSignConfig();
		if (sc != null) {
			Font df = sc.getDefaultFont();
			if (df != null)
				fonts.add(df.getNumber());
		}
		groups = findGroups(dms);
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

	/** Find all sign groups for the DMS */
	private LinkedList<SignGroup> findGroups(DMS dms) {
		LinkedList<SignGroup> g = new LinkedList<SignGroup>();
		Iterator<DmsSignGroup> it = DmsSignGroupHelper.iterator();
		while(it.hasNext()) {
			DmsSignGroup dsg = it.next();
			if(dsg.getDms() == dms)
				g.add(dsg.getSignGroup());
		}
		return g;
	}

	/** Find font tags in all quick messages for the sign's groups */
	private void findQuickMessageTags() {
		Iterator<QuickMessage> it = QuickMessageHelper.iterator();
		while(it.hasNext()) {
			QuickMessage qm = it.next();
			SignGroup sg = qm.getSignGroup();
			if(sg != null && groups.contains(sg))
				findFontTags(qm.getMulti());
		}
	}

	/** Find font tags in all SignText for the sign's groups */
	private void findSignTextTags() {
		Iterator<SignText> it = SignTextHelper.iterator();
		while(it.hasNext()) {
			SignText st = it.next();
			if(groups.contains(st.getSignGroup()))
				findFontTags(st.getMulti());
		}
	}

	/** Find font tags in all DMS actions for the sign's groups */
	private void findDmsActionTags() {
		Iterator<DmsAction> it = DmsActionHelper.iterator();
		while(it.hasNext()) {
			DmsAction da = it.next();
			SignGroup sg = da.getSignGroup();
			QuickMessage qm = da.getQuickMessage();
			if(sg != null && qm != null && groups.contains(sg))
				findFontTags(qm.getMulti());
		}
	}

	/** Find font tags in a MULTI string */
	private void findFontTags(String multi) {
		new MultiString(multi).parse(fontTagFinder);
	}
}
