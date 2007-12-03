/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007  Minnesota Department of Transportation
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
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Base64;
import us.mn.state.dot.tms.BitmapGraphic;
import us.mn.state.dot.tms.Font;
import us.mn.state.dot.tms.Glyph;
import us.mn.state.dot.tms.Graphic;

/**
 * Renderer for font glyphs in a Jlist
 *
 * @author Douglas Lau
 */
public class GlyphCellRenderer extends DefaultListCellRenderer {

	/** Margin reserved for default renderer */
	static protected final int MARGIN = 16;

	/** Hash of characters to bitmap graphics */
	protected final HashMap<String, BitmapGraphic> bitmaps =
		new HashMap<String, BitmapGraphic>();

	/** Create a new glyph cell renderer */
	public GlyphCellRenderer(Font font, TypeCache<Glyph> glyphs,
		TypeCache<Graphic> graphics) throws IOException
	{
		Map<String, Glyph> gmap = glyphs.getAll();
		LinkedList<Glyph> glist = new LinkedList<Glyph>();
		HashMap<Integer, Graphic> cp = new HashMap<Integer, Graphic>();
		synchronized(gmap) {
			for(Glyph g: gmap.values())
				if(g.getFont() == font)
					glist.add(g);
		}
		for(Glyph g: glist) {
			Graphic gr = g.getGraphic();
			BitmapGraphic b = new BitmapGraphic(gr.getWidth(),
				gr.getHeight());
			b.setBitmap(Base64.decode(gr.getPixels()));
			String c = String.valueOf((char)g.getCodePoint());
			bitmaps.put(c, b);
		}
		setBackground(Color.BLACK);
	}

	/** Get a renderer for the specified list value */
	public Component getListCellRendererComponent(JList list, Object value,
		int index, boolean isSelected, boolean cellHasFocus)
	{
		bitmap = bitmaps.get(value.toString());
		return super.getListCellRendererComponent(list, value,
			index, isSelected, cellHasFocus);
	}

	/** Configure the list cell renderer */
	protected void configureRenderer() {
		pitch = calculatePitch();
		left = calculateLeft();
		top = calculateTop();
	}

	/** Bitmap for currently configured glyph */
	protected BitmapGraphic bitmap;

	/** Pitch for currently configured glyph */
	protected float pitch;

	/** Calculate the pitch for the current glyph */
	protected float calculatePitch() {
		float w = getWidth() / bitmap.width;
		float h = getHeight() / bitmap.height;
		return Math.min(w, h);
	}

	/** Left margin for currently configured glyph */
	protected int left;

	/** Calculate the left side of the current glyph */
	protected int calculateLeft() {
		return MARGIN + (int)(getWidth() - MARGIN -
			bitmap.width * pitch) / 2;
	}

	/** Top margin for currently configured glyph */
	protected int top;

	/** Calculate the top of the current glyph */
	protected int calculateTop() {
		return (int)(getHeight() - bitmap.height * pitch) / 2;
	}

	/** Paint the pixels for the current glyph */
	protected void paintPixels(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
			RenderingHints.VALUE_ANTIALIAS_ON);
		Ellipse2D pixel = new Ellipse2D.Float();
		float yy = top;
		for(int y = 0; y < bitmap.height; y++, yy += pitch) {
			float xx = left;
			for(int x = 0; x < bitmap.width; x++, xx += pitch) {
				if(bitmap.getPixel(x, y) > 0)
					g.setColor(Color.YELLOW);
				else
					g.setColor(Color.GRAY);
				pixel.setFrame(xx, yy, pitch, pitch);
				g.fill(pixel);
			}
		}
	}

	/** Paint the currently configured glyph */
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if(bitmap != null) {
			configureRenderer();
			g.setColor(Color.BLACK);
			g.fillRect(left, top, (int)(bitmap.width * pitch),
				(int)(bitmap.height * pitch));
			paintPixels((Graphics2D)g);
		}
	}
}
