/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2022  Minnesota Department of Transportation
 * Copyright (C) 2014-2015  AHMCT, University of California
 * Copyright (C) 2019-2020  SRF Consulting Group
 * Copyright (C) 2021  Iteris Inc.
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
package us.mn.state.dot.tms.utils;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import us.mn.state.dot.tms.PageTimeHelper;
import us.mn.state.dot.tms.units.Interval;
import us.mn.state.dot.tms.utils.Multi.OverLimitMode;

/**
 * MULTI String (MarkUp Language for Transportation Information), as specified
 * in NTCIP 1203.
 *
 * @author Douglas Lau
 * @author Michael Darter
 * @author Travis Swanston
 * @author John L. Stanley - SRF Consulting
 * @author Gordon Parikh - SRF Consulting
 */
public class MultiString {

	/** Regular expression to match text spans between MULTI tags */
	static private final Pattern SPAN = Pattern.compile(
		"[ !\"#$%&'()*+,-./0-9:;<=>?@A-Z\\[\\\\\\]^_`a-z{|}~]*");

	/** Regular expression to match message ending with a text rectangle */
	static private final Pattern ENDS_WITH_TR = Pattern.compile(
		".*\\[tr\\d+,\\d+,\\d+,\\d+\\]");

	/** A MULTI builder for normalizing spans and removing invalid tags */
	static private class MultiNormalizer extends MultiBuilder {
		@Override
		public void addSpan(String s) {
			Matcher m = SPAN.matcher(s);
			while (m.find())
				super.addSpan(filterSpan(m.group()));
		}
	}

	/** A MULTI builder for normalizing by line */
	static private class LineMultiNormalizer extends MultiNormalizer {
		// Strip tags which don't associate with a line
		@Override
		public void setColorBackground(Integer x) {}
		@Override
		public void setPageBackground(Integer z) {}
		@Override
		public void setPageBackground(int r, int g, int b) {}
		@Override
		public void addColorRectangle(int x, int y, int w, int h,
			int z) {}
		@Override
		public void addColorRectangle(int x, int y, int w, int h,
			int r, int g, int b) {}
		@Override
		public void addGraphic(int g_num, Integer x, Integer y,
			String g_id) {}
		@Override
		public void setJustificationPage(Multi.JustificationPage jp) {}
		@Override
		public void addLine(Integer spacing) {}
		@Override
		public void addPage() {}
		@Override
		public void setPageTimes(Integer on, Integer off) {}
		@Override
		public void setTextRectangle(int x, int y, int w, int h) {}
		// action tags not allowed in SignText
		@Override
		public void addClearGuideAdvisory(String dms, int rid,
			int tsp, String mode, int ridx) {}
		@Override
		public void addExitWarning(String did, int occ) {}
		@Override
		public void addFeed(String fid) {}
		@Override
		public void addParking(String pid, String l_txt, String c_txt){}
		@Override
		public void addSlowWarning(int spd, int dist, String mode) {}
		@Override
		public void addStandby() {}
		@Override
		public void addTimeAction(String dir, String format) {}
		@Override
		public void addTravelTime(String sid, OverLimitMode mode,
			String o_txt) {}
		@Override
		public void addTolling(String mode, String[] zones) {}
		@Override
		public void addSpeedAdvisory() {}
	}

	/** Filter brackets in a span of text */
	static private String filterSpan(String s) {
		return s.replace("[[", "[").replace("]]", "]");
	}

	/** Parse an integer value */
	static private Integer parseInt(String[] args, int n) {
		return (n < args.length) ? parseInt(args[n]) : null;
	}

	/** Parse an integer value */
	static private Integer parseInt(String param) {
		try {
			return Integer.parseInt(param);
		}
		catch (NumberFormatException e) {
			return null;
		}
	}

