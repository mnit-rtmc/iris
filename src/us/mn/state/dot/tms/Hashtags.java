/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2023-2026  Minnesota Department of Transportation
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

/**
 * Set of hashtags.
 *
 * @author Douglas Lau
 */
public class Hashtags {

	/** Check if a character is valid in a tag */
	static private boolean isTagChar(char c) {
		return (c >= '0' && c <= '9')
		    || (c >= 'A' && c <= 'Z')
		    || (c >= 'a' && c <= 'z');
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
		String ht = normalize(tag);
		return (ht != null) ? notes.trim() + '\n' + ht : notes;
	}

	/** Remove a hashtag from a notes field */
	static public String remove(String notes, String tag) {
		String ht = normalize(tag);
		if (ht == null)
			return notes;
		ht = ht.toLowerCase();
		String lower_notes = notes.toLowerCase();
		int len = notes.length();
		for (int i = 0; i < len; i++) {
			int j = lower_notes.indexOf(ht, i);
			if (j >= 0) {
				int end = j + ht.length();
				if (end == len)
					return notes.substring(0, j).trim();
				// NOTE: end > len
				if (!isTagChar(notes.charAt(end))) {
					return (
						notes.substring(0, j) +
						notes.substring(end)
					).trim();
				}
			}
		}
		return notes;
	}

	/** Set of tags */
	private final TreeSet<String> tags = new TreeSet<String>();

	/** Construct hashtags from notes */
	public Hashtags(String notes) {
		if (notes != null) {
			int len = notes.length();
			for (int i = 0; i < len; i++) {
				if (notes.charAt(i) == '#') {
					int j = i + 1;
					for (; j < len; j++) {
						char t = notes.charAt(j);
						if (!isTagChar(t))
							break;
					}
					if (j > i + 1)
						tags.add(notes.substring(i, j));
					i = j;
				}
			}
		}
	}

	/** Check if a hashtag is contained in set */
	public boolean contains(String ht) {
		if (ht != null) {
			for (String tag: tags) {
				if (ht.equalsIgnoreCase(tag))
					return true;
			}
		}
		return false;
	}

	/** Check if any hashtag is contained in set */
	public boolean containsAny(Set<String> hashtags) {
		for (String ht: hashtags) {
			if (contains(ht))
				return true;
		}
		return false;
	}

	/** Get all hashtags in a set */
	public TreeSet<String> tags() {
		return tags;
	}
}
