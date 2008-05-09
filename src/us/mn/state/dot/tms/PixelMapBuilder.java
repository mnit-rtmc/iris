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

	/** Pixel width of characters */
	protected final int c_width;

	/** Font to render text */
	protected final Font font;

	/** Glyph finder */
	protected final GlyphFinder finder;

	/** Mapping of page numbers to pixel maps */
	protected final TreeMap<Integer, BitmapGraphic> pixmaps =
		new TreeMap<Integer, BitmapGraphic>();

	/** Create a new pixel map builder */
	public PixelMapBuilder(int w, int h, int cw, Font f, GlyphFinder gf) {
		width = w;
		height = h;
		c_width = Math.max(1, cw);
		font = f;
		finder = gf;
	}

	/** Glyph finder interface */
	static public interface GlyphFinder {
		Graphic lookupGraphic(int cp) throws InvalidMessageException;
	}

	/** Add a span of text */
	public void addText(int p, int l, MultiString.JustificationLine j,
		String t)
	{
		BitmapGraphic bg = getBitmap(p);
		try {
			int x = calculatePixelX(j, t);
			int y = l * (font.getHeight() + font.getLineSpacing());
			render(bg, t, x, y);
		}
		catch(InvalidMessageException e) {
			log("Missing code point: " + t);
		}
		catch(IOException e) {
			log("Invalid Base64 glyph data");
		}
	}

	/** Get a bitmap graphic for the specified page number */
	protected BitmapGraphic getBitmap(int p) {
		if(pixmaps.containsKey(p))
			return pixmaps.get(p);
		BitmapGraphic g = new BitmapGraphic(width, height);
		pixmaps.put(p, g);
		return g;
	}

	/** Calculate the X pixel position to place text */
	protected int calculatePixelX(MultiString.JustificationLine j,
		String t) throws InvalidMessageException
	{
		switch(j) {
		case LEFT:
			return 0;
		case CENTER:
			int w = width / c_width;
			int r = calculateWidth(t) / c_width;
			return c_width * Math.max(0, (w - r) / 2);
		case RIGHT:
			return width - calculateWidth(t) - 1;
		default:
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
		byte[] bitmap = Base64.decode(g.getPixels());
		BitmapGraphic c = new BitmapGraphic(width, height);
		c.setBitmap(bitmap);
		for(int yy = 0; yy < height; yy++) {
			for(int xx = 0; xx < width; xx++) {
				int p = c.getPixel(xx, yy);
				bg.setPixel(x + xx, y + yy, p);
			}
		}
	}

	/** Log an error message */
	protected void log(String m) {
		System.err.println("PixelMapBuilder:" + m);
	}
}
