/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2011  Minnesota Department of Transportation
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

import java.util.TreeSet;
import us.mn.state.dot.sonar.Checker;

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

	/** Lookup the lane-use indications for an LCS */
	static public LaneUseIndication[] lookupIndications(LCS lcs) {
		final TreeSet<LaneUseIndication> indications =
			new TreeSet<LaneUseIndication>();
		lookupIndication(lcs, new Checker<LCSIndication>() {
			public boolean check(LCSIndication li) {
				indications.add(LaneUseIndication.fromOrdinal(
					li.getIndication()));
				return false;
			}
		});
		return indications.toArray(new LaneUseIndication[0]);
	}

	/** Lookup the indications for an LCS */
	static public LCSIndication lookupIndication(final LCS lcs,
		final Checker<LCSIndication> checker)
	{
		return (LCSIndication)namespace.findObject(
			LCSIndication.SONAR_TYPE, new Checker<LCSIndication>()
		{
			public boolean check(LCSIndication li) {
				if(li.getLcs() == lcs)
					return checker.check(li);
				else
					return false;
			}
		});
	}

	/** Lookup the LCS with the specified name */
	static public LCS lookup(String name) {
		return (LCS)namespace.lookupObject(LCS.SONAR_TYPE, name);
	}

	/** Check if an LCS needs maintenance */
	static public boolean needsMaintenance(LCS lcs) {
		String name = lcs.getName();
		DMS dms = DMSHelper.lookup(name);
		if(dms != null)
			return DMSHelper.needsMaintenance(dms);
		else
			return false;
	}
}
