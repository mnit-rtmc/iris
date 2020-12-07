/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2019  Minnesota Department of Transportation
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Helper class for DMS sign groups.
 *
 * @author Douglas Lau
 */
public class DmsSignGroupHelper extends BaseHelper {

	/** Prevent object creation */
	private DmsSignGroupHelper() {
		assert false;
	}

	/** Get a DMS sign group iterator */
	static public Iterator<DmsSignGroup> iterator() {
		return new IteratorWrapper<DmsSignGroup>(namespace.iterator(
			DmsSignGroup.SONAR_TYPE));
	}

	/** Get an ArrayList of all signs in a sign group */
	static public ArrayList<DMS> getSignsInGroup(String sgn) {
		ArrayList<DMS> groupDms = new ArrayList<DMS>();
		Iterator<DmsSignGroup> it = iterator();
		while (it.hasNext()) {
			DmsSignGroup dsg = it.next();
			if (dsg.getSignGroup().getName().equals(sgn))
				groupDms.add(dsg.getDms());
		}
		return groupDms;
	}
	
	/** Find all sign groups for a DMS */
	static public Set<SignGroup> findGroups(DMS dms) {
		HashSet<SignGroup> groups = new HashSet<SignGroup>();
		Iterator<DmsSignGroup> it = iterator();
		while (it.hasNext()) {
			DmsSignGroup dsg = it.next();
			if (dsg.getDms() == dms)
				groups.add(dsg.getSignGroup());
		}
		return groups;
	}
}
