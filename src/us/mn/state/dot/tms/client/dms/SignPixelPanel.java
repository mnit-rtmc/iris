/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2017  Minnesota Department of Transportation
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
import java.awt.geom.AffineTransform;
import javax.swing.JPanel;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DmsColor;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.RasterGraphic;
import us.mn.state.dot.tms.SignConfig;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;

/**
 * Pixel panel renders a representation of the pixels on a sign.
 *
 * @author Douglas Lau
 */
public class SignPixelPanel extends JPanel {

	/** Create a filter color */
	static private Color filterColor(Color clr, int alpha) {
		return new Color(clr.getRed(), clr.getGreen(), clr.getBlue(),
			alpha);
	}

	/** Filter color for failed DMS */
	static private final Color FILTER_FAILED = filterColor(Color.GRAY, 192);

	/** Filter color for DMS with controller errors */
	static private final Color FILTER_ERROR = new Color(255, 64, 0, 64);

	/** Get the filter color for a DMS */
	static public Color filterColor(DMS dms) {
		if (DMSHelper.isFailed(dms))
			return FILTER_FAILED;
		else if (DMSHelper.getCriticalError(dms).length() > 0)
			return FILTER_ERROR;
		else
			return null;
	}

	/** Color of sign face */
	private Color face_color;

	/** Color if filter mask */
	private Color filter_color;

	/** Sign width (mm) */
	private int width_mm = 0;

	/** Sign height (mm) */
	private int height_mm = 0;

	/** Width of horizontal border (mm) */
	private int hborder_mm;

	/** Height of vertical border (mm) */
	private int vborder_mm;

	/** Width of individual pixels (mm) */
	private int hpitch_mm = 1;

	/** Height of individual pixels (mm) */
	private int vpitch_mm = 1;

	/** Sign pixel width */
	private int width_pix = 0;

	/** Sign pixel height */
	private int height_pix = 0;

	/** Width of characters (pixels), zero for variable */
	private int width_char;

	/** Height of lines (pixels), zero for variable */
	private int height_line;

	/** Transform from user (mm) to screen coordinates */
	private AffineTransform transform;

	/** Raster graphic to paint */
	private RasterGraphic graphic;

	/** Bloom size relative to pixel size (0 means no blooming) */
	private float bloom = 0f;

	/** Create a new sign pixel panel.
	 * @param h Height of panel.
	 * @param w Width of panel.
	 * @param a If true, render with antialiasing. */
	public SignPixelPanel(int h, int w, boolean a) {
		super(true);
		face_color = Color.BLACK;
		setSizes(h, w);
	}

	/** Set the sign face color.
	 * @param fc Face color of sign. */
	public void setFaceColor(Color fc) {
		face_color = fc;
		repaint();
	}

	/** Set the sign filter color.
	 * @param fc Filter color of sign. */
	public void setFilterColor(Color fc) {
		filter_color = fc;
		repaint();
	}

	/** Set the panel size */
	private void setSizes(int height, int width) {
		Dimension d = UI.dimension(width, height);
		setMinimumSize(d);
		setPreferredSize(d);
	}

	/** Clear the pixel panel */
	public void clear() {
		setPhysicalDimensions(0, 0, 0, 0, 1, 1);
		setLogicalDimensions(0, 0, 0, 0);
		repaint();
	}

	/** Set the graphic displayed */
	public void setGraphic(RasterGraphic rg) {
		graphic = rg;
		repaint();
	}

	/** Paint this on the screen */
	@Override
	public void paintComponent(Graphics g) {
		rescale();
		doPaint((Graphics2D) g, graphic);
	}

	/** Rescale when the component is resized or the sign changes */
	private void rescale() {
		int wp = getWidth();
		int hp = getHeight();
		if (wp > 0 && hp > 0)
			rescale(wp, hp);
	}

