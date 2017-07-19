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
import static us.mn.state.dot.tms.units.Distance.Units.FEET;
import static us.mn.state.dot.tms.units.Distance.Units.METERS;
import static us.mn.state.dot.tms.units.Distance.Units.MILES;
import static us.mn.state.dot.tms.units.Distance.Units.KILOMETERS;
import static us.mn.state.dot.tms.units.Interval.Units.SECONDS;
import static us.mn.state.dot.tms.units.Interval.Units.HOURS;
import us.mn.state.dot.tms.SystemAttrEnum;

/**
 * Speed of travel units.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public final class Speed {

	/** Create a value based on a given distance / interval pair.
	 * @param d Distance part of pair.
	 * @param i Interval part of pair.
	 * @return Value to use for speed. */
	static private double createValue(Distance d, Interval i) {
		if (Units.lookup(d.units, i.units) != null)
			return d.value / i.value;
		else {
			d = d.convert(KILOMETERS);
			i = i.convert(HOURS);
			return d.value / i.value;
		}
	}

	/** Create units based on a given distance / interval pair.
	 * @param d Distance part of pair.
	 * @param i Interval part of pair.
	 * @return Units to use for speed. */
	static private Units createUnits(Distance d, Interval i) {
		Units u = Units.lookup(d.units, i.units);
		return (u != null) ? u : Units.KPH;
	}

        /** Get system units */
        static private boolean useSi() {
                return SystemAttrEnum.CLIENT_UNITS_SI.getBoolean();
        }

	/** Enumeration of speed units */
	public enum Units {
		FPS(1.097280, "fps", FEET, SECONDS),
		MPH(1.609344, "mph", MILES, HOURS),
		KPH(1.000000, "kph", KILOMETERS, HOURS),
		MPS(3.600000, "mps", METERS, SECONDS);

		/** Conversion rate to kilometers per hour*/
		public final double kph;

		/** Unit label */
		public final String label;

		/** Distance units */
		public final Distance.Units d_units;

		/** Interval units */
		public final Interval.Units i_units;

		/** Create units */
		private Units(double k, String l, Distance.Units d,
			Interval.Units i)
		{
			kph = k;
			label = l;
			d_units = d;
			i_units = i;
		}

		/** Lookup declared units.
		 * @param d Distance units.
		 * @param i Interval units.
		 * @return Speed units. */
		static private Units lookup(Distance.Units d, Interval.Units i){
			for (Units u: Units.values()) {
				if (u.d_units == d && u.i_units == i)
					return u;
			}
			return null;
		}
	}

	/** Factory to create a new quantity with the null case handled.
	 * @param v Value in units u or null.
	 * @param u Units for arg v.
	 * @return A new quantity in system units or null */
	static public Speed create(Integer v, Units u) {
		Speed s = null;
		if (v != null) {
			s = new Speed(v, u);
			if (!useSi())
				s = s.convert(Units.MPH);
		}
		return s;
	}

	/** Speed value */
	public final double value;

	/** Speed units */
	public final Units units;

	/** Create a new speed.
	 * @param v Value of speed.
	 * @param u Units of speed. */
	public Speed(double v, Units u) {
		value = v;
		units = u;
	}

	/** Create a new speed.
	 * @param v Value in kph. */
	public Speed(double v) {
		this(v, Units.KPH);
	}

	/** Create a new speed.
	 * @param d Distance for speed.
	 * @param i Interval for speed. */
	public Speed(Distance d, Interval i) {
		this(createValue(d, i), createUnits(d, i));
	}

	/** Get the speed in kph */
	public double kph() {
		return value * units.kph;
	}

	/** Convert a speed to specified units.
	 * @param u Units to convert to.
	 * @return Speed in specified units. */
	public Speed convert(Units u) {
		if (u == units)
			return this;
		else {
			double v = kph();
			return new Speed(v / u.kph, u);
		}
	}

	/** Get a speed as a float in specified units.
	 * @param u Units to return.
	 * @return Speed as a float value. */
	public float asFloat(Units u) {
		if (u == units)
			return (float)value;
		else
			return (float)(kph() / u.kph);
	}

	/** Round a speed to nearest whole unit.
	 * @param u Units to return.
	 * @return Speed rounded to nearest whole unit. */
	public int round(Units u) {
		if (u == units)
			return (int)Math.round(value);
		else
			return (int)Math.round(kph() / u.kph);
	}

	/** Add another speed.
	 * @param d Other speed.
	 * @return Sum of speeds. */
	public Speed add(Speed d) {
		if (d.units == units)
			return new Speed(value + d.value, units);
		else
			return new Speed(kph() + d.kph());
	}

	/** Calculate elapsed interval.
	 * @param d Distance travelled.
	 * @return Time interval required to travel distance. */
	public Interval elapsed(Distance d) {
		// Calculate distance travelled per second
		Distance ps = new Distance(convert(Units.FPS).value, FEET);
		return new Interval(d.m() / ps.m());
	}

	/** Compare for equality */
	@Override
	public boolean equals(Object other) {
		if (other instanceof Speed) {
			Speed o = (Speed)other;
			if (units == o.units)
				return value == o.value;
			else
				return kph() == o.kph();
		} else
			return false;
	}

	/** Get a speed hash code */
	@Override
	public int hashCode() {
		return new Double(kph()).hashCode();
	}

	/** Get a string representation of a speed */
	@Override
	public String toString() {
		return new Formatter(0).format(this);
	}

	/** Get speed as an NTCIP value.
	 * @return Speed in tenths of a meter per second or 65535 for 
	 *         missing. See NTCIP essAvgWindSpeed. */
	public Integer ntcip() {
		return new Integer((int)Math.round(kph() * 10));
	}

	/** Speed formatter */
	static public class Formatter {
		private final NumberFormat format;
		public Formatter(int d) {
			format = NumberFormat.getInstance();
			format.setMaximumFractionDigits(d);
			format.setMinimumFractionDigits(d);
		}
		public String format(Speed d) {
			return format.format(d.value) + " " + d.units.label;
		}
	}
}
