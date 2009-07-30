/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2009  Minnesota Department of Transportation
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
import java.io.IOException;
import java.util.TreeMap;

/**
 * A pixel map builder creates pixel maps for DMS display.
 * @see MultiStringState, MultiStringStateAdapter, MultiString
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class PixelMapBuilder {

	/** Pixel width of sign */
	public final int width;

	/** Pixel height of sign */
	public final int height;

	/** Character width (pixels) for character-matrix signs.  Use 0 for
	 * line-matrix or full-matrix signs. */
	protected final int c_width;

	/** Character height (pixels) for character- or line-matrix signs.
	 * Use 0 for full-matrix signs. */
	protected final int c_height;

	/**
	 * Create a new pixel map builder.
	 * @param w Sign width in pixels.
	 * @param h Sign height in pixels.
	 * @param cw Character width (pixels) for character-matrix signs.
	 *           Use 0 for line-matrix or full-matrix signs.
	 * @param ch Character height (pixels) for character- or line-matrix
	 *           signs.  Use 0 for full-matrix signs.
	 */
	public PixelMapBuilder(int w, int h, int cw, int ch) {
		width = w;
		height = h;
		c_width = cw;
		c_height = ch;
	}

	/** Find all matching fonts */
	public void findFonts(Checker<Font> checker) {
		FontFinder ff = new FontFinder();
		FontHelper.find(ff);
		ff.findFonts(checker);
	}

	/** Simple class to find matching fonts */
	protected class FontFinder implements Checker<Font> {
		protected final TreeMap<Integer, Font> fonts =
			new TreeMap<Integer, Font>();
		public boolean check(Font f) {
			if(isFontUsable(f))
				fonts.put(f.getNumber(), f);
			return false;
		}
		public Font getFirstFont() {
			if(fonts.size() > 0)
				return fonts.get(fonts.firstKey());
			else
				return null;
		}
		protected void findFonts(Checker<Font> checker) {
			for(Font f: fonts.values())
				checker.check(f);
		}
	}

	/** Check if a font is usable */
	public boolean isFontUsable(Font f) {
		if(f.getWidth() > width)
			return false;
		if(f.getHeight() > height)
			return false;
		if(c_width > 0) {
			// char-matrix signs must match font width
			if(c_width != f.getWidth())
				return false;
			// char-matrix signs must not have char spacing
			if(f.getCharSpacing() > 0)
				return false;
		} else {
			// line- or full-matrix signs must have char spacing
			if(f.getCharSpacing() == 0)
				return false;
		}
		if(c_height > 0) {
			// char- or line-matrix signs must match font height
			if(c_height != f.getHeight())
				return false;
			// char- or line-matrix signs must not have line spacing
			if(f.getLineSpacing() > 0)
				return false;
		} else {
			// full-matrix signs must have line spacing
			if(f.getLineSpacing() == 0)
				return false;
		}
		return true;
	}

	/** Get the optimal line height (pixels) */
	public int getLineHeightPixels() {
		if(c_height > 0)
			return c_height;
		Font f = getDefaultFont();
		if(f != null)
			return f.getHeight();
		else
			return height;
	}

	/** Get the default font */
	protected Font getDefaultFont() {
		FontFinder ff = new FontFinder();
		FontHelper.find(ff);
		return ff.getFirstFont();
	}

	/** Get the default font number */
	public int getDefaultFontNumber() {
		Font f = getDefaultFont();
		if(f != null)
			return f.getNumber();
		else
			return 1;
	}

	/** Render a BitmapGraphic for each page */
	public BitmapGraphic[] createPixmaps(MultiString ms) {
		int n_pages = ms.getNumPages();
		BitmapGraphic[] pixmaps = new BitmapGraphic[n_pages];
		for(int p = 0; p < n_pages; p++)
			pixmaps[p] = createBitmap(ms, p);
		return pixmaps;
	}

	/** Create and render a BitmapGraphic for the specified page number */
	protected BitmapGraphic createBitmap(MultiString ms, final int p) {
		int nltp = ms.countPageLines(p);
		BitmapRenderer cb = new BitmapRenderer(nltp, p);
		ms.parse(cb);
		return cb.bg;
	}

	/** Bitmap renderer.
	 * FIXME: add support for text rectangles */
	protected class BitmapRenderer extends MultiStringStateAdapter {
		protected final BitmapGraphic bg =
			new BitmapGraphic(width, height);
		protected final int nltp;
		protected final int page;
		public BitmapRenderer(int nl, int p) {
			nltp = nl;
			page = p;
			ms_fnum = getDefaultFontNumber();
		}
		public void addSpan(String span) {
			if(page == ms_page)
				render(span);
		}
		protected void render(String span) {
			Font font = FontHelper.find(ms_fnum);
			if(font == null)
				return;
			try {
				int x = _calculatePixelX(ms_justl, font, span);
				int y = _calculatePixelY(ms_justp, font,
					ms_line, nltp);
				renderSpan(bg, font, span, x, y);
			}
			catch(Exception e) {
				// not much we can do here ...
			}
		}
		public void addGraphic(int g_num, Integer x, Integer y,
			String g_id)
		{
			Graphic graphic = GraphicHelper.find(g_num);
			if(graphic == null)
				return;
			int x0 = 0;
			int y0 = 0;
			if(x != null)
				x0 = x - 1;
			if(y != null)
				y0 = y - 1;
			try {
				renderGraphic(graphic, bg, x0, y0);
			}
			catch(IOException e) {
				// not much we can do here ...
			}
		}
	}

	/** Calculate the X pixel position to place a span */
	protected int _calculatePixelX(MultiString.JustificationLine jl,
		Font font, String span) throws InvalidMessageException
	{
		int x = calculatePixelX(jl, font, span);
		if(x >= 0)
			return x;
		throw new InvalidMessageException("Message too long: " + span);
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

	/** Calculate the Y pixel position to place a span.
	 * @param nltp Number of lines of actual text on the page. */
	protected int _calculatePixelY(MultiString.JustificationPage jp,
		Font font, int line, int nltp) throws InvalidMessageException
	{
		int y = calculatePixelY(jp, font, line, nltp);
		if(y >= 0)
			return y;
		throw new InvalidMessageException("Too many lines");
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
		byte[] pixels = Base64.decode(g.getPixels());
		BitmapGraphic c = new BitmapGraphic(w, h);
		c.setPixels(pixels);
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
		Glyph g = FontHelper.lookupGlyph(font, cp);
		if(g != null) {
			Graphic gr = g.getGraphic();
			if(gr != null)
				return gr;
		}
		throw new InvalidMessageException("Invalid code point");
	}
}