	/** Rescale the component to the specified size */
	private void rescale(double w, double h) {
		int w_mm = width_mm;
		int h_mm = height_mm;
		if (w_mm > 0 && h_mm > 0) {
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
		if (t != null) {
			g.transform(t);
			g.setColor(face_color);
			g.fillRect(0, 0, width_mm, height_mm);
			if (rg != null)
				paintPixels(g, rg);
			Color fc = filter_color;
			if (fc != null) {
				g.setColor(fc);
				g.fillRect(0, 0, width_mm, height_mm);
			}
		}
	}

	/** Paint the pixels of the sign */
	private void paintPixels(Graphics2D g, RasterGraphic rg) {
		// NOTE: unlit pixels are drawn first to allow blooming to
		//       overdraw for lit pixels
		paintUnlitPixels(g, rg);
		paintLitPixels(g, rg);
	}

	/** Paint the unlit pixels */
	private void paintUnlitPixels(Graphics2D g, RasterGraphic rg) {
		setBloom(0);
		g.setColor(Color.DARK_GRAY);
		int px = Math.round(getHorizontalPitch() + getBloomX());
		int py = Math.round(getVerticalPitch() + getBloomY());
		for (int y = 0; y < height_pix; y++) {
			int yy = Math.round(getPixelY(y));
			for (int x = 0; x < width_pix; x++) {
				int xx = Math.round(getPixelX(x));
				if (!rg.getPixel(x, y).isLit())
					g.fillRect(xx, yy, px, py);
			}
		}
	}

	/** Paint the lit pixels */
	private void paintLitPixels(Graphics2D g, RasterGraphic rg) {
		setBloom(1);
		int px = Math.round(getHorizontalPitch() + getBloomX());
		int py = Math.round(getVerticalPitch() + getBloomY());
		for (int y = 0; y < height_pix; y++) {
			int yy = Math.round(getPixelY(y));
			for (int x = 0; x < width_pix; x++) {
				int xx = Math.round(getPixelX(x));
				DmsColor clr = rg.getPixel(x, y);
				if (clr.isLit()) {
					g.setColor(clr.color);
					g.fillOval(xx, yy, px, py);
				}
			}
		}
	}

	/** Set the bloom factor */
	private void setBloom(float b) {
		bloom = b;
	}

	/** Get the bloom in the x-direction */
	private float getBloomX() {
		return getHorizontalPitch() * bloom / 2;
	}

	/** Get the x-distance to the given pixel */
	private float getPixelX(int x) {
		return getHorizontalBorder() + getCharacterOffset(x) +
			getHorizontalPitch() * x - getBloomX() / 2;
	}

	/** Get the character offset (for character-matrix signs only) */
	private float getCharacterOffset(int x) {
		if (width_char > 0)
			return (x / width_char) * calculateCharGap();
		else
			return 0;
	}

	/** Calculate the width of the gap between characters (mm) */
	private float calculateCharGap() {
		float excess = width_mm - 2 * getHorizontalBorder() -
			width_pix * getHorizontalPitch();
		int gaps = getCharacterGaps();
		if (excess > 0 && gaps > 0)
			return excess / gaps;
		else
			return 0;
	}

	/** Get the horizontal border (mm).  This does some sanity checks in
	 * case the sign vendor supplies stupid values. */
	private float getHorizontalBorder() {
		float excess = width_mm - getHorizontalPitch() *
			(width_pix + getCharacterGaps());
		return Math.min(hborder_mm, Math.max(0, excess / 2));
	}

	/** Get the horizontal pitch (mm).  This does some sanity checks in
	 * case the sign vendor supplies stupid values. */
	private float getHorizontalPitch() {
		float gaps = width_pix + getCharacterGaps();
		float mx = gaps > 0 ? width_mm / gaps : width_mm;
		return Math.min(hpitch_mm, mx);
	}

	/** Get the number of gaps between characters */
	private int getCharacterGaps() {
		return (width_char > 1 && width_pix > width_char) ?
			width_pix / width_char - 1 : 0;
	}

	/** Get the bloom in the y-direction */
	private float getBloomY() {
		return getVerticalPitch() * bloom / 2;
	}

	/** Get the y-distance to the given pixel */
	private float getPixelY(int y) {
		return getVerticalBorder() + getLineOffset(y) +
			getVerticalPitch() * y - getBloomY() / 2;
	}

	/** Get the line offset (for line- or character-matrix signs) */
	private float getLineOffset(int y) {
		if (height_line > 0)
			return (y / height_line) * calculateLineGap();
		else
			return 0;
	}

	/** Calculate the height of the gap between lines (mm) */
	private float calculateLineGap() {
		float excess = height_mm - 2 * getVerticalBorder() -
			height_pix * getVerticalPitch();
		int gaps = getLineGaps();
		if (excess > 0 && gaps > 0)
			return excess / gaps;
		else
			return 0;
	}

	/** Get the vertical border (mm).  This does some sanity checks in case
	 * the sign vendor supplies stupid values. */
	private float getVerticalBorder() {
		float excess = height_mm - getVerticalPitch() *
			(height_pix + getLineGaps());
		return Math.min(vborder_mm, Math.max(0, excess / 2));
	}

	/** Get the vertical pitch (mm).  This does some sanity checks in case
	 * the sign vendor supplies stupid values. */
	private float getVerticalPitch() {
		float gaps = height_pix + getLineGaps();
		float mx = gaps > 0 ? height_mm / gaps : height_mm;
		return Math.min(vpitch_mm, mx);
	}

	/** Get the number of gaps between lines */
	private int getLineGaps() {
		return (height_line > 1 && height_pix > height_line) ?
			height_pix / height_line - 1 : 0;
	}

	/** Set the dimensions from a DMS */
	public void setDimensions(DMS dms) {
		setPhysicalDimensions(dms);
		setLogicalDimensions(dms);
	}

	/** Set the physical dimensions from a DMS */
	private void setPhysicalDimensions(DMS dms) {
		SignConfig sc = dms.getSignConfig();
		if (sc != null) {
			int w = sc.getFaceWidth();
			int h = sc.getFaceHeight();
			int ph = sc.getPitchHoriz();
			int pv = sc.getPitchVert();
			int bh = sc.getBorderHoriz();
			int bv = sc.getBorderVert();
			setPhysicalDimensions(w, h, bh, bv, ph, pv);
		} else
			setPhysicalDimensions(0, 0, 0, 0, 0, 0);
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
	}

	/** Set the logical dimensions from a DMS */
	private void setLogicalDimensions(DMS dms) {
		SignConfig sc = dms.getSignConfig();
		if (sc != null) {
			int pw = sc.getPixelWidth();
			int ph = sc.getPixelHeight();
			int cw = sc.getCharWidth();
			int ch = sc.getCharHeight();
			setLogicalDimensions(pw, ph, cw, ch);
		} else
			setLogicalDimensions(0, 0, 0, 0);
	}

	/** Set the logical sign dimensions */
	public void setLogicalDimensions(int w, int h, int wc, int hl) {
		width_pix = Math.max(0, w);
		height_pix = Math.max(0, h);
		width_char = Math.max(0, wc);
		height_line = Math.max(0, hl);
	}
}
