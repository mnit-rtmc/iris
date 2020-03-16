/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group
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

package us.mn.state.dot.tms.client.wysiwyg.editor;

import javax.swing.JPanel;

import us.mn.state.dot.tms.InvalidMsgException;
import us.mn.state.dot.tms.utils.wysiwyg.WPage;
import us.mn.state.dot.tms.utils.wysiwyg.WRaster;
import us.mn.state.dot.tms.utils.wysiwyg.WToken;

import static us.mn.state.dot.tms.client.widget.Widgets.UI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Arrays;


/**
 * Panel for displaying a WYSIWYG DMS image.
 *
 * @author Gordon Parikh - SRF Consulting
 */
@SuppressWarnings("serial")

public class WImagePanel extends JPanel {
	
	private int width;
	private int wiWidth;
	private int height;
	private int wiHeight;
	private boolean scaleImg = false;
	private double scale = 0;
	private WPage pg;
	private WRaster wr;
	private BufferedImage image = null;
	
	private boolean caretOn = false;
	private int caretX;
	private int caretY;
	private int caretH;
	private int caretW = 0;
	private Color caretColor = Color.WHITE;
	
	// TODO I don't think we should need this, but for now it makes things look better
	private int offset = 2;
	
	public WImagePanel(int w, int h) {
		width = w;
		height = h;
		wiWidth = w;
		wiHeight = h;
		Dimension d = UI.dimension(width, height);
		setMinimumSize(d);
		setMaximumSize(d);
		setPreferredSize(d);
	}

	// TODO I don't think we should have to do this - we may be able to fix in
	// the renderer
	public WImagePanel(int w, int h, double s) {
		width = w;
		height = h;
		wiWidth = (int) (w/s);
		wiHeight = (int) (h/s);
		Dimension d = UI.dimension(width, height);
		setMinimumSize(d);
		setMaximumSize(d);
		setPreferredSize(d);
		
		// we got a scale factor - scale the image (NOTE only do this for
		// previews)
		scaleImg = true;
		scale = s;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		if (image != null) {
			g.drawImage(image, 0, 0, width, height, null);
			
			// add the caret to the image if it's enabled
			if (caretOn)
				drawCaret(g);
			
			// TODO draw things with g.draw*
//			g.drawLine(x1, y1, x2, y2);
		}
	}
	
	public void setPage(WPage p) {
		pg = p;
		
		// get the raster object, set the image size, and get the image
		wr = pg.getRaster();
		
		if (wr != null) {
			try {
				wr.setWysiwygImageSize(wiWidth, wiHeight);
				image = wr.getWysiwygImage();
				if (wr.isBlank()) {
					System.out.println("POSSIBLE ERROR:  Blank render image");
				}
			} catch (InvalidMsgException e) {
				// TODO do something with this
				image = null;
				e.printStackTrace();
			}
		}
		repaint();
	}
	
	/** Convert the sign-tag coordinate x to WYSIWYG/raster coordinates.
	 *  If start is true, the first matching WYSIWYG coordinate will be
	 *  returned, otherwise the last one will be returned.
	 *  Returns an X coordinate usable for drawing on a graphics context,
	 *  or -1 if there was a problem.
	 */
	private int convertSignToWysiwygX(int x, boolean start) {
		if (wr != null) {
			int[] wx = wr.cvtSignToWysiwygX(x);
			if (wx.length > 0) {
				if (start)
					return wx[0];
				else
					return wx[1];
			}
		}
		return -1;
	}
	
	/** Convert the sign-tag coordinate y to WYSIWYG/raster coordinates.
	 *  If start is true, the first matching WYSIWYG coordinate will be
	 *  returned, otherwise the last one will be returned.
	 *  Returns an Y coordinate usable for drawing on a graphics context,
	 *  or -1 if there was a problem.
	 */
	private int convertSignToWysiwygY(int y, boolean start) {
		if (wr != null) {
			int[] wy = wr.cvtSignToWysiwygY(y);
			System.out.println(Integer.toString(y) + " -> " + Arrays.toString(wy));
			if (wy.length > 0) {
				if (start)
					return wy[0];
				else
					return wy[1];
			}
		}
		return -1;
	}

	/** Clip the given x coordinate to the drawing area [0 width-1] */
	public int clipX(int x) {
		if (x < 0)
			return 0;
		else if (x >= width)
			return width-1;
		return x;
	}
	
	/** Clip the given y coordinate to the drawing area [0 height-1] */
	public int clipY(int y) {
		if (y < 0)
			return 0;
		else if (y >= height)
			return height-1;
		return y;
	}
	
	/** Set the caret location given the token. */
	public void setCaretLocation(WToken tok) {
		// get coordinates from the token
		// TODO -2 is offset to make things look OK for now, but I think
		// there's a bug to be fixed...
		int tX = tok.getCoordX() - offset;
		int tY = tok.getCoordY() - offset;
		int tY2 = tY + tok.getCoordH();
		
		// convert token (sign) coordinates to raster coordinates and clip any
		// overrun
		// TODO we need to adjust the position some, but not sure how...
		caretX = clipX(convertSignToWysiwygX(tX, true));
		caretY = clipY(convertSignToWysiwygY(tY, true));
		int cY2 = clipY(convertSignToWysiwygY(tY2, false));
		caretH = cY2-caretY;
//		System.out.println(String.format(
//				"Drawing caret at (%d, %d) w/h (%d, %d); tY2 = %d -> cY2 = %d",
//				caretX, caretY, caretW, caretH, tY2, cY2));
		
		// set the caret to enabled and repaint everything
		showCaret();
		repaint();
	}
	
	/** Show the caret on the panel (must have a valid location too). */
	public void showCaret() {
		caretOn = true;
	}
	
	/** Hide the caret on the panel. */
	public void hideCaret() {
		caretOn = false;
	}
	
	/** Draw the caret on the image at/with the caretX/Y/W/H/color and TODO
	 *  blink rate values. 
	 */
	private void drawCaret(Graphics g) {
		// get the original color so we can reset the color
		Color oc = g.getColor();
		
		// set color to to the cursor color and draw a rectangle
		g.setColor(Color.WHITE);
//		g.drawArc(caretX, caretY, 10, 10, 0, 360);
		g.drawRect(caretX, caretY, caretW, caretH);
		g.fillRect(caretX, caretY, caretW, caretH);
		
		// return the color
		g.setColor(oc);
	}
}
