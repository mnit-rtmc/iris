/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2022-2024  Minnesota Department of Transportation
 * Copyright (C) 2024       SRF Consulting Group
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import us.mn.state.dot.tms.ColorScheme;
import us.mn.state.dot.tms.Font;
import us.mn.state.dot.tms.FontHelper;
import us.mn.state.dot.tms.Glyph;
import us.mn.state.dot.tms.RasterBuilder;
import us.mn.state.dot.tms.WordHelper;

/**
 * Text rectangle on a full-matrix sign
 *
 * @author Douglas Lau
 * @author John L. Stanley - SRF Consulting
 */
public class TextRect {
	public final int page_number;
	public final int rx;
	public final int ry;
	public final int width;
	public final int height;
	public final int c_height;
	public final int font_num;
	public final boolean implied;

	/** Glyph width cache */
	private HashMap<Integer, Integer> glyph_widths;

	/** Create a new text rectangle */
	public TextRect(int pn, int x, int y, int w, int h, int ch, int fn,
		boolean imp)
	{
		page_number = pn;
		rx = x;
		ry = y;
		width = w;
		height = h;
		c_height = ch;
		font_num = fn;
		implied = imp;
	}

	/** Create an implied full-page TextRect */
	private TextRect pageRect(int page, int font_cur) {
		return new TextRect(page, rx, ry, width, height, c_height,
			font_cur, true);
	}

