/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2015  Minnesota Department of Transportation
 * Copyright (C) 2014  AHMCT, University of California
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

/**
 * MULTI String (MarkUp Language for Transportation Information), as specified
 * in NTCIP 1203.
 *
 * @author Douglas Lau
 * @author Michael Darter
 * @author Travis Swanston
 */
public class MultiString implements Multi {

	/** MULTI string buffer */
	protected final StringBuilder multi = new StringBuilder();

        /** Test if the MULTI string is equal to another MULTI string */
	@Override
        public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o != null) {
			String ms = MultiParser.normalize(toString());
			String oms = MultiParser.normalize(o.toString());
			return ms.equals(oms);
		}
                return false;
        }

	/** Calculate a hash code for the MULTI string */
	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	/** Create an empty MULTI string */
	public MultiString() { }

	/** Create a new MULTI string.
	 * @param m MULTI string, may not be null.
	 * @throws NullPointerException if m is null. */
	public MultiString(String m) {
		if (m != null)
			multi.append(m);
		else
			throw new NullPointerException();
	}

	/** Append another MULTI string. */
	public void append(MultiString ms) {
		multi.append(ms.multi);
	}

	/** Add a span of text */
	@Override
	public void addSpan(String s) {
		if (s.length() > 0)
			multi.append(s);
	}

	/** Add a new line */
	@Override
	public void addLine(Integer spacing) {
		multi.append("[nl");
		if (spacing != null)
			multi.append(spacing);
		multi.append("]");
	}

	/** Add a new page */
	@Override
	public void addPage() {
		multi.append("[np]");
	}

	/** Set the page times.
	 * @param pt_on Page on time (deciseconds; null means default)
	 * @param pt_off Page off time (deciseconds; null means default) */
	@Override
	public void setPageTimes(Integer pt_on, Integer pt_off) {
		multi.append("[pt");
		if (pt_on != null)
			multi.append(pt_on);
		multi.append('o');
		if (pt_off != null)
			multi.append(pt_off);
		multi.append("]");
	}

	/** Set the page justification */
	@Override
	public void setJustificationPage(JustificationPage jp) {
		if (jp != JustificationPage.UNDEFINED) {
			multi.append("[jp");
			multi.append(jp.ordinal());
			multi.append("]");
		}
	}

	/** Set the line justification */
	@Override
	public void setJustificationLine(JustificationLine jl) {
		if (jl != JustificationLine.UNDEFINED) {
			multi.append("[jl");
			multi.append(jl.ordinal());
			multi.append("]");
		}
	}

	/** Add a graphic */
	@Override
	public void addGraphic(int g_num, Integer x, Integer y,
		String g_id)
	{
		multi.append("[g");
		multi.append(g_num);
		if (x != null && y != null) {
			multi.append(',');
			multi.append(x);
			multi.append(',');
			multi.append(y);
			if (g_id != null) {
				multi.append(',');
				multi.append(g_id);
			}
		}
		multi.append("]");
	}

	/** Set a new font number */
	@Override
	public void setFont(int f_num, String f_id) {
		multi.append("[fo");
		multi.append(f_num);
		if (f_id != null) {
			multi.append(',');
			multi.append(f_id);
		}
		multi.append("]");
	}

	/** Set the character spacing.
	 * @param sc Character spacing (null means use font spacing) */
	@Override
	public void setCharSpacing(Integer sc) {
		multi.append("[");
		if (sc != null) {
			multi.append("sc");
			multi.append(sc);
		} else
			multi.append("/sc");
		multi.append("]");
	}

	/** Set the (deprecated) message background color.
	 * @param x Background color (0-9; colorClassic value). */
	@Override
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
	@Override
	public void setPageBackground(int z) {
		multi.append("[pb");
		multi.append(z);
		multi.append("]");
	}

	/** Set the page background color for color24bit color scheme.
	 * @param r Red component (0-255).
	 * @param g Green component (0-255).
	 * @param b Blue component (0-255). */
	@Override
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
	@Override
	public void setColorForeground(int x) {
		multi.append("[cf");
		multi.append(x);
		multi.append("]");
	}

	/** Set the foreground color for color24bit color scheme.
	 * @param r Red component (0-255).
	 * @param g Green component (0-255).
	 * @param b Blue component (0-255). */
	@Override
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
	@Override
	public void addColorRectangle(int x, int y, int w, int h,
		int z)
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
	@Override
	public void addColorRectangle(int x, int y, int w, int h,
		int r, int g, int b)
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
	@Override
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
	@Override
	public void addTravelTime(String sid) {
		multi.append("[tt");
		multi.append(sid);
		multi.append("]");
	}

	/** Add a speed advisory */
	@Override
	public void addSpeedAdvisory() {
		multi.append("[vsa]");
	}

	/** Add a slow traffic warning.
	 * @param spd Highest speed to activate warning.
	 * @param b Distance to end of backup (negative indicates upstream).
	 * @param units Units for speed (mph or kph).
	 * @param dist If true, replace tag with distance to slow station. */
	@Override
	public void addSlowWarning(int spd, int b, String units, boolean dist) {
		multi.append("[slow");
		multi.append(spd);
		multi.append(',');
		multi.append(b);
		if (dist || !units.equals("mph")) {
			multi.append(',');
			multi.append(units);
			if (dist)
				multi.append(",dist");
		}
		multi.append("]");
	}

	/** Add a feed message */
	@Override
	public void addFeed(String fid) {
		multi.append("[feed");
		multi.append(fid);
		multi.append("]");
	}

	/** Get the value of the MULTI string */
	@Override
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
			@Override
			public void addSpan(String span) {
				_b.append(span);
			}
			@Override
			public void setPageBackground(int red, int green,
				int blue)
			{
				_b.append("PB");
			}
			@Override
			public void addColorRectangle(int x, int y, int w,
				int h, int r, int g, int b)
			{
				_b.append("CR");
			}
			@Override
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
		if (f_num < 1 || f_num > 255)
			return new int[0];
		int np = getNumPages();
		final int[] ret = new int[np]; // font numbers indexed by pg
		for (int i = 0; i < ret.length; i++)
			ret[i] = f_num;
		MultiAdapter msa = new MultiAdapter() {
			@Override
			public void addSpan(String span) {
				// note: fields in span use ms prefix
				if (ms_page >= 0 && ms_page < ret.length)
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
		int np = getNumPages();
		PageTimeCounter ptc = new PageTimeCounter(np);
		MultiParser.parse(toString(), ptc);
		return ptc.pageOnIntervals(dflt);
	}

	/** Get the page-off interval for the 1st page. If no page-off is
	 * specified in the MULTI string, the default is returned.
	 * @return The page-off interval. */
	public Interval pageOffInterval() {
		Interval dflt = PageTimeHelper.defaultPageOffInterval();
		Interval[] pg_off = pageOffIntervals(dflt);
		// return 1st page off-time read, even if specified per page
		return pg_off[0];
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

	/** Replace all the page times in a MULTI string.
	 * If no page time tag exists, then a page time tag is prepended.
	 * @param multi MULTI string.
	 * @param pt_on Page on-time in tenths of a second.
	 * @param pt_off Page off-time in tenths of a second.
	 * @return The updated MULTI string. */
	static public String replacePageTime(String multi, final Integer pt_on,
		final Integer pt_off)
	{
		if (multi.indexOf("[pt") < 0) {
			MultiString ms = new MultiString();
			ms.setPageTimes(pt_on, pt_off);
			return ms.toString() + multi;
		}
		MultiString ms = new MultiString() {
			@Override
			public void setPageTimes(Integer on, Integer off) {
				super.setPageTimes(pt_on, pt_off);
			}
		};
		MultiParser.parse(multi, ms);
		return ms.toString();
	}

	/** Strip all page time tags from a MULTI string */
	static public String stripPageTime(String multi) {
		MultiString ms = new MultiString() {
			@Override
			public void setPageTimes(Integer on, Integer off) { }
		};
		MultiParser.parse(multi, ms);
		return ms.toString();
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
		return multi.toString().split("\\[np\\]");
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
			int p = i * n_lines;
			String[] lns = pages[i].split("\\[nl.?\\]");
			for (int ln = 0; ln < lns.length; ln++) {
				int j = p + ln;
				if (j < n_total) {
					lines[j] = MultiParser.normalizeLine(
						lns[ln]);
				} else {
					// MULTI string defines more than
					// n_lines on this page.  We'll just
					// have to ignore this span.
				}
			}
		}
		return lines;
	}

	/** Get a MULTI string as text only (tags stripped) */
	public String asText() {
		final StringBuilder sb = new StringBuilder();
		MultiParser.parse(toString(), new MultiAdapter() {
			@Override
			public void addSpan(String span) {
				sb.append(span.trim());
				sb.append(' ');
			}
			@Override
			public void addLine(Integer s) {
				sb.append(' ');
			}
			@Override
			public void addPage() {
				sb.append(' ');
			}
		});
		return sb.toString().trim();
	}

	/**
	 * Normalize a MULTI string.
	 * @param ms The MULTI string to normalize.
	 * @return The normalized MULTI string, never null.
	 */
	static public String normalize(String ms) {
		return MultiParser.normalize(ms);
	}
}
