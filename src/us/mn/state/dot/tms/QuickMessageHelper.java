/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2010  Minnesota Department of Transportation
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
 * Helper class for quick messages.
 *
 * @author Douglas Lau
 */
public class QuickMessageHelper extends BaseHelper {

	/** Don't allow instances to be created */
	private QuickMessageHelper() {
		assert false;
	}

	/** Lookup the quick message with the specified name */
	static public QuickMessage lookup(String name) {
		return (QuickMessage)namespace.lookupObject(
			QuickMessage.SONAR_TYPE, name);
	}

	/** Find a quick message using a Checker */
	static public QuickMessage find(Checker<QuickMessage> checker) {
		return (QuickMessage)namespace.findObject(
			QuickMessage.SONAR_TYPE, checker);
	}

	/** Find a quick message with the specified MULTI string.
	 * @param ms MULTI string.
	 * @return A matching quick message or null if no match is found. */
	static public QuickMessage find(final String ms) {
		if(ms == null)
			return null;
		return find(new Checker<QuickMessage>() {
			public boolean check(QuickMessage qm) {
				return MultiString.isEquivalent(qm.getMulti(), ms);
			}
		});
	}
}
