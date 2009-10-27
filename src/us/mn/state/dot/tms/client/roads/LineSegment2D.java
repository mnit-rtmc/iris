/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.roads;

/**
 * Simple 2D line segment class
 *
 * @author Douglas Lau
 */
public class LineSegment2D {

	/** X-coordinate of point A */
	protected final double ax;

	/** Y-coordinate of point A */
	protected final double ay;

	/** X-coordinate of point B */
	protected final double bx;

	/** Y-coordinate of point B */
	protected final double by;

	/** Create a new 2D line segment */
	public LineSegment2D(double x0, double y0, double x1, double y1) {
		ax = x0;
		ay = y0;
		bx = x1;
		by = y1;
	}

	/** Calculate the distance to a given point */
	public double distanceTo(double cx, double cy) {
		// If the dot product of ab and bc is greater than zero,
		// then the nearest point on the segment is b.
		Vector2D ab = new Vector2D(bx - ax, by - ay);
		Vector2D bc = new Vector2D(cx - bx, cy - by);
		if(ab.dot(bc) > 0)
			return bc.getMagnitude();
		// If the dot product of ba and ac is greater than zero,
		// then the nearest point on the segment is a.
		Vector2D ba = new Vector2D(ax - bx, ay - by);
		Vector2D ac = new Vector2D(cx - ax, cy - ay);
		if(ba.dot(ac) > 0)
			return ac.getMagnitude();
		// Otherwise, the nearest point on the segment is between
		// a and b, so calculate the point-line distance.
		return Math.abs(ab.cross(ac)) / ab.getMagnitude();
	}
}
