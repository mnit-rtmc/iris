/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016  Iteris Inc.
 * Copyright (C) 2019-2020  Minnesota Department of Transportation
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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import us.mn.state.dot.tms.utils.I18N;
import us.mn.state.dot.tms.utils.MultiString;
import us.mn.state.dot.tms.utils.SString;

/**
 * Static Word convenience methods accessible from the client and server.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public class WordHelper extends BaseHelper {

	/** Word scheme system attribute */
	private enum WordScheme {
		OFF(),		// 0
		RECOMMEND(),	// 1
		ENFORCE();	// 2

		/** Get the number of items in the enum */
		public int size() {
			return WordScheme.values().length;
		}

		/** Return the enum from an ordinal or null if invalid */
		static private WordScheme fromOrdinal(int o) {
			if (o >= 0 && o < values().length)
				return values()[o];
			else
				return null;
		}
	}

	/** Disallow instantiation */
	private WordHelper() {
		assert false;
	}

	/** Indent for messages */
	static private String INDENT = "    ";

	/** Get an iterator */
	static public Iterator<Word> iterator() {
		return new IteratorWrapper<Word>(
			namespace.iterator(Word.SONAR_TYPE));
	}

	/** Lookup a Word in the SONAR namespace. 
	 * @param name Unencoded word to lookup. Will be converted
	 * 	       to uppercase to determine if it exists.
	 * @return The specified word or null if not in namespace */
	static public Word lookup(String name) {
		if (namespace == null || name == null)
			return null;
		String up = name.toUpperCase();
		String en = WordHelper.encode(up);
		return (Word) namespace.lookupObject(Word.SONAR_TYPE, en);
	}

	/** Get the word scheme for allowed or banned words.
	 * @param allow True for the allowed list else banned.
	 * @return Word scheme specified by system attribute */
	static private WordScheme getWordScheme(boolean allow) {
		if (allow) {
			return WordScheme.fromOrdinal(SystemAttrEnum.
				DICT_ALLOWED_SCHEME.getInt());
		} else {
			return WordScheme.fromOrdinal(SystemAttrEnum.
				DICT_BANNED_SCHEME.getInt());
		}
	}

	/** Is spell checking enabled? */
	static public boolean spellCheckEnabled() {
		WordScheme ads = getWordScheme(true);
		WordScheme bds = getWordScheme(false);
		return ads != WordScheme.OFF || bds != WordScheme.OFF;
	}

	/** Is spell checking in enforcement mode? */
	static public boolean spellCheckEnforced() {
		WordScheme ads = getWordScheme(true);
		WordScheme bds = getWordScheme(false);
		return ads == WordScheme.ENFORCE || bds == WordScheme.ENFORCE;
	}

	/** Is spell checking in recommend mode? */
	static public boolean spellCheckRecommend() {
		if (!spellCheckEnabled() || spellCheckEnforced())
			return false;
		else
			return true;
	}

	/** Spell check a MULTI string.
	 * @param multi MULTI string containing words to check.
	 * @return An empty string if no words are misspelled, otherwise a 
	 *	   message for the user. */
	static public String spellCheck(String multi) {
		if (!spellCheckEnabled())
			return "";
		StringBuilder msg = new StringBuilder();
		msg.append(buildBannedUserMsg(multi));
		msg.append(buildAllowedUserMsg(multi));
		return msg.toString();
	}

	/** Build a user message for words that are banned.
	 * @param ms MULTI string
	 * @return A user message indicating which words are misspelled */
	static private String buildBannedUserMsg(String multi) {
		WordScheme bds = getWordScheme(false);
		if (bds == WordScheme.OFF)
			return "";
		List<String> bw = WordHelper.spellCheck(multi, false);
		if (bw.size() <= 0)
			return "";
		StringBuilder msg = new StringBuilder();
		if (bds == WordScheme.RECOMMEND) {
			msg.append(I18N.get("word.not.recommended"));
			msg.append("\n").append(INDENT);
		} else if (bds == WordScheme.ENFORCE) {
			msg.append(I18N.get("word.banned.msg"));
			msg.append("\n").append(INDENT);
		}
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
		WordScheme ads = getWordScheme(true);
		if (ads == WordScheme.OFF)
			return "";
		List<String> aw = WordHelper.spellCheck(multi, true);
		if (aw.size() <= 0)
			return "";
		StringBuilder msg = new StringBuilder();
		if (ads == WordScheme.RECOMMEND) {
			msg.append(I18N.get("word.not.recommended"));
			msg.append("\n").append(INDENT);
		} else if (ads == WordScheme.ENFORCE) {
			msg.append(I18N.get("word.not.allowed"));
			msg.append("\n").append(INDENT);
		}
		for (int i = 0; i < aw.size(); ++i) {
			msg.append(aw.get(i));
			if (i < aw.size() - 1)
				msg.append(", ");
		}
		msg.append("\n");
		return msg.toString();
	}

	/** Check if any words in the specified MULTI string can be
	 * abbreviated.
	 * @param ms MULTI string containing words to check.
	 * @return User message listing words that can be abbreviated */
	static public String abbreviationCheck(String ms) {
		List<String> mwds = new MultiString(ms).getWords();
		List<Word> awords = new LinkedList<Word>();
		for (String mwd : mwds) {
			if (ignoreWord(mwd))
				continue;
			Iterator<Word> it = WordHelper.iterator();
			while (it.hasNext()) {
				Word dwd = it.next();
				if (!hasAbbr(dwd))
					continue;
				if (WordHelper.equals(mwd, dwd.getName()))
					awords.add(dwd);
			}
		}
		if (awords.isEmpty())
			return "";
		StringBuilder msg = new StringBuilder();
		msg.append("These words can be abbreviated:\n");
		for (Word aword : awords) {
			msg.append(INDENT).append(aword.getName());
			msg.append(" (").append(aword.getAbbr()).append(")");
			msg.append("\n");
		}
		return msg.toString();
	}

	/** Does a word have an abbreviation? */
	static public boolean hasAbbr(Word wd) {
		return wd != null && wd.getAllowed() &&
		      !wd.getAbbr().trim().isEmpty();
	}

	/** Are two words equal?
	 * @return True if both words are equal */
	static public boolean equals(String w1, String w2) {
		if (w1 == null || w2 == null)
			return false;
		return w1.toUpperCase().equals(w2.toUpperCase());
	}

	/** Spell check a MULTI string, returning the misspelled words.
	 * @param ms MULTI string containing words to check.
	 * @param a True to check against allowed words else banned words.
	 * @return A list of incorrect words. */
	static private List<String> spellCheck(String ms, boolean a) {
		List<String> wrong = new LinkedList<String>();
		return spellCheck(new MultiString(ms).getWords(), a);
	}

	/** Spell check a list of words, returning the misspelled words.
	 * @param ws List of words in the message to spellcheck
	 * @param a True for the allowed list else banned word list.
	 * @return A list of words from the argument ws that are not 
	 * 	   contained in the dictionary */
	static private List<String> spellCheck(List<String> ws, boolean a) {
		LinkedList<String> wrong = new LinkedList<String>();
		for (String w : ws) {
			if (!spellCheckWord(w, a))
				wrong.add(w);
		}
		return wrong;
	}

	/** Is the specified word misspelled?
	 * @param w Word to check
	 * @param allow True to spell check with allowed word list else use
	 * 		the banned word list.
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

	/** Encode a word to avoid sonar namespace issues,
	 * for example "US89/191". Words are sonar names.
	 * @param n Word to encode
	 * @return Encoded word  */
	static public String encode(String n) {
		try {
			return URLEncoder.encode(n, "UTF-8");
		} catch (UnsupportedEncodingException ex) {
			System.err.println("ex=" + ex);
			return n;
		}
	}

	/** Decode a word
	 * @param n Word to decode
	 * @return Decoded word  */
	static public String decode(String n) {
		try {
			return URLDecoder.decode(n, "UTF-8");
		} catch (UnsupportedEncodingException ex) {
			System.err.println("ex=" + ex);
			return n;
		}
	}

	/** Lookup a word and return its abbreviation */
	static public String abbreviate(String w) {
		Word word = lookup(w);
		return (word != null && word.getAllowed())
		      ? word.getAbbr()
		      : null;
	}
}
