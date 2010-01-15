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

import java.util.Collection;
import java.util.LinkedList;
import us.mn.state.dot.sonar.Checker;

/**
 * Helper class for sign groups.
 *
 * @author Douglas Lau
 */
public class SignGroupHelper extends BaseHelper {

	/** Don't allow instances to be created */
	private SignGroupHelper() {
		assert false;
	}

	/** Lookup the sign group with the specified name */
	static public SignGroup lookup(String name) {
		return (SignGroup)namespace.lookupObject(SignGroup.SONAR_TYPE,
			name);
	}

	/** Find sign groups using a Checker */
	static public SignGroup find(final Checker<SignGroup> checker) {
		return (SignGroup)namespace.findObject(SignGroup.SONAR_TYPE,
			checker);
	}

	/** Find all the DMS in a sign group */
	static public Collection<DMS> find(final SignGroup sg) {
		final LinkedList<DMS> dmss = new LinkedList<DMS>();
		DmsSignGroupHelper.find(new Checker<DmsSignGroup>() {
			public boolean check(DmsSignGroup g) {
				if(g.getSignGroup() == sg)
					dmss.add(g.getDms());
				return false;
			}
		});
		return dmss;
	}

	/** Find all the sign groups for a DMS */
	static public Collection<SignGroup> find(final DMS dms) {
		final LinkedList<SignGroup> groups =
			new LinkedList<SignGroup>();
		DmsSignGroupHelper.find(new Checker<DmsSignGroup>() {
			public boolean check(DmsSignGroup g) {
				if(g.getDms() == dms)
					groups.add(g.getSignGroup());
				return false;
			}
		});
		return groups;
	}
}
