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
import us.mn.state.dot.tms.utils.MultiString;

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

	/** Check if there is a matching msg line */
	static public boolean match(MsgPattern pat, short line, String multi) {
		Iterator<MsgLine> it = iterator();
		while (it.hasNext()) {
			MsgLine mt = it.next();
			if (mt.getMsgPattern() == pat &&
			    mt.getLine() == line &&
			    mt.getMulti().equals(multi))
				return true;
		}
		return false;
	}

	/** Validate a MULTI string */
	static public boolean isMultiValid(String m) {
		return m.length() <= MsgLine.MAX_LEN_MULTI &&
		       m.equals(new MultiString(m).normalizeLine().toString());
	}
}
