/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2013  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DmsColor;
import us.mn.state.dot.tms.RasterGraphic;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;

/**
 * Pixel panel renders a representation of the pixels on a sign.
 *
 * @author Douglas Lau
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
	protected int hpitch_mm = 1;

	/** Height of individual pixels (mm) */
	protected int vpitch_mm = 1;

	/** Sign pixel width */
	protected int width_pix = 0;

	/** Sign pixel height */
	protected int height_pix = 0;

	/** Width of characters (pixels), zero for variable */
	protected int width_char;

	/** Height of lines (pixels), zero for variable */
	protected int height_line;

	/** Transform from user (mm) to screen coordinates */
	private AffineTransform transform;

	/** Raster graphic to paint */
	private RasterGraphic graphic;

	/** Buffer for screen display */
	private BufferedImage buffer;

	/** Bloom size relative to pixel size (0 means no blooming) */
	protected float bloom = 0f;

	/** Flag that determines if buffer needs repainting */
	protected boolean dirty = false;

	/** Create a new sign pixel panel.
	 * @param h Height of panel.
	 * @param w Width of panel.
	 * @param a If true, render with antialiasing. */
	public SignPixelPanel(int h, int w, boolean a) {
		this(h, w, a, Color.BLACK);
	}

	/** Create a new sign pixel panel.
	 * @param h Height of panel.
	 * @param w Width of panel.
	 * @param a If true, render with antialiasing.
	 * @param f Face color of sign. */
	public SignPixelPanel(int h, int w, boolean a, Color f) {
		super(true);
		antialias = a;
		face_color = f;
		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				dirty = true;
				repaint();
			}
		});
		setSizes(h, w);
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
			updateBuffer(graphic);
		BufferedImage b = buffer;	// Avoid NPE race
		if(b != null)
			g.drawImage(b, 0, 0, this);
	}

	/** Update the screen buffer to reflect current sign state */
	private void updateBuffer(RasterGraphic rg) {
		dirty = false;
		BufferedImage b = getBufferedImage();
		rescale();
		doPaint(b.createGraphics(), rg);
		buffer = b;
	}

	/** Get an appropriate buffered image */
	private BufferedImage getBufferedImage() {
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
		int wp = getWidth();
		int hp = getHeight();
		if(wp > 0 && hp > 0)
			rescale(wp, hp);
	}

	/** Rescale the component to the specified size */
	protected void rescale(double w, double h) {
		int w_mm = width_mm;
		int h_mm = height_mm;
		if(w_mm > 0 && h_mm > 0) {
			double sx = w / w_mm;
			double sy = h / h_mm;
			double scale = Math.min(sx, sy);
			double tx = w_mm * (sx - scale) / 2;
			double ty = h_mm * (sy - scale) / 2;
			AffineTransform t = new AffineTransform();
			t.translate(tx, ty);
			t.scale(scale, scale);
			transform = t;
		} else
			transform = null;
	}

	/** Paint the pixel panel onto a graphics context */
	private void doPaint(Graphics2D g, RasterGraphic rg) {
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
		AffineTransform t = transform;
		if(t != null) {
			g.transform(t);
			g.setColor(face_color);
			g.fillRect(0, 0, width_mm, height_mm);
			if(rg != null)
				paintPixels(g, rg);
		}
	}

	/** Paint the pixels of the sign */
	private void paintPixels(Graphics2D g, RasterGraphic rg) {
		if(antialias) {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		}
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
		int px = Math.round(getHorizontalPitch() + getBloomX());
		int py = Math.round(getVerticalPitch() + getBloomY());
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
		int px = Math.round(getHorizontalPitch() + getBloomX());
		int py = Math.round(getVerticalPitch() + getBloomY());
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

	/** Get the bloom in the x-direction */
	protected float getBloomX() {
		return getHorizontalPitch() * bloom / 2;
	}

	/** Get the x-distance to the given pixel */
	protected float getPixelX(int x) {
		return getHorizontalBorder() + getCharacterOffset(x) +
			getHorizontalPitch() * x - getBloomX() / 2;
	}

	/** Get the character offset (for character-matrix signs only) */
	protected float getCharacterOffset(int x) {
		if(width_char > 0)
			return (x / width_char) * calculateCharGap();
		else
			return 0;
	}

	/** Calculate the width of the gap between characters (mm) */
	protected float calculateCharGap() {
		float excess = width_mm - 2 * getHorizontalBorder() -
			width_pix * getHorizontalPitch();
		int gaps = getCharacterGaps();
		if(excess > 0 && gaps > 0)
			return excess / gaps;
		else
			return 0;
	}

	/** Get the horizontal border (mm).  This does some sanity checks in
	 * case the sign vendor supplies stupid values. */
	protected float getHorizontalBorder() {
		float excess = width_mm - getHorizontalPitch() *
			(width_pix + getCharacterGaps());
		return Math.min(hborder_mm, Math.max(0, excess / 2));
	}

	/** Get the horizontal pitch (mm).  This does some sanity checks in
	 * case the sign vendor supplies stupid values. */
	protected float getHorizontalPitch() {
		float gaps = width_pix + getCharacterGaps();
		float mx = gaps > 0 ? width_mm / gaps : width_mm;
		return Math.min(hpitch_mm, mx);
	}

	/** Get the number of gaps between characters */
	protected int getCharacterGaps() {
		return (width_char > 1 && width_pix > width_char) ?
			width_pix / width_char - 1 : 0;
	}

	/** Get the bloom in the y-direction */
	protected float getBloomY() {
		return getVerticalPitch() * bloom / 2;
	}

	/** Get the y-distance to the given pixel */
	protected float getPixelY(int y) {
		return getVerticalBorder() + getLineOffset(y) +
			getVerticalPitch() * y - getBloomY() / 2;
	}

	/** Get the line offset (for line- or character-matrix signs) */
	protected float getLineOffset(int y) {
		if(height_line > 0)
			return (y / height_line) * calculateLineGap();
		else
			return 0;
	}

	/** Calculate the height of the gap between lines (mm) */
	protected float calculateLineGap() {
		float excess = height_mm - 2 * getVerticalBorder() -
			height_pix * getVerticalPitch();
		int gaps = getLineGaps();
		if(excess > 0 && gaps > 0)
			return excess / gaps;
		else
			return 0;
	}

	/** Get the vertical border (mm).  This does some sanity checks in case
	 * the sign vendor supplies stupid values. */
	protected float getVerticalBorder() {
		float excess = height_mm - getVerticalPitch() *
			(height_pix + getLineGaps());
		return Math.min(vborder_mm, Math.max(0, excess / 2));
	}

	/** Get the vertical pitch (mm).  This does some sanity checks in case
	 * the sign vendor supplies stupid values. */
	protected float getVerticalPitch() {
		float gaps = height_pix + getLineGaps();
		float mx = gaps > 0 ? height_mm / gaps : height_mm;
		return Math.min(vpitch_mm, mx);
	}

	/** Get the number of gaps between lines */
	protected int getLineGaps() {
		return (height_line > 1 && height_pix > height_line) ?
			height_pix / height_line - 1 : 0;
	}

	/** Set the panel size */
	private void setSizes(int height, int width) {
		Dimension d = UI.dimension(width, height);
		setMinimumSize(d);
		setPreferredSize(d);
		setMaximumSize(d);
	}

	/** Set the dimensions from a DMS */
	public void setDimensions(DMS dms) {
		setPhysicalDimensions(dms);
		setLogicalDimensions(dms);
	}

	/** Set the physical dimensions from a DMS */
	private void setPhysicalDimensions(DMS dms) {
		Integer w = dms.getFaceWidth();
		Integer h = dms.getFaceHeight();
		Integer hp = dms.getHorizontalPitch();
		Integer vp = dms.getVerticalPitch();
		Integer hb = dms.getHorizontalBorder();
		Integer vb = dms.getVerticalBorder();
		if(w != null && h != null && hp != null && vp != null &&
		   hb != null && vb != null)
		{
			setPhysicalDimensions(w, h, hb, vb, hp, vp);
		} else
			setPhysicalDimensions(0, 0, 0, 0, 0, 0);
	}

	/** Set the logical dimensions from a DMS */
	private void setLogicalDimensions(DMS dms) {
		Integer wp = dms.getWidthPixels();
		Integer hp = dms.getHeightPixels();
		Integer cw = dms.getCharWidthPixels();
		Integer ch = dms.getCharHeightPixels();
		if(wp != null && hp != null && cw != null && ch != null)
			setLogicalDimensions(wp, hp, cw, ch);
		else
			setLogicalDimensions(0, 0, 0, 0);
	}
}
