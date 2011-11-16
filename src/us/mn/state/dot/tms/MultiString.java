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

import java.util.Arrays;
import java.util.ArrayList;
import java.util.LinkedList;
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
public class MultiString implements Multi {

	/** Regular expression to locate tags */
	static protected final Pattern TAG = Pattern.compile(
		"\\[([A-Za-z,0-9]*)\\]");

	/** Regular expression to match supported MULTI tags */
	static protected final Pattern TAGS = Pattern.compile(
		"(nl|np|pt|jl|jp|fo|g|pb|cf|cr|tr|tt|vsa|feed)(.*)");

	/** Regular expression to match invalid line-oriented MULTI tags */
	static protected final Pattern TAG_LINE = Pattern.compile(
		"\\[(nl|np|pt|jp|g|pb|cr|tr|feed)(.*)\\]");

	/** Regular expression to match text between MULTI tags */
	static protected final Pattern TEXT_PATTERN = Pattern.compile(
		"[' !#$%&()*+,-./0-9:;<=>?@A-Za-z^_`]*");

	/** New page MULTI tag */
	static public final String NEWPAGE = "[np]";

	/** Parse page times from a [pt.o.] tag.
	 * @param v Page time tag value.
	 * @param cb Callback to set page times. */
	static protected void parsePageTimes(String v, Multi cb) {
		String[] args = v.split("o", 2);
		Integer pt_on = parseInt(args, 0);
		Integer pt_off = parseInt(args, 1);
		cb.setPageTimes(pt_on, pt_off);
	}

	/** Parse a line justification tag.
	 * @param v Line justification tag value.
	 * @param cb Callback to set line justification. */
	static protected void parseJustificationLine(String v, Multi cb) {
		JustificationLine jl = JustificationLine.UNDEFINED;
		Integer j = parseInt(v);
		if(j != null)
			jl = JustificationLine.fromOrdinal(j);
		cb.setJustificationLine(jl);
	}

	/** Parse a page justification tag.
	 * @param v Page justification tag value.
	 * @param cb Callback to set page justification. */
	static protected void parseJustificationPage(String v, Multi cb) {
		JustificationPage jp = JustificationPage.UNDEFINED;
		Integer j = parseInt(v);
		if(j != null)
			jp = JustificationPage.fromOrdinal(j);
		cb.setJustificationPage(jp);
	}

	/** Parse a page background color tag */
	static protected void parsePageBackground(String v, Multi cb) {
		String[] args = v.split(",", 3);
		Integer r = parseInt(args, 0);
		Integer g = parseInt(args, 1);
		Integer b = parseInt(args, 2);
		if(r != null && g != null && b != null)
			cb.setPageBackground(r, g, b);
	}

