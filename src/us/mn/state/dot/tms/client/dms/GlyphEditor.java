/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013-2016  Minnesota Department of Transportation
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
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.JPanel;
import us.mn.state.dot.tms.BitmapGraphic;
import us.mn.state.dot.tms.DmsColor;

/**
 * Glyph editor.
 *
 * @author Douglas Lau
 */
public class GlyphEditor extends JPanel {

	/** Working bitmap graphic */
	private BitmapGraphic bmap = new BitmapGraphic(0, 0);

	/** Current color */
	private DmsColor color = DmsColor.BLACK;

	/** Mouse listener */
	private final MouseListener m_list = new MouseAdapter() {
		@Override public void mousePressed(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1)
				color = DmsColor.AMBER;
			else
				color = DmsColor.BLACK;
			setPixel(e);
		}
	};

	/** Mouse motion listener */
	private final MouseMotionListener mot_list = new MouseMotionListener() {
		public void mouseMoved(MouseEvent e) { }
		public void mouseDragged(MouseEvent e) {
			setPixel(e);
		}
	};

	/** Set one pixel color */
	private void setPixel(MouseEvent e) {
		BitmapGraphic bg = bmap;
		float pitch = calculatePitch(bg);
		int p = (int) pitch;
		if (p > 0) {
			int left = calculateLeft(bg, pitch);
			int top = calculateTop(bg, pitch);
			int x = (e.getX() - left) / p;
			int y = (e.getY() - top) / p;
			if (x >= 0 && x < bg.getWidth() &&
			    y >= 0 && y < bg.getHeight())
			{
				if (bg.getPixel(x, y) != color) {
					bg.setPixel(x, y, color);
					repaint();
				}
			}
		}
	}

	/** Create a new glyph editor */
	public GlyphEditor() {
		addMouseListener(m_list);
		addMouseMotionListener(mot_list);
	}

	/** Dispose of glyph editor */
	public void dispose() {
		removeMouseMotionListener(mot_list);
		removeMouseListener(m_list);
	}

	/** Set glyph bitmap */
	public void setBitmap(BitmapGraphic bg) {
		bmap = bg;
		repaint();
	}

	/** Paint the glyph */
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		paintPixels(g, bmap);
	}

	/** Paint the pixels for the glyph */
	private void paintPixels(Graphics g, BitmapGraphic bg) {
		float pitch = calculatePitch(bg);
		int left = calculateLeft(bg, pitch);
		int top = calculateTop(bg, pitch);
		int yy = top;
		int p = Math.max((int) pitch - 1, 1);
		for (int y = 0; y < bg.getHeight(); y++, yy += pitch) {
			int xx = left;
			for (int x = 0; x < bg.getWidth(); x++, xx += pitch) {
				g.setColor(bg.getPixel(x, y).color);
				g.fillRect(xx, yy, p, p);
			}
		}
	}

	/** Calculate the pitch for the glyph */
	private float calculatePitch(BitmapGraphic bg) {
		if (bg.getHeight() > 0)
			return getHeight() / bg.getHeight();
		else
			return 0;
	}

	/** Calculate the left side of the glyph */
	private int calculateLeft(BitmapGraphic bg, float pitch) {
		return (int) ((getWidth() - bg.getWidth() * pitch) / 2);
	}

	/** Calculate the top of the glyph */
	private int calculateTop(BitmapGraphic bg, float pitch) {
		return (int) ((getHeight() - bg.getHeight() * pitch) / 2);
	}
}
