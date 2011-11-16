/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2011  Minnesota Department of Transportation
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A parser for MULTI (MarkUp Language for Transportation Information), as
 * specified in NTCIP 1203.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class MultiParser {

	/** Regular expression to locate tags */
	static private final Pattern TAG = Pattern.compile(
		"\\[([A-Za-z,0-9]*)\\]");

	/** Regular expression to match supported MULTI tags */
	static private final Pattern TAGS = Pattern.compile(
		"(nl|np|pt|jl|jp|fo|g|pb|cf|cr|tr|tt|vsa|feed)(.*)");

	/** Regular expression to match invalid line-oriented MULTI tags */
	static private final Pattern TAG_LINE = Pattern.compile(
		"\\[(nl|np|pt|jp|g|pb|cr|tr|feed)(.*)\\]");

	/** Regular expression to match text between MULTI tags */
	static private final Pattern TEXT_PATTERN = Pattern.compile(
		"[' !#$%&()*+,-./0-9:;<=>?@A-Za-z^_`]*");

	/** Don't allow instantiation */
	private MultiParser() { }

	/** Parse a MULTI string.
	 * @param multi The string to parse.
	 * @param cb A callback which keeps track of the MULTI state. */
	static public void parse(String multi, Multi cb) {
		int offset = 0;
		Matcher m = TAG.matcher(multi);
		while(m.find()) {
			if(m.start() > offset)
				cb.addSpan(multi.substring(offset, m.start()));
			offset = m.end();
			// m.group(1) strips off tag brackets
			parseTag(m.group(1), cb);
		}
		if(offset < multi.length())
			cb.addSpan(multi.substring(offset));
	}

	/** Parse one MULTI tag */
	static private void parseTag(String tag, Multi cb) {
		Matcher mtag = TAGS.matcher(tag);
		if(mtag.find()) {
			String tid = mtag.group(1).toLowerCase();
			String tparam = mtag.group(2);
			if(tid.equals("nl"))
				cb.addLine(parseInt(tparam));
			else if(tid.equals("np"))
				cb.addPage();
			else if(tid.equals("pt"))
				parsePageTimes(tparam, cb);
			else if(tid.equals("jl"))
				parseJustificationLine(tparam, cb);
			else if(tid.equals("jp"))
				parseJustificationPage(tparam, cb);
			else if(tid.equals("pb"))
				parsePageBackground(tparam, cb);
			else if(tid.equals("cf"))
				parseColorForeground(tparam, cb);
			else if(tid.equals("fo"))
				parseFont(tparam, cb);
			else if(tid.equals("g"))
				parseGraphic(tparam, cb);
			else if(tid.equals("cr"))
				parseColorRectangle(tparam, cb);
			else if(tid.equals("tr"))
				parseTextRectangle(tparam, cb);
			else if(tid.equals("tt"))
				cb.addTravelTime(tparam);
			else if(tid.equals("vsa"))
				cb.addSpeedAdvisory();
			else if(tid.equals("feed"))
				cb.addFeed(tparam);
		}
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

	/** Parse a line justification tag.
	 * @param v Line justification tag value.
	 * @param cb Callback to set line justification. */
	static private void parseJustificationLine(String v, Multi cb) {
		Multi.JustificationLine jl = Multi.JustificationLine.UNDEFINED;
		Integer j = parseInt(v);
		if(j != null)
			jl = Multi.JustificationLine.fromOrdinal(j);
		cb.setJustificationLine(jl);
	}

	/** Parse a page justification tag.
	 * @param v Page justification tag value.
	 * @param cb Callback to set page justification. */
	static private void parseJustificationPage(String v, Multi cb) {
		Multi.JustificationPage jp = Multi.JustificationPage.UNDEFINED;
		Integer j = parseInt(v);
		if(j != null)
			jp = Multi.JustificationPage.fromOrdinal(j);
		cb.setJustificationPage(jp);
	}

	/** Parse a page background color tag */
	static private void parsePageBackground(String v, Multi cb) {
		String[] args = v.split(",", 3);
		Integer r = parseInt(args, 0);
		Integer g = parseInt(args, 1);
		Integer b = parseInt(args, 2);
		if(r != null && g != null && b != null)
			cb.setPageBackground(r, g, b);
	}

	/** Parse a color foreground tag */
	static private void parseColorForeground(String v, Multi cb) {
		String[] args = v.split(",", 3);
		Integer r = parseInt(args, 0);
		Integer g = parseInt(args, 1);
		Integer b = parseInt(args, 2);
		if(r != null && g != null && b != null)
			cb.setColorForeground(r, g, b);
	}

	/** Parse a font number from an [fox] or [fox,cccc] tag.
	 * @param f Font tag value (x or x,cccc from [fox] or [fox,cccc] tag).
	 * @param cb Callback to set font information. */
	static private void parseFont(String f, Multi cb) {
		String[] args = f.split(",", 2);
		Integer f_num = parseInt(args, 0);
		String f_id = null;
		if(args.length > 1)
			f_id = args[1];
		if(f_num != null)
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
		String g_id = null;
		if(args.length > 3)
			g_id = args[3];
		if(g_num != null)
			cb.addGraphic(g_num, x, y, g_id);
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
		if(x != null && y != null && w != null && h != null &&
		   r != null && g != null && b != null)
			cb.addColorRectangle(x, y, w, h, r, g, b);
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
		if(x != null && y != null && w != null && h != null)
			cb.setTextRectangle(x, y, w, h);
	}

	/** Parse an integer value */
	static private Integer parseInt(String[] args, int n) {
		if(n < args.length)
			return parseInt(args[n]);
		else
			return null;
	}

	/** Parse an integer value */
	static private Integer parseInt(String param) {
		try {
			return Integer.parseInt(param);
		}
		catch(NumberFormatException e) {
			return null;
		}
	}

	/** Validate a MULTI string */
	static public boolean isValid(String multi) {
		for(String t: TAG.split(multi)) {
			Matcher m = TEXT_PATTERN.matcher(t);
			if(!m.matches())
				return false;
		}
		return true;
	}

	/** Return the MULTI string as a normalized valid MULTI string.
	 * @return A normalized MULTI string with invalid characters and
	 *         invalid tags removed, etc. */
	static public String normalize(String multi) {
		MultiString ms = new MultiString() {
			public void addSpan(String s) {
				Matcher m = TEXT_PATTERN.matcher(s);
				while(m.find())
					super.addSpan(m.group());
			}
		};
		parse(multi, ms);
		return ms.toString();
	}

	/** Normalize a single line MULTI string */
	static public String normalizeLine(String multi) {
		StringBuilder sb = new StringBuilder();
		for(String txt: TAG_LINE.split(normalize(multi)))
			sb.append(txt);
		return sb.toString();
	}
}
