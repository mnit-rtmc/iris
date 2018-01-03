/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017  Minnesota Department of Transportation
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

	/** Find a play list with the specific num */
	static public PlayList findNum(final int num) {
		Iterator<PlayList> it = iterator();
		while (it.hasNext()) {
			PlayList pl = it.next();
			if (pl != null) {
				Integer n = pl.getNum();
				if (n != null && n == num)
					return pl;
			}
		}
		return null;
	}

	/** Find a play list with the specific num */
	static public PlayList findNum(String num) {
		Integer n = parseNum(num);
		return (n != null) ? findNum(n) : null;
	}

	/** Parse the integer num of a play list */
	static public Integer parseNum(String num) {
		try {
			return Integer.parseInt(num);
		}
		catch (NumberFormatException e) {
			return null;
		}
	}
}