	/** Parse one MULTI tag */
	static private void parseTag(String tag, Multi cb) {
		String ltag = tag.toLowerCase();
		if (ltag.startsWith("cb"))
			parseColorBackground(tag.substring(2), cb);
		else if (ltag.startsWith("pb"))
			parsePageBackground(tag.substring(2), cb);
		else if (ltag.startsWith("cf"))
			parseColorForeground(tag.substring(2), cb);
		else if (ltag.startsWith("cr"))
			parseColorRectangle(tag.substring(2), cb);
		else if (ltag.startsWith("fo"))
			parseFont(tag.substring(2), cb);
		else if (ltag.startsWith("g"))
			parseGraphic(tag.substring(1), cb);
		else if (ltag.startsWith("jl"))
			parseJustificationLine(tag.substring(2), cb);
		else if (ltag.startsWith("jp"))
			parseJustificationPage(tag.substring(2), cb);
		else if (ltag.startsWith("nl"))
			cb.addLine(parseInt(tag.substring(2)));
		else if (ltag.startsWith("np"))
			cb.addPage();
		else if (ltag.startsWith("pt"))
			parsePageTimes(tag.substring(2), cb);
		else if (ltag.startsWith("sc"))
			parseCharSpacing(tag.substring(2), cb);
		else if (ltag.startsWith("/sc"))
			parseCharSpacing(null, cb);
		else if (ltag.startsWith("tr"))
			parseTextRectangle(tag.substring(2), cb);
		else if (ltag.startsWith("cg"))
			parseClearGuideAdvisory(tag.substring(2), cb);
		else if (ltag.startsWith("exit"))
			parseExitWarning(tag.substring(4), cb);
		else if (ltag.startsWith("feed"))
			cb.addFeed(tag.substring(4));
		else if (ltag.startsWith("loc"))
			parseLocator(tag.substring(3), cb);
		else if (ltag.startsWith("pa"))
			parseParking(tag, cb);
		else if (ltag.startsWith("slow"))
			parseSlowWarning(tag.substring(4), cb);
		else if (ltag.startsWith("standby"))
			cb.addStandby();
		else if (ltag.startsWith("ta"))
			parseTimeAction(tag.substring(2), cb);
		else if (ltag.startsWith("tt"))
			parseTravelTime(tag.substring(2), cb);
		else if (ltag.startsWith("tz"))
			parseTolling(tag.substring(2), cb);
		else if (ltag.startsWith("vsa"))
			cb.addSpeedAdvisory();
		else
			cb.unsupportedTag(tag);
	}

	/** Parse a (deprecated) background color tag */
	static private void parseColorBackground(String v, Multi cb) {
		Integer x = parseInt(v);
		if (x != null)
			cb.setColorBackground(x);
	}

	/** Parse a page background color tag */
	static private void parsePageBackground(String v, Multi cb) {
		String[] args = v.split(",", 3);
		if (args.length == 1) {
			Integer z = parseInt(args, 0);
			if (z != null)
				cb.setPageBackground(z);
		} else {
			Integer r = parseInt(args, 0);
			Integer g = parseInt(args, 1);
			Integer b = parseInt(args, 2);
			if (r != null && g != null && b != null)
				cb.setPageBackground(r, g, b);
		}
	}

	/** Parse a color foreground tag */
	static private void parseColorForeground(String v, Multi cb) {
		String[] args = v.split(",", 3);
		if (args.length == 1) {
			Integer x = parseInt(args, 0);
			cb.setColorForeground(x);
		} else {
			Integer r = parseInt(args, 0);
			Integer g = parseInt(args, 1);
			Integer b = parseInt(args, 2);
			if (r != null && g != null && b != null)
				cb.setColorForeground(r, g, b);
		}
	}

