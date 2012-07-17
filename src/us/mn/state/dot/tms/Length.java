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

import us.mn.state.dot.tms.utils.SString;

/**
 * Immutable length, which can also be in a missing state.
 *
 * @author Michael Darter
 */
final public class Length extends PhysicalQuantity {

	/** Constants */
	public static final double METERS_PER_MILE = 1609.344;
	public static final double KM_PER_MILE = METERS_PER_MILE / 1000d;

	/** Length in meters */
	private final double length_m;

	/** Constructor for non-missing value.
	 * @param l Length in meters. */
	public Length(double l) {
		super(false);
		length_m = l;
	}

	/** Constructor for missing or non-missing value.
	 * @param l Length in meters or null for missing. */
	public Length(Integer l) {
		super(l == null);
		length_m = (l == null ? 0 : l);
	}

	/** Constructor for missing or non-missing value.
	 * @param l Length in meters or null for missing. */
	public Length(Double l) {
		super(l == null);
		length_m = (l == null ? 0 : l);
	}

	/** Constructor for missing value */
	public Length() {
		super(true);
		length_m = 0;
	}

	/** Get the length in client units.
	 * @return Length in client units or null if missing. */
	public String toString2() {
		return toString2(0);
	}

	/** Get the length in client units.
	 * @param ndigs Number of digits to the right of the decimal point.
	 * @return Length in client units or null if missing. */
	public String toString2(int ndigs) {
		if(isMissing())
			return null;
		double t = (useSi() ? length_m : mToFt(length_m));
		return SString.doubleToString(t, ndigs) + " " + getUnits();
	}

	/** Get units */
	public String getUnits() {
		return useSi() ? "m" : "ft";
	}

	/** Get the length in meters */
	public double toM() {
		return (double)length_m;
	}

	/** Get the length in ft */
	public double toFt() {
		return mToFt(length_m);
	}

	/** Convert meters to ft */
	static private double mToFt(double m) {
		final double FT_PER_METER = 3.2808399;
		return m * FT_PER_METER;
	}
}
