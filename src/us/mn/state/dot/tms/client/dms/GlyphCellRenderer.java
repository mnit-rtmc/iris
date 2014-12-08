/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2014  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.dms;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.util.HashMap;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import us.mn.state.dot.tms.BitmapGraphic;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;

/**
 * Renderer for font glyphs in a Jlist
 *
 * @author Douglas Lau
 */
public class GlyphCellRenderer extends DefaultListCellRenderer {

	/** Margin reserved for default renderer */
	static private final int MARGIN = UI.scaled(12);

	/** Hash of code points to bitmap graphics */
	private final HashMap<Integer, BitmapGraphic> bitmaps =
		new HashMap<Integer, BitmapGraphic>();

	/** Set bitmap for one character */
	public void setBitmap(int c, BitmapGraphic bmap) {
		bitmaps.put(c, bmap);
	}

	/** Clear bitmaps for all characters */
	public void clearBitmaps() {
		bitmaps.clear();
	}

	/** Lookup one bitmap */
	private BitmapGraphic lookupBitmap(int c) {
		return bitmaps.get(c);
	}

	/** Get a renderer for the specified list value */
	@Override
	public Component getListCellRendererComponent(JList list, Object value,
		int index, boolean isSelected, boolean cellHasFocus)
	{
		String val = "";
		if (value instanceof Integer) {
			int v = (Integer)value;
			bitmap = lookupBitmap(v);
			val = String.valueOf((char)v);
		} else
			bitmap = null;
		return super.getListCellRendererComponent(list, val, index,
			isSelected, cellHasFocus);
	}

	/** Bitmap for currently configured glyph */
	private BitmapGraphic bitmap;

	/** Pitch for currently configured glyph */
	private float pitch;

	/** Left margin for currently configured glyph */
	private int left;

	/** Top margin for currently configured glyph */
	private int top;

	/** Paint the currently configured glyph */
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		BitmapGraphic bmap = bitmap;
		if (bmap != null) {
			configureRenderer(bmap);
			paintPixels((Graphics2D)g, bmap);
		}
	}

	/** Configure the list cell renderer */
	private void configureRenderer(BitmapGraphic bmap) {
		pitch = calculatePitch(bmap);
		left = calculateLeft(bmap);
		top = calculateTop(bmap);
	}

	/** Calculate the pitch for the current glyph */
	private float calculatePitch(BitmapGraphic bmap) {
		if (bmap.getHeight() > 0)
			return getBitmapHeight() / bmap.getHeight();
		else
			return 0;
	}

	/** Get height of area for bitmap */
	private float getBitmapHeight() {
		int h = getHeight();
		return (h > 2) ? h - 2 : 0;
	}

	/** Calculate the left side of the current glyph */
	private int calculateLeft(BitmapGraphic bmap) {
		return MARGIN + (int)(getBitmapWidth() -
			bmap.getWidth() * pitch) / 2;
	}

	/** Get width of area for bitmap */
	private int getBitmapWidth() {
		return getWidth() - MARGIN;
	}

	/** Calculate the top of the current glyph */
	private int calculateTop(BitmapGraphic bmap) {
		return 1 + (int)((getBitmapHeight() -
			bmap.getHeight() * pitch) / 2);
	}

	/** Paint the pixels for the current glyph */
	private void paintPixels(Graphics2D g, BitmapGraphic bmap) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
			RenderingHints.VALUE_ANTIALIAS_ON);
		Ellipse2D pixel = new Ellipse2D.Float();
		float yy = top;
		for (int y = 0; y < bmap.getHeight(); y++, yy += pitch) {
			float xx = left;
			for (int x = 0; x < bmap.getWidth(); x++, xx += pitch) {
				g.setColor(bmap.getPixel(x, y).color);
				pixel.setFrame(xx, yy, pitch, pitch);
				g.fill(pixel);
			}
		}
	}
}
