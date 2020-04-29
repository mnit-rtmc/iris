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

import java.awt.Point;
import java.awt.event.MouseEvent;


/**
 * A class representing a point referenced in the WYSIWYG DMS message editor
 * GUI (e.g. created by mouse pointer activities like clicking, moving,
 * dragging, etc.). Contains both the original coordinates (in image space)
 * and the equivalent sign coordinates of the point.
 *
 * @author Gordon Parikh - SRF Consulting
 */

public class WPoint {
	
	/** X and Y coordinates in image/WYSIWYG space */
	private int wx;
	private int wy;
	
	/** Point object in image/WYSIWYG space */
	private Point wp;
	
	/** X and Y coordinates in sign coordinate space */
	private int sx;
	private int sy;
	
	/** Point object in image sign coordinate space */
	private Point sp;
	
	/** WRaster object used to convert between coordinate spaces */
	private WRaster wr;
	
	/** Construct a WPoint object from a set of image coordinates and a
	 *  WRaster object.
	 */
	public WPoint(int x, int y, WRaster r) {
		wx = x;
		wy = y;
		wr = r;
		init();
	}
	
	/** Construct a WPoint object from a mouse event and WRaster. */
	public WPoint(MouseEvent e, WRaster r) {
		wx = e.getX();
		wy = e.getY();
		wr = r;
		init();
	}
	
	private void init() {
		// construct a point for the image coordinates
		wp = new Point(wx, wy);
		
		// get sign coordinates
		convertSignCoordinates();
	}
	
	/** Convert image coordinates to sign coordinates */
	private void convertSignCoordinates() {
		// convert into sign coordinates then make a Point object
		if (wr != null) {
			sx = wr.cvtWysiwygToSignX(wx);
			sy = wr.cvtWysiwygToSignY(wy);
			sp = new Point(sx, sy);
		}
	}
	
	public Point getWysiwygPoint() {
		return wp;
	}
	
	public int getWysiwygX() {
		return wx;
	}

	public int getWysiwygY() {
		return wy;
	}
	
	public Point getSignPoint() {
		return sp;
	}
	
	public int getSignX() {
		return sx;
	}

	public int getSignY() {
		return sy;
	}
	
	public WRaster getWRaster() {
		return wr;
	}
}
















