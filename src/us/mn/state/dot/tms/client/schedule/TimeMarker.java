/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.schedule;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Marker used to paint time plans.
 *
 * @author Douglas Lau
 */
public class TimeMarker implements Shape {

	/** Size in pixels to render marker */
	static protected final int MARKER_SIZE_PIX = 36;

	/** Arc representing a clock face */
	static private final Arc2D.Float ARC = new Arc2D.Float(0, 0,
		MARKER_SIZE_PIX, MARKER_SIZE_PIX, 0, 360, Arc2D.OPEN);

	/** Actual shape being delegated */
	private final GeneralPath path;

	/** Create a new time marker */
	public TimeMarker() {
		float size = MARKER_SIZE_PIX;
		float half = size / 2;
		float quarter = size / 4;
		float x = half;
		float y = size;
		path = new GeneralPath(ARC);
		path.moveTo(x, y);
		path.lineTo(x, y -= half);
		path.moveTo(x, y);
		path.lineTo(x += quarter, y);
	}

	public boolean contains(double x, double y) {
		return path.contains(x, y);
	}

	public boolean contains(double x, double y, double w, double h) {
		return path.contains(x, y, w, h);
	}

	public boolean contains(Point2D p) {
		return path.contains(p);
	}

	public boolean contains(Rectangle2D r) {
		return path.contains(r);
	}

	public Rectangle getBounds() {
		return path.getBounds();
	}

	public Rectangle2D getBounds2D() {
		return path.getBounds2D();
	}

	public PathIterator getPathIterator(AffineTransform t) {
		return path.getPathIterator(t);
	}

	public PathIterator getPathIterator(AffineTransform t, double f) {
		return path.getPathIterator(t, f);
	}

	public boolean intersects(double x, double y, double w, double h) {
		return path.intersects(x, y, w, h);
	}

	public boolean intersects(Rectangle2D r) {
		return path.intersects(r);
	}

	/** Create a transformed marker with the specified transform */
	public Shape createTransformedShape(AffineTransform at) {
		return path.createTransformedShape(at);
	}
}
