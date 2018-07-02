/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2018  Minnesota Department of Transportation
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
 * Geographic map vector.
 *
 * @author Douglas Lau
 */
public class MapVector {

	/** X-coordinate */
	public final double x;

	/** Y-coordinate */
	public final double y;

	/** Create a new map vector */
	public MapVector(double x, double y) {
		this.x = x;
		this.y = y;
	}

	/** Get the string representation of the vector */
	@Override
	public String toString() {
		return "" + x + "," + y;
	}

	/** Test for equality */
	@Override
	public boolean equals(Object o) {
		if (o instanceof MapVector) {
			MapVector other = (MapVector) o;
			return x == other.x && y == other.y;
		} else
			return false;
	}

	/** Get the magnitude (length) */
	public double getMagnitude() {
		return Math.hypot(x, y);
	}

	/** Get the vector angle (radians) */
	public double getAngle() {
		return Math.atan2(y, x);
	}

	/** Add a vector to this one */
	public MapVector add(MapVector other) {
		return new MapVector(x + other.x, y + other.y);
	}

	/** Subtract another vector from this one */
	public MapVector subtract(MapVector other) {
		return new MapVector(x - other.x, y - other.y);
	}

	/** Calculate the dot product with another vector */
	public double dot(MapVector other) {
		return x * other.x + y * other.y;
	}

	/** Calculate the cross product with another vector.  This returns
	 * the magnitude of the 3D cross product, since cross product only
	 * makes sense for 3D vectors. */
	public double cross(MapVector other) {
		return x * other.y - y * other.x;
	}

	/** Get a perpendicular vector */
	public MapVector perpendicular() {
		return new MapVector(y, -x);
	}

	/** Get a perpendicular vector */
	public MapVector perpendicular2() {
		return new MapVector(-y, x);
	}

	/** Rotate vector.
	 * @param th Angle (radians).
	 * @return New vector. */
	public MapVector rotate(double th) {
		double sn = Math.sin(th);
		double cs = Math.cos(th);
		return new MapVector(x * cs - y * sn, x * sn + y * cs);
	}
}
