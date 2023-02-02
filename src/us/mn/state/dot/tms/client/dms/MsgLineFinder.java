/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2023  Minnesota Department of Transportation
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.MsgLine;
import us.mn.state.dot.tms.MsgLineHelper;
import us.mn.state.dot.tms.MsgPattern;
import us.mn.state.dot.tms.MsgPatternHelper;
import us.mn.state.dot.tms.SignConfig;
import us.mn.state.dot.tms.SignConfigHelper;
import us.mn.state.dot.tms.TransMsgLine;
import us.mn.state.dot.tms.WordHelper;
import us.mn.state.dot.tms.utils.TextRect;

/**
 * Finder for message lines for a single DMS.  It creates and contains
 * MsgLineCBoxModel objects for each combobox in MessageComposer.
 *
 * @author Douglas Lau
 */
public class MsgLineFinder {

	/** Create a message line finder */
	static public MsgLineFinder create(DMS d, MsgPattern pat) {
		return (d != null && pat != null)
		      ? new MsgLineFinder(d, pat)
		      : null;
	}

	/** The DMS */
	private final DMS dms;

	/** List of TextRect values for each line */
	private final ArrayList<TextRect> line_rects =
		new ArrayList<TextRect>();

	/** Mapping of line numbers to models */
	private final HashMap<Short, MsgLineCBoxModel> models =
		new HashMap<Short, MsgLineCBoxModel>();

	/** Create a new message line finder */
	private MsgLineFinder(DMS d, MsgPattern pat) {
		dms = d;
		SignConfig sc = dms.getSignConfig();
		TextRect full_rect = SignConfigHelper.textRect(sc);
		// make line-to-TextRect array
		line_rects.add(null); // line 0 is invalid
		for (TextRect tr: full_rect.find(pat.getMulti())) {
			for (int i = 0; i < tr.getLineCount(); i++)
				line_rects.add(tr);
		}
		Iterator<MsgLine> it = MsgLineHelper.iterator();
		while (it.hasNext()) {
			MsgLine ml = it.next();
			if (ml.getMsgPattern() == pat)
				checkLine(ml);
		}
	}

	/** Check if a message line belongs */
	private void checkLine(MsgLine ml) {
		String rht = ml.getRestrictHashtag();
		if (rht == null || DMSHelper.hasHashtag(dms, rht)) {
			short line = ml.getLine();
			// it's possible to configure infinite abbrev loops,
			// so limit this to 20 iterations
			for (int i = 0; i < 20 && ml != null; i++) {
				if (isWidthOk(line, ml)) {
					getLineModel(line).add(ml);
					break;
				}
				ml = createAbbreviated(ml);
			}
		}
	}

	/** Check if a message linee width is OK */
	private boolean isWidthOk(int line, MsgLine ml) {
		if (line < line_rects.size()) {
			TextRect tr = line_rects.get(line);
			int w = tr.calculateWidth(ml.getMulti());
			return (w > 0 && w <= tr.width);
		} else
			return false;
	}

	/** Create an abbreviated message line */
	private MsgLine createAbbreviated(MsgLine ml) {
		String ms = WordHelper.abbreviate(ml.getMulti());
		return (ms != null)
		      ? new TransMsgLine(ms, ml.getLine(), ml.getRank())
		      : null;
	}

	/** Get the model for the specified line */
	public MsgLineCBoxModel getLineModel(short line) {
		if (models.containsKey(line))
			return models.get(line);
		else {
			MsgLineCBoxModel mdl = new MsgLineCBoxModel(line);
			models.put(line, mdl);
			return mdl;
		}
	}
}