	/** Compare with another text rectangle for equality.
	 * Note that an implied text-rectangle is counted as
	 * different than an explicit text rectangle of the
	 * same size to make the fillable text rectangle
	 * algorithm implemented here work correctly. */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TextRect) {
			TextRect rhs = (TextRect) obj;
			return page_number == rhs.page_number &&
			       rx == rhs.rx &&
			       ry == rhs.ry &&
			       width == rhs.width &&
			       height == rhs.height &&
			       font_num == rhs.font_num &&
			       implied == rhs.implied;
		} else
			return false;
	}

	/** Get the number of lines of text on the rectangle */
	public int getLineCount() {
		// color scheme doesn't matter here
		RasterBuilder rb = new RasterBuilder(width, height,
			0, c_height, font_num, ColorScheme.COLOR_24_BIT);
		return rb.getLineCount();
	}

	/** Scanner for text rectangles in MULTI strings */
	abstract private class Scanner extends MultiAdapter {
		int page = 1;  // current page
		int font_cur;  // current font
		TextRect page_rect; // page rect
		TextRect rect; // current rect
		boolean fillable; // current rect fillable

		private Scanner() {
			font_cur = font_num;
			page_rect = pageRect(page, font_cur);
			rect = page_rect;
			fillable = true;
		}
		void startRect(TextRect tr) {
			if (fillable)
				fillableRect(rect);
			rect = tr;
			fillable = true;
		}
		abstract void fillableRect(TextRect tr);

		@Override public void addSpan(String span) {
			fillable = false;
		}
		@Override public void setFont(Integer f_num, String f_id) {
			font_cur = (f_num != null) ? f_num : font_num;
		}
		@Override public void addLine(Integer spacing) {
			fillable = false;
		}
		@Override public void addPage() {
			page++;
			page_rect = pageRect(page, font_cur);
			startRect(page_rect);
		}
		@Override public void setTextRectangle(int x, int y,
			int w, int h)
		{
			if (w == 0)
				w = width - (x - 1);
			if (h == 0)
				h = height - (y - 1);
			if (rect.equals(page_rect))
				fillable = false;
			startRect(new TextRect(page, x, y, w, h, c_height,
				font_cur, false));
		}
	}

	/** Find fillable text rectangles in a MULTI string */
	public List<TextRect> find(String multi) {
		final ArrayList<TextRect> rects = new ArrayList<TextRect>();
		Scanner scanner = new Scanner() {
			@Override void fillableRect(TextRect tr) {
				rects.add(tr);
			}
		};
		// find text rectangles in MULTI string
		new MultiString(multi).parse(scanner);
		// this creates a text rect on last page only if it's clean
		scanner.addPage();
		return rects;
	}

	/** Filler for text rectangles in MULTI strings */
	private class Filler extends MultiBuilder {
		final List<TextRect> rects;
		final Iterator<String> lines;
		int page = 1;  // current page
		int font_cur;  // current font

		private Filler(List<TextRect> trs, List<String> lns) {
			rects = trs;
			lines = lns.iterator();
			font_cur = font_num;
			fillRect(pageRect(page, font_cur));
		}

		private void fillRect(TextRect tr) {
			if (!rects.contains(tr))
				return;
			int n_lines = tr.getLineCount();
			while (n_lines > 0) {
				if (lines.hasNext()) {
					String ln = lines.next();
					Filler.this.append(
						new MultiString(ln));
				}
				n_lines--;
				if (n_lines > 0)
					Filler.super.addLine(null);
			}
		}

		@Override public void setFont(Integer f_num, String f_id) {
			super.setFont(f_num, f_id);
			font_cur = (f_num != null) ? f_num : font_num;
		}
		@Override public void addPage() {
			super.addPage();
			page++;
			fillRect(pageRect(page, font_cur));
		}
		@Override public void setTextRectangle(int x, int y,
			int w, int h)
		{
			super.setTextRectangle(x, y, w, h);
			if (w == 0)
				w = width - (x - 1);
			if (h == 0)
				h = height - (y - 1);
			fillRect(new TextRect(page, x, y, w, h, c_height,
				font_cur, false));
		}
		@Override public void addFeed(String fid) {
			// strip feed tags
		}
	}

	/** Fill text rectangles in a pattern MULTI string.
	 *
	 * This is the inverse of `splitLines`.
	 *
	 * @param pat_ms The pattern MULTI string to fill.
	 * @param lines Text lines in order of text rectangles, padded to
	 *              each rectangle's getLineCount total.
	 * @return The filled MULTI string.
	 */
	public String fill(String pat_ms, List<String> lines) {
		List<TextRect> rects = find(pat_ms);
		Filler filler = new Filler(rects, lines);
		// fill text rectangles in MULTI string
		new MultiString(pat_ms).parse(filler);
		return filler.toString();
	}

	/** Splitter for lines in text rectangles */
	private class Splitter extends Scanner {
		final List<TextRect> rects; // fillable rectangles from pattern
		final ArrayList<String> lines = new ArrayList<String>();
		boolean within; // currently within a fillable rectangle
		int n_lines;

		private Splitter(List<TextRect> trs) {
			rects = trs;
			within = rects.contains(TextRect.this);
			n_lines = 0;
			if (within)
				lines.add("");
		}
		private void append(String ms) {
			if (within) {
				String line = lines.remove(lines.size() - 1);
				lines.add(line + ms);
			}
		}

		@Override void startRect(TextRect tr) {
			if (within) {
				int n = lines.size() - n_lines;
				int c = rect.getLineCount();
				// remove excess lines which don't fit
				while (n > c) {
					int j = lines.size() - 1;
					lines.remove(j);
					n--;
				}
				// pad lines to fill text rectangle
				while (n < c) {
					lines.add("");
					n++;
				}
			}
			n_lines = lines.size();
			within = rects.contains(tr);
			if (within)
				lines.add("");
			rect = tr;
		}
		@Override void fillableRect(TextRect tr) {}
		@Override public void addSpan(String span) {
			append(span);
		}
		@Override public void addLine(Integer spacing) {
			if (within)
				lines.add("");
		}
		@Override public void setColorForeground(Integer x) {
			String xx = (x != null) ? "" + x : "";
			append("[cf" + xx + "]");
		}
		@Override public void setColorForeground(int r, int g, int b) {
			append("[cf" + r + ',' + g + ',' + b + ']');
		}
		@Override public void setCharSpacing(Integer sc) {
			append((sc != null)
				? "[sc" + sc + "]"
				: "[/sc]");
		}
		@Override
		public void setJustificationLine(JustificationLine jl) {
			String n = (jl != null &&
			            jl != JustificationLine.UNDEFINED)
				? "" + jl.ordinal()
				: "";
			append("[jl" + n + "]");
		}
	}

	/** Split a MULTI string into lines based on text rectangles in a
	 *  pattern.
	 *
	 * This is the inverse of `fill`.
	 *
	 * @param pat_ms The MULTI string from a message pattern.
	 * @param multi The MULTI string to split (not pattern).
	 * @return Text lines in order of text rectangles, padded to
	 *         each rectangle's getLineCount total.
	 */
	public List<String> splitLines(String pat_ms, String multi) {
		List<TextRect> rects = find(pat_ms);
		Splitter splitter = new Splitter(rects);
		// fill text rectangles in MULTI string
		new MultiString(multi).parse(splitter);
		// if there's still a text rectangle, split it at the end
		splitter.addPage();
		return splitter.lines;
	}

	/** Width checker for MULTI lines */
	private class WidthChecker extends MultiAdapter {
		final int font_char_spacing;
		Integer char_spacing; // current character spacing
		int px_width;        // pixel width

		private WidthChecker(int cs) {
			font_char_spacing = cs;
			char_spacing = null;
			px_width = 0;
		}
		private int getCharSpacing() {
			return (char_spacing != null)
			      ? char_spacing
			      : font_char_spacing;
		}

		@Override public void addSpan(String span) {
			int len = span.length();
			if (len > 0)
				px_width += getCharSpacing() * (len - 1);
			for (char cp: span.toCharArray()) {
				Integer w = glyph_widths.get((int) cp);
				// if glyph not found, make it "too wide"
				px_width += (w != null) ? w : width + 1;
			}
		}
		@Override public void setCharSpacing(Integer sc) {
			char_spacing = sc;
		}
	}

	/** Calculate the width of a single line MULTI string.
	 * Note: result will be invalid if not a single line. */
	public int calculateWidth(String ms) {
		Font font = FontHelper.find(font_num);
		if (font == null)
			return -1;
		if (glyph_widths == null)
			cacheGlyphWidths(font);
		WidthChecker checker = new WidthChecker(font.getCharSpacing());
		new MultiString(ms).parse(checker);
		return checker.px_width;
	}

	/** Create a cache of glyph widths for a font */
	private void cacheGlyphWidths(Font font) {
		glyph_widths = new HashMap<Integer, Integer>();
		for (Map.Entry<Integer, Glyph> ent :
			FontHelper.lookupGlyphs(font).entrySet())
		{
			glyph_widths.put(
				ent.getKey(),
				ent.getValue().getWidth()
			);
		}
	}

	/** Check if a MULTI line fits in the text rectangle.
	 * @param ms MULTI line (only line-valid tags allowed!).
	 * @param abbrev If true, try to abbreviate words if necessary.
	 * @return Possibly abbreviated line, or null if it does not fit. */
	public String checkLine(String ms, boolean abbrev) {
		// it's possible to configure infinite abbrev loops,
		// so limit this to 20 iterations
		for (int i = 0; i < 20 && ms != null; i++) {
			int w = calculateWidth(ms);
			if (w >= 0 && w <= width)
				return ms;
			if (abbrev)
				ms = WordHelper.abbreviate(ms);
			else
				break;
		}
		return null;
	}
}
