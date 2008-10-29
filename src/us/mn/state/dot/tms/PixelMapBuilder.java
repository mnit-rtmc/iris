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

import java.io.IOException;
import java.util.TreeMap;

/**
 * A pixel map builder creates pixel maps for DMS display.
 *
 * @author Douglas Lau
 */
public class PixelMapBuilder implements MultiString.Callback {

	/** Pixel width of sign */
	protected final int width;

	/** Pixel height of sign */
	protected final int height;

	/** Pixel width of a character, where 0 indicates a variable 
	 *  width font, otherwise a fixed character width is assumed. */
	protected final int c_width;

	/** Font to render text */
	protected final Font font;

	/** Glyph finder */
	protected final GlyphFinder finder;

	/** Mapping of page numbers to pixel maps */
	protected final TreeMap<Integer, BitmapGraphic> pixmaps =
		new TreeMap<Integer, BitmapGraphic>();

	/** 
	 * Create a new pixel map builder 
	 * @param w Sign width in pixels.
	 * @param h Sign height in pixels.
	 * @param cw Character width in pixels for fixed width fonts, 
	 *           else 0 for proportional fonts.
	 * @param f Character font.
	 * @param gf GlyphFinder.
	 */
	public PixelMapBuilder(int w, int h, int cw, Font f, GlyphFinder gf) {
		width = w;
		height = h;
		c_width = cw;
		font = f;
		finder = gf;
	}

	/** Glyph finder interface */
	static public interface GlyphFinder {
		Graphic lookupGraphic(int cp) throws InvalidMessageException;
	}

	/** 
	 * Add a span of text.
	 * @param p Page number, zero based.
	 * @param line Line number, zero based.
	 * @param nltp Number of lines of actual text on the page.
	 * @param jl Line justification, e.g. centered, left, right.
	 * @param jp Line justification, e.g. top, middle, bottom.
	 * @param t Text to render.
	 */
	public void addText(int p, int line, int nltp, MultiString.JustificationLine jl,
		MultiString.JustificationPage jp, String t)
	{
		assert p >= 0;
		assert line >= 0;
		assert nltp >= 0;
		BitmapGraphic bg = getBitmap(p);
		try {
			int x = calculatePixelX(jl, t);
			int y = calculatePixelY(jp, t, line, nltp);
			render(bg, t, x, y);
		}
		catch(IndexOutOfBoundsException e) {
			log("Message text too long: " + t);
		}
		catch(InvalidMessageException e) {
			log(e.getMessage() + ": " + t);
		}
		catch(IOException e) {
			log("Invalid Base64 glyph data");
		}
	}

	/** Get a bitmap graphic for the specified page number */
	protected BitmapGraphic getBitmap(int p) {
		assert p >= 0;
		if(pixmaps.containsKey(p))
			return pixmaps.get(p);
		BitmapGraphic g = new BitmapGraphic(width, height);
		pixmaps.put(p, g);
		return g;
	}

	/** Calculate the X pixel position to place text */
	protected int calculatePixelX(MultiString.JustificationLine jl,
		String t) throws InvalidMessageException
	{
		int x = _calculatePixelX(jl, t);
		if(x < 0)
			throw new InvalidMessageException("Message too long");
		else
			return x;
	}

	/** Calculate the Y pixel position to place text.
	 *  @param jp Line justification, e.g. top, middle, bottom.
	 *  @param t Text to render.
	 *  @param line Line number, zero based.
	 *  @param nltp Number of lines of actual text on the page.
	 */
	protected int calculatePixelY(MultiString.JustificationPage jp,
		String t, int line, int nltp) throws InvalidMessageException
	{
		int y = _calculatePixelY(jp, t, line, nltp);
		if(y < 0)
			throw new InvalidMessageException("Too many lines in message");
		else
			return y;
	}

	/** Calculate the X pixel position to place text */
	protected int _calculatePixelX(MultiString.JustificationLine jl,
		String t) throws InvalidMessageException
	{
		switch(jl) {
		case UNDEFINED:
			return 0;
		case OTHER:
			return 0;
		case LEFT:
			return 0;
		case CENTER:
			// determine centering mode: block or bit oriented.
			final int pseudo_c_width = (c_width<=0 ? 1 : c_width);
			final int w = width / pseudo_c_width;
			final int r = calculateWidth(t) / pseudo_c_width;
			return (w - r) / 2 * pseudo_c_width;
		case RIGHT:
			return width - calculateWidth(t) - 1;
		case FULL:
			return 0;
		default:
			assert false;
			return 0;
		}
	}

	/** Calculate the Y pixel position to place text 
	 *  @param jp Line justification, e.g. top, middle, bottom.
	 *  @param t Text to render.
	 *  @param line Line number, zero based.
	 *  @param nltp Number of lines of actual text on the page.
	 */
	protected int _calculatePixelY(MultiString.JustificationPage jp,
		String t, int line, int nltp) throws InvalidMessageException
	{
		switch(jp) {
		// everything is top justified except for MIDDLE
		case UNDEFINED:
		case OTHER:
		case TOP:
		case BOTTOM:
			// top justified
			return line * (font.getHeight() + font.getLineSpacing());
		case MIDDLE:
			final int lineHeight = 
				font.getHeight() + font.getLineSpacing();
			final double linesPerPage = 
				(double)height / (double)lineHeight;
			final double vertBorder = Math.max((linesPerPage - 
				(double)nltp) / 2, 0);
			final double y = (vertBorder+line) * lineHeight;
			final int ret = (int)Math.round(y);
			//System.err.println("_calculatePixelY(): ret="+ret+", vertBorder="+vertBorder+", height="+height+
			//",linesPerPage="+linesPerPage+", t="+t+", line="+line+", nltp="+nltp+", lineHeight="+lineHeight);
			return ret;
		default:
			assert false;
			return 0;
		}
	}

	/** Calculate the width of a span of text */
	protected int calculateWidth(String t) throws InvalidMessageException {
		int w = 0;
		for(int i = 0; i < t.length(); i++) {
			if(i > 0)
				w += font.getCharSpacing();
			int cp = t.charAt(i);
			Graphic c = finder.lookupGraphic(cp);
			w += c.getWidth();
		}
		return w;
	}

	/** 
	 * Render text onto a bitmap graphic.
	 *
	 * @param bg BitmapGraphic to render into.
	 * @param t String to render
	 * @param x Horizontal position to start rendering
	 * @param y Vertical position to strat rendering
	 *
	 * @throws InvalidMessageException if the message contains chars that
	 *                                 don't exist.
	 * @throws IOException If a Base64 decoding error on Graphic.
	 */
	protected void render(BitmapGraphic bg, String t, int x, int y)
		throws InvalidMessageException, IOException
	{
		for(int i = 0; i < t.length(); i++) {
			int cp = t.charAt(i);
			Graphic g = finder.lookupGraphic(cp);
			render(bg, g, x, y);
			x += g.getWidth() + font.getCharSpacing();
		}
	}

	/** Render a graphic onto a bitmap graphic */
	protected void render(BitmapGraphic bg, Graphic g, int x, int y)
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

	/** Log an error message */
	protected void log(String m) {
		System.err.println("PixelMapBuilder:" + m);
	}

	/** Get the pixmap graphics */
	public TreeMap<Integer, BitmapGraphic> getPixmaps() {
		return pixmaps;
	}
}
