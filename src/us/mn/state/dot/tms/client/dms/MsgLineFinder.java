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
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.MsgLine;
import us.mn.state.dot.tms.MsgLineHelper;
import us.mn.state.dot.tms.MsgPattern;

/**
 * Finder for message lines for a single DMS.  It creates and contains
 * MsgLineCBoxModel objects for each combobox in MessageComposer.
 *
 * @author Douglas Lau
 */
public class MsgLineFinder {

	/** Create a message line finder */
	static public MsgLineFinder create(MsgPattern pat, DMS dms) {
		return (pat != null && dms != null)
		      ? new MsgLineFinder(pat, dms)
		      : null;
	}

	/** Mapping of line numbers to models */
	private final HashMap<Short, MsgLineCBoxModel> models =
		new HashMap<Short, MsgLineCBoxModel>();

	/** Create a new message line finder.
	 * @param pat Message pattern for matching lines.
	 * @param dms The sign. */
	private MsgLineFinder(MsgPattern pat, DMS dms) {
		for (MsgLine ml: MsgLineHelper.findAllCompose(pat, dms)) {
			getLineModel(ml.getLine()).add(ml);
		}
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

	/** Check if no lines were found */
	public boolean isEmpty() {
		return models.isEmpty();
	}
}
