/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018  Minnesota Department of Transportation
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
 * Road affix helper methods.
 *
 * @author Douglas Lau
 */
public class RoadAffixHelper extends BaseHelper {

	/** Disallow instantiation */
	private RoadAffixHelper() {
		assert false;
	}

	/** Lookup a road affix in the SONAR namespace */
	static public RoadAffix lookup(String name) {
		return (RoadAffix) namespace.lookupObject(RoadAffix.SONAR_TYPE,
			name);
	}

	/** Get a road affix iterator */
	static public Iterator<RoadAffix> iterator() {
		return new IteratorWrapper<RoadAffix>(namespace.iterator(
			RoadAffix.SONAR_TYPE));
	}

	/** Replace any affixes on a string.
	 * @param s String to modify.
	 * @param fixup_retain Fixup or retain affix if allowed.
	 * @return Updated string. */
	static public String replace(String s, boolean fixup_retain) {
		Iterator<RoadAffix> it = iterator();
		while (it.hasNext()) {
			RoadAffix ra = it.next();
			boolean retain = fixup_retain && ra.getAllowRetain();
			if (!retain) {
				String ns = ra.getPrefix()
				          ? replacePrefix(ra, s, fixup_retain)
				          : replaceSuffix(ra, s, fixup_retain);
				if (ns != null)
					return ns.trim();
			}
		}
		return s.trim();
	}

	/** Replace prefix on a given string */
	static private String replacePrefix(RoadAffix ra, String s,
		boolean allow_fixup)
	{
		String a = fullPrefix(ra.getName());
		if (s.startsWith(a)) {
			String root = s.substring(a.length());
			if (allow_fixup) {
				String f = ra.getFixup();
				return (f != null) ? fullPrefix(f) + root : s;
			} else
				return root;
		}
		return null;
	}

	/** Get the full prefix string (including space) */
	static private String fullPrefix(String a) {
		if (a.length() > 0) {
			int cp = a.codePointAt(a.length() - 1);
			if (Character.isLetterOrDigit(cp))
				return a + " ";
		}
		return a;
	}

	/** Replace suffix on a given string */
	static private String replaceSuffix(RoadAffix ra, String s,
		boolean allow_fixup)
	{
		String a = fullSuffix(ra.getName());
		if (s.endsWith(a)) {
			String root = s.substring(0, s.length() - a.length());
			if (allow_fixup) {
				String f = ra.getFixup();
				return (f != null) ? root + fullSuffix(f) : s;
			} else
				return root;
		}
		return null;
	}

	/** Get the full suffix string (including space) */
	static private String fullSuffix(String a) {
		if (a.length() > 0) {
			int cp = a.codePointAt(0);
			if (Character.isLetterOrDigit(cp))
				return " " + a;
		}
		return a;
	}
}
