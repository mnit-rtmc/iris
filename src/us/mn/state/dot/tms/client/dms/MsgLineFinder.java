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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.MsgLine;
import us.mn.state.dot.tms.MsgLineHelper;
import us.mn.state.dot.tms.MsgPattern;
import us.mn.state.dot.tms.MsgPatternHelper;

/**
 * Finder for message lines for a single DMS.  It creates and contains
 * MsgLineCBoxModel objects for each combobox in MessageComposer.
 *
 * @author Douglas Lau
 */
public class MsgLineFinder {

	/** The DMS */
	private final DMS dms;

	/** Mapping of line numbers to models */
	private final HashMap<Short, MsgLineCBoxModel> lines =
		new HashMap<Short, MsgLineCBoxModel>();

	/** Create a new message line finder */
	public MsgLineFinder(DMS d) {
		dms = d;
		Iterator<MsgLine> it = MsgLineHelper.iterator();
		while (it.hasNext())
			checkLine(it.next());
	}

	/** Check if a message line belongs */
	private void checkLine(MsgLine ml) {
		MsgPattern pat = ml.getMsgPattern();
		String cht = pat.getComposeHashtag();
		if (cht != null && DMSHelper.hasHashtag(dms, cht)) {
			String rht = ml.getRestrictHashtag();
			if (rht == null || DMSHelper.hasHashtag(dms, rht)) {
				short ln = ml.getLine();
				getLineModel(ln).add(ml);
			}
		}
	}

	/** Get the model for the specified line */
	public MsgLineCBoxModel getLineModel(short line) {
		if (lines.containsKey(line))
			return lines.get(line);
		else {
			MsgLineCBoxModel mdl = new MsgLineCBoxModel(line);
			lines.put(line, mdl);
			return mdl;
		}
	}
}