	/** Parse a color foreground tag */
	static protected void parseColorForeground(String v, Multi cb) {
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
	static protected void parseFont(String f, Multi cb) {
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
	static protected void parseGraphic(String g, Multi cb) {
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
	static protected void parseTextRectangle(String v, Multi cb) {
		String[] args = v.split(",", 4);
		Integer x = parseInt(args, 0);
		Integer y = parseInt(args, 1);
		Integer w = parseInt(args, 2);
		Integer h = parseInt(args, 3);
		if(x != null && y != null && w != null && h != null)
			cb.setTextRectangle(x, y, w, h);
	}

	/** Parse an integer value */
	static protected Integer parseInt(String[] args, int n) {
		if(n < args.length)
			return parseInt(args[n]);
		else
			return null;
	}

	/** Parse an integer value */
	static protected Integer parseInt(String param) {
		try {
			return Integer.parseInt(param);
		}
		catch(NumberFormatException e) {
			return null;
		}
	}

	/** MULTI string buffer */
	protected final StringBuilder multi = new StringBuilder();

        /** Test if the MULTI string is equal to another MULTI string. */
        public boolean equals(Object o) {
		if(this == o)
			return true;
		if(o != null) {
			String ms = normalize(toString());
			String oms = normalize(o.toString());
			return ms.equals(oms);
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
	 * @param m MULTI string, may not be null.
	 * @throws NullPointerException if m is null. */
	public MultiString(String m) {
		if(m == null)
			throw new NullPointerException();
		multi.append(m);
	}

	/** Validate message text */
	public boolean isValid() {
		for(String t: TAG.split(multi.toString())) {
			Matcher m = TEXT_PATTERN.matcher(t);
			if(!m.matches())
				return false;
		}
		return true;
	}

	/** Add a span of text */
	public void addSpan(String s) {
		if(s.length() > 0)
			multi.append(s);
	}

	/** Add a new line */
	public void addLine(Integer spacing) {
		multi.append("[nl");
		if(spacing != null)
			multi.append(spacing);
		multi.append("]");
	}

	/** Add a new page */
	public void addPage() {
		multi.append(NEWPAGE);
	}

	/** Set page times.
	 * @param pt_on Page on-time (tenths of second; null for default).
	 * @param pt_off Page off-time (tenths of second; null for default). */
	public void setPageTimes(Integer pt_on, Integer pt_off) {
		multi.append("[pt");
		if(pt_on != null)
			multi.append(pt_on);
		multi.append('o');
		if(pt_off != null)
			multi.append(pt_off);
		multi.append("]");
	}

	/** Set the page justification */
	public void setJustificationPage(JustificationPage jp) {
		if(jp != JustificationPage.UNDEFINED) {
			multi.append("[jp");
			multi.append(jp.ordinal());
			multi.append("]");
		}
	}

	/** Set the line justification */
	public void setJustificationLine(JustificationLine jl) {
		if(jl != JustificationLine.UNDEFINED) {
			multi.append("[jl");
			multi.append(jl.ordinal());
			multi.append("]");
		}
	}

	/** Add a graphic */
	public void addGraphic(int g_num, Integer x, Integer y, String g_id) {
		multi.append("[g");
		multi.append(g_num);
		if(x != null && y != null) {
			multi.append(',');
			multi.append(x);
			multi.append(',');
			multi.append(y);
			if(g_id != null) {
				multi.append(',');
				multi.append(g_id);
			}
		}
		multi.append("]");
	}

	/** Set a new font number */
	public void setFont(int f_num, String f_id) {
		multi.append("[fo");
		multi.append(f_num);
		if(f_id != null) {
			multi.append(',');
			multi.append(f_id);
		}
		multi.append("]");
	}

	/** Set the page background color */
	public void setPageBackground(int red, int green, int blue) {
		multi.append("[pb");
		multi.append(red);
		multi.append(',');
		multi.append(green);
		multi.append(',');
		multi.append(blue);
		multi.append("]");
	}

	/** Set the color foreground */
	public void setColorForeground(int red, int green, int blue) {
		multi.append("[cf");
		multi.append(red);
		multi.append(',');
		multi.append(green);
		multi.append(',');
		multi.append(blue);
		multi.append("]");
	}

	/** Add a color rectangle */
	public void addColorRectangle(int x, int y, int w, int h, int r, int g,
		int b)
	{
		multi.append("[cr");
		multi.append(x);
		multi.append(',');
		multi.append(y);
		multi.append(',');
		multi.append(w);
		multi.append(',');
		multi.append(h);
		multi.append(',');
		multi.append(r);
		multi.append(',');
		multi.append(g);
		multi.append(',');
		multi.append(b);
		multi.append("]");
	}

	/** Set the text rectangle */
	public void setTextRectangle(int x, int y, int w, int h) {
		multi.append("[tr");
		multi.append(x);
		multi.append(',');
		multi.append(y);
		multi.append(',');
		multi.append(w);
		multi.append(',');
		multi.append(h);
		multi.append("]");
	}

	/** Add a travel time destination */
	public void addTravelTime(String sid) {
		multi.append("[tt");
		multi.append(sid);
		multi.append("]");
	}

	/** Add a speed advisory */
	public void addSpeedAdvisory() {
		multi.append("[vsa]");
	}

	/** Add a feed message */
	public void addFeed(String fid) {
		multi.append("[feed");
		multi.append(fid);
		multi.append("]");
	}

	/** Get the value of the MULTI string */
	public String toString() {
		return multi.toString();
	}

	/** Clear the MULTI string */
	public void clear() {
		multi.setLength(0);
	}

	/** Parse the MULTI string.
	 * @param cb A callback which keeps track of the MULTI state. */
	public void parse(Multi cb) {
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
	protected void parseTag(String tag, Multi cb) {
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

	/** Is the MULTI string blank? */
	public boolean isBlank() {
		final StringBuilder _b = new StringBuilder();
		parse(new MultiAdapter() {
			public void addSpan(String span) {
				_b.append(span);
			}
			public void addGraphic(int g_num, Integer x, Integer y,
				String g_id)
			{
				_b.append("GRAPHIC");
			}
		});
		return _b.toString().trim().equals("");
	}

	/** Return a value indicating if the message is single or multi-page.
	 *  @return True if the message contains a single page else false 
	 *  for multi-page. */
	public boolean singlePage() {
		return getNumPages() <= 1;
	}

	/** Get the number of pages in the multistring */
	public int getNumPages() {
		MultiAdapter msa = new MultiAdapter();
		parse(msa);
		return msa.ms_page + 1;
	}

	/** Return the MULTI string as a normalized valid MULTI string.
	 * @return A normalized MULTI string with lowercase spans converted
	 *         to uppercase, invalid character removed, invalid tags
	 *         removed, etc. */
	static public String normalize(String multi) {
		MultiString ms = new MultiString() {
			public void addSpan(String s) {
				Matcher m = TEXT_PATTERN.matcher(s);
				while(m.find())
					super.addSpan(m.group());
			}
		};
		new MultiString(multi).parse(ms);
		return ms.toString();
	}

	/** Return the canonical version of a MULTI string.
	 * @return A canonical MULTI string with all default tag values
	 *         included and redundant tags removed. */
	static public String canonical(String multi) {
		/* FIXME: include default tag values */
		return normalize(multi);
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
		MultiAdapter msa = new MultiAdapter() {
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

	/** Get the page on-time for the 1st page. If no page on-time is 
	 *  specified in the MULTI string, the default is returned, which
	 *  is a function of the number of pages in the multi-string.
	 *  @return An integer, which is in tenths of secs. */
	public DmsPgTime getPageOnTime() {
		DmsPgTime def = DmsPgTime.getDefaultOn(singlePage());
		int[] pont = getPageOnTimes(def.toTenths());
		if(pont.length < 1)
			return def;
		// return 1st page on-time read, even if specified per page
		return new DmsPgTime(pont[0]);
	}

	/** Get an array of page on times. The page on time is assumed
	 *  to apply to subsequent pages if not specified on each page.
	 *  @param def_pont Default page on time, in tenths of a sec.
	 *  @return An integer array with length equal to the number 
	 *	    of pages in the message, containing tenths of secs. */
	public int[] getPageOnTimes(final int def_pont) {
		int np = getNumPages();
		final int[] ret = new int[np]; // pg time indexed by pg
		for(int i = 0; i < ret.length; ++i)
			ret[i] = def_pont;
		parse(new MultiAdapter() {
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

	/** Replace all the page on times in a MULTI string with the specified
	 * value.  If no on-time is specified, then a page time tag is
	 * prepended.
	 * @param multi MULTI string.
	 * @param pt_on Page on-time in tenths of a second.
	 * @return The updated MULTI string. */
	public MultiString replacePageOnTime(final int pt_on) {
		return new MultiString(replacePageOnTime(toString(), pt_on));
	}

	/** Replace all the page on times in a MULTI string with the specified
	 * value.  If no on-time is specified, then a page time tag is
	 * prepended.
	 * @param multi MULTI string.
	 * @param pt_on Page on-time in tenths of a second.
	 * @return The updated MULTI string. */
	static public String replacePageOnTime(String multi, final int pt_on) {
		if(multi.indexOf("[pt") < 0) {
			MultiString ms = new MultiString();
			ms.setPageTimes(pt_on, null);
			return ms.toString() + multi;
		}
		MultiString ms = new MultiString() {
			public void setPageTimes(Integer on, Integer off) {
				super.setPageTimes(pt_on, off);
			}
		};
		new MultiString(multi).parse(ms);
		return ms.toString();
	}

	/** Get message lines text as an array of strings. See the test
	 *  cases for further information.
	 * @return A string array containing text spans for each line. */
	public String[] getText() {
		final LinkedList<String> ls = new LinkedList<String>();
		parse(new MultiAdapter() {
			private int n_lines = 0;
			public void addSpan(String span) {
				// note: fields in span use ms prefix
				n_lines = Math.max(n_lines, ms_line + 1);
				while(ls.size() < (ms_page + 1) * n_lines)
					ls.add("");
				int i = ms_page * n_lines + ms_line;
				String v = ls.get(i);
				ls.set(i, SString.trimJoin(v, span));
			}
		});
		return ls.toArray(new String[0]);
	}

	/** Get message lines text as an array of strings. See the test
	 *  cases for further information.
	 * @return A string array containing text spans for each line. */
	public String[] getLines() {
		String[] pages = multi.toString().split("\\[np\\]");
		int n_lines = 0;
		for(String pg: pages) {
			String[] lns = pg.split("\\[nl.?\\]");
			n_lines = Math.max(n_lines, lns.length);
		}
		String[] lines = new String[n_lines * pages.length];
		for(int i = 0; i < lines.length; i++)
			lines[i] = "";
		for(int i = 0; i < pages.length; i++) {
			String[] lns = pages[i].split("\\[nl.?\\]");
			for(int ln = 0; ln < lns.length; ln++)
				lines[i*n_lines + ln] = normalizeLine(lns[ln]);
		}
		return lines;
	}

	/** Normalize a single line MULTI string */
	static public String normalizeLine(String multi) {
		StringBuilder sb = new StringBuilder();
		for(String txt: TAG_LINE.split(normalize(multi)))
			sb.append(txt);
		return sb.toString();
	}

	/** Get a MULTI string as text only (tags stripped) */
	public String asText() {
		final StringBuilder sb = new StringBuilder();
		parse(new MultiAdapter() {
			public void addSpan(String span) {
				if(sb.length() > 0)
					sb.append(' ');
				sb.append(span);
			}
		});
		return sb.toString();
	}
}
