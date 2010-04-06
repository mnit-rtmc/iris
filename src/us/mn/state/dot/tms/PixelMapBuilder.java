/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2010  Minnesota Department of Transportation
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
		return isFontWidthUsable(f) && isFontHeightUsable(f);
	}

	/** Check if a font width is usable */
	protected boolean isFontWidthUsable(Font f) {
		if(f.getWidth() > width)
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
		return true;
	}

	/** Check if a font height is usable */
	protected boolean isFontHeightUsable(Font f) {
		if(f.getHeight() > height)
			return false;
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
			return f.getHeight() + f.getLineSpacing();
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
	protected BitmapGraphic createBitmap(MultiString ms, int p) {
		BitmapGraphic bg = new BitmapGraphic(width, height);
		MultiRenderer mr = new MultiRenderer(bg, p, c_width, c_height,
			getDefaultFontNumber());
		ms.parse(mr);
		mr.complete();
		// FIXME: check MultiRenderer.syntax_err
		return bg;
	}
}
