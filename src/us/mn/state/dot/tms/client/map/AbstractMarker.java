/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.map;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * An abstract marker which delegates all Shape methods to a general path
 *
 * @author Douglas Lau
 */
abstract public class AbstractMarker implements Shape {

	/** Actual shape being delegated */
	protected final GeneralPath path;

	/** Create a new abstract marker */
	protected AbstractMarker(GeneralPath p) {
		path = p;
	}

	/** Create a new abstract marker */
	protected AbstractMarker(int c) {
		this(new GeneralPath(GeneralPath.WIND_EVEN_ODD, c));
	}

	/** Check if the marker contains a point */
	public boolean contains(double x, double y) {
		return path.contains(x, y);
	}

	/** Check if the marker contains a rectangle */
	public boolean contains(double x, double y, double w, double h) {
		return path.contains(x, y, w, h);
	}

	/** Check if the marker contains a point */
	public boolean contains(Point2D p) {
		return path.contains(p);
	}

	/** Check if the marker contains a rectangle */
	public boolean contains(Rectangle2D r) {
		return path.contains(r);
	}

	/** Get the marker bounds */
	public Rectangle getBounds() {
		return path.getBounds();
	}

	/** Get the marker bounds */
	public Rectangle2D getBounds2D() {
		return path.getBounds2D();
	}

	/** Get a path iterator */
	public PathIterator getPathIterator(AffineTransform t) {
		return path.getPathIterator(t);
	}

	/** Get a path iterator */
	public PathIterator getPathIterator(AffineTransform t, double f) {
		return path.getPathIterator(t, f);
	}

	/** Check if the marker intersects with a rectangle */
	public boolean intersects(double x, double y, double w, double h) {
		return path.intersects(x, y, w, h);
	}

	/** Check if the marker intersects with a rectangle */
	public boolean intersects(Rectangle2D r) {
		return path.intersects(r);
	}

	/** Create a transformed shape */
	public Shape createTransformedShape(AffineTransform at) {
		return path.createTransformedShape(at);
	}
}
