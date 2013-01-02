/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011-2012  Minnesota Department of Transportation
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

	/** Get a sign text iterator */
	static public Iterator<SignText> iterator() {
		return new IteratorWrapper<SignText>(namespace.iterator(
			SignText.SONAR_TYPE));
	}

	/** Check if there is a matching sign text */
	static public boolean match(SignGroup sg, short line, String multi) {
		Iterator<SignText> it = iterator();
		while(it.hasNext()) {
			SignText st = it.next();
			if(st.getSignGroup() == sg &&
			   st.getLine() == line &&
			   st.getMulti().equals(multi))
				return true;
		}
		return false;
	}
}
