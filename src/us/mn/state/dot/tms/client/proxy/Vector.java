/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.proxy;

/**
 * Simple 2D vector class
 *
 * @author Douglas Lau
 */
public class Vector {

	/** X-coordinate */
	protected final double x;

	/** Y-coordinate */
	protected final double y;

	/** Create a new vector */
	public Vector(double x, double y) {
		this.x = x;
		this.y = y;
	}

	/** Get the magnitude (length) */
	public double getMagnitude() {
		return Math.hypot(x, y);
	}

	/** Get the vector angle (radians) */
	public double getAngle() {
		double a = Math.acos(x / getMagnitude());
		if(y > 0)
			return a;
		else
			return -a;
	}

	/** Subtract another vector from this one */
	public Vector subtract(Vector other) {
		return new Vector(x - other.x, y - other.y);
	}
}