	/** Parse color rectangle from a [cr...] tag.
	 * @param v Color rectangle tag value.
	 * @param cb Callback to set color rectangle. */
	static private void parseColorRectangle(String v, Multi cb) {
		String[] args = v.split(",", 7);
		Integer x = parseInt(args, 0);
		Integer y = parseInt(args, 1);
		Integer w = parseInt(args, 2);
		Integer h = parseInt(args, 3);
		Integer r = parseInt(args, 4);
		Integer g = parseInt(args, 5);
		Integer b = parseInt(args, 6);
		if (x != null && y != null && w != null && h != null &&
		    r != null)
		{
			if (g != null && b != null)
				cb.addColorRectangle(x, y, w, h, r, g, b);
			else
				cb.addColorRectangle(x, y, w, h, r);
		}
	}

	/** Parse a font number from an [fox] or [fox,cccc] tag.
	 * @param f Font tag value (x or x,cccc from [fox] or [fox,cccc] tag).
	 * @param cb Callback to set font information. */
	static private void parseFont(String f, Multi cb) {
		String[] args = f.split(",", 2);
		Integer f_num = parseInt(args, 0);
		String f_id = (args.length > 1) ? args[1] : null;
		cb.setFont(f_num, f_id);
	}

	/** Parse a graphic number from a [gn] or [gn,x,y] or [gn,x,y,cccc] tag.
	 * @param g Graphic tag value (n or n,x,y or n,x,y,cccc from tag).
	 * @param cb Callback to set graphic information */
	static private void parseGraphic(String g, Multi cb) {
		String[] args = g.split(",", 4);
		Integer g_num = parseInt(args, 0);
		Integer x = parseInt(args, 1);
		Integer y = parseInt(args, 2);
		String g_id = (args.length > 3) ? args[3] : null;
		if (g_num != null)
			cb.addGraphic(g_num, x, y, g_id);
	}

	/** Parse a line justification tag.
	 * @param v Line justification tag value.
	 * @param cb Callback to set line justification. */
	static private void parseJustificationLine(String v, Multi cb) {
		Integer j = parseInt(v);
		Multi.JustificationLine jl = (j != null)
			? Multi.JustificationLine.fromOrdinal(j)
			: null;
		cb.setJustificationLine(jl);
	}

	/** Parse a page justification tag.
	 * @param v Page justification tag value.
	 * @param cb Callback to set page justification. */
	static private void parseJustificationPage(String v, Multi cb) {
		Integer j = parseInt(v);
		Multi.JustificationPage jp = (j != null)
			? Multi.JustificationPage.fromOrdinal(j)
			: null;
		cb.setJustificationPage(jp);
	}

	/** Parse page times from a [pt.o.] tag.
	 * @param v Page time tag value.
	 * @param cb Callback to set page times. */
	static private void parsePageTimes(String v, Multi cb) {
		String[] args = v.split("o", 2);
		Integer pt_on = parseInt(args, 0);
		Integer pt_off = parseInt(args, 1);
		cb.setPageTimes(pt_on, pt_off);
	}

	/** Parse character spacing from a [scx] tag.
	 * @param sc Character spacing value from tag.
	 * @param cb Callback to set spacing information. */
	static private void parseCharSpacing(String sc, Multi cb) {
		cb.setCharSpacing(parseInt(sc));
	}

	/** Parse text rectangle from a [tr...] tag.
	 * @param v Text rectangle tag value.
	 * @param cb Callback to set text rectangle. */
	static private void parseTextRectangle(String v, Multi cb) {
		String[] args = v.split(",", 4);
		Integer x = parseInt(args, 0);
		Integer y = parseInt(args, 1);
		Integer w = parseInt(args, 2);
		Integer h = parseInt(args, 3);
		if (x != null && y != null && w != null && h != null)
			cb.setTextRectangle(x, y, w, h);
	}

	/** Parse travel time from a [tts], [tts,m] or [tts,m,t] tag.
	 * @param v Travel time tag value (s or s,m or s,m,t from tag).
	 * @param cb Callback to set travel time. */
	static private void parseTravelTime(String v, Multi cb) {
		String[] args = v.split(",", 3);
		String sid = (args.length > 0) ? args[0] : null;
		OverLimitMode mode = (args.length > 1)
		                   ? parseOverMode(args[1])
		                   : OverLimitMode.prepend;
		String o_txt = (args.length > 2) ? args[2] : "OVER ";
		if (sid != null)
			cb.addTravelTime(sid, mode, o_txt);
	}

