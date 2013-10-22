/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2013  Minnesota Department of Transportation
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
import java.util.TreeSet;

/**
 * Helper class for LCS.
 *
 * @author Douglas Lau
 */
public class LCSHelper extends BaseHelper {

	/** Prevent object creation */
	private LCSHelper() {
		assert false;
	}

	/** Lookup the LCS with the specified name */
	static public LCS lookup(String name) {
		return (LCS)namespace.lookupObject(LCS.SONAR_TYPE, name);
	}

	/** Get a LCS iterator */
	static public Iterator<LCS> iterator() {
		return new IteratorWrapper<LCS>(namespace.iterator(
			LCS.SONAR_TYPE));
	}

	/** Lookup the lane-use indications for an LCS */
	static public LaneUseIndication[] lookupIndications(LCS lcs) {
		TreeSet<LaneUseIndication> indications =
			new TreeSet<LaneUseIndication>();
		Iterator<LCSIndication> it = LCSIndicationHelper.iterator();
		while(it.hasNext()) {
			LCSIndication li = it.next();
			if(li.getLcs() == lcs) {
				indications.add(LaneUseIndication.fromOrdinal(
					li.getIndication()));
			}
		}
		return indications.toArray(new LaneUseIndication[0]);
	}
}
