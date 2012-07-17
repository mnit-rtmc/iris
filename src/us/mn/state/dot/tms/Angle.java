/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011  AHMCT, University of California
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

package us.mn.state.dot.tms;

/**
 * Immutable angle, which can also be in a missing state. Methods return 
 * the angle in radians (double) or integer degrees.
 *
 * @author Michael Darter
 */
final public class Angle extends PhysicalQuantity {

	/** One complete revolution in radians */
	final static double REV = 2 * Math.PI;

	/** Angle in radians */
	private final double angle_rads;

	/** Constructor for missing or non-missing angle.
	 * @param d Angle in degrees or null for missing. */
	public Angle(Integer d) {
		super(d == null);
		angle_rads = (d == null ? 0 : degsToRads(d));
	}

	/** Constructor for missing angle */
	public Angle() {
		super(true);
		angle_rads = 0;
	}

	/** Constructor for radians */
	public Angle(double r) {
		super(false);
		angle_rads = r;
	}

	/** Constructor for degrees.
	 * @param d Angle in degrees, positive or negative. */
	public Angle(int d) {
		super(false);
		angle_rads = degsToRads(d);
	}

	/** Return a new rounded Angle.
	 * @param p Rounding precision, e.g. 10 for 1 digit after decimal.
	 * @return A new angle that is a rounded version of this. */
	public Angle round(int p) {
		if(isMissing())
			return new Angle();
		else
			return new Angle((int)round((int)toDegs(), p));
	}

	/** Round a number to the specified precision.
	 * @param p Rounding precision, e.g. 10 for 1 digit after decimal. */
	static private double round(double num, double p) {
		return Math.round(num / p) * p;
	}

	/** Convert degrees to radians */
	static private double degsToRads(double d) {
		return d * Math.PI / 180d;
	}

	/** To string
	 * @return Angle in degrees or null for missing */
	public String toString2() {
		return isMissing() ? null : 
			Integer.toString(toDegs()) + getUnits();
	}

	/** Get units */
	public String getUnits() {
		return "\u00B0";
	}

	/** Equals */
	public boolean equals(Angle a) {
		if(a == null)
			return false;
		else if(a.isMissing() && isMissing())
			return true;
		else if(a.isMissing() || isMissing())
			return false;
		else 
			return a.toRads() == toRads();
	}

	/** Get angle in radians */
	public double toRads() {
		return angle_rads;
	}

	/** Get angle in degrees */
	public int toDegs() {
		return radsToDegs(angle_rads);
	}

	/** Get angle in degrees as an Integer.
	 * @return Angle in degrees or null if missing. */
	public Integer toDegsInteger() {
		return isMissing() ? null : toDegs();
	}

	/** Convert radians to degs */
	static private int radsToDegs(double r) {
		return (int)Math.round(r * 180d / Math.PI);
	}

	/** Get the angle in normalized degrees.
	 * @return Angle in degrees (0-359) */
	public int toNormalizedDegs() {
		return degsToNormalizedDegs(toDegs());
	}

	/** Return an inverted angle, which is the equivalent angle 
	 * in the other direction. */
	public Angle invert() {
		return new Angle(floorRev(toRads()) + 
			ceilRev(toRads()) - toRads());
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
	 * @param i Angle in degrees.
	 * @return Angle in degrees 0 - 359. */
	static public int degsToNormalizedDegs(int i) {
		return i < 0 ? 360 + (i % 360) : (i % 360);
	}

	/** Return the direction as a human readable string.
	 * @return The direction as N, NE, E, SE, S, SW, W, NW, 
	 *	   or null if missing. */
	public String toShortDir() {
		if(isMissing())
			return null;
		int d = degsToNormalizedDegs(toDegs());
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
