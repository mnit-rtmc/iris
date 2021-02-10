/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2021  Minnesota Department of Transportation
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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import us.mn.state.dot.tms.utils.UniqueNameCreator;

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

	/** Create a unique DMS sign group name */
	static public String createUniqueName(String template) {
		UniqueNameCreator unc = new UniqueNameCreator(template, 42,
			(n)->lookup(n));
		return unc.createUniqueName();
	}

	/** Lookup the DMS sign grou pwith the specified name */
	static public DmsSignGroup lookup(String name) {
		return (DmsSignGroup) namespace.lookupObject(
			DmsSignGroup.SONAR_TYPE, name);
	}

	/** Get a DMS sign group iterator */
	static public Iterator<DmsSignGroup> iterator() {
		return new IteratorWrapper<DmsSignGroup>(namespace.iterator(
			DmsSignGroup.SONAR_TYPE));
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

	/** Find a DMS sign group */
	static public DmsSignGroup find(DMS dms, SignGroup sg) {
		Iterator<DmsSignGroup> it = iterator();
		while (it.hasNext()) {
			DmsSignGroup dsg = it.next();
			if (dsg.getDms() == dms && dsg.getSignGroup() == sg)
				return dsg;
		}
		return null;
	}
}