	/** Parse ClearGuide advisory args from tag
	 * @param v Tag [cg dms_name,cg_route_id,statistic_min,mode,route_num]
	 * @param cb Callback to add tag */
	static private void parseClearGuideAdvisory(String v, Multi cb) {
		String[] args = v.split(",", 5);
		String dms = safeGet(args, 0);
		int wid = safeGetInt(args, 1);
		int tsp = safeGetInt(args, 2);
		String mode = safeGet(args, 3);
		int ridx = safeGetInt(args, 4);  // optional, defaults to 0
		cb.addClearGuideAdvisory(dms, wid, tsp, mode, ridx);
	}

	/** Get the nth (zero-based) element or empty string
	 * @return Trimmed never null element idx */
	static private String safeGet(String[] args, int idx) {
		return (args.length > idx ? args[idx] : "").trim();
	}

	/** Get the nth (zero-based) element or empty string
	 * @return Trimmed element idx as integer */
	static private int safeGetInt(String[] args, int idx) {
		String stri = safeGet(args, idx);
		return !stri.isEmpty() ? Integer.parseInt(stri) : 0;
	}

	/** Parse a over limit mode value */
	static private OverLimitMode parseOverMode(String mode) {
		for (OverLimitMode m : OverLimitMode.values()) {
			if (mode.equals(m.toString()))
				return m;
		}
		return OverLimitMode.prepend;
	}

	/** Parse slow traffic warning from a [slows,d] or [slows,d,m] tag.
	 * @param v Slow traffic tag value (s,d or s,d,m from tag).
	 * @param cb Callback to set slow warning. */
	static private void parseSlowWarning(String v, Multi cb) {
		String[] args = v.split(",", 3);
		Integer spd = parseInt(args, 0);
		Integer dist = parseInt(args, 1);
		String mode = (args.length > 2) ? parseSlowMode(args[2]) : null;
		if (isSpeedValid(spd) && isDistValid(dist))
			cb.addSlowWarning(spd, dist, mode);
	}

	/** Parse exit backup warning from a [exitd,o] tag.
	 * @param v Exit backup tag value (d,o tag).
	 * @param cb Callback to set exit warning. */
	static private void parseExitWarning(String v, Multi cb) {
		String[] args = v.split(",", 2);
		String did = (args.length > 0) ? args[0] : null;
		Integer occ = parseInt(args, 1);
		if (did != null && isOccValid(occ))
			cb.addExitWarning(did, occ);
	}

	/** Parse tolling tag [tz{p,o,c},z1,...zn].
	 * @param v Tolling tag value ({p,o,c},z1,...zn).
	 * @param cb Callback to set tag. */
	static private void parseTolling(String v, Multi cb) {
		String[] args = v.split(",", 2);
		if (args.length == 2) {
			String mode = args[0];
			String[] zones = args[1].split(",");
			if (mode.equals("p") ||
			    mode.equals("o") ||
			    mode.equals("c"))
				cb.addTolling(mode, zones);
		}
	}

	/** Parse parking tag [pax,l_txt,c_txt].
	 * @param tag Parking area tag.
	 * @param cb Callback to set tag. */
	static private void parseParking(String tag, Multi cb) {
		String[] args = tag.split(",", 3);
		String pid = (args.length > 0) ? args[0] : "pa";
		String l_txt = (args.length > 1) ? args[1] : "LOW";
		String c_txt = (args.length > 2) ? args[2] : "CLOSED";
		cb.addParking(pid, l_txt, c_txt);
	}

