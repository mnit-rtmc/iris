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
 * Geographic map line segment.
 *
 * @author Douglas Lau
 */
public class MapLineSegment {

	/** X-coordinate of point A */
	private final double ax;

	/** Y-coordinate of point A */
	private final double ay;

	/** X-coordinate of point B */
	private final double bx;

	/** Y-coordinate of point B */
	private final double by;

	/** Create a new map line segment */
	public MapLineSegment(double x0, double y0, double x1, double y1) {
		ax = x0;
		ay = y0;
		bx = x1;
		by = y1;
	}

	/** Calculate the distance to a given point */
	public double distanceTo(double cx, double cy) {
		// If the dot product of ab and bc is greater than zero,
		// then the nearest point on the segment is b.
		MapVector ab = new MapVector(bx - ax, by - ay);
		MapVector bc = new MapVector(cx - bx, cy - by);
		if (ab.dot(bc) > 0)
			return bc.getMagnitude();
		// If the dot product of ba and ac is greater than zero,
		// then the nearest point on the segment is a.
		MapVector ba = new MapVector(ax - bx, ay - by);
		MapVector ac = new MapVector(cx - ax, cy - ay);
		if (ba.dot(ac) > 0)
			return ac.getMagnitude();
		// Otherwise, the nearest point on the segment is between
		// a and b, so calculate the point-line distance.
		return Math.abs(ab.cross(ac)) / ab.getMagnitude();
	}

	/** Snap a point onto the line segment */
	public MapVector snap(double cx, double cy) {
		// If the dot product of ab and bc is greater than zero,
		// then the nearest point on the segment is b.
		MapVector ab = new MapVector(bx - ax, by - ay);
		MapVector bc = new MapVector(cx - bx, cy - by);
		if (ab.dot(bc) > 0)
			return new MapVector(bx, by);
		// If the dot product of ba and ac is greater than zero,
		// then the nearest point on the segment is a.
		MapVector ba = new MapVector(ax - bx, ay - by);
		MapVector ac = new MapVector(cx - ax, cy - ay);
		if (ba.dot(ac) > 0)
			return new MapVector(ax, ay);
		// The nearest point is somewhere between a and b, so just
		// project c onto the line fromed by a and b.
		MapLine line = new MapLine(ax, ay, bx, by);
		return line.project(cx, cy);
	}
}
