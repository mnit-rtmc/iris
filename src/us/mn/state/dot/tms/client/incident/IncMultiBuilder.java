/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2019-2023  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.incident;

import java.util.ArrayList;
import java.util.Set;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.MsgPattern;
import us.mn.state.dot.tms.MsgPatternHelper;
import us.mn.state.dot.tms.SignConfigHelper;
import us.mn.state.dot.tms.units.Distance;
import us.mn.state.dot.tms.utils.MultiString;
import us.mn.state.dot.tms.utils.TextRect;

/**
 * MULTI string builder for incidents.
 *
 * @author Douglas Lau
 */
public class IncMultiBuilder {

	/** Message pattern */
	private final MsgPattern msg_pattern;

	/** Message lines */
	private final ArrayList<String> lines = new ArrayList<String>();

	/** Full text rectangle */
	private final TextRect full_rect;

	/** Location of incident */
	private final GeoLoc loc;

	/** Distance upstream of incident */
	private final Distance dist;

	/** Create a new incident MULTI builder */
	public IncMultiBuilder(DMS dms, GeoLoc l, Distance d) {
		MsgPattern best = null;
		for (MsgPattern mp: MsgPatternHelper.findAllCompose(dms))
			best = MsgPatternHelper.better(best, mp);
		msg_pattern = best;
		full_rect = SignConfigHelper.textRect(dms.getSignConfig());
		loc = l;
		dist = d;
	}

	/** Add a line to MULTI string */
	public boolean addLine(String ms) {
		if (full_rect != null && ms != null) {
			TextRect tr = getTextRect(lines.size() + 1);
			if (tr != null) {
				String line = checkLine(tr, ms);
				if (line != null) {
					lines.add(line);
					return true;
				}
			}
		}
		return false;
	}

	/** Get the text rectangle for a given line number */
	private TextRect getTextRect(int n_line) {
		int count = 0;
		for (TextRect tr: full_rect.find(msg_pattern.getMulti())) {
			count += tr.getLineCount();
			if (n_line <= count)
				return tr;
		}
		return null;
	}

	/** Check a MULTI line, replacing [loc] tags and abbreviating if
	 * necessary */
	private String checkLine(TextRect tr, String ms) {
		// First try to retain affixes, but strip if necessary
		String line = checkLine(tr, ms, true);
		return (line != null) ? line : checkLine(tr, ms, false);
	}

	/** Check if a MULTI line fits on a text rectangle */
	private String checkLine(TextRect tr, String ms,
		boolean retain_affixes)
	{
		LocMultiBuilder lmb = new LocMultiBuilder(loc, dist,
			retain_affixes);
		new MultiString(ms).parse(lmb);
		// Don't try abbreviating if we're retaining affixes
		return tr.checkLine(lmb.toString(), !retain_affixes);
	}

	/** Get the MULTI as a String */
	@Override
	public String toString() {
		MultiString multi = new MultiString(
			full_rect.fill(msg_pattern.getMulti(), lines)
		);
		return multi.stripTrailingWhitespaceTags();
	}
}
