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
System.err.println("PixelMapBuilder.addText(): bg="+bg+", t="+t+", x="+x+", y="+y);
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
		int x = _calculatePixelX(j, t);
		if(x < 0)
			throw new InvalidMessageException("Message too long");
		else
			return x;
	}

	/** Calculate the X pixel position to place text */
	protected int _calculatePixelX(MultiString.JustificationLine j,
		String t) throws InvalidMessageException
	{
		switch(j) {
		case LEFT:
			return 0;
		case CENTER:
   			// note: round(a/b) = (a+b/2)/b. This eliminates
			// rounding error when dividing by 2.
			// this code has no rounding errors.
			int a = Math.max(0,width - calculateWidth(t));
			return (a+1)/2;

//FIXME: Doug, remove unnecessary comments below

			// original code is below. Note: line 120, dividing by 2 and then multipling by c_width magnifies rounding error.
			// instead, at least, multiply by c_width and then divide by 2, using the above formula (a+1)/2.
			// I wasn't sure if you were trying to force chars into block positions...so I left this code if you were.
			//int w = width / c_width;
			//int r = calculateWidth(t) / c_width;
			//int cx = (w - r) / 2 * c_width;
			//better: int cx = (w - r) * c_width/2;
			//best: int cx = ((w - r) * c_width + 1)/2; // no rounding error
			//return cx;
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
