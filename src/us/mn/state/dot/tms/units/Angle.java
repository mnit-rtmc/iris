/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011  AHMCT, University of California
 * Copyright (C) 2017  Iteris Inc.
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

package us.mn.state.dot.tms.units;

/**
 * Immutable angle with methods that return the angle in
 * radians or degrees.
 *
 * @author Michael Darter
 */
final public class Angle {

	/** Factory which handles the null case.
	 * @param ang Angle in degrees or null
	 * @return A new Angle or null */
	static public Angle create(Integer ang) {
		return (ang != null ? new Angle(ang) : null);
	}

	/** One complete revolution in radians */
	final static double REV = 2 * Math.PI;

	/** Convert radians to degs */
	static private double radsToDegs(double r) {
		return r * 180 / Math.PI;
	}

	/** Return the ceiling revolution in radians.
	 * @param rads Angle in radians.
	 * @return The angle in radians of the next complete revolution. */
	static public double ceilRev(double rads) {
		if(rads >= 0)
			return REV * Math.ceil(rads / (REV));
		else
			return REV * Math.floor(rads / (REV));
	}

	/** Return the floor revolution in radians.
	 * @param rads Angle in radians.
	 * @return Angle in radians of the previous complete revolution. */
	static public double floorRev(double rads) {
		if(rads >= 0)
			return REV * Math.floor(rads / (REV));
		else
			return REV * Math.ceil(rads / (REV));
	}

	/** Normalize degrees to 0 - 359. See the test cases for more info.
	 * @param d Angle in degrees.
	 * @return Angle in degrees 0 - 359. */
	static public int toNormalizedDegs(double d) {
		int i = (int)round(d);
		return i < 0 ? 360 + (i % 360) : (i % 360);
	}

	/** Round a number */
	static protected double round(double num) {
		return round(num, 1);
	}

	/** Round a number to the specified precision.
	 * @param p Rounding precision, e.g. 1 for 0 digits after decimal,
	 *          10 for 1 digit after decimal etc. */
	static protected double round(double num, int p) {
		p = (p > 0 ? p : 1);
		return Math.round(num / (double)p) * (double)p;
	}

	/** Convert degrees to radians */
	static private double degsToRads(double d) {
		return d * Math.PI / 180d;
	}

	/** Angle in radians */
	private final double angle_rads;

	/** Constructor
	 * @param d Angle in degrees */
	public Angle(double d) {
		angle_rads = degsToRads(d);
	}

	/** Constructor */
	public Angle() {
		angle_rads = 0;
	}

	/** Get units, which are degrees */
	public String getUnits() {
		return "\u00B0";
	}

	/** To string
	 * @return Angle in degrees with unit */
	public String toString() {
		return Integer.toString(toDegsInt()) + getUnits();
	}

	/** Equals */
	public boolean equals(Angle a) {
		if(a == null)
			return false;
		else 
			return a.toRads() == toRads();
	}

	/** Get angle in radians */
	public double toRads() {
		return angle_rads;
	}

	/** Get angle in degrees */
	public double toDegs() {
		return radsToDegs(angle_rads);
	}

	/** Get angle in integer degrees */
	public int toDegsInt() {
		return (int)round(radsToDegs(angle_rads));
	}

	/** Return a new Angle rounded in degrees.
	 * @param p Rounding precision, e.g. 10 for 1 digit after decimal.
	 * @return A new angle rounded in degrees. */
	public Angle round(int p) {
		return new Angle(round(toDegs(), p));
	}

	/** Get the angle in normalized degrees.
	 * @return Angle in degrees (0-359) */
	public int toNormalizedDegs() {
		return toNormalizedDegs(toDegs());
	}

	/** Return an inverted angle, which is the equivalent angle 
	 * in the other direction. */
	public Angle invert() {
		return new Angle(radsToDegs(floorRev(toRads())) + 
			radsToDegs(ceilRev(toRads()) - toRads()));
	}

	/** Add to the angle */
	public Angle add(double degs) {
		return new Angle(toDegs() + degs);
	}

	/** Return the direction as a human readable string.
	 * @return The direction as N, NE, E, SE, S, SW, W, NW */
	public String toShortDir() {
		int d = toNormalizedDegs(toDegs());
		if(d <= 22)
			return "N";
		else if(d >= 23 && d <= 68)
			return "NE";
		else if(d >= 69 && d <= 112)
			return "E";
		else if(d >= 113 && d <= 158)
			return "SE";
		else if(d >= 159 && d <= 202)
			return "S";
		else if(d >= 203 && d <= 248)
			return "SW";
		else if(d >= 249 && d <= 292)
			return "W";
		else if(d >= 293 && d <= 337)
			return "NW";
		else
			return "N";
	}
}
