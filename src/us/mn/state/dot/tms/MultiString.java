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
import us.mn.state.dot.tms.utils.SString;

/**
 * NTCIP -- MULTI (MarkUp Language for Transportation Information),
 * as specified in NTCIP 1203.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class MultiString {

	/** Regular expression to match supported MULTI tags.
	 *  @see MultiStringTest */
	static protected final Pattern TAG = Pattern.compile(
		"\\[(nl|np|jl|jp|fo|g|cf|pt|tr|tt)([A-Za-z,0-9]*)\\]");

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

	/** Parse a color foreground tag */
	static protected void parseColorForeground(String v,
		MultiStringState mss)
	{
		String[] args = v.split(",", 3);
		Integer r = parseInt(args, 0);
		Integer g = parseInt(args, 1);
		Integer b = parseInt(args, 2);
		if(r != null && g != null && b != null)
			mss.setColorForeground(r, g, b);
	}

	/** Parse a font number from an [fox] or [fox,cccc] tag.
	 * @param f Font tag value (x or x,cccc from [fox] or [fox,cccc] tag).
	 * @param mss Callback to set font information. */
	static protected void parseFont(String f, MultiStringState mss) {
		String[] args = f.split(",", 2);
		Integer f_num = parseInt(args, 0);
		Integer f_id = parseInt(args, 1);
		if(f_num != null)
			mss.setFont(f_num, f_id);
	}

	/** Parse a graphic number from a [gn] or [gn,x,y] or [gn,x,y,cccc] tag.
	 * @param g Graphic tag value (n or n,x,y or n,x,y,cccc from tag).
	 * @param mss Callback to set graphic information */
	static protected void parseGraphic(String g, MultiStringState mss) {
		String[] args = g.split(",", 4);
		Integer g_num = parseInt(args, 0);
		Integer x = parseInt(args, 1);
		Integer y = parseInt(args, 2);
		Integer g_id = parseInt(args, 3);
		if(g_num != null)
			mss.addGraphic(g_num, x, y, g_id);
	}

	/** Parse page times form a [pt.o.] tag.
	 * @param v Page time tag value.
	 * @param mss Callback to set page times. */
	static protected void parsePageTimes(String v, MultiStringState mss) {
		String[] args = v.split("o", 2);
		Integer pt_on = parseInt(args, 0);
		Integer pt_off = parseInt(args, 1);
		mss.setPageTimes(pt_on, pt_off);
	}

	/** Parse an integer value */
	static protected Integer parseInt(String[] args, int n) {
		try {
			if(n < args.length)
				return Integer.parseInt(args[n]);
			else
				return null;
		}
		catch(NumberFormatException e) {
			return null;
		}
	}

	/** MULTI string buffer */
	protected final StringBuilder b = new StringBuilder();

	/** Flag for trailing message text */
	protected boolean trailing = false;

	/** Test if the MULTI string is equal to another MULTI string */
	public boolean equals(Object o) {
		if(o instanceof MultiString) {
			MultiString ms = (MultiString)o;
			return normalize().equals(ms.normalize());
		}
		if(o instanceof String) {
			MultiString ms = new MultiString((String)o);
			return normalize().equals(ms.normalize());
		}
		return false;
	}

	/** Calculate a hash code for the MULTI string */
	public int hashCode() {
		return toString().hashCode();
	}

	/** Create an empty MULTI string */
	public MultiString() {
	}

	/** Create a new MULTI string.
	 * @param m MULTI string.
	 * @throws NullPointerException if m is null. */
	public MultiString(String m) {
		b.append(m);
		if(b.length() > 0)
			trailing = true;
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

	/** Set page times.
	 * @param pt_on Page on-time (tenths of second; null for default).
	 * @param pt_off Page off-time (tenths of second; null for default). */
	public void setPageTimes(Integer pt_on, Integer pt_off) {
		b.append("[pt");
		if(pt_on != null)
			b.append(pt_on);
		b.append('o');
		if(pt_off != null)
			b.append(pt_off);
		b.append("]");
	}

	/** Add a spann of text */
	public void addSpan(String s) {
		if(s != null && s.length() > 0) {
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
	public void addGraphic(int g_num, Integer x, Integer y, Integer g_id) {
		b.append("[g");
		b.append(g_num);
		if(x != null && y != null) {
			b.append(',');
			b.append(x);
			b.append(',');
			b.append(y);
			if(g_id != null) {
				b.append(',');
				b.append(g_id);
			}
		}
		b.append("]");
	}

	/** Set a new font number */
	public void setFont(int f_num, Integer f_id) {
		b.append("[fo");
		b.append(f_num);
		if(f_id != null) {
			b.append(',');
			b.append(f_id);
		}
		b.append("]");
	}

	/** Set the color foreground */
	public void setColorForeground(int red, int green, int blue) {
		b.append("[cf");
		b.append(red);
		b.append(',');
		b.append(green);
		b.append(',');
		b.append(blue);
		b.append("]");
	}

	/** Get an array of font numbers.
	 * @param f_num Default font number, one based.
	 * @return An array of font numbers for each page of the message. */
	public int[] getFonts(final int f_num) {
		if(f_num < 1 || f_num > 255)
			return new int[0];
		int np = getNumPages();
		final int[] ret = new int[np]; // font numbers indexed by pg
		for(int i = 0; i < ret.length; i++)
			ret[i] = f_num;
		MultiStringStateAdapter msa = new MultiStringStateAdapter() {
			public void addSpan(String span) {
				// note: fields in span use ms prefix
				if(ms_page >= 0 && ms_page < ret.length)
					ret[ms_page] = ms_fnum;
				else
					assert false : "bogus # pages";
			}
		};
		msa.setFont(f_num, null);
		parse(msa);
		return ret;
	}

	/** Get the value of the MULTI string */
	public String toString() {
		return b.toString();
	}

	/** Parse the MULTI string.
	 * @param cb A callback which keeps track of the MULTI state. */
	public void parse(MultiStringState cb) {
		int offset = 0;
		Matcher m = TAG.matcher(b);
		while(m.find()) {
			if(m.start() > offset)
				cb.addSpan(b.substring(offset, m.start()));
			offset = m.end();
			String tag = m.group(1);
			if(tag.equals("nl"))
				cb.addLine();
			else if(tag.equals("np"))
				cb.addPage();
			else if(tag.equals("jl")) {
				String v = m.group(2);
				cb.setLineJustification(
					JustificationLine.parse(v));
			} else if(tag.equals("jp")) {
				String v = m.group(2);
				cb.setPageJustification(
					JustificationPage.parse(v));
			} else if(tag.equals("cf")) {
				String v = m.group(2);
				parseColorForeground(v, cb);
			} else if(tag.equals("fo")) {
				String v = m.group(2);
				parseFont(v, cb);
			} else if(tag.equals("g")) {
				String v = m.group(2);
				parseGraphic(v, cb);
			} else if(tag.startsWith("pt")) {
				String v = m.group(2);
				parsePageTimes(v, cb);
			} else if(tag.startsWith("tr")) {
				// FIXME: complete
			}
		}
		if(offset < b.length())
			cb.addSpan(b.substring(offset));
	}

	/** Is the MULTI string blank? */
	public boolean isBlank() {
		final StringBuilder _b = new StringBuilder();
		parse(new MultiStringStateAdapter() {
			public void addSpan(String span) {
				_b.append(span);
			}
			public void addGraphic(int g_num, Integer x, Integer y,
				Integer g_id)
			{
				_b.append("GRAPHIC");
			}
		});
		return _b.toString().trim().equals("");
	}

	/** Get the number of pages in the multistring */
	public int getNumPages() {
		MultiStringStateAdapter msa = new MultiStringStateAdapter();
		parse(msa);
		return msa.ms_page + 1;
	}

	/** Travel time calculating callback interface */
	public interface TravelCallback {

		/** Calculate the travel time to a destination */
		String calculateTime(String sid) 
			throws InvalidMessageException;

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

	/** Parse the MULTI string.
	 * @param cb Normalization callback. */
	protected void parseNormalize(NormalizeCallback cb) {
		int offset = 0;
		Matcher m = TAG.matcher(b);
		while(m.find()) {
			if(m.start() > offset)
				cb.addSpan(b.substring(offset, m.start()));
			offset = m.end();
			cb.addTag(m.group());
		}
		if(offset < b.length())
			cb.addSpan(b.substring(offset));
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

	/** Get an array of page on times. The page on time is assumed
	 *  to apply to subsequent pages if not specified on each page.
	 *  @param def_pont Default page on time, in tenths of a sec.
	 *  @return An integer array with length equal to the number 
	 *	    of pages in the message, containing tenths of secs. */
	public int[] getPageOnTime(final int def_pont) {
		int np = getNumPages();
		final int[] ret = new int[np]; // pg time indexed by pg
		for(int i = 0; i < ret.length; ++i)
			ret[i] = def_pont;
		parse(new MultiStringStateAdapter() {
			public void addSpan(String span) {
				// note: fields in span use ms prefix
				if(ms_page >= 0 && ms_page < ret.length) {
					if(ms_pt_on != null)
						ret[ms_page] = ms_pt_on;
				} else
					assert false : "bogus # pages";
			}
		});
		return ret;
	}

	/** Return the existing MULTI string with all of the on-times 
	 *  replaced with the specified value. If no on-time is specified
	 *  then a page time tag is prepended.
	 *  @param pont Page on-time in tenths of a second.
	 *  @return The updated MULTI string. */
	public String replacePageOnTime(final int pont) {
		final StringBuilder ret = new StringBuilder();
		if(b.toString().indexOf("[pt") < 0) {
			MultiString t = new MultiString();
			t.setPageTimes(pont, null);
			return t.toString() + b.toString();
		}
		parseNormalize(new NormalizeCallback() {
			public void addSpan(String s) {
				s = (s == null ? "" : s);
				Matcher m = TEXT_PATTERN.matcher(s);
				while(m.find()) {
					ret.append(m.group());
				}
			}
			// e.g. tag = "[pt25o15]"
			public void addTag(String tag) {
				if(tag.startsWith("[pt")) {
					ret.append("[pt");
					//String v = m.group(2); // e.g. 25o6
					String[] t = tag.split("o", 2);
					// page on time: replace existing
					if(t.length >= 1 && t[0].length() > 0)
						ret.append(SString.
							intToString(pont,0));
					ret.append('o');
					// page off time: use existing
					if(t.length >= 2 && t[1].length() > 0)
						ret.append(t[1]);
				} else {
					ret.append(tag);
				}
			}
		});
		return ret.toString();
	}
}
