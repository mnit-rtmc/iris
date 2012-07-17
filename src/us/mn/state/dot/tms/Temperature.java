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
 * Immutable temperature, which can have a missing state.
 *
 * @author Michael Darter
 */
final public class Temperature extends PhysicalQuantity {

	/** Temperature in Celsius */
	private final double temp_celsius;

	/** Constructor for missing or non-missing temperature.
	 * @param t Temperature in degrees C or null for missing. */
	public Temperature(Integer t) {
		super(t == null);
		temp_celsius = (t == null ? 0 : t);
	}

	/** Constructor for missing or non-missing temperature.
	 * @param t Temperature in degrees C or null for missing. */
	public Temperature(Double t) {
		super(t == null);
		temp_celsius = (t == null ? 0 : new Double(t));
	}

	/** Constructor for missing or non-missing temperature.
	 * @param t Temperature in degrees C or null or empty for missing. */
	public Temperature(String t) {
		super(t == null || t.isEmpty());
		temp_celsius = SString.stringToDouble(
			t == null || t.isEmpty() ? "0" : t);
	}

	/** Constructor for missing value */
	public Temperature() {
		super(true);
		temp_celsius = 0;
	}

	/** Return the temperature as a string + units or null if missing */
	public String toString2() {
		if(isMissing())
			return null;
		double t = useSi() ? temp_celsius : cToF(temp_celsius);
		return SString.doubleToString(t, 0) + " " + getUnits();
	}

	/** Get units */
	public String getUnits() {
		return useSi() ? "\u00B0" + "C" : "\u00B0" + "F";
	}

	/** Get the temperature in Celsius */
	public double toC() {
		return temp_celsius;
	}

	/** Return a rounded temperature.
	 * @param ndigs Number of digits right of the decimal.
	 * @return A temperature rounded as specified. */
	public Temperature round(int ndigs) {
		if(ndigs >= 0) {
			double f = Math.pow(10, ndigs);
			double nc = Math.round(temp_celsius * f) / f;
			return new Temperature(nc);
		} else
			return this;
	}

	/** Get the temperature in Celsius.
	 * @return Null if missing else the temperature in C as an Integer */
	public Integer toCInteger() {
		return isMissing() ? null : 
			new Integer((int)Math.round(temp_celsius));
	}

	/** Get the temperature in Fahrenheit */
	public double toF() {
		return cToF(temp_celsius);
	}

	/** Convert Celcius to Fahrenheit */
	static public double cToF(double c) {
		return c * 9 / 5 + 32;
	}
}
