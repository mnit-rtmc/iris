/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2013  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.units.Interval;
import us.mn.state.dot.tms.utils.SString;

/**
 * MULTI String (MarkUp Language for Transportation Information), as specified
 * in NTCIP 1203.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class MultiString implements Multi {

	/** MULTI string buffer */
	protected final StringBuilder multi = new StringBuilder();

        /** Test if the MULTI string is equal to another MULTI string */
        public boolean equals(Object o) {
		if(this == o)
			return true;
		if(o != null) {
			String ms = MultiParser.normalize(toString());
			String oms = MultiParser.normalize(o.toString());
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
		if(m != null)
			multi.append(m);
		else
			throw new NullPointerException();
	}

	/** Append another MULTI string. */
	public void append(MultiString ms) {
		multi.append(ms.multi);
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
		multi.append("[np]");
	}

	/** Set the page times.
	 * @param pt_on Page on time (deciseconds; null means default)
	 * @param pt_off Page off time (deciseconds; null means default) */
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

	/** Set the character spacing.
	 * @param sc Character spacing (null means use font spacing) */
	public void setCharSpacing(Integer sc) {
		multi.append("[");
		if(sc != null) {
			multi.append("sc");
			multi.append(sc);
		} else
			multi.append("/sc");
		multi.append("]");
	}

	/** Set the (deprecated) message background color.
	 * @param x Background color (0-9; colorClassic value). */
	public void setColorBackground(int x) {
		multi.append("[cb");
		multi.append(x);
		multi.append("]");
	}

	/** Set the page background color for monochrome1bit, monochrome8bit,
	 * and colorClassic color schemes.
	 * @param z Background color (0-1 for monochrome1bit),
	 *                           (0-255 for monochrome8bit),
	 *                           (0-9 for colorClassic). */
	public void setPageBackground(int z) {
		multi.append("[pb");
		multi.append(z);
		multi.append("]");
	}

	/** Set the page background color for color24bit color scheme.
	 * @param r Red component (0-255).
	 * @param g Green component (0-255).
	 * @param b Blue component (0-255). */
	public void setPageBackground(int red, int green, int blue) {
		multi.append("[pb");
		multi.append(red);
		multi.append(',');
		multi.append(green);
		multi.append(',');
		multi.append(blue);
		multi.append("]");
	}

	/** Set the foreground color for monochrome1bit, monochrome8bit, and
	 * colorClassic color schemes.
	 * @param x Foreground color (0-1 for monochrome1bit),
	 *                           (0-255 for monochrome8bit),
	 *                           (0-9 for colorClassic). */
	public void setColorForeground(int x) {
		multi.append("[cf");
		multi.append(x);
		multi.append("]");
	}

	/** Set the foreground color for color24bit color scheme.
	 * @param r Red component (0-255).
	 * @param g Green component (0-255).
	 * @param b Blue component (0-255). */
	public void setColorForeground(int red, int green, int blue) {
		multi.append("[cf");
		multi.append(red);
		multi.append(',');
		multi.append(green);
		multi.append(',');
		multi.append(blue);
		multi.append("]");
	}

	/** Add a color rectangle for monochrome1bit, monochrome8bit, and
	 * colorClassic color schemes.
	 * @param x X pixel position of upper left corner.
	 * @param y Y pixel position of upper left corner.
	 * @param w Width in pixels.
	 * @param h Height in pixels.
	 * @param z Color of rectangle (0-1 for monochrome1bit),
	 *                             (0-255 for monochrome8bit),
	 *                             (0-9 for colorClassic). */
	public void addColorRectangle(int x, int y, int w, int h, int z) {
		multi.append("[cr");
		multi.append(x);
		multi.append(',');
		multi.append(y);
		multi.append(',');
		multi.append(w);
		multi.append(',');
		multi.append(h);
		multi.append(',');
		multi.append(z);
		multi.append("]");
	}

	/** Add a color rectangle for color24bit color scheme.
	 * @param x X pixel position of upper left corner.
	 * @param y Y pixel position of upper left corner.
	 * @param w Width in pixels.
	 * @param h Height in pixels.
	 * @param r Red component (0-255).
	 * @param g Green component (0-255).
	 * @param b Blue component (0-255). */
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

	/** Add a slow traffic warning.
	 * @param spd Highest speed to activate warning.
	 * @param b Distance to end of backup (negative indicates upstream).
	 * @param units Units for speed (mph or kph).
	 * @param dist If true, replace tag with distance to slow station. */
	public void addSlowWarning(int spd, int b, String units, boolean dist) {
		multi.append("[slow");
		multi.append(spd);
		multi.append(',');
		multi.append(b);
		if(dist || !units.equals("mph")) {
			multi.append(',');
			multi.append(units);
			if(dist)
				multi.append(",dist");
		}
		multi.append("]");
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

	/** Validate the MULTI string */
	public boolean isValid() {
		return MultiParser.isValid(toString());
	}

	/** Clear the MULTI string */
	public void clear() {
		multi.setLength(0);
	}

	/** Is the MULTI string blank? */
	public boolean isBlank() {
		final StringBuilder _b = new StringBuilder();
		MultiParser.parse(toString(), new MultiAdapter() {
			public void addSpan(String span) {
				_b.append(span);
			}
			public void setPageBackground(int red, int green,
				int blue)
			{
				_b.append("PB");
			}
			public void addColorRectangle(int x, int y, int w,
				int h, int r, int g, int b)
			{
				_b.append("CR");
			}
			public void addGraphic(int g_num, Integer x, Integer y,
				String g_id)
			{
				_b.append("GRAPHIC");
			}
		});
		return _b.toString().trim().isEmpty();
	}

	/** Return a value indicating if the message is single or multi-page.
	 * @return True if the message contains a single page else false
	 * for multi-page. */
	public boolean singlePage() {
		return getNumPages() <= 1;
	}

	/** Get the number of pages in the multistring */
	public int getNumPages() {
		MultiAdapter msa = new MultiAdapter();
		MultiParser.parse(toString(), msa);
		return msa.ms_page + 1;
	}

	/** Return the canonical version of a MULTI string.
	 * @return A canonical MULTI string with all default tag values
	 *         included and redundant tags removed. */
	static public String canonical(String multi) {
		/* FIXME: include default tag values */
		return MultiParser.normalize(multi);
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
		MultiParser.parse(toString(), msa);
		return ret;
	}

	/** Get the page-on interval for the 1st page. If no page-on is
	 * specified in the MULTI string, the default is returned, which
	 * is a function of the number of pages in the multi-string.
	 * @return The page-on interval. */
	public Interval pageOnInterval() {
		Interval dflt = PageTimeHelper.defaultPageOnInterval(
			singlePage());
		Interval[] pg_on = pageOnIntervals(dflt);
		// return 1st page on-time read, even if specified per page
		return pg_on[0];
	}

	/** Get an array of page-on time intervals.
	 * @param dflt Default page-on time.
	 * @return An array of page-on time Intervals, one value per page. */
	public Interval[] pageOnIntervals(Interval dflt) {
		int np = getNumPages();
		PageTimeCounter ptc = new PageTimeCounter(np);
		MultiParser.parse(toString(), ptc);
		return ptc.pageOnIntervals(dflt);
	}

	/** Get an array of page-off time intervals.
	 * @param dflt Default page-off time.
	 * @return An array of page-off time Intervals, one value per page. */
	public Interval[] pageOffIntervals(Interval dflt) {
		int np = getNumPages();
		PageTimeCounter ptc = new PageTimeCounter(np);
		MultiParser.parse(toString(), ptc);
		return ptc.pageOffIntervals(dflt);
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
		MultiParser.parse(multi, ms);
		return ms.toString();
	}

	/** Get message text as an array of strings (with no tags).
	 * Every n_lines elements in the returned array represent one page.
	 * @param n_lines Number of lines per page.
	 * @return A string array containing text for each line. */
	public String[] getText(final int n_lines) {
		final LinkedList<String> ls = new LinkedList<String>();
		MultiParser.parse(toString(), new MultiAdapter() {
			public void addSpan(String span) {
				// note: fields in span use ms prefix
				while(ls.size() < (ms_page + 1) * n_lines)
					ls.add("");
				int i = ms_page * n_lines + ms_line;
				String v = ls.get(i);
				ls.set(i, SString.trimJoin(v, span));
			}
		});
		while("".equals(ls.peekLast()))
			ls.removeLast();
		return ls.toArray(new String[0]);
	}

	/** Get message lines as an array of strings (with tags).
	 * Every n_lines elements in the returned array represent one page.
	 * @param n_lines Number of lines per page.
	 * @return A string array containing text for each line. */
	public String[] getLines(int n_lines) {
		String[] pages = multi.toString().split("\\[np\\]");
		String[] lines = new String[n_lines * pages.length];
		for(int i = 0; i < lines.length; i++)
			lines[i] = "";
		for(int i = 0; i < pages.length; i++) {
			String[] lns = pages[i].split("\\[nl.?\\]");
			for(int ln = 0; ln < lns.length; ln++) {
				lines[i * n_lines + ln] =
					MultiParser.normalizeLine(lns[ln]);
			}
		}
		return lines;
	}

	/** Get a MULTI string as text only (tags stripped) */
	public String asText() {
		final StringBuilder sb = new StringBuilder();
		MultiParser.parse(toString(), new MultiAdapter() {
			public void addSpan(String span) {
				if(sb.length() > 0 &&
				   sb.charAt(sb.length() - 1) != ' ')
					sb.append(' ');
				sb.append(span);
			}
		});
		return sb.toString();
	}

	/** Normalize a MULTI string */
	static public String normalize(String ms) {
		return MultiParser.normalize(ms);
	}
}
