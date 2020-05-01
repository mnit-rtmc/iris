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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

import us.mn.state.dot.tms.InvalidMsgException;
import us.mn.state.dot.tms.utils.wysiwyg.WPage;
import us.mn.state.dot.tms.utils.wysiwyg.WRaster;
import us.mn.state.dot.tms.utils.wysiwyg.WToken;
import us.mn.state.dot.tms.utils.wysiwyg.WTokenList;
import us.mn.state.dot.tms.utils.wysiwyg.WTokenType;
import us.mn.state.dot.tms.utils.wysiwyg.WgRectangle;
import us.mn.state.dot.tms.utils.wysiwyg.token.WtGraphic;
import us.mn.state.dot.tms.utils.wysiwyg.token.WtNewLine;
import us.mn.state.dot.tms.utils.wysiwyg.token.Wt_Rectangle;

import static us.mn.state.dot.tms.client.widget.Widgets.UI;

/**
 * Panel for displaying a WYSIWYG DMS image.
 *
 * @author Gordon Parikh - SRF Consulting
 */
@SuppressWarnings("serial")

public class WImagePanel extends JPanel {
	
	private WController controller;
	
	private int width;
	private int height;
	private WPage pg;
	private WRaster wr;
	private boolean preview = false;
	private BufferedImage image = null;
	
	private boolean caretOn = false;
	private int caretX;
	private int caretY;
	private int caretH;
	private int caretW = 1;
	private final static Color caretColor = Color.WHITE;
	
	/** For working with selection drawing */
	private boolean selectionOn = false;
	private ArrayList<Rectangle> selectRects = new ArrayList<Rectangle>();
	
	/** For working with text/color rectangle drawing */
	private boolean tcgRectsOn = false;
	private ArrayList<Rectangle> tcgRects = new ArrayList<Rectangle>();
	private final static Color tcrColor = Color.LIGHT_GRAY;
	private Rectangle selectedRegion;
	private ArrayList<Rectangle> srHandles = new ArrayList<Rectangle>();
	
	/** For feedback when creating new text/color rectangles */
	private Rectangle newRectInProgress;
	
	/** For drawing dashed lines */
	private final static float dashA[] = {10.0f};
    private final static BasicStroke dashed = new BasicStroke(1.0f,
    		BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dashA, 0.0f);
	
	// constants used for converting sign coordinates to WYSIWYG coordinates
    // (for readability only)
    private final static boolean PIX_START = true;
    private final static boolean PIX_END = false;
    private final static boolean LED_SEP = true;
    private final static boolean LED = false;
	
	public WImagePanel(WController c, int w, int h) {
		controller = c;
		width = w;
		height = h;
		init();
		controller.setSignPanel(this);
	}
	
	public WImagePanel(int w, int h, boolean usePreviewImg) {
		width = w;
		height = h;
		preview = usePreviewImg;
		init();
	}
	
	private void init() {
		setLayout(new FlowLayout(FlowLayout.CENTER));
		Dimension d = new Dimension(width, height);
		setPreferredSize(d);
		addComponentListener(new WImagePanelComponentListener());
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		if (image != null) {
			g.drawImage(image, 0, 0, width, height, null);
			
			// add the caret to the image if it's enabled
			if (caretOn)
				drawCaret(g);
			
			// add a text selection if something is selected
			if (selectionOn)
				drawTextSelection(g);
			
			// same with text rectangles
			if (tcgRectsOn)
				drawRectangles(g);
		}
	}
	
	public void setPage(WPage p) {
		pg = p;
		
		// get the raster object, set the image size, and get the image
		wr = pg.getRaster();
		
		if (wr != null) {
			try {
				if (!preview) {
					wr.setWysiwygImageSize(width, height);
					image = wr.getWysiwygImage();
				} else {
					image = wr.getPreviewImage();
				}
				
			} catch (InvalidMsgException e) {
				image = null;
				e.printStackTrace();
			}
		}
		repaint();
	}
	
