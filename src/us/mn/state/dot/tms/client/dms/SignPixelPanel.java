/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2011  Minnesota Department of Transportation
 * Copyright (C) 2010  AHMCT, University of California, Davis
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
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import us.mn.state.dot.tms.DmsColor;
import us.mn.state.dot.tms.RasterGraphic;

/**
 * Pixel panel renders a representation of the pixels on a sign.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class SignPixelPanel extends JPanel {

	/** Flag to turn on antialiasing */
	protected final boolean antialias;

	/** Color of sign face */
	protected final Color face_color;

	/** Sign width (mm) */
	protected int width_mm = 0;

	/** Sign height (mm) */
	protected int height_mm = 0;

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
	protected AffineTransform transform;

	/** Raster graphic to paint */
	protected RasterGraphic graphic;

	/** Buffer for screen display */
	protected BufferedImage buffer;

	/** Bloom size relative to pixel size (0 means no blooming) */
	protected float bloom = 0f;

	/** Flag that determines if buffer needs repainting */
	protected boolean dirty = false;

	/** Create a new sign pixel panel */
	public SignPixelPanel(boolean a) {
		this(a, Color.BLACK);
	}

	/** Create a new sign pixel panel */
	public SignPixelPanel(boolean a, Color f) {
		super(true);
		antialias = a;
		face_color = f;
		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				dirty = true;
				repaint();
			}
		});
		setSizes();
	}

	/** Set the physical sign dimensions */
	public void setPhysicalDimensions(int w, int h, int hb, int vb, int hp,
		int vp)
	{
		width_mm = Math.max(0, w);
		height_mm = Math.max(0, h);
		hborder_mm = Math.max(0, hb);
		vborder_mm = Math.max(0, vb);
		hpitch_mm = Math.max(1, hp);
		vpitch_mm = Math.max(1, vp);
		dirty = true;
	}

	/** Set the logical sign dimensions */
	public void setLogicalDimensions(int w, int h, int wc, int hl) {
		width_pix = Math.max(0, w);
		height_pix = Math.max(0, h);
		width_char = Math.max(0, wc);
		height_line = Math.max(0, hl);
		dirty = true;
	}

	/** Clear the pixel panel */
	public void clear() {
		setPhysicalDimensions(0, 0, 0, 0, 1, 1);
		setLogicalDimensions(0, 0, 0, 0);
		dirty = true;
	}

	/** Set the graphic displayed */
	public void setGraphic(RasterGraphic rg) {
		graphic = rg;
		dirty = true;
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
		BufferedImage b = getBufferedImage();
		rescale();
		doPaint(b.createGraphics());
		buffer = b;
	}

	/** Get an appropriate buffered image */
	protected BufferedImage getBufferedImage() {
		BufferedImage b = buffer;	// Avoid NPE race
		int w = getWidth();
		int h = getHeight();
		if(b == null || w  != b.getWidth() || h != b.getHeight()) {
			return new BufferedImage(w, h,
				BufferedImage.TYPE_INT_RGB);
		} else
			return b;
	}

	/** Rescale when the component is resized or the sign changes */
	protected void rescale() {
		float w = width_mm - hborder_mm * 2;
		if(width_pix > 0 && w > 0 && width_pix * hpitch_mm > w)
			hpitch_mm = w / width_pix;
		float h = height_mm - vborder_mm * 2;
		if(height_pix > 0 && h > 0 && height_pix * vpitch_mm > h)
			vpitch_mm = h / height_pix; 

		int wp = getWidth();
		int hp = getHeight();
		if(wp > 0 && hp > 0)
			rescale(wp, hp);
	}

	/** Rescale the component to the specified size */
	protected void rescale(double w, double h) {
		if(width_mm > 0 && height_mm > 0) {
			double sx = w / width_mm;
			double sy = h / height_mm;
			double scale = Math.min(sx, sy);
			double tx = width_mm * (sx - scale) / 2;
			double ty = height_mm * (sy - scale) / 2;
			AffineTransform t = new AffineTransform();
			t.translate(tx, ty);
			t.scale(scale, scale);
			transform = t;
		} else
			transform = null;
	}

	/** Paint the pixel panel onto a graphics context */
	protected void doPaint(Graphics2D g) {
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
		if(transform == null)
			return;
		g.transform(transform);
		g.setColor(face_color);
		g.fillRect(0, 0, width_mm, height_mm);
		if(antialias) {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		}
		if(graphic != null)
			paintPixels(g, graphic);
	}

	/** Paint the pixels of the sign */
	protected void paintPixels(Graphics2D g, RasterGraphic rg) {
		// NOTE: unlit pixels are drawn first to allow blooming to
		//       overdraw for lit pixels
		paintUnlitPixels(g, rg);
		paintLitPixels(g, rg);
	}

	/** Paint the unlit pixels */
	protected void paintUnlitPixels(Graphics2D g, RasterGraphic rg) {
		if(antialias)
			setBloom(0);
		else
			setBloom(1);
		g.setColor(Color.DARK_GRAY);
		int px = Math.round(hpitch_mm + getBloomX());
		int py = Math.round(vpitch_mm + getBloomY());
		for(int y = 0; y < height_pix; y++) {
			int yy = Math.round(getPixelY(y));
			for(int x = 0; x < width_pix; x++) {
				int xx = Math.round(getPixelX(x));
				if(!rg.getPixel(x, y).isLit())
					g.fillOval(xx, yy, px, py);
			}
		}
	}

	/** Paint the lit pixels */
	protected void paintLitPixels(Graphics2D g, RasterGraphic rg) {
		if(antialias)
			setBloom(0.6f);
		else
			setBloom(1);
		int px = Math.round(hpitch_mm + getBloomX());
		int py = Math.round(vpitch_mm + getBloomY());
		for(int y = 0; y < height_pix; y++) {
			int yy = Math.round(getPixelY(y));
			for(int x = 0; x < width_pix; x++) {
				int xx = Math.round(getPixelX(x));
				DmsColor clr = rg.getPixel(x, y);
				if(clr.isLit()) {
					g.setColor(new Color(clr.rgb()));
					g.fillOval(xx, yy, px, py);
				}
			}
		}
	}

	/** Set the bloom factor */
	protected void setBloom(float b) {
		bloom = b;
	}

	/** Get the bloom in the y-direction */
	protected float getBloomY() {
		return vpitch_mm * bloom / 2;
	}

	/** Get the y-distance to the given pixel */
	protected float getPixelY(int y) {
		return vborder_mm + getLineOffset(y) + vpitch_mm * y -
			getBloomY() / 2;
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

	/** Get the bloom in the x-direction */
	protected float getBloomX() {
		return hpitch_mm * bloom / 2;
	}

	/** Get the x-distance to the given pixel */
	protected float getPixelX(int x) {
		return hborder_mm + getCharacterOffset(x) + hpitch_mm * x -
			getBloomX() / 2;
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

	/** Set the panel size */
	private void setSizes() {
		setMinimumSize(calcDimsUsingHeight(54));
		setPreferredSize(calcDimsUsingHeight(108));
		setMaximumSize(calcDimsUsingHeight(108));
	}

	/** Calculate pixel panel dimensions as a function of height */
	private Dimension calcDimsUsingHeight(int height) {
		final double WTH_RATIO = 3.61;
		return new Dimension((int)(height * WTH_RATIO), height);
	}
}
