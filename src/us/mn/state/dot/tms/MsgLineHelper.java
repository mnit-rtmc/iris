/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011-2026  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.utils.MultiString;
import us.mn.state.dot.tms.utils.TextRect;

/**
 * Helper class for message lines.
 *
 * @author Douglas Lau
 */
public class MsgLineHelper extends BaseHelper {

	/** Don't allow instances to be created */
	private MsgLineHelper() {
		assert false;
	}

	/** Lookup the message line with the specified name */
	static public MsgLine lookup(String name) {
		return (MsgLine) namespace.lookupObject(MsgLine.SONAR_TYPE,
			name);
	}

	/** Get a message line iterator */
	static public Iterator<MsgLine> iterator() {
		return new IteratorWrapper<MsgLine>(namespace.iterator(
			MsgLine.SONAR_TYPE));
	}

	/** Validate a MULTI string */
	static public boolean isMultiValid(String m) {
		return m.length() <= MsgLine.MAX_LEN_MULTI &&
		       m.equals(new MultiString(m).normalizeLine().toString());
	}

	/** Find all lines for a message pattern, including prototype */
	static public List<MsgLine> findAllLines(MsgPattern pat, DMS dms) {
		ArrayList<MsgLine> lines = new ArrayList<MsgLine>();
		List<TextRect> line_rects =
			MsgPatternHelper.lineTextRects(pat, dms);
		if (line_rects == null || line_rects.size() <= 1)
			return lines;
		String prototype = pat.getPrototype();
		Iterator<MsgLine> it = iterator();
		while (it.hasNext()) {
			MsgLine ml = it.next();
			MsgPattern mp = ml.getMsgPattern();
			if (mp == pat || mp.getName().equals(prototype)) {
				MsgLine aml = abbreviateLine(ml, line_rects);
				if (aml != null)
					lines.add(aml);
			}
		}
		return lines;
	}

	/** Abbreviate a line for available text rectangle */
	static private MsgLine abbreviateLine(MsgLine ml,
		List<TextRect> line_rects)
	{
		short line = ml.getLine();
		if (line < line_rects.size()) {
			TextRect tr = line_rects.get(line);
			String ms = tr.checkLine(ml.getMulti(), true);
			if (ms != null) {
				return new TransMsgLine(ms, line,
					ml.getRank());
			}
		}
		return null;
	}
}
