/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008  Minnesota Department of Transportation
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

import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.sonar.Namespace;
import java.io.IOException;
import java.util.LinkedList;

/**
 * A pixel map builder creates pixel maps for DMS display.
 *
 * @author Douglas Lau
 */
public class PixelMapBuilder implements MultiString.SpanCallback {

	/** Special value to indicate an invalid line spacing */
	static protected final int INVALID_LINE_SPACING = -1;

	/** SONAR namespace */
	protected final Namespace namespace;

	/** Pixel width of sign */
	protected final int width;

	/** Pixel height of sign */
	protected final int height;

	/** Character width (pixels) for character-matrix signs.  Use 0 for
	 * line-matrix or full-matrix signs. */
	protected final int c_width;

	/** Character height (pixels) for character- or line-matrix signs.
	 * Use 0 for full-matrix signs. */
	protected final int c_height;

	/**
	 * Create a new pixel map builder.
	 * @param ns SONAR namespace.
	 * @param w Sign width in pixels.
	 * @param h Sign height in pixels.
	 * @param cw Character width (pixels) for character-matrix signs.
	 *           Use 0 for line-matrix or full-matrix signs.
	 * @param ch Character height (pixels) for character- or line-matrix
	 *           signs.  Use 0 for full-matrix signs.
	 */
	public PixelMapBuilder(Namespace ns, int w, int h, int cw, int ch) {
		namespace = ns;
		width = w;
		height = h;
		c_width = cw;
		c_height = ch;
	}

	/** Get a font with the given font number */
	public Font getFont(int f_num) {
		return lookupFont(f_num, getLineHeightPixels(f_num), c_width,0);
	}

	/** Get the optimal line height (pixels) */
	protected int getLineHeightPixels(int f_num) {
		if(c_height > 0)
			return c_height;
		for(int h = height; h > 0; h--) {
			int ls = calculateLineSpacing(h);
			if(ls != INVALID_LINE_SPACING) {
				if(lookupFont(f_num, h, c_width, ls) != null)
					return h;
			}
		}
		// No optimal height found; just grab a font...
		Font f = lookupFont(f_num, 0, c_width, 0);
		if(f != null)
			return f.getHeight();
		else
			return SystemAttributeHelper.getDmsDefaultFontHeight();
	}

	/** Calculate the line spacing for a given font height */
	protected int calculateLineSpacing(int font_height) {
		int extra = height % font_height;
		int gaps = (height / font_height) - 1;
		if(extra == 0)
			return 0;
		else if((gaps > 0) && (extra % gaps == 0))
			return extra / gaps;
		else
			return INVALID_LINE_SPACING;
	}

	/** Lookup the best font.
	 * @param n Font number.
	 * @param h Font height (pixels).  Zero matches any height.
	 * @param w Font width (pixels).  Zero matches any width.
	 * @param ls Line spacing (pixels).  Zero matches any line spacing. */
	protected Font lookupFont(int n, int h, int w, int ls) {
		Font f = _lookupFont(n, h, w, ls);
		if(f != null || w == 0)
			return f;
		else
			return _lookupFont(n, h, 0, ls);
	}

	/** Lookup the best font.
	 * @param n Font number.
	 * @param h Font height (pixels).  Zero matches any height.
	 * @param w Font width (pixels).  Zero matches any width.
	 * @param ls Line spacing (pixels).  Zero matches any line spacing. */
	protected Font _lookupFont(final int n, final int h, final int w,
		final int ls)
	{
		return (Font)namespace.findObject(Font.SONAR_TYPE,
			new Checker<Font>()
		{
			public boolean check(Font f) {
				return checkFont(f, n, h, w, ls);
			}
		});
	}

	/** Check if a font matches criteria.
	 * @param n Font number.
	 * @param h Font height (pixels).  Zero matches any height.
	 * @param w Font width (pixels).  Zero matches any width.
	 * @param ls Line spacing (pixels).  Zero matches any line spacing. */
	protected boolean checkFont(Font f, int n, int h, int w, int ls) {
		boolean n_match = n == f.getNumber();
		boolean ls_match = (ls == 0) || ls == f.getLineSpacing();
		boolean h_match = (h == 0) || h == f.getHeight();
		boolean w_match = (w == 0) || w == f.getWidth();
		return n_match && ls_match && h_match && h_match && w_match;
	}

