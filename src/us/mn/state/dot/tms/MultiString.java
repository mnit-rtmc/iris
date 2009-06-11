/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2009  Minnesota Department of Transportation
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
import java.util.ArrayList;
import us.mn.state.dot.tms.utils.SString;

/**
 * NTCIP -- MULTI (MarkUp Language for Transportation Information)
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class MultiString {

	/* FIXME: add support for page time [ptxoy] tag */

	/** Regular expression to match supported MULTI tags */
	static protected final Pattern TAG = Pattern.compile(
		"\\[(nl|np|jl|jp|fo|tt|g)([A-Za-z,0-9]*)\\]");

	/** Regular expression to match text between MULTI tags */
	static protected final Pattern TEXT_PATTERN = Pattern.compile(
		"[ !#$%&()*+,-./0-9:;<=>?@A-Z]*");

	/** Regular expression to match travel time tag */
	static protected final Pattern TRAVEL_TAG = Pattern.compile(
		"(.*?)\\[tt([A-Za-z0-9]+)\\]");

	/** New line MULTI tag */
	static public final String NEWLINE = "[nl]";

	/** New page MULTI tag */
	static public final String NEWPAGE = "[np]";

	/** Page Justification enumeration. See NTCIP 1203 as necessary. */
	public enum JustificationPage {
		UNDEFINED, OTHER, TOP, MIDDLE, BOTTOM;

		static public JustificationPage fromOrdinal(int v) {
			for(JustificationPage pj: JustificationPage.values()) {
				if(pj.ordinal() == v)
					return pj;
			}
			return UNDEFINED;
		}

		static protected JustificationPage parse(String v) {
			try {
				int j = Integer.parseInt(v);
				return fromOrdinal(j);
			}
			catch(NumberFormatException e) {
				return UNDEFINED;
			}
		}
	}

	/** Line Justification enumeration */
	public enum JustificationLine {
		UNDEFINED, OTHER, LEFT, CENTER, RIGHT, FULL;

		static public JustificationLine fromOrdinal(int v) {
			for(JustificationLine lj: JustificationLine.values()) {
				if(lj.ordinal() == v)
					return lj;
			}
			return UNDEFINED;
		}

		static protected JustificationLine parse(String v) {
			try {
				int j = Integer.parseInt(v);
				return fromOrdinal(j);
			}
			catch(NumberFormatException e) {
				return UNDEFINED;
			}
		}
	}

	/** Parse a font number from an [fox] or [fox,cccc] tag.
	 * @param f Font tag value (x or x,cccc from [fox] or [fox,cccc] tag).
	 * @return Font number contained in the tag. */
	static protected int parseFont(String f) {
		String[] args = f.split(",", 2);
		try {
			return Integer.parseInt(args[0]);
		}
		catch(NumberFormatException e) {
			return 1;
		}
	}

	/** Parse a graphic number from a [gn] or [gn,x,y] or [gn,x,y,cccc] tag.
	 * @param g Graphic tag value (n or n,x,y or n,x,y,cccc from tag).
	 * @return Graphic number contained in the tag. */
	static protected int parseGraphic(String g) {
		String[] args = g.split(",", 3);
		try {
			return Integer.parseInt(args[0]);
		}
		catch(NumberFormatException e) {
			return 1;
		}
	}

	/** MULTI string buffer */
	protected final StringBuilder b = new StringBuilder();

	/** Flag for trailing message text */
	protected boolean trailing = false;

	/** Test if the MULTI string is equal to another MULTI string */
	public boolean equals(Object o) {
		if(o instanceof MultiString)
			return toString().equals(o.toString());
		if(o instanceof String)
			return toString().equals(o.toString());
		return false;
	}

	/** Calculate a hash code for the MULTI string */
	public int hashCode() {
		return toString().hashCode();
	}

	/** Create an empty MULTI string */
	public MultiString() {
	}

	/** Create a new MULTI string */
	public MultiString(String t) {
		addText(t);
	}

	/** Validate message text */
	public boolean isValid() {
		for(String t: TAG.split(b.toString())) {
			Matcher m = TEXT_PATTERN.matcher(t);
			if(!m.matches())
				return false;
		}
		return true;
	}

	/** Add text to the current line */
	public void addText(String s) {
		if(s.length() > 0) {
			b.append(s);
			trailing = true;
		}
	}

	/** Add a new line */
	public void addLine() {
		if(trailing ||
		   SystemAttrEnum.DMS_MESSAGE_BLANK_LINE_ENABLE.getBoolean())
		{
			b.append(NEWLINE);
			trailing = false;
		}
	}

	/** Add a new page */
	public void addPage() {
		b.append(NEWPAGE);
		trailing = false;
	}

	/** Add a graphic */
	public void addGraphic(int g_num) {
		b.append("[g");
		b.append(g_num);
		b.append("]");
	}

	/** Add a graphic at the specified position */
	public void addGraphic(int g_num, int x, int y) {
		b.append("[g");
		b.append(g_num);
		b.append(',');
		b.append(x);
		b.append(',');
		b.append(y);
		b.append("]");
	}

	/** Set a new font number */
	public void setFont(int f_num) {
		b.append("[fo");
		b.append(f_num);
		b.append("]");
	}

	/** Get an array of font numbers.
	 * @param f_num Default font number, one based.
	 * @return An array of font numbers for each page of the message. */
	public int[] getFonts(int f_num) {
		if(f_num < 1 || f_num > 255)
			return new int[0];
		final ArrayList<Integer> al = new ArrayList<Integer>(3); 
		parse(new SpanCallback() {
			public void addSpan(int p, JustificationPage jp,
				int l, JustificationLine jl, int f, String t)
			{
				al.add(new Integer(f));
			}
		}, f_num);
		int np = getNumPages();
		if(np > al.size()) {
			assert false;
			return new int[0];
		}
		int[] ret = new int[np];
		for(int i = 0; i < np; ++i) {
			if(i >= al.size())
				ret[i] = f_num;
			else
				ret[i] = (int)al.get(i).intValue();
		}
		return ret;
	}

	/** Get the value of the MULTI string */
	public String toString() {
		return b.toString();
	}

	/** MULTI string parsing callback interface */
	public interface SpanCallback {
		void addSpan(int page, JustificationPage justp, 
			int line, JustificationLine justl,
			int f_num, String span);
	}

	/** Parse the MULTI string 
	 * @param cb SpanCallback, called per span.
	 * @param f_num Default font number */
	public void parse(SpanCallback cb, int f_num) {
		int page = 0;
		JustificationPage justp = JustificationPage.fromOrdinal(
			SystemAttrEnum.DMS_DEFAULT_JUSTIFICATION_PAGE.getInt());
		int line = 0;
		JustificationLine justl = JustificationLine.fromOrdinal(
			SystemAttrEnum.DMS_DEFAULT_JUSTIFICATION_LINE.getInt());
		Matcher m = TAG.matcher(b);
		for(String span: TAG.split(b)) {
			if(span.length() > 0)
				cb.addSpan(page, justp, line, justl,f_num,span);
			if(m.find()) {
				String tag = m.group(1);
				if(tag.equals("nl"))
					line++;
				else if(tag.equals("np")) {
					line = 0;
					page++;
				} else if(tag.equals("jl")) {
					String v = m.group(2);
					justl = JustificationLine.parse(v);
				} else if(tag.equals("jp")) {
					String v = m.group(2);
					justp = JustificationPage.parse(v);
				} else if(tag.equals("fo")) {
					String v = m.group(2);
					f_num = parseFont(v);
				} else if(tag.equals("g")) {
					String v = m.group(2);
					int g_num = parseGraphic(v);
					// FIXME: add a graphic to the callback
				}
			}
		}
	}

	/** Is the MULTI string blank? */
	public boolean isBlank() {
		final StringBuilder _b = new StringBuilder();
		parse(new SpanCallback() {
			public void addSpan(int p, JustificationPage jp,
				int l, JustificationLine jl, int f, String t)
			{
				_b.append(t);
			}
		}, 1);
		return _b.toString().trim().equals("");
	}

	/** Parsing callback to count the number of pages */
	protected class PageCounter implements SpanCallback {
		int num_pages = 0;
		public void addSpan(int p, JustificationPage jp, int l,
			JustificationLine jl, int f, String t)
		{
			num_pages = Math.max(p + 1, num_pages);
		}
	}

	/** Get the number of pages in the multistring */
	public int getNumPages() {
		PageCounter pc = new PageCounter();
		parse(pc, 1);
		return pc.num_pages;
	}

	/** Parsing callback to extract message text for each line */
	protected class LineExtractor implements SpanCallback {
		int num_pages = 0;
		public void addSpan(int p, JustificationPage jp, int l,
			JustificationLine jl, int f, String t)
		{
			num_pages = Math.max(p + 1, num_pages);
		}
	}

	/** Travel time calculating callback interface */
	public interface TravelCallback {

		/** Calculate the travel time to a destination */
		String calculateTime(String sid) throws InvalidMessageException;

		/** Check if the callback changed state */
		boolean isChanged();
	}

	/** Replace travel time tags with current travel time data */
	public String replaceTravelTimes(TravelCallback cb)
		throws InvalidMessageException
	{
		int end = 0;
		StringBuilder _b = new StringBuilder();
		Matcher m = TRAVEL_TAG.matcher(b);
		while(m.find()) {
			_b.append(m.group(1));
			_b.append(cb.calculateTime(m.group(2)));
			end = m.end();
		}
		_b.append(b.substring(end));
		return _b.toString();
	}

	/** Return the MULTI string as a normalized valid MULTI string.
	 *  @return A normalized MULTI string with lowercase spans converted
	 *	    to uppercase, invalid character removed, invalid tags
	 *	    removed, etc. */
	public String normalize() {
		final StringBuilder _b = new StringBuilder();
		parseNormalize(new NormalizeCallback() {
			public void addSpan(String s) {
				s = (s == null ? "" : s.toUpperCase());
				Matcher m = TEXT_PATTERN.matcher(s);
				while(m.find()) {
					_b.append(m.group());
				}
			}
			public void addTag(String tag) {
				_b.append(tag);
			}
		});
		return _b.toString();
	}

	/** MULTI string parsing callback interface */
	public interface NormalizeCallback {
		void addSpan(String span);
		void addTag(String tag);
	}

	/** Parse the MULTI string. The addSpan and addTag methods
	 *  are called for each span and tag that are found.
	 * @param cb Normalization callback. */
	public void parseNormalize(NormalizeCallback cb) {
		Matcher m = TAG.matcher(b);
		for(String span: TAG.split(b)) {
			if(span.length() > 0)
				cb.addSpan(span);
			if(m.find()) {
				String tag = m.group();
				cb.addTag(tag);
			}
		}
	}

	/** 
	 * This is a hack. It is used by the ComboBoxEditor and 
	 * SignMessageModel to recognize when a sign message line
	 * should be ignored. By convention, a line begining and
	 * ending with an underscore is to be ignored. IRIS assumes
	 * that non-blank DMS messages have both a bitmap and multistring,
	 * which is not the case for D10, so a bogus multistring is created
	 * in comm/dmslite (with a prepended and appended underscore). 
	 */
	static public boolean ignoreLineHack(String line) {
		if(line == null)
			return false;
		return SString.enclosedBy(line, "_");
	}

	// see the above note
	static public String flagIgnoredSignLineHack(String line) {
		return "_" + line + "_";
	}
}