	/** Parse time action tag [tadir,format].
	 *  @param tag Time action tag.
	 *  @param cb Callback to set tag.  */
	static private void parseTimeAction(String tag, Multi cb) {
		String[] args = tag.split(",", 2);
		String dir = (args.length > 0) ? args[0] : "n";
		String format = (args.length > 1) ? args[1] : "h a";
		cb.addTimeAction(dir, format);
	}

	/** Parse locator tag [loc{rn,rd,md,xn,xa,mi}].
	 * @param code Locator tag code ({rn,rd,md,xn,xa,mi}).
	 * @param cb Callback to set tag. */
	static private void parseLocator(String code, Multi cb) {
		if (code.equals("rn") ||
		    code.equals("rd") ||
		    code.equals("md") ||
		    code.equals("xn") ||
		    code.equals("xa") ||
		    code.equals("mi"))
			cb.addLocator(code);
	}

	/** Test if a parsed speed is valid */
	static private boolean isSpeedValid(Integer spd) {
		return spd != null && spd > 0 && spd < 100;
	}

	/** Test if a parsed distance is valid (1/10 mile units) */
	static private boolean isDistValid(Integer d) {
		return d != null && d > 0 && d <= 160;
	}

	/** Parse a slow mode value */
	static private String parseSlowMode(String param) {
		return ("dist".equals(param) || "speed".equals(param))
		      ? param
		      : null;
	}

	/** Test if a parsed occupancy is valid */
	static private boolean isOccValid(Integer occ) {
		return occ != null && occ > 0 && occ < 100;
	}

	/** Make a combined message (either shared or sequenced) */
	static public String makeCombined(String first, String second) {
		final MultiString ms1 = new MultiString(first);
		final MultiString ms2 = new MultiString(second);
		if (ms1.isValidSharedFirst() && ms2.isValidSharedSecond()) {
			// Prepend first message before each page of second
			MultiBuilder mb = new MultiBuilder(first) {
				@Override
				public void addPage() {
					super.addPage();
					// Add first message to next page
					append(ms1);
					// Reset these to default values
					setColorForeground(null);
					setFont(null, null);
					setJustificationLine(null);
					setJustificationPage(null);
				}
			};
			mb.setColorForeground(null);
			mb.setFont(null, null);
			mb.setJustificationLine(null);
			mb.setJustificationPage(null);
			ms2.parse(mb);
			return mb.toString();
		} else {
			MultiBuilder mb = new MultiBuilder(first);
			// Reset these to default values
			mb.setColorForeground(null);
			mb.setFont(null, null);
			mb.setJustificationLine(null);
			mb.setJustificationPage(null);
			// Add second page
			mb.addPage();
			mb.append(ms2);
			return mb.toString();
		}
	}

	/** MULTI string buffer */
	private final String multi;

	/** Create a new MULTI string.
	 * @param m MULTI string, may not be null.
	 * @throws NullPointerException if m is null. */
	public MultiString(String m) {
		if (m == null)
			throw new NullPointerException();
		multi = m;
	}