	/** Count of pages */
	protected int n_pages;

	/** List of all text spans */
	protected final LinkedList<TextSpan> spans = new LinkedList<TextSpan>();

	/** Text span encapsulation */
	protected class TextSpan {
		final int page;
		final MultiString.JustificationPage jp;
		final int line;
		final MultiString.JustificationLine jl;
		final Font font;
		final String text;
		TextSpan(int p, MultiString.JustificationPage _jp, int l,
			MultiString.JustificationLine _jl, Font f, String t)
		{
			page = p;
			jp = _jp;
			line = l;
			jl = _jl;
			font = f;
			text = t;
		}

		/** Render one span on the given bitmap */
		protected void render(BitmapGraphic bg, int nltp) {
			try {
				int x = _calculatePixelX();
				int y = _calculatePixelY(nltp);
				renderSpan(bg, font, text, x, y);
			}
			catch(IndexOutOfBoundsException e) {
				log("Message text too long: " + text);
			}
			catch(InvalidMessageException e) {
				log(e.getMessage() + ": " + text);
			}
			catch(IOException e) {
				log("Invalid Base64 glyph data");
			}
		}

		/** Calculate the X pixel position to place a span */
		protected int _calculatePixelX() throws InvalidMessageException{
			int x = calculatePixelX(jl, font, text);
			if(x >= 0)
				return x;
			throw new InvalidMessageException("Message too long: " +
				text);
		}

		/** Calculate the Y pixel position to place a span.
		 * @param nltp Number of lines of actual text on the page. */
		protected int _calculatePixelY(int nltp)
			throws InvalidMessageException
		{
			int y = calculatePixelY(jp, font, line, nltp);
			if(y >= 0)
				return y;
			throw new InvalidMessageException("Too many lines");
		}
	}

	/**
	 * Add a span of text.
	 * @param page Page number, zero based.
	 * @param jp Page justification, e.g. top, middle, bottom.
	 * @param line Line number, zero based.
	 * @param jl Line justification, e.g. centered, left, right.
	 * @param f_num Font to use for rendering.
	 * @param text Text span to render.
	 */
	public void addSpan(int page, MultiString.JustificationPage jp,
		int line, MultiString.JustificationLine jl, int f_num,
		String text)
	{
		Font font = getFont(f_num);
		spans.add(new TextSpan(page, jp, line, jl, font, text));
		n_pages = Math.max(page + 1, n_pages);
	}

	/** Get the pixmap graphics */
	public BitmapGraphic[] getPixmaps() {
		BitmapGraphic[] pixmaps = new BitmapGraphic[n_pages];
		for(int p = 0; p < n_pages; p++)
			pixmaps[p] = getBitmap(p);
		return pixmaps;
	}

	/** Get a bitmap graphic for the specified page number */
	protected BitmapGraphic getBitmap(int p) {
		int nltp = getLinesOnPage(p);
		BitmapGraphic bg = new BitmapGraphic(width, height);
		for(TextSpan span: spans) {
			if(p == span.page)
				span.render(bg, nltp);
		}
		return bg;
	}

	/** Calculate the number of text lines on a page */
	protected int getLinesOnPage(int p) {
		int n_lines = 0;
		for(TextSpan span: spans) {
			if(p == span.page)
				n_lines = Math.max(span.line + 1, n_lines);
		}
		return n_lines;
	}

	/** Calculate the X pixel position to place text */
	protected int calculatePixelX(MultiString.JustificationLine jl,
		Font font, String t) throws InvalidMessageException
	{
		switch(jl) {
		case LEFT:
			return 0;
		case CENTER:
			// determine centering mode: block or bit oriented.
			int pseudo_c_width = (c_width <= 0 ? 1 : c_width);
			int w = width / pseudo_c_width;
			int r = calculateWidth(font, t) / pseudo_c_width;
			return (w - r) / 2 * pseudo_c_width;
		case RIGHT:
			return width - calculateWidth(font, t) - 1;
		default:
			throw new InvalidMessageException(
				"Invalid line justification: " + jl);
		}
	}

