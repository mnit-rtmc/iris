/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2022  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.DmsSignGroupHelper;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.SignText;
import us.mn.state.dot.tms.SignTextHelper;

/**
 * Model for sign text messages for a single DMS.  It creates and contains
 * SignTextComboBoxModel objects for each combobox in MessageComposer.
 *
 * @author Douglas Lau
 */
public class SignTextModel {

	/** Mapping of line numbers to combo box models */
	private final HashMap<Short, SignTextComboBoxModel> lines =
		new HashMap<Short, SignTextComboBoxModel>();

	/** Last line in model */
	private short last_line = 1;

	/** Create a new sign text model */
	public SignTextModel(DMS dms) {
		Set<SignGroup> groups = DmsSignGroupHelper.findGroups(dms);
		Iterator<SignText> it = SignTextHelper.iterator();
		while (it.hasNext()) {
			SignText st = it.next();
			if (groups.contains(st.getSignGroup())) {
				short ln = st.getLine();
				getLineModel(ln).add(st);
				last_line = (short) Math.max(last_line, ln);
			}
		}
	}

	/** Get the combobox line model for the specified line */
	public SignTextComboBoxModel getLineModel(short line) {
		if (lines.containsKey(line))
			return lines.get(line);
		else {
			SignTextComboBoxModel mdl = new SignTextComboBoxModel(
				line);
			lines.put(line, mdl);
			return mdl;
		}
	}

	/** Get the last line number with sign text */
	public short getLastLine() {
		return last_line;
	}
}