	/** Initialize the raster provided with the sign panel's dimensions. */
	public void initRaster(WRaster r) {
		if (r != null) {
			try {
				r.setWysiwygImageSize(width, height);
			} catch (InvalidMsgException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void setImageSize(int w, int h) {
		width = w;
		height = h;
		setPreferredSize(new Dimension(width, height));
		
		if (controller != null) {
			controller.update();
			controller.updateCaret();
		}
		
		repaint();
	}
	
	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
	
	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WRaster#cvtSignToWysiwygX(int, boolean, boolean)
	 */
	private int convertSignToWysiwygX(int x, boolean first, boolean sep) {
		if (wr != null) {
			if (x > wr.getWidth())
				x = wr.getWidth();
			return wr.cvtSignToWysiwygX(x, first, sep);
		}
		return -1;
	}

	/* Shortcut method to convert sign X coordinate to WYSIWYG coordinates
	 * and return the separator coordinates (which often looks better when
	 * drawing).
	 */
	private int convertSignToWysiwygX(int x, boolean first) {
		return convertSignToWysiwygX(x, first, LED_SEP);
	}
	
	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WRaster#cvtSignToWysiwygY(int, boolean, boolean)
	 */
	private int convertSignToWysiwygY(int y, boolean first, boolean sep) {
		if (wr != null) {
			if (y > wr.getHeight())
				y = wr.getHeight();
			return wr.cvtSignToWysiwygY(y, first, sep);
		}
		return -1;
	}
	
	/* Shortcut method to convert sign X coordinate to WYSIWYG coordinates
	 * and return the separator coordinates (which often looks better when
	 * drawing).
	 */
	private int convertSignToWysiwygY(int y, boolean first) {
		return convertSignToWysiwygY(y, first, LED_SEP);
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
	
	/** Get a Rectangle in WYSIWYG coordinates from a token (generally a text/
	 *  color rectangle token or graphic token).
	 */
	private Rectangle getRectangleFromToken(WToken rTok) {
		if (rTok != null) {
			// convert from sign coordinates to image coordinates
			int tx = rTok.getParamX();
			int x = clipX(convertSignToWysiwygX(tx, PIX_START));
			int x2 = clipX(convertSignToWysiwygX(
					tx+rTok.getParamW()-1, PIX_END));
			
			int ty = rTok.getParamY();
			int y = clipX(convertSignToWysiwygY(ty, PIX_START));
			int y2 = clipY(convertSignToWysiwygY(
					ty+rTok.getParamH()-1, PIX_END));
			
			// store the drawing coordinates in a rectangle
			return new Rectangle(x, y, x2 - x, y2 - y);
		}
		return null;
	}
	
	/** Set the caret location given the token, placing it on the left. */
	public void setCaretLocation(WToken tok) {
		setCaretLocation(tok, false);
	}
	
	/** Set the caret location given the token. If toRight is true, it will be
	 *  placed to the right of this token, otherwise it will be placed on the
	 *  left.
	 */
	public void setCaretLocation(WToken tok, boolean toRight) {
		// get coordinates from the token and set the caret to appear there
		try {
			int y = tok.getCoordY();
			int h = tok.getCoordH();
			int x = tok.getCoordX();
			if (toRight)
				x += tok.getCoordW();
			setCaretLocation(x, y, h);
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}
	
	/** Set the caret location given the x,y coordinates and height h (all in
	 *  sign coordinates).
	 */
	public void setCaretLocation(int sx, int sy, int h) {
		// convert from sign to raster coordinates and clip any overrun
		caretX = clipX(convertSignToWysiwygX(sx, PIX_START));
		caretY = clipY(convertSignToWysiwygY(sy, PIX_START));
		int cY2 = clipY(convertSignToWysiwygY(sy+h-1, PIX_END));
		caretH = cY2-caretY;
		
//		System.out.println(String.format(
//				"Caret sign at (%d, %d) H = %d", sx, sy, h));
//		System.out.println(String.format(
//				"Caret img at (%d, %d) H = %d (cY2 = %d)",
//				caretX, caretY, caretH, cY2));
		
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
		repaint();
	}
	
	/** Draw the caret on the image at/with the caretX/Y/W/H/color */
	private void drawCaret(Graphics g) {
		// get the original color so we can reset the color
		Color oc = g.getColor();
		
		// set color to to the cursor color and draw a rectangle
		g.setColor(Color.WHITE);
		g.drawRect(caretX, caretY, caretW, caretH);
		g.fillRect(caretX, caretY, caretW, caretH);
		
		// return the color
		g.setColor(oc);
	}
	
	/** Set the text selection based on the list of tokens selected. */
	public void setTextSelection(WTokenList tokensSelected) {
		// first turn on selection feedback
		selectionOn = true;
		
		// reset the selection rectangles
		selectRects.clear();
		
		// now go through the tokens to make rectangles - we may need more
		// than one if the selection spans multiple lines
		int rx = -1, ry = -1;
		int rx2 = -1, ry2 = -1;
		for (WToken tok : tokensSelected) {
			// get the token X coordinates in image space (Y is less important)
			int tx = tok.getCoordX();
			int x = clipX(convertSignToWysiwygX(tx, PIX_START));
			int x2 = clipX(convertSignToWysiwygX(tx+tok.getCoordW()-1, PIX_END));
			
			// initialize the rectangle if it hasn't been started
			if (rx == -1 && ry == -1) {
				int ty = tok.getCoordY();
				int y = clipX(convertSignToWysiwygY(ty, PIX_START));
				int y2 = clipY(convertSignToWysiwygY(ty+tok.getCoordH()-1, PIX_END));
				
				rx = x;
				ry = y;
				rx2 = x2;
				ry2 = y2;
			} else {
				// otherwise just update the width to include the current token
				rx2 = x2;
			}
			
			// if we hit a newline token, start a new rectangle
			if (tok instanceof WtNewLine) {
				addSelectRectangle(rx, ry, rx2, ry2);
				
				// reset the machine
				rx = -1;
				ry = -1;
				rx2 = -1;
				ry2 = -1;
			}
		}
		
		// if we haven't finished the current rectangle, finish it
		if (rx != -1 && ry != -1) {
			addSelectRectangle(rx, ry, rx2, ry2);
		}
		repaint();
	}
	
	/** Add a selection rectangle with the given corners. */
	private void addSelectRectangle(int x, int y, int x2, int y2) {
		int rw = x2 - x;
		int rh = y2 - y;
		Rectangle r = new Rectangle(x, y, rw, rh);
		selectRects.add(r);
	}
	
	/** Clear the text selection. */
	public void clearTextSelection() {
		selectRects.clear();
		selectionOn = false;
	}
	
	/** Draw the text selection rectangle(s) on the graphics context. */
	private void drawTextSelection(Graphics g) {
		// get the original color so we can reset the color
		Color oc = g.getColor();
		
		// use a transparent filling for the rectangles
		Color fill = new Color((float) 1, (float) 1, (float) 1, (float) 0.3);
		
		// set color to to the caret color and draw one or more rectangles
		for (Rectangle r: selectRects) {
			g.setColor(caretColor);
			g.drawRect(r.x, r.y, r.width, r.height);
			
			g.setColor(fill);
			g.fillRect(r.x, r.y, r.width, r.height);
		}
		
		// return the color
		g.setColor(oc);
	}
	
	/** Set the text or color rectangles for drawing based on the list of
	 *  rectangles provided.
	 */
	public void setRectangles(ArrayList<WgRectangle> rects) {
		// turn display of text/color rectangles on and reset before updating
		tcgRectsOn = true;
		tcgRects.clear();
		
		for (WgRectangle tr: rects) {
			Wt_Rectangle rTok = tr.getRectToken();
			Rectangle r = getRectangleFromToken(rTok);
			if (r != null)
				tcgRects.add(r);
		}
	}
	
	/** Create rectangles used to outline graphics based on the list of
	 *  graphics provided.
	 */
	public void setGraphics(WTokenList graphics) {
		tcgRectsOn = true;
		tcgRects.clear();
		
		for (WToken t: graphics) {
			if (t.isType(WTokenType.graphic)) {
				Rectangle r = getRectangleFromToken(t);
				if (r != null)
					tcgRects.add(r);
			}
		}
	}
	
	/** Add resizing handles to indicate the selected the rectangle. */
	public void setSelectedRectangle(WgRectangle sr) {
		// set the selected rectangle - it will be drawn with solid lines (not
		// dashed)
		Wt_Rectangle rt = sr.getRectToken();
		if (rt != null)
			selectedRegion = getRectangleFromToken(sr.getRectToken());
		else
			selectedRegion = null;
		
		// store resize handles for drawing - note that we don't care about
		// direction here
		HashMap<String, Rectangle> rHandles = sr.getResizeHandles();
		if (rHandles != null)
			srHandles = new ArrayList<Rectangle>(rHandles.values());
		else
			srHandles.clear();
		repaint();
	}
	
	public void clearSelectedRegion() {
		selectedRegion = null;
		srHandles.clear();
		repaint();
	}
	
	/** Set the selected graphic visually on the panel by outlining it and
	 *  adding a single handle in the top-left.
	 */
	public void setSelectedGraphic(WtGraphic gt) {
		if (gt != null) {
			// get a rectangle for drawing from the graphic
			selectedRegion = getRectangleFromToken(gt);
			
			// add a handle
			srHandles.clear();
			srHandles.add(WgRectangle.getHandleRectangle(selectedRegion.x,
					selectedRegion.y, WController.rThreshold));
		} else
			clearSelectedRegion();
		repaint();
	}
	
	/** Draw the text/color rectangles on the graphics context. */
	private void drawRectangles(Graphics g) {
		// change to Graphics2D so we can set the stroke for dashed lines
		Graphics2D g2d = (Graphics2D) g;
		
		// save the original stroke
		Stroke os = g2d.getStroke();
		
		g2d.setStroke(dashed);
		
		// get the original color so we can reset the color
		Color oc = g2d.getColor();
		g2d.setColor(tcrColor);
		
		// set color to to the text rectangle color and draw
		for (Rectangle r: tcgRects) {
			g2d.drawRect(r.x, r.y, r.width, r.height);
		}
		
		// draw the selected region
		g2d.setStroke(os);
		if (selectedRegion != null) {
			g2d.drawRect(selectedRegion.x, selectedRegion.y,
					selectedRegion.width, selectedRegion.height);
		}
		
		// add handles to the selected region (solid lines & filled)
		for (Rectangle hr: srHandles) {
			g2d.drawRect(hr.x, hr.y, hr.width, hr.height);
			g2d.fillRect(hr.x, hr.y, hr.width, hr.height);
		}
		
		// if a new rectangle is being created, draw it
		if (newRectInProgress != null) {
			g2d.drawRect(newRectInProgress.x, newRectInProgress.y,
					newRectInProgress.width, newRectInProgress.height);
		}
		
		// return the color
		g.setColor(oc);
	}

	/** Clear the text/color rectangles or graphics and any resize handles. */
	public void clearRectangles() {
		tcgRects.clear();
		selectedRegion = null;
		srHandles.clear();
		tcgRectsOn = false;
		repaint();
	}
	
	public void setRectangleInProgress(int x, int y, int w, int h) {
		newRectInProgress = new Rectangle(x, y, w, h);
		repaint();
	}
	
	public void clearRectangleInProgress() {
		newRectInProgress = null;
		repaint();
	}
	
	
	/** Component listener for handling resize events */
	private class WImagePanelComponentListener extends ComponentAdapter {
		public void componentResized(ComponentEvent e) {
			Dimension d = e.getComponent().getSize();
			int w = (int) Math.floor(((float) d.width)/10)*10;
			int h = (int) Math.floor(((float) d.height)/10)*10;
//			System.out.println(String.format(
//					"WImagePanel: %d x %d -> %d x %d", d.width, d.height, w, h));
			setImageSize(w, h);
		}
	}
}




















