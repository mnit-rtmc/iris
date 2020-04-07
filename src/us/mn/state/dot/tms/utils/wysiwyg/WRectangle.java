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

package us.mn.state.dot.tms.utils.wysiwyg;

import java.awt.Rectangle;
import java.util.HashMap;

import us.mn.state.dot.tms.utils.wysiwyg.token.Wt_Rectangle;

/**
 * Parent class for working with text and color rectangles in the WYSIWYG DMS
 * Message Editor. Not to be confused with the text/color rectangle MULTI tag
 * token parent class Wt_Rectangle.
 *
 * @author Gordon Parikh - SRF Consulting
 */

abstract public class WRectangle {
	
	/** The token that represents this rectangle in the MULTI string */
	protected Wt_Rectangle rectTok;
	
	/** Handles for user control of resizing (WYSIWYG/image space) */
	protected HashMap<String, Rectangle> resizeHandles;
	
	/** Constants for referring to handles on rectangle */
	public final static String N = "N";
	public final static String S = "S";
	public final static String E = "E";
	public final static String W = "W";
	public final static String NE = "NE";
	public final static String NW = "NW";
	public final static String SE = "SE";
	public final static String SW = "SW";
	
	public WRectangle(Wt_Rectangle rTok) {
		rectTok = rTok;
	}
	
	/** Return whether or not the point p is near (i.e. within some threshold
	 *  of) this rectangle.
	 */
	public boolean isNear(WPoint p, int threshold) {
		if (rectTok != null) {
			// calculate the coordinates of a slightly larger rectangle
			int tX = rectTok.getParamX() - threshold;
			int tY = rectTok.getParamY() - threshold;
			int rX = rectTok.getParamX()
					+ rectTok.getParamW() + threshold;
			int bY = rectTok.getParamY()
					+ rectTok.getParamH() + threshold;
			
			// check if the point is inside it
			boolean inX = (p.getSignX() >= tX) && (p.getSignX() < rX);
			boolean inY = (p.getSignY() >= tY) && (p.getSignY() < bY);
			return inX && inY;
		}
		return false;
	}
	
	/** Return whether or not the point p is on the border (within some
	 *  threshold of) this rectangle.
	 */
	public boolean isOnBorder(WPoint p, int threshold) {
		if (rectTok != null) {
			// calculate the coordinates of two rectangles - one slightly larger
			// than this rectangle and one slightly smaller
			int LtX = rectTok.getParamX() - threshold;
			int LtY = rectTok.getParamY() - threshold;
			int LrX = rectTok.getParamX()
					+ rectTok.getParamW() + threshold;
			int LbY = rectTok.getParamY()
					+ rectTok.getParamH() + threshold;
			
			int StX = rectTok.getParamX() + threshold;
			int StY = rectTok.getParamY() + threshold;
			int SrX = rectTok.getParamX()
					+ rectTok.getParamW() - threshold;
			int SbY = rectTok.getParamY()
					+ rectTok.getParamH() - threshold;
			
			int px = p.getSignX();
			int py = p.getSignY();
			
			// inside the larger rectangle
			boolean inLX = (px >= LtX) && (px < LrX);
			boolean inLY = (py >= LtY) && (py < LbY);
			boolean inL = inLX && inLY;
			
			// outside the smaller rectangle
			boolean outSX = (px < LtX) || (px > LrX);
			boolean outSY = (py < LtY) || (py > LbY);
			boolean outS = inLX || inLY;
			
			// we'll return true if the point is inside the larger rectangle and
			// outside the smaller rectangle
			return inL && outS;
		}
		return false;
	}
	
	/** Get a list of WPoints that represent the center-point of the resizing
	 *  handles on this rectangle.
	 *  @param r - 1/2 width of the handles in WYSIWYG coordinates  
	 *  @param wr - WRaster for converting between coordinate spaces
	 */
	public HashMap<String, Rectangle> getHandles(int r, WRaster wr) {
		if (rectTok != null && resizeHandles == null) {
			// initialize the hashmap
			resizeHandles = new HashMap<String, Rectangle>();
			
			// get all the various coordinates we will use repeatedly
			int x = wr.cvtSignToWysiwygX(rectTok.getParamX(), true, true);
			int rx = wr.cvtSignToWysiwygX(
					rectTok.getParamX() + rectTok.getParamW() - 1, false, true);
			int y = wr.cvtSignToWysiwygY(rectTok.getParamY(), true, true);
			int by = wr.cvtSignToWysiwygY(
					rectTok.getParamY() + rectTok.getParamH() - 1, false, true);
			int mx = wr.cvtSignToWysiwygX(rectTok.getCentroidX(), true, false);
			int my = wr.cvtSignToWysiwygY(rectTok.getCentroidY(), true, false);
			int rw = 2*r;
			
			// create one handle for each midpoint/corner (N,S,E,W,NE,NW,SE,SW)
			resizeHandles.put(N, new Rectangle(mx-r,y-r,rw,rw));
			resizeHandles.put(S, new Rectangle(mx-r,by-r,rw,rw));
			resizeHandles.put(E, new Rectangle(rx-r,my-r,rw,rw));
			resizeHandles.put(W, new Rectangle(x-r,my-r,rw,rw));
			resizeHandles.put(NE, new Rectangle(rx-r,y-r,rw,rw));
			resizeHandles.put(NW, new Rectangle(x-r,y-r,rw,rw));
			resizeHandles.put(SE, new Rectangle(rx-r,by-r,rw,rw));
			resizeHandles.put(SW, new Rectangle(x-r,by-r,rw,rw));
		}
		return resizeHandles;
	}
	
}























