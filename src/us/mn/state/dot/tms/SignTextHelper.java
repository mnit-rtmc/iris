/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011  Minnesota Department of Transportation
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

import us.mn.state.dot.sonar.Checker;

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

	/** Find sign text using a Checker */
	static public SignText find(final Checker<SignText> checker) {
		return (SignText)namespace.findObject(SignText.SONAR_TYPE,
			checker);
	}

	/** Check if there is a matching sign text */
	static public boolean match(final SignGroup sg, final short line,
		final String msg)
	{
		return find(new Checker<SignText>() {
			public boolean check(SignText st) {
				return st.getSignGroup() == sg &&
				       st.getLine() == line &&
				       st.getMessage().equals(msg);
			}
		}) != null;
	}
}
