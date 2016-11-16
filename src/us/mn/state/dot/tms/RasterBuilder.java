/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2016  Minnesota Department of Transportation
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

import java.util.ArrayList;
import java.util.TreeMap;
import us.mn.state.dot.tms.utils.MultiRenderer;
import us.mn.state.dot.tms.utils.MultiString;
import us.mn.state.dot.tms.utils.MultiSyntaxError;

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
	private final int c_width;

	/** Check for character-matrix sign */
	public boolean isCharMatrix() {
		return c_width > 0;
	}

	/** Character height (pixels) for character- or line-matrix signs.
	 * Use 0 for full-matrix signs. */
	private final int c_height;

	/** Check for full-matrix sign */
	public boolean isFullMatrix() {
		return c_height <= 0;
	}

	/** Default font number */
	private final int default_font;

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
	private boolean isFontWidthUsable(Font f) {
		if (f.getWidth() > width)
			return false;
		if (isCharMatrix()) {
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
	private boolean isFontHeightUsable(Font f) {
		if (f.getHeight() > height)
			return false;
		if (isFullMatrix()) {
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
		if (c_height > 0)
			return c_height;
		Font f = FontHelper.find(default_font);
		return (f != null) ? f.getHeight() : height;
	}

	/** Get the optimal line spacing (pixels) */
	public int getLineSpacingPixels() {
		if (c_height > 0)
			return 0;
		Font f = FontHelper.find(default_font);
		return (f != null) ? f.getLineSpacing() : 1;
	}

	/** Get the number of lines of text using the default font */
	public int getLineCount() {
		int lh = getLineHeightPixels();
		int ls = getLineSpacingPixels();
		int l_max = SystemAttrEnum.DMS_MAX_LINES.getInt();
		for (int lines = 1; lines < l_max; lines++) {
			if (lh * (lines + 1) + ls * lines > height)
				return lines;
		}
		return l_max;
	}

	/** Render a BitmapGraphic for each page */
	public BitmapGraphic[] createBitmaps(MultiString ms)
		throws InvalidMsgException
	{
		final ArrayList<BitmapGraphic> bitmaps =
			new ArrayList<BitmapGraphic>();
		RasterGraphic.Factory factory = new RasterGraphic.Factory() {
			public RasterGraphic create() {
				BitmapGraphic bg = new BitmapGraphic(width,
					height);
				bitmaps.add(bg);
				return bg;
			}
		};
		render(ms, factory);
		return bitmaps.toArray(new BitmapGraphic[0]);
	}

	/** Render a PixmapGraphic for each page */
	public RasterGraphic[] createPixmaps(MultiString ms)
		throws InvalidMsgException
	{
		final ArrayList<RasterGraphic> pixmaps =
			new ArrayList<RasterGraphic>();
		RasterGraphic.Factory factory = new RasterGraphic.Factory() {
			public RasterGraphic create() {
				PixmapGraphic pg = new PixmapGraphic(width,
					height);
				pixmaps.add(pg);
				return pg;
			}
		};
		render(ms, factory);
		return pixmaps.toArray(new RasterGraphic[0]);
	}

	/** Render to a RasterGraphic for the specified page number */
	private void render(MultiString ms, RasterGraphic.Factory factory)
		throws InvalidMsgException
	{
		MultiRenderer mr = new MultiRenderer(factory, c_width, c_height,
			default_font);
		MultiString multi = DMSHelper.ignoreFilter(ms);
		multi.parse(mr);
		mr.complete();
		MultiSyntaxError err = mr.getSyntaxError();
		if (err != MultiSyntaxError.none) {
			throw new InvalidMsgException(err.toString() +
				": \"" + ms + '"');
		}
	}
}
