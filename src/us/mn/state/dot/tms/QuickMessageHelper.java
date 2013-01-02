/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2012  Minnesota Department of Transportation
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

	/** Get a quick message iterator */
	static public Iterator<QuickMessage> iterator() {
		return new IteratorWrapper<QuickMessage>(namespace.iterator(
			QuickMessage.SONAR_TYPE));
	}

	/** Find a quick message with the specified MULTI string.
	 * @param ms MULTI string.
	 * @return A matching quick message or null if no match is found. */
	static public QuickMessage find(String ms) {
		if(ms != null) {
			MultiString multi = new MultiString(ms);
			Iterator<QuickMessage> it = iterator();
			while(it.hasNext()) {
				QuickMessage qm = it.next();
				if(multi.equals(qm.getMulti()))
					return qm;
			}
		}
		return null;
	}
}
