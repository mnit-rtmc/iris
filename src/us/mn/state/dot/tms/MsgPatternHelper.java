/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2023  Minnesota Department of Transportation
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import us.mn.state.dot.tms.utils.MultiString;
import us.mn.state.dot.tms.utils.NumericAlphaComparator;
import us.mn.state.dot.tms.utils.TextRect;

/**
 * Helper class for messages patterns.
 *
 * @author Douglas Lau
 */
public class MsgPatternHelper extends BaseHelper {

	/** Don't allow instances to be created */
	private MsgPatternHelper() {
		assert false;
	}

	/** Lookup the message pattern with the specified name */
	static public MsgPattern lookup(String name) {
		return (MsgPattern) namespace.lookupObject(
			MsgPattern.SONAR_TYPE, name);
	}

	/** Get a message pattern iterator */
	static public Iterator<MsgPattern> iterator() {
		return new IteratorWrapper<MsgPattern>(namespace.iterator(
			MsgPattern.SONAR_TYPE));
	}

	/** Find a message pattern with the specified MULTI string.
	 * @param ms MULTI string.
	 * @return A matching message pattern or null if no match is found. */
	static public MsgPattern find(String ms) {
		if (ms != null && !ms.isEmpty()) {
			MultiString multi = new MultiString(ms);
			Iterator<MsgPattern> it = iterator();
			while (it.hasNext()) {
				MsgPattern pat = it.next();
				if (multi.equals(pat.getMulti()))
					return pat;
			}
		}
		return null;
	}

	/** Find all compose message patterns for the specified DMS, sorted */
	static public Set<MsgPattern> findAllCompose(DMS dms) {
		TreeSet<MsgPattern> pats = new TreeSet<MsgPattern>(
			new NumericAlphaComparator<MsgPattern>());
		if (dms == null)
			return pats;
		Iterator<MsgPattern> it = iterator();
		while (it.hasNext()) {
			MsgPattern pat = it.next();
			String cht = pat.getComposeHashtag();
			if (cht != null && isValidMulti(pat)) {
				if (DMSHelper.hasHashtag(dms, cht))
					pats.add(pat);
			}
		}
		return pats;
	}

	/** Check if a message pattern contains only valid MULTI */
	static private boolean isValidMulti(MsgPattern pat) {
		MultiString ms = new MultiString(pat.getMulti());
		return ms.isValidMulti();
	}

	/** Compare two message patterns, preferring those with a trailing
	 * text rectangle.
	 * @return "Better" of the provided patterns. */
	static public MsgPattern better(MsgPattern p0, MsgPattern p1) {
		if (p0 == null)
			return p1;
		if (p1 == null)
			return p0;
		String t0 = new MultiString(p0.getMulti())
			.trailingTextRectangle();
		String t1 = new MultiString(p1.getMulti())
			.trailingTextRectangle();
		if (t0 == null && t1 != null)
			return p1;
		else if (t1 == null && t0 != null)
			return p0;
		else {
			int l0 = p0.getMulti().length();
			int l1 = p1.getMulti().length();
			return (l0 < l1) ? p0 : p1;
		}
	}

	/** Find all sign configs for a message pattern.
	 * This includes configs for:
	 * - the pattern's compose hashtag
	 * - Lane use multi with the pattern
	 * - DMS action with the pattern
	 * - Alert config + message with the pattern */
	static public List<SignConfig> findSignConfigs(MsgPattern pat) {
		ArrayList<SignConfig> cfgs = new ArrayList<SignConfig>();
		if (pat == null)
			return cfgs;
		LinkedHashSet<String> hashtags = new LinkedHashSet<String>();
		String cht = pat.getComposeHashtag();
		if (cht != null)
			hashtags.add(cht);
		hashtags.addAll(LaneUseMultiHelper.findHashtags(pat));
		hashtags.addAll(DmsActionHelper.findHashtags(pat));
		for (String ht: hashtags) {
			for (DMS dms: DMSHelper.findAllTagged(ht)) {
				SignConfig sc = dms.getSignConfig();
				if (sc != null && !cfgs.contains(sc))
					cfgs.add(sc);
			}
		}
		for (SignConfig sc: AlertMessageHelper.findSignConfigs(pat)) {
			if (!cfgs.contains(sc))
				cfgs.add(sc);
		}
		return cfgs;
	}

	/** Check if a message pattern has associated lines */
	static public boolean hasLines(MsgPattern pat) {
		Iterator<MsgLine> it = MsgLineHelper.iterator();
		while (it.hasNext()) {
			MsgLine ml = it.next();
			if (ml.getMsgPattern() == pat)
				return true;
		}
		return false;
	}

	/** Validate text lines for a message pattern */
	static public boolean validateLines(MsgPattern pat, SignConfig sc,
		String ms)
	{
		if (sc == null || pat == null)
			return false;
		TextRect tr = SignConfigHelper.textRect(sc);
		List<String> lines = tr.splitLines(pat.getMulti(), ms);
		short num = 0;
		for (String line: lines) {
			num++;
			if ((!line.isEmpty()) &&
			    (!MsgLineHelper.match(pat, num, line)))
				return false;
		}
		return true;
	}

	/** Validate text words for a message pattern */
	static public String validateWords(MsgPattern pat, SignConfig sc,
		String ms)
	{
		if (sc == null || pat == null)
			return "NULL VALUE";
		StringBuilder sb = new StringBuilder();
		TextRect tr = SignConfigHelper.textRect(sc);
		List<String> lines = tr.splitLines(pat.getMulti(), ms);
		for (String line: lines) {
			for (String word: line.split(" ")) {
				word = word.trim();
				if (WordHelper.isBanned(word)) {
					if (sb.length() > 0)
						sb.append(", ");
					sb.append(word);
				}
			}
		}
		return (sb.length() > 0) ? sb.toString() : null;
	}
}
