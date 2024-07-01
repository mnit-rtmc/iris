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

import java.util.TreeSet;
import us.mn.state.dot.tms.utils.NumericAlphaComparator;

/**
 * Helper class for hashtags.
 *
 * @author Douglas Lau
 */
public class HashtagHelper {

	/** don't instantiate */
	private HashtagHelper() {
		assert false;
	}

	/** Normalize a hashtag value */
	static public String normalize(String ht) {
		if (ht != null) {
			ht = ht.trim();
			return ht.matches("#[A-Za-z0-9]+") ? ht : null;
		} else
			return null;
	}

	/** Make an ordered array of hashtags */
	static public String[] makeHashtags(String[] ht) {
		TreeSet<String> tags = new TreeSet<String>();
		for (String tag: ht) {
			tag = normalize(tag);
			if (tag != null)
				tags.add(tag);
		}
		return tags.toArray(new String[0]);
	}

	/** Check if a taggable has a hashtag */
	static public boolean hasHashtag(Taggable t, String hashtag) {
		if (hashtag != null) {
			for (String tag: t.getHashtags()) {
				if (hashtag.equalsIgnoreCase(tag))
					return true;
			}
		}
		return false;
	}
}
