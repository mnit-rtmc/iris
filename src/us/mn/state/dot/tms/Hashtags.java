/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2023-2024  Minnesota Department of Transportation
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

import java.util.Set;
import java.util.TreeSet;
import us.mn.state.dot.tms.utils.NumericAlphaComparator;

/**
 * Set of hashtags.
 *
 * @author Douglas Lau
 */
public class Hashtags {

	/** Characters for tag boundaries */
	static private final String BOUNDARY = "# \t\n\r\f,;:()[]{}<>";

	/** Check if a character is a tag boundary */
	static private boolean isBoundary(char c) {
		return BOUNDARY.indexOf(c) >= 0;
	}

	/** Normalize a hashtag value */
	static public String normalize(String ht) {
		if (ht != null) {
			ht = ht.trim();
			return ht.matches("#[A-Za-z0-9]+") ? ht : null;
		} else
			return null;
	}

	/** Add a hashtag to a notes field */
	static public String add(String notes, String tag) {
		return notes.trim() + '\n' + tag;
	}

	/** Remove a hashtag from a notes field */
	static public String remove(String notes, String tag) {
		String t = tag.toLowerCase();
		int i = notes.toLowerCase().indexOf(t);
		if (i >= 0) {
			notes = (
				notes.substring(0, i) +
				notes.substring(i + t.length())
			).trim();
		}
		return notes;
	}

	/** Set of tags */
	private final TreeSet<String> tags = new TreeSet<String>();

	/** Valid tags flag */
	public final boolean valid;

	/** Construct hashtags from notes */
	public Hashtags(String notes) {
		boolean v = true;
		if (notes != null) {
			int len = notes.length();
			for (int i = 0; i < len; i++) {
				if (notes.charAt(i) == '#') {
					int j = i + 1;
					for (; j < len; j++) {
						char t = notes.charAt(j);
						if (isBoundary(t))
							break;
					}
					String tag = notes.substring(i, j);
					String t = normalize(tag);
					if (t != null) {
						tags.add(t);
						if (!t.equals(tag))
							v = false;
					}
					i = j;
				}
			}
		}
		valid = v;
	}

	/** Check if hashtag is contained in a set */
	public boolean contains(String ht) {
		if (ht != null) {
			for (String tag: tags) {
				if (ht.equalsIgnoreCase(tag))
					return true;
			}
		}
		return false;
	}

	/** Get all hashtags in a set */
	public TreeSet<String> tags() {
		return tags;
	}
}
