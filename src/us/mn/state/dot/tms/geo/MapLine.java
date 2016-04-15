/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.geo;

/**
 * Geographic map line.
 *
 * @author Douglas Lau
 */
public class MapLine {

	/** X-coordinate of point A */
	protected final double ax;

	/** Y-coordinate of point A */
	protected final double ay;

	/** X-coordinate of point B */
	protected final double bx;

	/** Y-coordinate of point B */
	protected final double by;

	/** Create a new map line */
	public MapLine(double x0, double y0, double x1, double y1) {
		ax = x0;
		ay = y0;
		bx = x1;
		by = y1;
	}

	/** Calculate the distance to a given point */
	public double distanceTo(double cx, double cy) {
		MapVector ab = vector();
		MapVector ac = new MapVector(cx - ax, cy - ay);
		return Math.abs(ab.cross(ac)) / ab.getMagnitude();
	}

	/** Get the direction vector */
	protected MapVector vector() {
		return new MapVector(bx - ax, by - ay);
	}

	/** Find the intersection point with another line */
	public MapVector intersect(MapLine other) {
		MapVector ab = vector();
		MapVector cd = other.vector();
		double den = ab.cross(cd);
		if(den == 0)
			return null;
		MapVector ca = new MapVector(ax - other.ax, ay - other.ay);
		double num = cd.cross(ca);
		double u = num / den;
		double x = ax + u * (bx - ax);
		double y = ay + u * (by - ay);
		return new MapVector(x, y);
	}

	/** Project a point onto the line */
	public MapVector project(double x, double y) {
		MapVector perp = vector().perpendicular();
		double x1 = x + perp.x;
		double y1 = y + perp.y;
		return intersect(new MapLine(x, y, x1, y1));
	}
}
