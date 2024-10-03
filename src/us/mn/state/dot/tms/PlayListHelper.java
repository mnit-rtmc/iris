/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017-2024  Minnesota Department of Transportation
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

/**
 * Helper class for play lists.
 *
 * @author Douglas Lau
 */
public class PlayListHelper extends BaseHelper {

	/** Don't allow instances to be created */
	private PlayListHelper() {
		assert false;
	}

	/** Lookup the play list with the specified name */
	static public PlayList lookup(String name) {
		return (PlayList) namespace.lookupObject(PlayList.SONAR_TYPE,
			name);
	}

	/** Get a play list iterator */
	static public Iterator<PlayList> iterator() {
		return new IteratorWrapper<PlayList>(namespace.iterator(
			PlayList.SONAR_TYPE));
	}

	/** Find a play list with the specified name or number */
	static public PlayList find(String n) {
		PlayList pl = lookup(n);
		return (pl != null) ? pl : findSeqNum(n);
	}

	/** Find a play list with the specific seq num */
	static public PlayList findSeqNum(final int sn) {
		Iterator<PlayList> it = iterator();
		while (it.hasNext()) {
			PlayList pl = it.next();
			if (pl != null) {
				Integer n = pl.getSeqNum();
				if (n != null && n == sn)
					return pl;
			}
		}
		return null;
	}

	/** Find a play list with the specific sequence num */
	static public PlayList findSeqNum(String sn) {
		Integer n = parseSeqNum(sn);
		return (n != null) ? findSeqNum(n) : null;
	}

	/** Parse the sequence num of a play list */
	static public Integer parseSeqNum(String n) {
		try {
			return Integer.parseInt(n);
		}
		catch (NumberFormatException e) {
			return null;
		}
	}

	/** Find a scratch play list for a user */
	static public PlayList findScratch(User u) {
		Role r = u.getRole();
		if (r == null || !r.getEnabled())
			return null;
		Set<String> per_tags = PermissionHelper.findScratch(r);
		PlayList scratch = null;
		Iterator<PlayList> it = iterator();
		while (it.hasNext()) {
			PlayList pl = it.next();
			Hashtags tags = new Hashtags(pl.getNotes());
			if (tags.containsAny(per_tags)) {
				// only one scratch PlayList allowed!
				if (scratch != null)
					return null;
				scratch = pl;
			}
		}
		return scratch;
	}
}
