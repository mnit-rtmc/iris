/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import us.mn.state.dot.tms.BitmapGraphic;

/**
 * Pixel panel renders a representation of the pixels on a sign.
 *
 * @author Douglas Lau
 */
public class SignPixelPanel extends JPanel {

	/** Sign width (mm) */
	protected int width_mm = 1;

	/** Sign height (mm) */
	protected int height_mm = 1;

	/** Width of horizontal border (mm) */
	protected int hborder_mm;

	/** Height of vertical border (mm) */
	protected int vborder_mm;

	/** Width of individual pixels (mm) */
	protected float hpitch_mm = 1;

	/** Height of individual pixels (mm) */
	protected float vpitch_mm = 1;

	/** Sign pixel width */
	protected int width_pix = 0;

	/** Sign pixel height */
	protected int height_pix = 0;

	/** Width of characters (pixels), zero for variable */
	protected int width_char;

	/** Height of lines (pixels), zero for variable */
	protected int height_line;

	/** Transform from user (mm) to screen coordinates */
	protected AffineTransform transform = new AffineTransform();

	/** Bitmap graphic to paint */
	protected BitmapGraphic graphic;

	/** Buffer for screen display */
	protected BufferedImage buffer;

	/** Flag that determines if buffer needs repainting */
	protected boolean dirty = false;

	/** Create a new sign pixel panel */
	public SignPixelPanel() {
		super(true);
		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				rescale();
			}
		});
		setPreferredSize(new Dimension(256, 32));
	}

	/** Rescale when the component is resized or the sign changes */
	protected void rescale() {
		double w = getWidth();
		double h = getHeight();
		if(w > 0 && h > 0)
			rescale(w, h);
	}

	/** Rescale the component to the specified size */
	protected void rescale(double w, double h) {
		buffer = null;
		dirty = true;
		double sx = w / width_mm;
		double sy = h / height_mm;
		double scale = Math.min(sx, sy);
		double tx = width_mm * (sx - scale) / 2;
		double ty = height_mm * (sy - scale) / 2;
		AffineTransform t = new AffineTransform();
		t.translate(tx, ty);
		t.scale(scale, scale);
		transform = t;
		repaint();
	}

	/** Set the physical sign dimensions */
	public void setPhysicalDimensions(int w, int h, int hb, int vb, int hp,
		int vp)
	{
		width_mm = Math.max(1, w);
		height_mm = Math.max(1, h);
		hborder_mm = Math.max(0, hb);
		vborder_mm = Math.max(0, vb);
		hpitch_mm = Math.max(1, hp);
		vpitch_mm = Math.max(1, vp);
	}

	/** Set the logical sign dimensions */
	public void setLogicalDimensions(int w, int h, int wc, int hl) {
		width_pix = Math.max(0, w);
		height_pix = Math.max(0, h);
		width_char = Math.max(0, wc);
		height_line = Math.max(0, hl);
	}

	/** Verify the sign dimensions */
	public void verifyDimensions() {
		float w = width_mm - hborder_mm * 2;
		if(width_pix > 0 && width_pix * hpitch_mm > w)
			hpitch_mm = w / width_pix;
		float h = height_mm - vborder_mm * 2;
		if(height_pix > 0 && height_pix * vpitch_mm > h)
			vpitch_mm = h / height_pix;
		rescale();
	}

	/** Set the bitmap graphic displayed */
	public void setGraphic(BitmapGraphic g) {
		dirty = true;
		graphic = g;
		repaint();
	}

	/** Paint this on the screen */
	public void paintComponent(Graphics g) {
		while(dirty)
			updateBuffer();
		BufferedImage b = buffer;	// Avoid NPE race
		if(b != null)
			g.drawImage(b, 0, 0, this);
	}

	/** Update the screen buffer to reflect current sign state */
	protected void updateBuffer() {
		dirty = false;
		BufferedImage b = buffer;	// Avoid NPE race
		if(b == null) {
			b = new BufferedImage(getWidth(), getHeight(),
				BufferedImage.TYPE_INT_RGB);
			buffer = b;
		}
		doPaint(b.createGraphics());
	}

	/** Paint the pixel panel onto a graphics context */
	protected void doPaint(Graphics2D g) {
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
		g.transform(transform);
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, width_mm, height_mm);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
			RenderingHints.VALUE_ANTIALIAS_ON);
		paintPixels(g, graphic);
	}

	/** Paint the pixels of the sign */
	protected void paintPixels(Graphics2D g, BitmapGraphic b) {
		Ellipse2D pixel = new Ellipse2D.Float();
		for(int y = 0; y < height_pix; y++) {
			float yy = getPixelY(y);
			for(int x = 0; x < width_pix; x++) {
				float xx = getPixelX(x);
				if(b != null && b.getPixel(x, y) > 0)
					g.setColor(Color.YELLOW);
				else
					g.setColor(Color.GRAY);
				pixel.setFrame(xx, yy, hpitch_mm, vpitch_mm);
				g.fill(pixel);
			}
		}
	}

	/** Get the y-distance to the given pixel */
	protected float getPixelY(int y) {
		return vborder_mm + getLineOffset(y) + vpitch_mm * y;
	}

	/** Get the line offset (for line- or character-matrix signs) */
	protected float getLineOffset(int y) {
		if(height_line > 0)
			return calculateLineGap() * (y / height_line);
		else
			return 0;
	}

	/** Calculate the height of the gap between lines (mm) */
	protected float calculateLineGap() {
		assert height_line > 0;
		float excess = height_mm - 2 * vborder_mm -
			height_pix * vpitch_mm;
		int gaps = height_pix / height_line - 1;
		if(excess > 0 && gaps > 0)
			return excess / gaps;
		else
			return 0;
	}

	/** Get the x-distance to the given pixel */
	protected float getPixelX(int x) {
		return hborder_mm + getCharacterOffset(x) + hpitch_mm * x;
	}

	/** Get the character offset (for character-matrix signs only) */
	protected float getCharacterOffset(int x) {
		if(width_char > 0)
			return calculateCharGap() * (x / width_char);
		else
			return 0;
	}

	/** Calculate the width of the gap between characters (mm) */
	protected float calculateCharGap() {
		assert width_char > 0;
		float excess = width_mm - 2 * hborder_mm -
			width_pix * vpitch_mm;
		int gaps = width_pix / width_char - 1;
		if(excess > 0 && gaps > 0)
			return excess / gaps;
		else
			return 0;
	}
}
