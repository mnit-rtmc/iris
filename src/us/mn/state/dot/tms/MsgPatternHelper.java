/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2025  Minnesota Department of Transportation
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
		Hashtags hashtags = new Hashtags(dms.getNotes());
		Iterator<MsgPattern> it = iterator();
		while (it.hasNext()) {
			MsgPattern pat = it.next();
			if (isValidMulti(pat) &&
			    hashtags.contains(pat.getComposeHashtag()))
			{
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

	/** Compare two message patterns, preferring those which can be
	 * combined with a scheduled message (shared combining).
	 * @return "Better" of the provided patterns. */
	static public MsgPattern better(MsgPattern p0, MsgPattern p1) {
		if (p0 == null)
			return p1;
		if (p1 == null)
			return p0;
		String tr0 = new MultiString(p0.getMulti())
			.combiningSharedTextRectangle();
		String tr1 = new MultiString(p1.getMulti())
			.combiningSharedTextRectangle();
		if (tr0 != null && tr1 == null)
			return p0;
		else if (tr0 == null && tr1 != null)
			return p1;
		else {
			int l0 = p0.getMulti().length();
			int l1 = p1.getMulti().length();
			return (l0 < l1) ? p0 : p1;
		}
	}

	/** Find all sign configs for a message pattern.
	 * This includes configs for:
	 * - the pattern's compose hashtag
	 * - Device action with the pattern
	 * - Alert config + message with the pattern */
	static public Set<SignConfig> findSignConfigs(MsgPattern pat) {
		TreeSet<SignConfig> cfgs = new TreeSet<SignConfig>(
			new NumericAlphaComparator<SignConfig>());
		if (pat == null)
			return cfgs;
		LinkedHashSet<String> hashtags = new LinkedHashSet<String>();
		String cht = pat.getComposeHashtag();
		if (cht != null)
			hashtags.add(cht);
		hashtags.addAll(DeviceActionHelper.findHashtags(pat));
		Iterator<DMS> it = DMSHelper.iterator();
		while (it.hasNext()) {
			DMS dms = it.next();
			Hashtags tags = new Hashtags(dms.getNotes());
			if (tags.containsAny(hashtags)) {
				SignConfig sc = dms.getSignConfig();
				if (sc != null)
					cfgs.add(sc);
			}
		}
		cfgs.addAll(AlertMessageHelper.findSignConfigs(pat));
		return cfgs;
	}

	/** Get the count of lines associated with a message pattern */
	static public int lineCount(MsgPattern pat) {
		int n_lines = 0;
		Iterator<MsgLine> it = MsgLineHelper.iterator();
		while (it.hasNext()) {
			MsgLine ml = it.next();
			if (ml.getMsgPattern() == pat)
				n_lines = Math.max(n_lines, ml.getLine());
		}
		return n_lines;
	}

	/** Get full text rectangle for a sign */
	static private TextRect fullTextRect(DMS dms) {
		return (dms != null)
		      ? SignConfigHelper.textRect(dms.getSignConfig())
		      : null;
	}

	/** Split lines of a message based on a pattern.
	 * @return Fillable message lines, or null on error. */
	static private List<String> splitLines(MsgPattern pat, DMS dms,
		String ms)
	{
		if (pat == null)
			return null;
		TextRect tr = fullTextRect(dms);
		if (tr == null)
			return null;
		String pat_ms = pat.getMulti();
		List<String> lines = tr.splitLines(pat_ms, ms);
		// validate that filling the rectangle produces the same msg
		String ms2 = new MultiString(tr.fill(pat_ms, lines))
			.stripTrailingWhitespaceTags();
		return ms2.equals(ms) ? lines : null;
	}

	/** Validate text lines for a message pattern.
	 * @return Empty string (wrong pattern), validation error message,
	 *         or null on success. */
	static public String validateLines(MsgPattern pat, DMS dms,
		String ms)
	{
		List<String> lines = splitLines(pat, dms, ms);
		if (lines == null)
			return "";
		List<MsgLine> msg_lines =
			MsgLineHelper.findAllLines(pat, dms);
		short num = 0;
		for (String line: lines) {
			num++;
			if (line.isEmpty())
				continue;
			if (!validateLine(msg_lines, num, line))
				return "FREE-FORM NOT PERMITTED: " + line;
		}
		return null;
	}

	/** Validate one message line */
	static private boolean validateLine(List<MsgLine> msg_lines,
		short num, String line)
	{
		for (MsgLine ml: msg_lines) {
			if (ml.getLine() == num && line.equals(ml.getMulti()))
				return true;
		}
		return false;
	}

	/** Validate text words for a message pattern.
	 * @return Empty string (wrong pattern), validation error message,
	 *         or null on success. */
	static public String validateWords(MsgPattern pat, DMS dms,
		String ms)
	{
		List<String> lines = splitLines(pat, dms, ms);
		if (lines == null)
			return "";
		for (String line: lines) {
			for (String word: line.split(" ")) {
				word = word.trim();
				if (WordHelper.isBanned(word))
					return "BANNED WORD: " + word;
			}
		}
		return null;
	}

	/** Make list of text rectangles for each line in a pattern */
	static public List<TextRect> lineTextRects(MsgPattern pat, DMS dms) {
		if (pat == null)
			return null;
		TextRect full_rect = fullTextRect(dms);
		if (full_rect == null)
			return null;
		ArrayList<TextRect> rects = new ArrayList<TextRect>();
		rects.add(null); // line 0 is invalid
		String pat_ms = pat.getMulti();
		for (TextRect tr: full_rect.find(pat_ms)) {
			for (int i = 0; i < tr.getLineCount(); i++)
				rects.add(tr);
		}
		return rects;
	}

	/** Find a substitute pattern with associated message lines */
	static public MsgPattern findSubstitute(MsgPattern pat, DMS dms,
		int n_lines)
	{
		Hashtags tags = new Hashtags(dms.getNotes());
		Iterator<MsgPattern> it = iterator();
		while (it.hasNext()) {
			MsgPattern mp = it.next();
			if (mp != pat && isValidMulti(mp)) {
				if (lineCount(mp) == n_lines) {
					String ht = mp.getComposeHashtag();
					if (tags.contains(ht))
						return mp;
				}
			}
		}
		return null;
	}
}