	/** Calculate the width of a span of text */
	protected int calculateWidth(Font font, String t)
		throws InvalidMessageException
	{
		int w = 0;
		for(int i = 0; i < t.length(); i++) {
			if(i > 0)
				w += font.getCharSpacing();
			int cp = t.charAt(i);
			Graphic c = lookupGraphic(font, cp);
			w += c.getWidth();
		}
		return w;
	}

	/** Calculate the Y pixel position to place text
	 * @param jp Line justification, e.g. top, middle, bottom.
	 * @param font Font to use.
	 * @param line Line number, zero based.
	 * @param nltp Number of lines of actual text on the page.
	 */
	protected int calculatePixelY(MultiString.JustificationPage jp,
		Font font, int line, int nltp) throws InvalidMessageException
	{
		int lineHeight = font.getHeight() + font.getLineSpacing();
		switch(jp) {
		case TOP:
			return line * lineHeight;
		case MIDDLE:
			return calculateYMiddle(line, nltp, lineHeight);
		case BOTTOM:
			return calculateYBottom(line, nltp, lineHeight);
		default:
			throw new InvalidMessageException(
				"Invalid page justification: " + jp);
		}
	}

	/** Calculate Y location using MIDDLE justification */
	protected int calculateYMiddle(int line, int nltp, int lineHeight) {
		int r = lineHeight * nltp;
		int top = (height - r) / 2;
		return top + line * lineHeight;
	}

	/** Calculate Y location using BOTTOM justification */
	protected int calculateYBottom(int line, int nltp, int lineHeight) {
		int r = lineHeight * nltp;
		int top = height - r - 1;
		return top + line * lineHeight;
	}

	/**
	 * Render a span of text onto a bitmap graphic.
	 *
	 * @param bg BitmapGraphic to render into.
	 * @param font Font to render
	 * @param t Text to render
	 * @param x Horizontal position to start rendering
	 * @param y Vertical position to strat rendering
	 *
	 * @throws InvalidMessageException if the message contains chars that
	 *                                 don't exist.
	 * @throws IOException If a Base64 decoding error on Graphic.
	 */
	protected void renderSpan(BitmapGraphic bg, Font font, String t, int x,
		int y) throws InvalidMessageException, IOException
	{
		for(int i = 0; i < t.length(); i++) {
			int cp = t.charAt(i);
			Graphic g = lookupGraphic(font, cp);
			renderGraphic(g, bg, x, y);
			x += g.getWidth() + font.getCharSpacing();
		}
	}

	/** Render a graphic onto a bitmap graphic */
	protected void renderGraphic(Graphic g, BitmapGraphic bg, int x, int y)
		throws IOException
	{
		int w = g.getWidth();
		int h = g.getHeight();
		byte[] bitmap = Base64.decode(g.getPixels());
		BitmapGraphic c = new BitmapGraphic(w, h);
		c.setBitmap(bitmap);
		for(int yy = 0; yy < h; yy++) {
			for(int xx = 0; xx < w; xx++) {
				int p = c.getPixel(xx, yy);
				bg.setPixel(x + xx, y + yy, p);
			}
		}
	}

	/** Look up a code point in the specified font */
	protected Graphic lookupGraphic(Font font, int cp)
		throws InvalidMessageException
	{
		Glyph g = lookupGlyph(font, cp);
		if(g != null) {
			Graphic gr = g.getGraphic();
			if(gr != null)
				return gr;
		}
		throw new InvalidMessageException("Invalid code point");
	}

	/** Lookup a glyph in the specified font */
	protected Glyph lookupGlyph(final Font f, final int cp) {
		return (Glyph)namespace.findObject(Glyph.SONAR_TYPE,
			new Checker<Glyph>()
		{
			public boolean check(Glyph g) {
				return g.getFont() == f &&
				       g.getCodePoint() == cp;
			}
		});
	}

	/** Log an error message */
	protected void log(String m) {
		System.err.println("PixelMapBuilder:" + m);
	}
}
