/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2022  Minnesota Department of Transportation
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
import java.util.List;
import us.mn.state.dot.tms.ColorScheme;
import us.mn.state.dot.tms.RasterBuilder;

/**
 * Text rectangle on a full-matrix sign
 *
 * @author Douglas Lau
 */
public class TextRect {
	public final int page_number;
	public final int width;
	public final int height;
	public final int font_num;

	/** Create a new text rectangle */
	public TextRect(int pn, int w, int h, int fn) {
		page_number = pn;
		width = w;
		height = h;
		font_num = fn;
	}

	/** Compare with another text rectangle for equality */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TextRect) {
			TextRect rhs = (TextRect) obj;
			return page_number == rhs.page_number &&
			       width == rhs.width &&
			       height == rhs.height &&
			       font_num == rhs.font_num;
		} else
			return false;
	}

	/** Get the number of lines of text on the rectangle */
	public int getLineCount() {
		// color scheme doesn't matter here
		RasterBuilder rb = new RasterBuilder(width, height, 0, 0,
			font_num, ColorScheme.COLOR_24_BIT);
		return rb.getLineCount();
	}

	/** Finder for text rectangles in MULTI strings */
	static private class Finder extends MultiAdapter {
		final int width;
		final int height;
		final int font_def;
		final ArrayList<TextRect> rects = new ArrayList<TextRect>();
		int font_num;
		int page = 1;
		TextRect tr;

		private Finder(int w, int h, int fn) {
			width = w;
			height = h;
			font_def = fn;
			font_num = fn;
			tr = pageRect();
		}
		private TextRect pageRect() {
			return new TextRect(page, width, height, font_num);
		}
		private void foundRect() {
			rects.add(new TextRect(tr.page_number, tr.width,
				tr.height, font_num));
		}

		@Override public void addSpan(String span) {
			tr = null;
		}
		@Override public void setFont(Integer f_num, String f_id) {
			font_num = (f_num != null) ? f_num : font_def;
		}
		@Override public void addGraphic(int g_num, Integer x,
			Integer y, String g_id)
		{
			tr = null;
		}
		@Override public void addLine(Integer spacing) {
			tr = null;
		}
		@Override public void addPage() {
			if (tr != null)
				foundRect();
			page++;
			tr = pageRect();
		}
		@Override public void setTextRectangle(int x, int y,
			int w, int h)
		{
			if (tr != null && !tr.equals(pageRect()))
				foundRect();
			tr = new TextRect(page, w, h, font_num);
		}
	}

	/** Find unused text rectangles in a MULTI string */
	static public List<TextRect> find(int width, int height, int fn,
		String multi)
	{
		Finder finder = new Finder(width, height, fn);
		// find text rectangles in MULTI string
		new MultiString(multi).parse(finder);
		// this creates a text rect on last page only if it's clean
		finder.addPage();
		return finder.rects;
	}

	/** Filler for text rectangles in MULTI strings */
	static private class Filler extends MultiBuilder {
		final String[] mess;
		int n_mess = 0;
		boolean page_clean = true;
		boolean page_rect = false;

		private Filler(String[] m) {
			mess = m;
		}

		private void fillRectangle() {
			if (n_mess < mess.length) {
				super.append(new MultiString(mess[n_mess]));
				n_mess++;
			}
		}

		@Override public void addSpan(String span) {
			super.addSpan(span);
			page_clean = false;
			page_rect = false;
		}
		@Override public void addGraphic(int g_num, Integer x,
			Integer y, String g_id)
		{
			super.addGraphic(g_num, x, y, g_id);
			page_clean = false;
			page_rect = false;
		}
		@Override public void addLine(Integer spacing) {
			super.addLine(spacing);
			page_clean = false;
			page_rect = false;
		}
		@Override public void addPage() {
			if (page_clean || page_rect)
				fillRectangle();
			super.addPage();
			page_clean = true;
			page_rect = false;
		}
		@Override public void setTextRectangle(int x, int y,
			int w, int h)
		{
			if (page_rect)
				fillRectangle();
			super.setTextRectangle(x, y, w, h);
			page_clean = false;
			page_rect = true;
		}
	}

	/** Fill text rectangles in a MULTI string */
	static public String fill(String multi, String[] mess) {
		Filler filler = new Filler(mess);
		// fill text rectangles in MULTI string
		new MultiString(multi).parse(filler);
		// if there's still a text rectangle, fill it at the end
		if (filler.page_clean || filler.page_rect)
			filler.fillRectangle();
		return filler.toString();
	}
}