	/** Test if the MULTI string is equal to another MULTI string */
	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (o != null) {
			String ms = normalize().toString();
			String oms = new MultiString(o.toString())
				.normalize().toString();
			return ms.equals(oms);
		}
		return false;
	}

	/** Calculate a hash code for the MULTI string */
	@Override
	public int hashCode() {
		return multi.hashCode();
	}

	/** Get the value of the MULTI string */
	@Override
	public String toString() {
		return multi;
	}

	/** Validate the MULTI string (may contain action / locator tags) */
	public boolean isValid() {
		final boolean[] valid = new boolean[] { true };
		parse(new MultiAdapter() {
			@Override public void unsupportedTag(String t) {
				valid[0] = false;
			}
			@Override public void addSpan(String s) {
				Matcher m = SPAN.matcher(s);
				if (!m.matches())
					valid[0] = false;
			}
		});
		return valid[0];
	}

	/** Validate the MULTI string (only standard MULTI) */
	public boolean isValidMulti() {
		final boolean[] valid = new boolean[] { true };
		parse(new MultiAdapter() {
			@Override public void unsupportedTag(String t) {
				valid[0] = false;
			}
			@Override public void addSpan(String s) {
				Matcher m = SPAN.matcher(s);
				if (!m.matches())
					valid[0] = false;
			}
			@Override public void addClearGuideAdvisory(
				String dms, int rid, int tsp, String mode,
				int ridx)
			{
				valid[0] = false;
			}
			@Override
			public void addExitWarning(String did, int occ) {
				valid[0] = false;
			}
			@Override public void addFeed(String fid) {
				valid[0] = false;
			}
			@Override
			public void addParking(String p, String lt, String ct) {
				valid[0] = false;
			}
			@Override
			public void addSlowWarning(int spd, int dist, String m){
				valid[0] = false;
			}
			@Override public void addStandby() {
				valid[0] = false;
			}
			@Override
			public void addTimeAction(String dir, String format) {
				valid[0] = false;
			}
			@Override public void addTravelTime(String sid,
				OverLimitMode me, String o_txt)
			{
				valid[0] = false;
			}
			@Override
			public void addTolling(String mode, String[] zones) {
				valid[0] = false;
			}
			@Override public void addSpeedAdvisory() {
				valid[0] = false;
			}
			@Override public void addLocator(String code) {
				valid[0] = false;
			}
		});
		return valid[0];
	}

	/** Parse the MULTI string.
	 * @param cb A callback which keeps track of the MULTI state. */
	public void parse(Multi cb) {
		int i = 0;
		while (i < multi.length()) {
			int b0 = findBracket('[', i);
			int b1 = findBracket(']', i);
			int bx = Math.max(b0, b1);
			if (bx < 0) {
				cb.addSpan(filterSpan(multi.substring(i)));
				break;
			}
			assert (b0 >= 0) || (b1 >= 0);
			int bm = Math.min(b0, b1);
			int bn = (bm < 0) ? bx : bm;
			if (bn > i)
				cb.addSpan(filterSpan(multi.substring(i, bn)));
			if (b1 < 0) {
				assert b0 >= 0;
				i = b0 + 1;
				cb.unsupportedTag("[");
			} else if (b0 < 0 || b0 > b1) {
				i = b1 + 1;
				cb.unsupportedTag("]");
			} else {
				i = bx + 1;
				assert (b0 >= 0) && (b1 > b0);
				parseTag(multi.substring(b0 + 1, b1), cb);
			}
		}
	}

	/** Find the next (non-doubled) bracket */
	private int findBracket(char val, int start) {
		int end = multi.length() - 1;
		for (int i = start; i < end; i++) {
			if (multi.charAt(i) == val) {
				if (multi.charAt(i + 1) != val)
					return i;
				else
					i++;
			}
		}
		if (start <= end && multi.charAt(end) == val) {
			if (start == end || multi.charAt(end - 1) != val)
				return end;
		}
		return -1;
	}

	/** Is the MULTI string blank? */
	public boolean isBlank() {
		final boolean[] blank = new boolean[] { true };
		parse(new MultiAdapter() {
			@Override public void addSpan(String span) {
				if (span.trim().length() > 0)
					blank[0] = false;
			}
			@Override public void setPageBackground(int red,
				int green, int blue)
			{
				blank[0] = false;
			}
			@Override public void addColorRectangle(int x, int y,
				int w, int h, int r, int g, int b)
			{
				blank[0] = false;
			}
			@Override public void addGraphic(int g_num, Integer x,
				Integer y, String g_id)
			{
				blank[0] = false;
			}
		});
		return blank[0];
	}

	/** Normalize a MULTI string.
	 * @return A normalized MULTI string with invalid characters and
	 *         invalid tags removed. */
	public MultiString normalize() {
		MultiBuilder mb = new MultiNormalizer();
		parse(mb);
		return mb.toMultiString();
	}

	/** Normalize an incident locator MULTI string */
	public MultiString normalizeLocator() {
		MultiBuilder mb = new LineMultiNormalizer();
		parse(mb);
		return mb.toMultiString();
	}

	/** Normalize a single line MULTI string */
	public MultiString normalizeLine() {
		MultiBuilder mb = new LineMultiNormalizer() {
			// locator tags also not allowed
			@Override
			public void addLocator(String code) {}
		};
		parse(mb);
		return mb.toMultiString();
	}

	/** Strip font tags from a MULTI string */
	public MultiString stripFonts() {
		MultiBuilder mb = new MultiBuilder() {
			@Override
			public void setFont(Integer fn, String f_id) {}
		};
		parse(mb);
		return mb.toMultiString();
	}

	/** Strip all page time tags from a MULTI string */
	public String stripPageTime() {
		MultiBuilder mb = new MultiBuilder() {
			@Override
			public void setPageTimes(Integer on, Integer off) { }
		};
		parse(mb);
		return mb.toString();
	}

	/** Strip trailing empty lines */
	public String stripTrailingLines() {
		String ms = multi;
		while (ms.endsWith("[nl]"))
			ms = ms.substring(0, ms.length() - 4);
		return ms;
	}

	/** Replace all the page times in a MULTI string.
	 * If no page time tag exists, then a page time tag is prepended.
	 * @param pt_on Page on-time in tenths of a second.
	 * @param pt_off Page off-time in tenths of a second.
	 * @return The updated MULTI string. */
	public String replacePageTime(final Integer pt_on, final Integer pt_off)
	{
		if (multi.indexOf("[pt") < 0) {
			MultiBuilder mb = new MultiBuilder();
			mb.setPageTimes(pt_on, pt_off);
			return mb.toString() + multi;
		}
		MultiBuilder mb = new MultiBuilder() {
			@Override
			public void setPageTimes(Integer on, Integer off) {
				super.setPageTimes(pt_on, pt_off);
			}
		};
		parse(mb);
		return mb.toString();
	}

	/** Return a value indicating if the message is single or multi-page.
	 * @return True if the message contains a single page else false
	 * for multi-page. */
	public boolean singlePage() {
		return getNumPages() <= 1;
	}

	/** Get the number of pages in the multistring */
	public int getNumPages() {
		final int[] n_pages = new int[] { 1 };
		parse(new MultiAdapter() {
			@Override public void addPage() {
				n_pages[0]++;
			}
		});
		return n_pages[0];
	}

	/** Get an array of font numbers.
	 * @param f_num Default font number, one based.
	 * @return An array of font numbers for each page of the message. */
	public int[] getFonts(final int f_num) {
		if (f_num < 1 || f_num > 255)
			return new int[0];
		final ArrayList<Integer> fonts = new ArrayList<Integer>();
		fonts.add(f_num);
		parse(new MultiAdapter() {
			private int font_num = f_num;
			@Override public void setFont(Integer fn, String f_id) {
				if (fn == null)
					font_num = f_num;
				else
					font_num = fn;
				fonts.set(fonts.size() - 1, font_num);
			}
			@Override public void addPage() {
				fonts.add(font_num);
			}
			@Override public void addSpan(String span) {
				fonts.set(fonts.size() - 1, font_num);
			}
		});
		int[] ret = new int[fonts.size()];
		for (int i = 0; i < ret.length; i++)
			ret[i] = fonts.get(i);
		return ret;
	}

	/** Get the page-on interval for the 1st page. If no page-on is
	 * specified in the MULTI string, the default is returned.
	 * @return The page-on interval. */
	public Interval pageOnInterval() {
		Interval dflt = PageTimeHelper.defaultPageOnInterval();
		Interval[] pg_on = pageOnIntervals(dflt);
		// return 1st page on-time read, even if specified per page
		return pg_on[0];
	}

	/** Get an array of page-on time intervals.
	 * @param dflt Default page-on time.
	 * @return An array of page-on time Intervals, one value per page. */
	public Interval[] pageOnIntervals(Interval dflt) {
		PageTimeCounter ptc = new PageTimeCounter();
		parse(ptc);
		return ptc.pageOnIntervals(dflt);
	}

	/** Get the page-off interval for the 1st page. If no page-off is
	 * specified in the MULTI string, the default is returned.
	 * @return The page-off interval. */
	public Interval pageOffInterval() {
		Interval dflt = PageTimeHelper.defaultPageOffInterval();
		Interval[] pg_off = pageOffIntervals(dflt);
		// return 1st page-off time read, even if specified per page
		return pg_off[0];
	}

	/** Get an array of page-off time intervals.
	 * @param dflt Default page-off time.
	 * @return An array of page-off time Intervals, one value per page. */
	public Interval[] pageOffIntervals(Interval dflt) {
		PageTimeCounter ptc = new PageTimeCounter();
		parse(ptc);
		return ptc.pageOffIntervals(dflt);
	}

	/** Get MULTI string for specified page */
	public String getPage(int p) {
		String[] pages = getPages();
		if (p >= 0 && p < pages.length)
			return pages[p];
		else
			return "";
	}

	/** Get message pages as an array of strings */
	private String[] getPages() {
		return multi.split("\\[np\\]");
	}

	/** Get message lines as an array of strings (with tags).
	 * Every n_lines elements in the returned array represent one page.
	 * @param n_lines Number of lines per page.
	 * @return A string array containing text for each line. */
	public String[] getLines(int n_lines) {
		String[] pages = getPages();
		int n_total = n_lines * pages.length;
		String[] lines = new String[n_total];
		for (int i = 0; i < lines.length; i++)
			lines[i] = "";
		for (int i = 0; i < pages.length; i++) {
			String page = removePrefix(pages[i]);
			int p = i * n_lines;
			String[] lns = page.split("\\[nl.?\\]");
			for (int ln = 0; ln < lns.length; ln++) {
				int j = p + ln;
				if (j < n_total) {
					MultiString ms = new MultiString(
						lns[ln]);
					lines[j] = ms.normalizeLine().toString();
				} else {
					// MULTI string defines more than
					// n_lines on this page.  We'll just
					// have to ignore this span.
				}
			}
		}
		return lines;
	}

	/** Remove all prefix before the last [tr...] tag */
	static private String removePrefix(String page) {
		String[] parts = page.split("\\[tr.*?\\]", -1);
		int len = parts.length;
		return (len > 0) ? parts[len - 1] : "";
	}

	/** Get a MULTI string as text only (tags stripped) */
	public String asText() {
		final StringBuilder sb = new StringBuilder();
		parse(new MultiAdapter() {
			@Override public void addSpan(String span) {
				if (sb.length() > 0)
					sb.append(' ');
				sb.append(filterSpan(span.trim()));
			}
		});
		return sb.toString().trim();
	}

	/** Get the words in the message as a list.
	 * @return A list containing each trimmed word in the message. */
	public List<String> getWords() {
		String[] words = asText().split(" ");
		for (int i = 0; i < words.length; ++i)
			words[i] = words[i].trim();
		return Arrays.asList(words);
	}

	/** Check if valid shared first (for combining) */
	private boolean isValidSharedFirst() {
		if (!endsWithTextRect())
			return false;
		final boolean[] valid = new boolean[] { true };
		parse(new MultiAdapter() {
			@Override
			public void addPage() {
				valid[0] = false;
			}
		});
		return valid[0];
	}

	/** Check if a message ends with a text rectangle */
	private boolean endsWithTextRect() {
		return ENDS_WITH_TR.matcher(multi).matches();
	}

	/** Check if valid shared second (for combining) */
	private boolean isValidSharedSecond() {
		final boolean[] valid = new boolean[] { true };
		parse(new MultiAdapter() {
			@Override
			public void setTextRectangle(int x, int y, int w,int h){
				valid[0] = false;
			}
		});
		return valid[0];
	}
}
