/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2023  Minnesota Department of Transportation
 * Copyright (C) 2023       SRF Consulting Group
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import us.mn.state.dot.tms.client.dms.DMSDispatcher;
import us.mn.state.dot.tms.utils.MultiAdapter;
import us.mn.state.dot.tms.utils.MultiBuilder;
import us.mn.state.dot.tms.utils.MultiString;
import us.mn.state.dot.tms.utils.TextRect;

/**
 * Helper class for messages patterns.
 *
 * @author Douglas Lau
 * @author John L. Stanley - SRF Consulting
 */
public class MsgPatternHelper extends BaseHelper {

	/** Don't allow instances to be created */
	private MsgPatternHelper() {
		assert false;
	}

	/** Lookup the message pattern with the specified name */
	static public MsgPattern lookup(String name) {
		return (MsgPattern) namespace.lookupObject(
			MsgPattern.SONAR_TYPE, name);
	}

	/** Get a message pattern iterator */
	static public Iterator<MsgPattern> iterator() {
		return new IteratorWrapper<MsgPattern>(namespace.iterator(
			MsgPattern.SONAR_TYPE));
	}

	/** Find a message pattern with the specified MULTI string.
	 * @param ms MULTI string.
	 * @return A matching message pattern or null if no match is found. */
	static public MsgPattern find(String ms) {
		if (ms != null && !ms.isEmpty()) {
			MultiString multi = new MultiString(ms);
			Iterator<MsgPattern> it = iterator();
			while (it.hasNext()) {
				MsgPattern pat = it.next();
				if (multi.equals(pat.getMulti()))
					return pat;
			}
		}
		return null;
	}

	/** Find unused text rectangles in a pattern */
	static public List<TextRect> findTextRectangles(MsgPattern pat) {
		TextRect tr = defaultRect(pat);
		return (tr != null)
			? tr.find(pat.getMulti())
			: new ArrayList<TextRect>();
	}

	/** Get default text rectangle for a pattern */
	static private TextRect defaultRect(MsgPattern pat) {
		SignConfig sc = DMSDispatcher.getSelectedSignConfig(pat);
		if (sc == null)
			return null;
		int width = sc.getPixelWidth();
		int height = sc.getPixelHeight();
		int fn = SignConfigHelper.getDefaultFontNum(sc);
		return new TextRect(1, width, height, fn);
	}

	/** Check if a pattern has unused text rectangles */
	static public boolean hasTextRectangles(MsgPattern pat) {
		return findTextRectangles(pat).size() > 0;
	}

	/** Fill text rectangles in a pattern */
	static public String fillTextRectangles(MsgPattern pat,
		List<String> lines)
	{
		TextRect tr = defaultRect(pat);
		return (tr != null)
			? tr.fill(pat.getMulti(), lines)
			: "";
	}

	/** Split MULTI string into lines with a pattern */
	static public List<String> splitLines(MsgPattern pat, String ms) {
		TextRect tr = defaultRect(pat);
		return (tr != null)
			? tr.splitLines(pat.getMulti(), ms)
			: new ArrayList<String>();
	}
}
