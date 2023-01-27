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
import java.util.List;
import us.mn.state.dot.tms.utils.MultiAdapter;
import us.mn.state.dot.tms.utils.MultiBuilder;
import us.mn.state.dot.tms.utils.MultiString;
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
		ArrayList<String> hashtags = new ArrayList<String>();
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

	/** Find unused text rectangles in a pattern */
	static public List<TextRect> findTextRectangles(MsgPattern pat,
		TextRect tr)
	{
		return (tr != null)
		      ? tr.find(pat.getMulti())
		      : new ArrayList<TextRect>();
	}

	/** Check if a pattern has "fillable" text rectangles */
	static public boolean isFillable(MsgPattern pat, TextRect tr) {
		return findTextRectangles(pat, tr).size() > 0;
	}

	/** Split MULTI string into lines with a pattern */
	static public List<String> splitLines(MsgPattern pat, TextRect tr,
		String ms)
	{
		return (tr != null)
		      ? tr.splitLines(pat.getMulti(), ms)
		      : new ArrayList<String>();
	}
}
