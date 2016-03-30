/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.utils;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import us.mn.state.dot.tms.PageTimeHelper;
import us.mn.state.dot.tms.units.Interval;

/**
 * MULTI String (MarkUp Language for Transportation Information), as specified
 * in NTCIP 1203.
 *
 * @author Douglas Lau
 * @author Michael Darter
 * @author Travis Swanston
 */
public class MultiString {

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
			String ms = MultiParser.normalize(multi);
			String oms = MultiParser.normalize(o.toString());
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

	/** Validate the MULTI string */
	public boolean isValid() {
		return MultiParser.isValid(multi);
	}

	/** Is the MULTI string blank? */
	public boolean isBlank() {
		final StringBuilder _b = new StringBuilder();
		parse(new MultiAdapter() {
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

	/** Normalize a MULTI string.
	 * @return The normalized MULTI string. */
	public String normalize() {
		return MultiParser.normalize(multi);
	}

	/** Return the canonical version of a MULTI string.
	 * @return A canonical MULTI string with all default tag values
	 *         included and redundant tags removed. */
	public String canonical() {
		/* FIXME: include default tag values */
		return MultiParser.normalize(multi);
	}

	/** Normalize a single line MULTI string.
	 * @return The normalized MULTI string. */
	public String normalizeLine() {
		return MultiParser.normalizeLine(multi);
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
		MultiAdapter msa = new MultiAdapter() {
			@Override public void addPage() {
				n_pages[0]++;
			}
		};
		parse(msa);
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
		MultiAdapter msa = new MultiAdapter() {
			private int font_num = f_num;
			@Override public void setFont(int fn, String f_id) {
				font_num = fn;
			}
			@Override public void addPage() {
				fonts.add(font_num);
			}
			@Override public void addSpan(String span) {
				fonts.set(fonts.size() - 1, font_num);
			}
		};
		parse(msa);
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
		parse(new MultiAdapter() {
			@Override
			public void addSpan(String span) {
				if (sb.length() > 0)
					sb.append(' ');
				sb.append(span.trim());
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

	/** Does the MULTI string have a tolling [tz] tag? */
	public boolean isTolling() {
		final StringBuilder _b = new StringBuilder();
		parse(new MultiAdapter() {
			@Override
			public void addTolling(String mode, String[] zones) {
				_b.append(mode);
			}
		});
		return _b.length() > 0;
	}

	/** Parse the MULTI string.
	 * @param cb A callback which keeps track of the MULTI state. */
	public void parse(Multi cb) {
		MultiParser.parse(multi, cb);
	}
}
