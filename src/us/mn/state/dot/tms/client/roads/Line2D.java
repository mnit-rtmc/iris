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
 * Simple 2D line class
 *
 * @author Douglas Lau
 */
public class Line2D {

	/** X-coordinate of point A */
	protected final double ax;

	/** Y-coordinate of point A */
	protected final double ay;

	/** X-coordinate of point B */
	protected final double bx;

	/** Y-coordinate of point B */
	protected final double by;

	/** Create a new 2D line */
	public Line2D(double x0, double y0, double x1, double y1) {
		ax = x0;
		ay = y0;
		bx = x1;
		by = y1;
	}

	/** Calculate the distance to a given point */
	public double distanceTo(double cx, double cy) {
		Vector2D ab = vector();
		Vector2D ac = new Vector2D(cx - ax, cy - ay);
		return Math.abs(ab.cross(ac)) / ab.getMagnitude();
	}

	/** Get the direction vector */
	protected Vector2D vector() {
		return new Vector2D(bx - ax, by - ay);
	}

	/** Find the intersection point with another line */
	public Vector2D intersect(Line2D other) {
		Vector2D ab = vector();
		Vector2D cd = other.vector();
		double den = ab.cross(cd);
		if(den == 0)
			return null;
		Vector2D ca = new Vector2D(other.ax - ax, other.ay - ay);
		double num = ab.cross(ca);
		double u = num / den;
		double x = ax + u * (bx - ax);
		double y = ay + u * (by - ay);
		return new Vector2D(x, y);
	}

	/** Project a point onto the line */
	public Vector2D project(double x, double y) {
		Vector2D perp = vector().perpendicular();
		double x1 = x + perp.x;
		double y1 = y + perp.y;
		return intersect(new Line2D(x, y, x1, y1));
	}
}
