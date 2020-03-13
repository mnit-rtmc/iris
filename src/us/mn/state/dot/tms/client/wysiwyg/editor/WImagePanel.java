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
			if (wy.length > 0) {
				if (start)
					return wy[0];
				else
					return wy[1];
			}
		}
		return -1;
	}
	
	/** Set the caret location given the token. */
	public void setCaretLocation(WToken tok) {
		// get coordinates from the token
		int tX = tok.getCoordX();
		int tY = tok.getCoordY();
		int tY2 = tY + tok.getCoordH();
		
		// convert token (sign) coordinates to raster coordinates
		// TODO do we need to move the X position at all??
		int cX = convertSignToWysiwygX(tX, true);
		int cY = convertSignToWysiwygY(tY, true);
		int cY2 = convertSignToWysiwygY(tY2, false);
		
		
		// validate the caret location before setting
		if ((cX >= 0 && cX <= width) && (cY >= 0 && cY <= height)
				&& (cY2 >= 0 && cY2 <= height)) {
			caretX = cX;
			caretY = cY;
			caretH = cY2-cY;

			// assume this means the caret should be displayed
			showCaret();
		} else {
			// if invalid, disable the caret
			System.out.println(String.format(
					"Problem with caret from (%d, %d) to (%d, %d)",
					cX, cY, cX+caretW, cY2));
			hideCaret();
		}
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
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		if (image != null) {
			g.drawImage(image, 0, 0, width, height, null);
			
			// add the cursor to the image if it's enabled
			if (caretOn) {
//				System.out.println(String.format(
//						"Drawing cursor at (%d, %d) w/h (%d, %d)",
//						caretX, caretY, caretW, caretH));
				Color oc = g.getColor();
				// set color to white
				g.setColor(Color.WHITE);
				g.drawRect(caretX, caretY, caretW, caretH);
				g.fillRect(caretX, caretY, caretW, caretH);
				g.setColor(oc);
			}
			
			// TODO draw things with g.draw*
//			g.drawLine(x1, y1, x2, y2);
		}
	}
	
	/** Draw the cursor on the image at the specified point with the given
	 *  blink rate.
	 */
	private void drawCursor(Graphics g) {
		
	}
}
