/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013-2015  Minnesota Department of Transportation
 * Copyright (C) 2017       Iteris Inc.
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
 * Temperature values.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public final class Temperature {

	/** Enumeration of temperature units */
	public enum Units {
		KELVIN(1, 0, "\u00B0K"),
		CELSIUS(1, 273.15, "\u00B0C"),
		FAHRENHEIT(5.0 / 9.0, 459.67, "\u00B0F"),
		RANKINE(Units.FAHRENHEIT.k_scale, 0, "\u00B0R");

		/** Conversion scale to Celsius/Kelvin */
		public final double k_scale;

		/** Offset to degrees Kelvin */
		public final double k_offset;

		/** Unit label */
		public final String label;

		/** Create units */
		private Units(double s, double o, String l) {
			k_scale = s;
			k_offset = o;
			label = l;
		}
	}

	/** Factory to create a new quantity with the null case handled.
	 * @param v Value in units u or null.
	 * @return A new quantity in system units or null */
	static public Temperature create(Integer v) {
		return create(v, Units.CELSIUS);
	}

	/** Factory to create a new quantity with the null case handled.
	 * @param v Value in units u or null.
	 * @param u Units for arg v.
	 * @return A new quantity in system units or null */
	static public Temperature create(Integer v, Units u) {
		Temperature t = null;
		if (v != null) {
			t = new Temperature(v, u);
			if (!useSi())
				t = t.convert(Units.FAHRENHEIT);
		}
		return t;
	}

        /** Get system units */
        static private boolean useSi() {
                return SystemAttrEnum.CLIENT_UNITS_SI.getBoolean();
        }

	/** Temperature value */
	public final double value;

	/** Temperature units */
	public final Units units;

	/** Create a new temperature.
	 * @param v Value of temperature.
	 * @param u Units of temperature. */
	public Temperature(double v, Units u) {
		value = v;
		units = u;
	}

	/** Create a new temperature.
	 * @param v Value in degrees Celsius. */
	public Temperature(double v) {
		this(v, Units.CELSIUS);
	}

	/** Get the temperature in degrees Kelvin */
	public double kelvin() {
		if (units == Units.KELVIN)
			return value;
		else
			return (value + units.k_offset) * units.k_scale;
	}

	/** Convert an temperature to specified units.
	 * @param u Units to convert to.
	 * @return Temperature in specified units. */
	public Temperature convert(Units u) {
		if (u == units)
			return this;
		else {
			double k = kelvin();
			double v = k / u.k_scale - u.k_offset;
			return new Temperature(v, u);
		}
	}

	/** Round a temperature to nearest whole unit.
	 * @param u Units to return.
	 * @return Temperature rounded to nearest whole unit. */
	public int round(Units u) {
		return (int)Math.round(convert(u).value);
	}

	/** Compare for equality */
	@Override
	public boolean equals(Object other) {
		if (other instanceof Temperature) {
			Temperature o = (Temperature)other;
			if (units == o.units)
				return value == o.value;
			else
				return kelvin() == o.kelvin();
		} else
			return false;
	}

	/** Get a temperature hash code */
	@Override
	public int hashCode() {
		return new Double(kelvin()).hashCode();
	}

	/** Get a string representation of a temperature */
	@Override
	public String toString() {
		return new Formatter(0).format(this);
	}

	/** Temperature formatter */
	static public class Formatter {
		private final NumberFormat format;
		public Formatter(int d) {
			format = NumberFormat.getInstance();
			format.setMaximumFractionDigits(d);
			format.setMinimumFractionDigits(d);
		}
		public String format(Temperature i) {
			return format.format(i.value) + " " + i.units.label;
		}
	}
}
