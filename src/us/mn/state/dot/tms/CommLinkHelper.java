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
 * Helper for comm links.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public class CommLinkHelper extends BaseHelper {

	/** Disallow instantiation */
	private CommLinkHelper() {
		assert false;
	}

	/** Find a comm link using a Checker */
	static public CommLink find(final Checker<CommLink> checker) {
		return (CommLink)namespace.findObject(CommLink.SONAR_TYPE,
			checker);
	}

	/** Get the first comm link associated with the specified protocol.
	 * @return The first comm link of the specified protocol or null if 
	 *	   one is not defined. */
	static public CommLink getCommLink(final CommProtocol proto) {
		return find(new Checker<CommLink>() {
			public boolean check(CommLink c) {
				return c.getProtocol() == proto.ordinal();
			}
		});
	}

	/** Count the number of comm links with the specified protocol */
	static public int countCommLinks(CommProtocol proto) {
		LinkCounter lc = new LinkCounter(proto);
		find(lc);
		return lc.n_links;
	}

	/** Simple class to count comm links */
	static protected class LinkCounter implements Checker<CommLink> {
		protected final CommProtocol proto;
		int n_links = 0;
		protected LinkCounter(CommProtocol cp) {
			proto = cp;
		}
		public boolean check(CommLink c) {
			if(c.getProtocol() == proto.ordinal())
				n_links++;
			return false;
		}
	}
}
