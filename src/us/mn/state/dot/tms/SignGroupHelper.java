/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2023  Minnesota Department of Transportation
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
import java.util.Set;
import java.util.TreeSet;
import us.mn.state.dot.tms.utils.NumericAlphaComparator;

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

	/** Get a sign group iterator */
	static public Iterator<SignGroup> iterator() {
		return new IteratorWrapper<SignGroup>(namespace.iterator(
			SignGroup.SONAR_TYPE));
	}

	/** Check if a sign group has any members */
	static public boolean hasMembers(SignGroup sg) {
		Iterator<DmsSignGroup> it = DmsSignGroupHelper.iterator();
		while (it.hasNext()) {
			DmsSignGroup dsg = it.next();
			if (dsg.getSignGroup() == sg)
				return true;
		}
		return false;
	}

	/** Get a set of all signs in a sign group */
	static public Set<DMS> getAllSigns(SignGroup sg) {
		TreeSet<DMS> signs = new TreeSet<DMS>(
			new NumericAlphaComparator<DMS>());
		Iterator<DmsSignGroup> it = DmsSignGroupHelper.iterator();
		while (it.hasNext()) {
			DmsSignGroup dsg = it.next();
			if (dsg.getSignGroup() == sg)
				signs.add(dsg.getDms());
		}
		return signs;
	}
}
