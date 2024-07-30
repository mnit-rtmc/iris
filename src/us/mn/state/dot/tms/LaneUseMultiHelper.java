/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2024  Minnesota Department of Transportation
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

/**
 * Helper class for LaneUseMulti.
 *
 * @author Douglas Lau
 */
public class LaneUseMultiHelper extends BaseHelper {

	/** Prevent object creation */
	private LaneUseMultiHelper() {
		assert false;
	}

	/** Lookup the lane-use MULTI with the specified name */
	static public LaneUseMulti lookup(String name) {
		return (LaneUseMulti)namespace.lookupObject(
			LaneUseMulti.SONAR_TYPE, name);
	}

	/** Get a lane-use MULTI iterator */
	static public Iterator<LaneUseMulti> iterator() {
		return new IteratorWrapper<LaneUseMulti>(namespace.iterator(
			LaneUseMulti.SONAR_TYPE));
	}

	/** Get a lane-use MULTI for a given indication.
	 * @param ind LaneUseIndication ordinal value.
	 * @param dms Sign to match.
	 * @return A lane-use MULTI. */
	static public LaneUseMulti find(int ind, DMS dms) {
		Hashtags hashtags = new Hashtags(dms.getNotes());
		Iterator<LaneUseMulti> it = iterator();
		while (it.hasNext()) {
			LaneUseMulti lum = it.next();
			if (lum.getIndication() == ind &&
			    hashtags.contains(lum.getDmsHashtag()))
			{
				return lum;
			}
		}
		return null;
	}

	/** Find all hashtags for a lane-use-multi pattern */
	static public Set<String> findHashtags(MsgPattern pat) {
		HashSet<String> hashtags = new HashSet<String>();
		Iterator<LaneUseMulti> it = iterator();
		while (it.hasNext()) {
			LaneUseMulti lum = it.next();
			if (lum.getMsgPattern() == pat) {
				String ht = lum.getDmsHashtag();
				if (ht != null)
					hashtags.add(ht);
			}
		}
		return hashtags;
	}
}
