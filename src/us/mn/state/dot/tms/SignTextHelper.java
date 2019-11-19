/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011-2019  Minnesota Department of Transportation
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
 * Helper class for sign text.
 *
 * @author Douglas Lau
 */
public class SignTextHelper extends BaseHelper {

	/** Don't allow instances to be created */
	private SignTextHelper() {
		assert false;
	}

	/** Lookup the sign text with the specified name */
	static public SignText lookup(String name) {
		return (SignText) namespace.lookupObject(SignText.SONAR_TYPE,
			name);
	}

	/** Get a sign text iterator */
	static public Iterator<SignText> iterator() {
		return new IteratorWrapper<SignText>(namespace.iterator(
			SignText.SONAR_TYPE));
	}

	/** Check if there is a matching sign text */
	static public boolean match(SignGroup sg, short line, String multi) {
		Iterator<SignText> it = iterator();
		while (it.hasNext()) {
			SignText st = it.next();
			if (st.getSignGroup() == sg &&
			    st.getLine() == line &&
			    st.getMulti().equals(multi))
				return true;
		}
		return false;
	}

	/** Validate a MULTI string */
	static public boolean isMultiValid(String m) {
		return m.length() <= SignText.MAX_LEN_MULTI &&
		       m.equals(new MultiString(m).normalizeLine().toString());
	}
}
