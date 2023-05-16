/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016  Iteris Inc.
 * Copyright (C) 2019-2023  Minnesota Department of Transportation
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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import us.mn.state.dot.tms.utils.SString;

/**
 * Static Word convenience methods accessible from the client and server.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class WordHelper extends BaseHelper {

	/** Disallow instantiation */
	private WordHelper() {
		assert false;
	}

	/** Get an iterator */
	static public Iterator<Word> iterator() {
		return new IteratorWrapper<Word>(
			namespace.iterator(Word.SONAR_TYPE));
	}

	/** Lookup a Word in the SONAR namespace.
	 * @param name Unencoded word to lookup.  Will be converted
	 *             to uppercase to determine if it exists.
	 * @return The specified word or null if not in namespace */
	static public Word lookup(String name) {
		if (namespace == null || name == null)
			return null;
		String nm = encodeWord(name.toUpperCase());
		return (Word) namespace.lookupObject(Word.SONAR_TYPE, nm);
	}

	/** Encode a word to avoid sonar namespace issues,
	 * for example "US89/191".  Words are sonar names.
	 * @param n Word to encode
	 * @return Encoded word */
	static public String encodeWord(String n) {
		try {
			return URLEncoder.encode(n, "UTF-8");
		}
		catch (UnsupportedEncodingException ex) {
			System.err.println("ex=" + ex);
			return n;
		}
	}

	/** Decode a word
	 * @param n Word to decode
	 * @return Decoded word */
	static public String decodeWord(String n) {
		try {
			return URLDecoder.decode(n, "UTF-8");
		}
		catch (UnsupportedEncodingException ex) {
			System.err.println("ex=" + ex);
			return n;
		}
	}

	/** Check if a word is banned */
	static public boolean isBanned(String w) {
		if (ignoreWord(w))
			return false;
		else {
			Word word = lookup(w);
			return (word != null) && !word.getAllowed();
		}
	}

	/** Ignore a word? */
	static private boolean ignoreWord(String w) {
		return w == null || w.trim().isEmpty() || SString.isNumeric(w);
	}

	/** Abbreviate text if possible.
	 * @return Abbreviated text, or null if can't be abbreviated. */
	static public String abbreviate(String txt) {
		String[] words = txt.split(" ");
		// Abbreviate words with non-blank abbreviations
		for (int i = words.length - 1; i >= 0; i--) {
			String abbr = lookupAbbrev(words[i]);
			if (abbr != null && abbr.length() > 0) {
				words[i] = abbr;
				return joinWords(words);
			}
		}
		// Abbreviate words with blank abbreviations
		for (int i = words.length - 1; i >= 0; i--) {
			String abbr = lookupAbbrev(words[i]);
			if (abbr != null) {
				words[i] = abbr;
				return joinWords(words);
			}
		}
		return null;
	}

	/** Lookup a word and get its abbreviation */
	static private String lookupAbbrev(String w) {
		Word word = lookup(w);
		return (word != null && word.getAllowed())
		      ? word.getAbbr()
		      : null;
	}

	/** Join an array of words, skipping blank entries */
	static private String joinWords(String[] words) {
		StringBuilder sb = new StringBuilder();
		for (String word: words) {
			if (word != null && word.length() > 0) {
				if (sb.length() > 0)
					sb.append(' ');
				sb.append(word);
			}
		}
		String ms = sb.toString();
		return (ms.length() > 0) ? ms : null;
	}
}
