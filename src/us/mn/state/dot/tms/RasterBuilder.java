/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2012  Minnesota Department of Transportation
 * Copyright (C) 2009-2010  AHMCT, University of California
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

import java.util.TreeMap;

/**
 * A raster builder creates raster graphics for DMS display.
 * @see Multi, MultiAdapter, MultiString
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class RasterBuilder {

	/** Pixel width of sign */
	public final int width;

	/** Pixel height of sign */
	public final int height;

	/** Character width (pixels) for character-matrix signs.  Use 0 for
	 * line-matrix or full-matrix signs. */
	protected final int c_width;

	/** Check for character-matrix sign */
	public boolean isCharMatrix() {
		return c_width > 0;
	}

	/** Character height (pixels) for character- or line-matrix signs.
	 * Use 0 for full-matrix signs. */
	protected final int c_height;

	/** Check for full-matrix sign */
	public boolean isFullMatrix() {
		return c_height <= 0;
	}

	/** Default font number */
	protected final int default_font;

	/**
	 * Create a new raster builder.
	 * @param w Sign width in pixels.
	 * @param h Sign height in pixels.
	 * @param cw Character width (pixels) for character-matrix signs.
	 *           Use 0 for line-matrix or full-matrix signs.
	 * @param ch Character height (pixels) for character- or line-matrix
	 *           signs.  Use 0 for full-matrix signs.
	 * @param df Default font number.
	 */
	public RasterBuilder(int w, int h, int cw, int ch, int df) {
		width = w;
		height = h;
		c_width = cw;
		c_height = ch;
		default_font = df;
	}

	/** Check if a font is usable */
	public boolean isFontUsable(Font f) {
		return isFontWidthUsable(f) && isFontHeightUsable(f);
	}

	/** Check if a font width is usable */
	protected boolean isFontWidthUsable(Font f) {
		if(f.getWidth() > width)
			return false;
		if(isCharMatrix()) {
			// char-matrix signs must match font width
			// and must not have character spacing
			return c_width == f.getWidth() &&
			       f.getCharSpacing() == 0;
		} else {
			// line- or full-matrix signs must have char spacing
			return f.getCharSpacing() > 0;
		}
	}

	/** Check if a font height is usable */
	protected boolean isFontHeightUsable(Font f) {
		if(f.getHeight() > height)
			return false;
		if(isFullMatrix()) {
			// full-matrix signs must have line spacing
			return f.getLineSpacing() > 0;
		} else {
			// char- or line-matrix signs must match font height
			// and must not have line spacing
			return c_height == f.getHeight() &&
			       f.getLineSpacing() == 0;
		}
	}

	/** Get the optimal line height (pixels) */
	public int getLineHeightPixels() {
		if(c_height > 0)
			return c_height;
		Font f = FontHelper.find(default_font);
		if(f != null)
			return f.getHeight();
		else
			return height;
	}

	/** Get the optimal line spacing (pixels) */
	public int getLineSpacingPixels() {
		if(c_height > 0)
			return 0;
		Font f = FontHelper.find(default_font);
		if(f != null)
			return f.getLineSpacing();
		else
			return 1;
	}

	/** Get the number of lines of text using the default font */
	public int getLineCount() {
		int lh = getLineHeightPixels();
		int ls = getLineSpacingPixels();
		int l_max = SystemAttrEnum.DMS_MAX_LINES.getInt();
		for(int lines = 1; lines < l_max; lines++) {
			if(lh * (lines + 1) + ls * lines > height)
				return lines;
		}
		return l_max;
	}

	/** Render a BitmapGraphic for each page */
	public BitmapGraphic[] createBitmaps(MultiString ms)
		throws InvalidMessageException
	{
		int n_pages = ms.getNumPages();
		BitmapGraphic[] bitmaps = new BitmapGraphic[n_pages];
		for(int p = 0; p < n_pages; p++) {
			bitmaps[p] = new BitmapGraphic(width, height);
			render(ms, p, bitmaps[p]);
		}
		return bitmaps;
	}

	/** Render a PixmapGraphic for each page */
	public RasterGraphic[] createPixmaps(MultiString ms)
		throws InvalidMessageException
	{
		int n_pages = ms.getNumPages();
		RasterGraphic[] pixmaps = new RasterGraphic[n_pages];
		for(int p = 0; p < n_pages; p++) {
			pixmaps[p] = new PixmapGraphic(width, height);
			render(ms, p, pixmaps[p]);
		}
		return pixmaps;
	}

	/** Render to a RasterGraphic for the specified page number */
	private void render(MultiString ms, int p, RasterGraphic rg)
		throws InvalidMessageException
	{
		MultiRenderer mr = new MultiRenderer(rg, p, c_width, c_height,
			default_font);
		String multi = DMSHelper.ignoreFilter(ms).toString();
		MultiParser.parse(multi, mr);
		mr.complete();
		MultiSyntaxError err = mr.getSyntaxError();
		if(err != MultiSyntaxError.none) {
			throw new InvalidMessageException(err.toString() +
				": \"" + ms + '"');
		}
	}
}
