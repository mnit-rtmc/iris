/*
 * IRIS -- Intelligent Roadway Information System
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

import java.text.NumberFormat;
import us.mn.state.dot.tms.SystemAttrEnum;

/**
 * Immutable pressure in various units.
 *
 * @author Michael Darter
 */
final public class Pressure {

	/** Enumeration of units */
	public enum Units {
		PASCALS(1, "Pa", 0),
		HECTOPASCALS(100, "hPa", 0),
		INHG(3386.39, "inHg", 1);

		/** Conversion scale to Pascals: pa = scale * X */
		public final double scale;

		/** Unit label */
		public final String label;

		/** Number of significant digits after decimal */
		public final int n_digits;

		/** Create units */
		private Units(double sc, String la, int nd) {
			scale = sc;
			label = la;
			n_digits = nd;
		}
	}

	/** Factory to create a new quantity with the null case handled.
	 * @param v Value in units u or null.
	 * @return A new quantity in system units or null */
	static public Pressure create(Integer v) {
		return create(v, Units.PASCALS);
	}

	/** Factory to create a new quantity with the null case handled.
	 * @param v Value in units u or null.
	 * @param u Units for arg v.
	 * @return A new quantity in system units or null */
	static public Pressure create(Integer v, Units u) {
		Pressure vr = null;
		if (v != null) {
			vr = new Pressure(v, u);
			if (!useSi())
				vr = vr.convert(Units.INHG);
		}
		return vr;
	}

        /** Get system units */
        static private boolean useSi() {
                return SystemAttrEnum.CLIENT_UNITS_SI.getBoolean();
        }

	/** Pressure in pascals */
	public final double value;

	/** units */
	public final Units units;

	/** Constructor with units
	 * @param v Value
	 * @param u Units */
	public Pressure(double v, Units u) {
		value = v;
		units = u;
	}

	/** Constructor with assummed units of Pascals
	 * @param v Pressure value in Pascals */
	public Pressure(double v) {
		this(v, Units.PASCALS);
	}

	/** To string
	 * @return Pressure rounded to the number of significant
	 * digit specified for the unit and the unit symbol. */
	public String toString() {
		return new Formatter(units.n_digits).format(this);
	}

	/** Equals */
	public boolean equals(Pressure a) {
		if(a == null)
			return false;
		else 
			return a.pascals() == pascals();
	}

	/** Convert to the specified units.
	 * @param nu Units to convert to.
	 * @return New object in the specified units */
	public Pressure convert(Units nu) {
		if (nu == units)
			return this;
		else {
			double pa = pascals();
			double nv = pa / nu.scale;
			return new Pressure(nv, nu);
		}
	}

	/** Get value */
	public double pascals() {
		if (units == Units.PASCALS)
			return value;
		else
			return (value * units.scale);
	}

	/** Get pressure as an NTCIP value.
	 * @return Pressure in 1/10ths of millibar, which is also tenths of
	 *                  a hectoPascal. See NTCIP essAtmosphericPressure. */
	public Integer ntcip() {
		return new Integer((int)Math.round(pascals() / (.1 * 100)));
	}

	/** Unit formatter */
	static public class Formatter {
		private final NumberFormat format;
		public Formatter(int d) {
			format = NumberFormat.getInstance();
			format.setMaximumFractionDigits(d);
			format.setMinimumFractionDigits(d);
		}
		public String format(Pressure p) {
			StringBuilder sb = new StringBuilder();
			sb.append(format.format(p.value)).append(" ").
				append(p.units.label);
			return sb.toString();
		}
	}
}
