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
import us.mn.state.dot.tms.utils.I18N;
import us.mn.state.dot.tms.utils.MultiString;
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

	/** Spell check a MULTI string.
	 * @param multi MULTI string containing words to check.
	 * @return An empty string if no words are misspelled, otherwise a 
	 *	   message for the user. */
	static public String spellCheck(String multi) {
		StringBuilder msg = new StringBuilder();
		msg.append(buildBannedUserMsg(multi));
		msg.append(buildAllowedUserMsg(multi));
		return msg.toString();
	}

	/** Indent for messages */
	static private String INDENT = "    ";

	/** Build a user message for words that are banned.
	 * @param ms MULTI string
	 * @return A user message indicating which words are misspelled */
	static private String buildBannedUserMsg(String multi) {
		List<String> bw = spellCheck(multi, false);
		if (bw.size() <= 0)
			return "";
		StringBuilder msg = new StringBuilder();
		msg.append(I18N.get("word.banned.msg"));
		msg.append("\n").append(INDENT);
		for (int i = 0; i < bw.size(); ++i) {
			msg.append(bw.get(i));
			if (i < bw.size() - 1)
				msg.append(", ");
		}
		msg.append("\n");
		return msg.toString();
	}

	/** Build a user message for words that are not explicitly allowed.
	 * @param ms MULTI string
	 * @return A user message indicating which words are misspelled */
	static private String buildAllowedUserMsg(String multi) {
		List<String> aw = spellCheck(multi, true);
		if (aw.size() <= 0)
			return "";
		StringBuilder msg = new StringBuilder();
		msg.append(I18N.get("word.not.allowed"));
		msg.append("\n").append(INDENT);
		for (int i = 0; i < aw.size(); ++i) {
			msg.append(aw.get(i));
			if (i < aw.size() - 1)
				msg.append(", ");
		}
		msg.append("\n");
		return msg.toString();
	}

	/** Spell check a MULTI string, returning the misspelled words.
	 * @param ms MULTI string containing words to check.
	 * @param a True to check against allowed words else banned words.
	 * @return A list of incorrect words. */
	static private List<String> spellCheck(String ms, boolean a) {
		return spellCheck(new MultiString(ms).getWords(), a);
	}

	/** Spell check a list of words, returning the misspelled words.
	 * @param ws List of words in the message to spellcheck
	 * @param a True for the allowed list else banned word list.
	 * @return A list of words from the argument ws that are not 
	 * 	   contained in the dictionary */
	static private List<String> spellCheck(List<String> ws, boolean a) {
		ArrayList<String> wrong = new ArrayList<String>();
		for (String w : ws) {
			if (!spellCheckWord(w, a))
				wrong.add(w);
		}
		return wrong;
	}

	/** Is the specified word misspelled?
	 * @param w Word to check
	 * @param allow True to spell check with allowed word list else use
	 *              the banned word list.
	 * @return True if word argument is in the dictionary else false */
	static private boolean spellCheckWord(String w, boolean allow) {
		if (ignoreWord(w))
			return true;
		Word dwd = lookup(w);
		return (dwd != null) ? dwd.getAllowed() : !allow;
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
