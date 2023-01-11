/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2023  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.utils.MultiBuilder;
import us.mn.state.dot.tms.utils.MultiRenderer;
import us.mn.state.dot.tms.utils.MultiString;
import us.mn.state.dot.tms.utils.MultiSyntaxError;

/**
 * A raster builder creates raster graphics for DMS display.
 *
 * @see us.mn.state.dot.tms.utils.Multi
 * @see us.mn.state.dot.tms.utils.MultiAdapter
 * @see us.mn.state.dot.tms.utils.MultiString
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

	/** Character height (pixels) for character- or line-matrix signs.
	 * Use 0 for full-matrix signs. */
	private final int c_height;

	/** Default font number */
	private final int default_font;

	/** Color scheme */
	private final ColorScheme scheme;

	/**
	 * Create a new raster builder.
	 * @param w Sign width in pixels.
	 * @param h Sign height in pixels.
	 * @param cw Character width (pixels) for character-matrix signs.
	 *           Use 0 for line-matrix or full-matrix signs.
	 * @param ch Character height (pixels) for character- or line-matrix
	 *           signs.  Use 0 for full-matrix signs.
	 * @param df Default font number.
	 * @param cs Color scheme.
	 */
	public RasterBuilder(int w, int h, int cw, int ch, int df,
		ColorScheme cs)
	{
		width = w;
		height = h;
		c_width = cw;
		c_height = ch;
		default_font = df;
		scheme = cs;
	}

	/** Get the optimal line height (pixels) */
	public int getLineHeightPixels() {
		if (c_height > 0)
			return c_height;
		Font f = FontHelper.find(default_font);
		return (f != null) ? f.getHeight() : 8;
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
		for (int lines = 1; lines < SignMessage.MAX_LINES; lines++) {
			if (lh * (lines + 1) + ls * lines > height)
				return lines;
		}
		return SignMessage.MAX_LINES;
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
			default_font, scheme);
		MultiString multi = DMSHelper.ignoreFilter(ms);
		multi.parse(mr);
		mr.complete();
		MultiSyntaxError err = mr.getSyntaxError();
		if (err != MultiSyntaxError.none) {
			throw new InvalidMsgException(err.toString() +
				": \"" + ms + '"');
		}
	}

	/** Create raster graphics from a multi string.
	 * @return Array of RasterGraphic, or null on error. */
	public RasterGraphic[] createRasters(String multi) {
		try {
			return createPixmaps(new MultiString(multi));
		}
		catch (IndexOutOfBoundsException e) {
			// dimensions too small for message
			return null;
		}
		catch (InvalidMsgException e) {
			// most likely a MultiSyntaxError ...
			return null;
		}
	}

	/** Try to make a combined message.
	 * @param first MULTI string of first message.
	 * @param second MULTI string of second message.
	 * @return Combined message, or null on error. */
	public String combineMulti(String first, String second) {
		if (first != null || second != null)
			return makeCombined(first, second);
		else
			return null;
	}

	/** Make a combined message (either sequenced or shared) */
	private String makeCombined(String first, String second) {
		MultiString ms1 = new MultiString(first);
		MultiString ms2 = new MultiString(second);
		if (ms1.isBlank() || ms2.isBlank())
			return null;
		if (canCombineSequence(first)) {
			// First message ends with new page tag
			MultiBuilder mb = new MultiBuilder(first);
			mb.addPage();
			// Reset some MULTI to default values
			// NOTE: [cf] already reset at end of first msg
			//       Don't need mb.setColorForeground(null);
			mb.setFont(null, null);
			mb.setJustificationPage(null);
			mb.setJustificationLine(null);
			// Add second message
			mb.append(ms2);
			return mb.toString();
		}
		if (canCombineShared(ms1, ms2)) {
			// Insert first message before each page of second
			MultiBuilder mb = new MultiBuilder() {
				@Override
				public void addPage() {
					super.addPage();
					// Reset these to default values
					setColorForeground(null);
					setFont(null, null);
					setJustificationPage(null);
					setJustificationLine(null);
				}
				@Override
				public void setTextRectangle(int x, int y,
					int w, int h)
				{
					// Insert first message, which ends
					// with this text rectangle, so there
					// is no need to repeat it
					append(ms1);
					// Reset these to default values
					setColorForeground(null);
					setFont(null, null);
					setJustificationPage(null);
					setJustificationLine(null);
				}
			};
			ms2.parse(mb);
			return mb.toString();
		}
		return null;
	}

	// Check if messages can be combined in a sequence
	private boolean canCombineSequence(String ms) {
		return ms.endsWith("[cf]");
	}

	/** Check if messages can be combined with shared pages */
	private boolean canCombineShared(MultiString first,
		MultiString second)
	{
		if (first.getNumPages() > 1)
			return false;
		String tr = first.trailingTextRectangle();
		return (tr != null)
			&& second.eachPageStartsWith(tr)
			&& second.hasOneTextRectPerPage();
	}
}
